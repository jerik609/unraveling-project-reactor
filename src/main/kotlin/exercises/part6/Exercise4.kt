package org.example.exercises.part6

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class Exercise4 {
    companion object {

        val task = { value: Int ->
            val computed = 2 * value
            println("(${Thread.currentThread().name}) - working on: $computed")
            Thread.sleep(500)
            computed
        }

        fun run() {
            //fluxFlatMapWithParallelismViaSubscribeOn()
            //fluxFlatMapWithParallelismViaPublishOn()
            fluxOther2()

        }

        // starts with main,
        // then via subscribe on switches to bounded elastic (and forces flatMap to run in parallel),
        // finally switches to parallel due to publish on
        fun fluxFlatMapWithParallelismViaSubscribeOn() {
            Flux.range(1, 10)
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .subscribeOn(Schedulers.boundedElastic())
                         }, 3)
                .publishOn(Schedulers.parallel()) // this will be overridden by flatMap publish on
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }

        // starts with main,
        // then via publish on switches to bounded elastic (and forces flatMap to run in parallel),
        // finally switches to parallel due to publish on
        fun fluxFlatMapWithParallelismViaPublishOn() {
            Flux.range(1, 10)
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .publishOn(Schedulers.boundedElastic())
                }, 3)
                .publishOn(Schedulers.parallel()) // this will be overridden by flatMap publish on
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }

        // subscribeOn makes the flux start with parallel
        // then due to publish on switches to bounded elastic
        fun fluxOther1() {
            Flux.range(1, 10)
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .publishOn(Schedulers.boundedElastic())
                }, 3)
                .subscribeOn(Schedulers.parallel()) // this will be overridden by flatMap publish on
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }

        // subscribeOn makes the flux start with parallel
        // internal subscribeOn has no effect, even though it's "higher" up the chain
        fun fluxOther2() {
            Flux.range(1, 10)
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .subscribeOn(Schedulers.boundedElastic())
                }, 3)
                .subscribeOn(Schedulers.parallel()) // this will be overridden by flatMap publish on
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }



        fun flux1() {
            Flux.range(1, 10)
                .publishOn(Schedulers.parallel()) // this will be ignored by flatMap ... or better, it will be respected, by we won't be running in parallel
                // must be callable and must specify executors via own publishOn ... previous definition won't work
                .flatMap({ value -> Mono.fromCallable { task(value) }.publishOn(Schedulers.parallel())  }, 3)
                .subscribeOn(Schedulers.single()) // this will be overridden by flatMap publish on
                .publishOn(Schedulers.single()) // switch to single thread explicitly
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }



    }
}