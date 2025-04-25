package org.example.exercises.part5

import reactor.core.Exceptions
import reactor.core.publisher.Flux

class Exercise5 {

    companion object {
        fun run() {

            Flux.just("1", "2", "three", "4", "5").map {input ->
                try {
                    Integer.valueOf(input)
                } catch (e: NumberFormatException) {
                    Exceptions.propagate(e)
                }
            }.subscribe({println(it)}, {println()})

        }
    }
}