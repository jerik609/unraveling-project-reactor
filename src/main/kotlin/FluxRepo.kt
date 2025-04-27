package org.example

import org.example.subscribers.MySubscriber
import org.example.subscribers.MySubscriberWithRequests
import org.example.subscribers.MySubscriberWithRequestsAndAutocancel
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Sinks
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


class FluxRepo {

    // SPECIAL FLUX CREATORS =============================================================

    // flux which waits for sink to emitter to be initialized by first subscription
    fun testColdHotFlux(): Flux<String> {
        val executor: ExecutorService = Executors.newFixedThreadPool(2)

        val atomicInt = AtomicInteger(0)

        val emitter = { sink: FluxSink<String> ->

            sink
                .onCancel { logger.info { "SINK ${sink.hashCode()}: was cancelled" } }
                .onDispose { logger.info { "SINK ${sink.hashCode()}: was disposed" } }
                .onRequest { logger.info { "SINK ${sink.hashCode()}: was requested" } }

            executor.submit {
                while (true) {

                    // make sure we stop when not needed
                    if (sink.isCancelled) {
                        logger.info { ">>> ${Thread.currentThread().threadId()} <<< [FluxSink flux] -> SINK ${sink.hashCode()}: was cancelled - BREAKING OUT OF THE EMITTER LOOP" }
                        break
                    }

                    val value = "hot value #${atomicInt.incrementAndGet()}"
                    logger.info(">>> ${Thread.currentThread().threadId()} <<< [FluxSink flux] -> emitting: $value")
                    sink.next(value)
                    Thread.sleep(1500)
                }
            }
            Unit
        }

        /*
        Here's how it works in the context of Flux.create:
        1.Subscriber Requests: The downstream Subscriber signals its readiness for more data by calling subscription.request(n)
            on the Subscription object it received during the onSubscribe phase.
        2.Reactor Relays Request: The Reactor framework intercepts this request(n) call.
        3.FluxSink.onRequest Callback: If you've registered a callback using sink.onRequest(LongConsumer consumer) within
            your Flux.create lambda, the Reactor framework will invoke this consumer.
        4.Producer Notified: The LongConsumer you provided to onRequest is executed, and the requested amount n is passed
            as an argument to it. This informs your producer code (the logic that calls sink.next()) exactly how many items
            the downstream subscriber is currently ready to receive.
         */

        return Flux.create(emitter)
    }

    // flux which is backed by a sink, which is actually created first and immediately starts emitting before first subscription
    fun testHotHotFlux(): Flux<String> {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()

        val atomicInt = AtomicInteger(0)

        val sink = Sinks.many().multicast().directBestEffort<String>()

        val tainted = AtomicBoolean(false)

        // feed the sink
        executor.submit {
            while (true) {

                // make sure we stop when not needed (make sure not to be killed on startup)
                if (sink.currentSubscriberCount() > 0 && !tainted.get()) {
                    logger.info(">>> ${Thread.currentThread().threadId()} <<< [Sinks flux] -> first subscriber - marking as tainted")
                    tainted.compareAndSet(false, true)
                }
                if (sink.currentSubscriberCount() == 0 && tainted.get()) {
                    logger.info(">>> ${Thread.currentThread().threadId()} <<< [Sinks flux] -> no subscribers and tainted - BREAKING OUT OF THE EMITTER LOOP")
                    break
                }

                val value = "hot value #${atomicInt.incrementAndGet()}"
                logger.info(">>> ${Thread.currentThread().threadId()} <<< [Sinks flux] -> emitting: $value")
                val result = sink.tryEmitNext(value)
                logger.info(">>> ${Thread.currentThread().threadId()} <<< [Sinks flux] -> emit result: $result")
                Thread.sleep(1500)
            }
        }

        /*
        It's quite different from Flux.create. Here's the breakdown:
        1.Producer Pushes Independently: The code calling sink.tryEmitNext(value) (your loop in testHotHotFlux) pushes
            data into the sink regardless of whether subscribers are ready or not. It doesn't typically get direct feedback
            about downstream demand via a callback like onRequest.
        2.Subscriber Requests: Downstream subscribers still subscribe to the Flux obtained via sink.asFlux() and signal
            their demand by calling subscription.request(n).
        3.Sink's Role (directBestEffort): This specific sink type lives up to its name:
            •When sink.tryEmitNext(value) is called, the sink looks at all its current subscribers.
            •For each subscriber, it checks if that individual subscriber has requested more items (i.e., has outstanding demand).
            •If a subscriber has demand, the sink sends the value to that subscriber.
            •If a subscriber does not have demand (it hasn't called request(n) recently enough or has already received the requested amount),
                the sink drops the value for that subscriber. It doesn't wait or buffer for that subscriber.
            •The tryEmitNext method itself primarily indicates if the emission attempt was accepted by the sink (e.g., EmitResult.OK),
                not necessarily if every subscriber received it.
        In essence:
            With Sinks.many().multicast().directBestEffort(), the communication of "need for more data" (request(n)) travels
            from the Subscriber to the Sink. However, the Sink uses this information not to directly throttle the producer
            (the code calling tryEmitNext), but rather to decide at the moment of emission whether each individual Subscriber
            is ready to receive the item. If a subscriber isn't ready, the item is simply dropped for them, ensuring the
            producer isn't blocked, but potentially leading to data loss for slower consumers.
         */
        return sink.asFlux()
    }

    // ===================================================================================

    fun consumeFiniteItems(flux: Flux<String>, req: Long) {
        println("first we sleep ...".uppercase())
        Thread.sleep(5000)
        println("then we subscribe the subscriber:".uppercase())
        flux.subscribe(MySubscriberWithRequests("#1", req))
    }

    fun consumeFiniteItemsAndCancel(flux: Flux<String>, req: Long) {
        logger.info("first we sleep ...".uppercase())
        Thread.sleep(5000)
        logger.info("then we subscribe the first subscriber:".uppercase())
        flux.subscribe(MySubscriberWithRequestsAndAutocancel("#1", req))
        logger.info("then we sleep some more ...".uppercase())
        Thread.sleep(5000)
        logger.info("then we subscribe the second subscriber:".uppercase())
        flux.subscribe(MySubscriberWithRequestsAndAutocancel("#2", 20))
    }

    fun consumeTwoDelayed(flux: Flux<String>) {
        println("first we sleep ...".uppercase())
        Thread.sleep(5000)
        println("then we subscribe the first subscriber:".uppercase())
        flux.subscribe(MySubscriber("#1"))
        println("then we sleep some more ...".uppercase())
        Thread.sleep(5000)
        println("then we subscribe the second subscriber:".uppercase())
        flux.subscribe(MySubscriber("#2"))
    }

}