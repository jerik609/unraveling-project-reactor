package org.example.exercises.part4

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Exercise3 {

    companion object {
        fun runExercise() {

            val stockSymbols = listOf("AAPL", "GOOG", "MSFT", "AMZN", "FB")
            val stockNames = listOf(
                "Apple Inc.",
                "Alphabet Inc.", // Google's parent company
                "Microsoft Corporation",
                "Amazon.com, Inc.",
                "Meta Platforms, Inc." // Facebook's parent company
            )

            Flux.fromIterable(stockSymbols)
                .zipWith(Flux.fromIterable(stockNames))
                .flatMap { Mono.just("{${it.t1}} - {${it.t2}}") }
                .subscribe { println("paired: $it") }
        }
    }

}