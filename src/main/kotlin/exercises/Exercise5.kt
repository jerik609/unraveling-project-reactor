package org.example.exercises

import org.reactivestreams.Subscription
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux

class Exercise5 {

    companion object {

        fun runExercise5() {
            Flux.just("1", "2", "3").subscribe(createBaseSubscriber())
        }

        fun createBaseSubscriber(): BaseSubscriber<String> {
            return object : BaseSubscriber<String>() {

                override fun hookOnCancel() {
                    println("cancelled")
                }

                override fun hookOnNext(value: String) {
                    println("Received: $value")
                }

                override fun hookOnComplete() {
                    println("Completed")
                }

                // must call super! or we won't request anything
                override fun hookOnSubscribe(subscription: Subscription) {
                    println("Subscribed")
                    super.hookOnSubscribe(subscription)
                }
            }
        }
    }

}