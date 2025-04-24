package org.example.exercises.part3

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Exercise3 {

    companion object {

        fun runExercise() {

            println("--- misunderstood, used pair")

            val square = { value: Int -> Mono.just(Pair(value, value * value)) }

            Flux.just(*(1..5).toList().toTypedArray())
                .flatMap { square(it) }
                .subscribe { println("${it.first} squared is ${it.second}") }

            println("--- flat map, may break ordering")

            val squareMess = { value: Int -> Flux.just(value, value * value) }

            Flux.just(*(1..5).toList().toTypedArray())
                .flatMap { squareMess(it) }
                .subscribe { println("val: $it") }

            println("--- enforce ordering via sequential")

            val square2 = { value: Int -> Flux.just(value, value * value) }

            Flux.just(*(1..5).toList().toTypedArray())
                .flatMapSequential { square2(it) }
                .subscribe { println("val: $it") }

            println("--- enforce ordering via iterable")

            val square3 = { value: Int -> listOf(value, value * value) }

            Flux.just(*(1..5).toList().toTypedArray())
                .flatMapIterable { square3(it) }
                .subscribe { println("val: $it") }
        }

    }

}