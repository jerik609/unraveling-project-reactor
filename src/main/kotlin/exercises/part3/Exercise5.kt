package org.example.exercises.part3

import reactor.core.publisher.Flux

class Exercise5 {

    companion object {

        fun runExercise() {
            Flux.just(*(1..5).toList().toTypedArray())
                .map { it * it * it }
                .flatMapSequential { Flux.just(it, it * it) }
                .subscribe { println("val: $it") }
        }

    }

}