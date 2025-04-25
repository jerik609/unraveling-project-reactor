package org.example.exercises.part5

import reactor.core.publisher.Flux
import kotlin.random.Random

class Exercise1 {

    companion object {
        fun run() {
            Flux.defer {
                Flux.fromIterable(simulateApiCall().invoke()) // inner supplier throws an error instead of creating a flux, but we do not generate an error flux
                    .doOnError { println("1: ${it.message}") } // so this is pointless
                    .map { if (it == "Alice") throw RuntimeException("BOOOM!") else it } // if we created the flux successfully, this would be triggered!
                    .doOnError { println("2: ${it.message}") }
            }
                .onErrorResume { Flux.error { throw RuntimeException("outer failure") } } // when thrown during creation, an error flux is created and we get the exception here
                .subscribe ({println(it)}, {println(it)}) // which we rethrow

            println("***")

            // alternatively
            Flux.defer {
                try {
                    Flux.fromIterable(simulateApiCall().invoke())
                } catch (e: Exception) {
                    Flux.error { RuntimeException("outer failure: ${e.message}") }
                }
            }
                .subscribe ({println(it)}, {println(it)})
        }

        fun simulateApiCall() = {
            Random(2).nextBoolean().let { // seed 2 throws and error
                //if (it)
                throw RuntimeException("failing randomly") // no random, just throw for the sake of demonstration!
            }
            listOf("Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona")
        }
    }

}