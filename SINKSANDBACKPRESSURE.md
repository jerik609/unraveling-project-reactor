# Flux.create((FluxSink<String>) -> Unit)

This is our typical backpressure sink - we can regulate the consumption from external source by the requests made by subscribers
via subscription.request(Long) ... when this request is made, it will be eventually propagated to sink.onRequest(LongConsumer).
Here we can react by e.g. reading more items from Kafka.

        Here's how it works in the context of Flux.create:
        1.Subscriber Requests: The downstream Subscriber signals its readiness for more data by calling subscription.request(n)
            on the Subscription object it received during the onSubscribe phase.
        2.Reactor Relays Request: The Reactor framework intercepts this request(n) call.
        3.FluxSink.onRequest Callback: If you've registered a callback using sink.onRequest(LongConsumer consumer) within
            your Flux.create lambda, the Reactor framework will invoke this consumer.
        4.Producer Notified: The LongConsumer you provided to onRequest is executed, and the requested amount n is passed
            as an argument to it. This informs your producer code (the logic that calls sink.next()) exactly how many items
            the downstream subscriber is currently ready to receive.

# Sinks.many().multicast().directBestEffort<String>().asFlux()

This is a true hot flux - it simply produces data continuously. We can stop when we realize there are no subscribers left 
via sink.currentSubscriberCount(). Otherwise, this sink does not have any way to react on a request. It will simply try to 
produce using tryEmitNext and may buffer, drop or fail when the subscribers are not ready (based on the result of the tryEmitNext).
This behavior is determined by directBestEffort and similar configurations set on creation of the sink.

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