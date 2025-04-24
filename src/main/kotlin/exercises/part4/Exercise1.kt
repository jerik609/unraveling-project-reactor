package org.example.exercises.part4

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

class Exercise1 {

    companion object {
        fun runExercise() {
            val fetchHistoricalPrices = {
                stockSymbol: String -> Mono.fromCallable {
                    val c = stockSymbol[0].code
                    listOf(c * 10.0, c * 20.0, c * 30.0)
                }
            }

            val stockSymbols = listOf("AAPL", "GOOG", "MSFT", "AMZN", "FB")

            Flux.fromIterable(stockSymbols)
                .concatMap { fetchHistoricalPrices(it).delayElement(Duration.ofMillis(1000)) }
                .subscribe { println(it) }

            Thread.sleep(10000)
        }
    }
}