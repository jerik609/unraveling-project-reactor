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