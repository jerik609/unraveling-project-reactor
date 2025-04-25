package org.example.exercises.part5

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Exercise4 {

    companion object {
        fun run() {

            val squareAndEvaluate = { inputVal: Int ->
                Mono.fromCallable {
                    val squared = inputVal * inputVal
                    if (squared % 4 == 0) {
                        throw RuntimeException("$squared is divisible by 4, we cannot have that!")
                    }
                    squared
                }
            }

            Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .flatMap { squareAndEvaluate(it)
                    .doOnError { value -> println("error processing: $value") }
                    // here we nicely ignore the error in the internal flux, not propagating the "cancel" further
                    // thus allowing the outer flux to continue processing the remaining values
                    .onErrorResume { Mono.just(-1) }
                }
                .subscribe({println(it)}, {println(it)})
        }
    }
}