package org.example.subscribers

import org.reactivestreams.Subscription

open class MySubscriberWithRequests(name: String, private val req: Long): MySubscriber(name) {

    override fun hookOnSubscribe(subscription: Subscription) {
        subscription.request(req)
    }

}