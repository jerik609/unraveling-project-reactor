package org.example.exercises.part4

import reactor.core.publisher.Flux

class Exercise7 {

    companion object {
        fun runExercise() {
            val stockPrices = Flux.just(120.0, 140.0, 130.0, 110.0, 150.0)

            stockPrices
                .doFirst { println("First stock price is coming") }
                .doOnComplete { println("All stock prices processed") }
                .doOnSubscribe { println("Subscription started") }
                .subscribe { println(it) }
        }
    }

}