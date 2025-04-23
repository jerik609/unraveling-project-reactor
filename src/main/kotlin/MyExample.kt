package org.example

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

class MyExample {

    fun test() {

        val eagerMono = Mono.fromFuture(
            CompletableFuture.supplyAsync {
                println("hello, eager world!")
                "eager"
            }
        )

        val lazyMono = Mono.fromFuture {
            CompletableFuture.supplyAsync {
                println("hello, lazy world")
                "lazy"
            }
        }


        eagerMono.doOnNext { println(it) }.subscribe()
        eagerMono.doOnNext { println(it) }.subscribe()

        lazyMono.doOnNext { println(it) }.subscribe()
        lazyMono.doOnNext { println(it) }.subscribe()

        val crazyFlux = Flux.defer {
            println("gimme my flux!")
            Flux.just(1, 2, 3)
        }

        crazyFlux.doOnNext { println("yoo-hoo!") }.subscribe()



    }



}