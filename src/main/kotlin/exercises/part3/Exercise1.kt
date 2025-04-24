package org.example.exercises.part3

import reactor.core.publisher.Flux

class Exercise1 {

    companion object {
        fun runExercise() {
            Flux.just(*(10..14).toList().toTypedArray())
                .map { String.format("0x%08X", it) }
                .subscribe { println("the value is: $it") }
        }
    }

}