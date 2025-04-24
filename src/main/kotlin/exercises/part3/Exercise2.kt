package org.example.exercises.part3

import reactor.core.publisher.Mono

class Exercise2 {

    companion object {

        val getSquareAsync = { value: Int -> Mono.just(value * value) }

        fun runExercise() {
            Mono.just(3)
                .flatMap { getSquareAsync(it) }
                .subscribe { println("the value is: $it") }
        }

    }

}