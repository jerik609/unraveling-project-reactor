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
                println("Running on thread: (${Thread.currentThread().name}) - processing: $result")
                Thread.sleep(50)
                result
            }

            println("====================================================================================== ")
            println("============================== parallelism via callable ============================== ")
            println("====================================================================================== ")
            Flux
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .publishOn(Schedulers.single())
                .flatMap({ input ->
                    Mono
                        .fromCallable { square(input) } // defines a callable
                        .publishOn(Schedulers.boundedElastic()) // and executes it on this scheduler
                        .doOnNext { println("Running on thread: (${Thread.currentThread().name}) - before downstream: $it") }
                })
                .map {
                    println("Running on thread: (${Thread.currentThread().name}) - mapped: $it")
                    it
                }
                .subscribe { println("Running on thread: (${Thread.currentThread().name}) - consumed: $it") }

            Thread.sleep(1000)

            println("====================================================================================== ")
            println("=============================== parallelism via future =============================== ")
            println("====================================================================================== ")
            Flux
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .publishOn(Schedulers.single())
                .flatMap({ input ->
                    Mono
                        .fromFuture { CompletableFuture.supplyAsync { square(input) } } // runs on fork-join pool, flatmap has no control over this
                        .doOnNext { println("Running on thread: (${Thread.currentThread().name}) - before downstream: $it") }
                }, 3)
                .subscribe { println("Running on thread: (${Thread.currentThread().name}) - consumed: $it") }

            Thread.sleep(1000)

            println("====================================================================================== ")
            println("=================================== something else =================================== ")
            println("====================================================================================== ")
            Flux
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .publishOn(Schedulers.single())
                .flatMap({ input ->
                    Mono
                        .just(square(input)) // this runs immediately on main thread
                        .publishOn(Schedulers.parallel()) // this still affects the downstream, since the thread context is switched here
                        .doOnNext { println("Running on thread: (${Thread.currentThread().name}) - before downstream: $it") }
                }, 3)
                .subscribe { println("Running on thread: (${Thread.currentThread().name}) - consumed: $it") }

            while (true) {

            }

        }
    }


}