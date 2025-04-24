package org.example.exercises.part2

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Exercise2 {

    companion object {

        fun runExercise2() {
            val publisher = createPublisher()
                .doOnNext { println("flux next: $it") }
                .doOnComplete { println("flux completed") }
                .doOnCancel { println("flux cancelled") }

            Mono.from(publisher).subscribe(
                { value -> println("Received: $value") },
                { error -> println("Error: ${error.message}") },
                { println("Completed") }
            )

            println("======================")

            Mono.fromDirect(publisher).subscribe(
                { value -> println("Received: $value") },
                { error -> println("Error: ${error.message}") },
                { println("Completed") }
            )

        }

        fun createPublisher(): Flux<Int> {
            return Flux.just(1, 2)
        }
    }

}