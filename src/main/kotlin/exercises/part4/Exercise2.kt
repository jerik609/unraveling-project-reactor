package org.example.exercises.part4

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

class Exercise2 {

    companion object {

        fun runExercise() {

            val stockSymbols = listOf("AAPL", "GOOG", "MSFT", "AMZN", "FB")

            val stockSymbolsFlux = Flux.fromIterable(stockSymbols).delayElements(Duration.ofSeconds(1))

            val fetchLatestPrice = { stockSymbol: String ->

                val c = stockSymbol[0].code
                val delay = 935L + c
                println("fetching for $stockSymbol with delay $delay")
                Mono
                    .just(c * 10.0)
                    .doOnNext { println("producing: $it") }
                    .delayElement(Duration.ofMillis(delay))
                    .doOnCancel { println("cancelled") }
            }

            stockSymbolsFlux
                // on a new value, it will create a publisher and wait for a value
                // if a new value is received while waiting, it switches to a new publisher (cancelling the previous one)
                // rinse and repeat
                .switchMap { symbol ->
                    println("=====================")
                    println("Symbol: $symbol - will fetch quote")
                    fetchLatestPrice(symbol) }
                .doOnNext { println("did receive: $it") }
                .subscribe { println(it) }

            Thread.sleep(15000)
        }

    }

}