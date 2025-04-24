package org.example.exercises.part4

import reactor.core.publisher.Flux

class Exercise4 {

    companion object {
        fun runExercise() {

            val stockPrices = Flux.just(120.0, 140.0, 130.0, 110.0, 150.0)

            stockPrices.filter { it > 130.0 }.subscribe(
                { println("filtered value is: $it")},
                { println("error: ${it.message}")},
                { println("completed") })
        }
    }

}