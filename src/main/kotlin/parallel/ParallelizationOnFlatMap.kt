package org.example.parallel

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CompletableFuture

class ParallelizationOnFlatMap {

    companion object {
        fun run() {

            val square = { value: Int ->
                val result = value * value
                println("Running on thread: (${Thread.currentThread().threadId()}) - processing: $result")
                Thread.sleep(50)
                result
            }

            println("=============================== parallelism via callable =============================== ")
            Flux
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .flatMap({ input ->
                    Mono
                        .fromCallable { square(input) }
                        .publishOn(Schedulers.parallel())
                        .doOnNext { println("Running on thread: (${Thread.currentThread().threadId()}) - produced: $it") }
                })
                .doOnNext { println("Running on thread: (${Thread.currentThread().threadId()}) - intermediate: $it") }
                .map {
                    println("Running on thread: (${Thread.currentThread().threadId()}) - mapped: $it")
                    it
                }
                .subscribe { println("Running on thread: (${Thread.currentThread().threadId()}) - consumed: $it") }

            Thread.sleep(1000)

            println("=============================== parallelism via future =============================== ")
            Flux
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .flatMap({ input ->
                    Mono
                        .fromFuture { CompletableFuture.supplyAsync { square(input) } }
                }, 3)
                //.subscribe { println("Running on thread: (${Thread.currentThread().threadId()}) - consumed: $it") }

            Thread.sleep(1000)

            println("=============================== something else =============================== ")
            Flux
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .flatMap({ input ->
                    Mono
                        .just(square(input)) // this runs immediately on main thread
                        .publishOn(Schedulers.parallel()) // this still affects the downstream, since the thread context is switched here
                }, 3)
                //.subscribe { println("Running on thread: (${Thread.currentThread().threadId()}) - consumed: $it") }

            while (true) {

            }

        }
    }


}