package org.example.exercises.part4

import reactor.core.publisher.Flux

class Exercise6 {

    companion object {
        fun runExercise() {
            val stockPrices = Flux.just(120.0, 140.0, 130.0, 110.0, 150.0)

            stockPrices.reduce { a, b -> a + b }.subscribe { println(it) }

        }

    }

}