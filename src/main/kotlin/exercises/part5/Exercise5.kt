package org.example.exercises.part5

import reactor.core.Exceptions
import reactor.core.publisher.Flux

class Exercise5 {

    companion object {
        fun run() {

            Flux.just("1", "2", "three", "4", "5")
                .map { input -> input }
                .map { input -> input }
                .onErrorMap { e ->
                    println("bubbling 2")
                    e
                }
                .map { input -> input }
                .map { input -> input }
                .onErrorMap { e ->
                    println("bubbling 1")
                    e
                }
                .map { input ->
                    try {
                        Integer.valueOf(input)
                    } catch (e: NumberFormatException) {
                        throw Exceptions.bubble(e)
                        //throw Exceptions.propagate(e)
                    }
                }
                .onErrorMap { e ->
                    println("propagating 1")
                    e
                }
                .map { input -> input }
                .map { input -> input }
                .onErrorMap { e ->
                    println("propagating 2")
                    e
                }
                .subscribe({println(it)}, { e -> println("ERROR for: ${e.message}")}, )

        }
    }
}