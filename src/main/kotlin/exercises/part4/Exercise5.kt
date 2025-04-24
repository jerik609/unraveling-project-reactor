package org.example.exercises.part4

import reactor.core.publisher.Flux

class Exercise5 {

    companion object {
        fun runExercise() {

            val stockSymbols = emptyList<String>()
            val defaultStockSymbols = listOf("AAPL", "GOOG", "MSFT", "AMZN", "FB")

            Flux.fromIterable(stockSymbols).switchIfEmpty(Flux.fromIterable(defaultStockSymbols)).subscribe { println(it) }

            Thread.sleep(1000)

        }

    }
}