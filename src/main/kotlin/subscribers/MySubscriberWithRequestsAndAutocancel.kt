package org.example.subscribers

import org.reactivestreams.Subscription

class MySubscriberWithRequestsAndAutocancel(name: String, req: Long): MySubscriberWithRequests(name, req) {

    private lateinit var subscription: Subscription
    private var counter: Long = req

    override fun hookOnNext(value: String) {
        logger.info("(sub: $name) - remaining counter: $counter")
        super.hookOnNext(value)
        this.counter--
        if (counter <= 0L) {
            logger.info("(sub: $name) - cancelling")
            subscription.cancel()
        }
    }

    override fun hookOnSubscribe(subscription: Subscription) {
        super.hookOnSubscribe(subscription)
        this.subscription = subscription
    }



}