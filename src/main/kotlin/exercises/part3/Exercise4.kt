package org.example.exercises.part3

import reactor.core.publisher.Flux

class Exercise4 {

    companion object {

        fun runExercise() {
            println("--- enforce ordering via iterable")

            val square3 = { value: Int -> listOf(value, value * value) }

            Flux.just(*(1..5).toList().toTypedArray())
                .flatMapIterable { square3(it) }
                .subscribe { println("val: $it") }
        }


    }

}