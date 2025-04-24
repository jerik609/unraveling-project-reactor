package org.example.exercises.part2

import reactor.core.publisher.Mono
import java.util.Optional

class Exercise1 {

    companion object {

        fun runExercise() {
            val value = Optional.of("moo")
            val empty = Optional.empty<String>()

            val monoWithValue = Mono.justOrEmpty(value)
            val monoWithEmpty = Mono.justOrEmpty(empty)

            monoWithValue.subscribe(
                { value -> println("mono with value - Value: $value") },
                { error -> println("mono with value - Error: ${error.message}") },
                { println("mono with value complete") }
            )

            monoWithEmpty.subscribe(
                { value -> println("mono with empty - Value: $value") },
                { error -> println("mono with empty - Error: ${error.message}") },
                { println("mono with empty complete") }
            )
        }

    }

}