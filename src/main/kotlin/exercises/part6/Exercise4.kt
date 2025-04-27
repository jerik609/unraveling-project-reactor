package org.example.exercises.part6

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class Exercise4 {
    companion object {

        fun run() {
            //fluxFlatMapWithParallelismViaSubscribeOn()
            //fluxFlatMapWithParallelismViaPublishOn()
            //fluxOther1()
            //fluxOther2()
            fluxFinal()
        }

        // starts with main thread, flat map would continue with it as a worker for its internal publishers, BUT:
        // via subscribeOn, we tell it to work on the whole boundedElastic
        // so for each internal publisher, it will switch to a bounded elastic thread
        // then it will provide the results downstream, along with the context of running on the bounded elastic threads, BUT:
        // we use publishOn to switch the scheduler again - to parallel
        fun fluxFlatMapWithParallelismViaSubscribeOn() {
            Flux.range(1, 10)
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .subscribeOn(Schedulers.boundedElastic())
                         }, 3)
                .publishOn(Schedulers.parallel())
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }

        // starts with main thread, flat map would continue with it as a worker for its internal publishers, BUT:
        // via publishOn, we tell it to work on the whole boundedElastic
        // so for each internal publisher, it will switch to a bounded elastic thread
        // then it will provide the results downstream, along with the context of running on the bounded elastic threads, BUT:
        // we use publishOn to switch the scheduler again - to parallel
        fun fluxFlatMapWithParallelismViaPublishOn() {
            Flux.range(1, 10)
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .publishOn(Schedulers.boundedElastic())
                         }, 3)
                .publishOn(Schedulers.parallel())
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }

        // subscribeOn tells the flux to start with parallel scheduler
        // flatMap would run on it, but it's instructed to run with bounded elastic by internal publishOn
        // the results of flatMap are forwarded downstream, subscribeOn has no effect at this point, since:
        // it (was respected on (main) flux subscription
        fun fluxOther1() {
            Flux.range(1, 10)
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .publishOn(Schedulers.boundedElastic())
                         }, 3)
                .subscribeOn(Schedulers.parallel())
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }

        // subscribeOn tells the flux to start with parallel scheduler
        // flatMap would run on it, but it's instructed to run with bounded elastic by internal subscribeOn, note that:
        // the internal subscribeOn has no effect upstream - it only matters for internal publishers
        // the results of flatMap are forwarded downstream, along with their context (=threads) and would run on boundedElastic, BUT:
        // due to publishOn will be handled by a single thread scheduler
        // subscribeOn has no effect at this point, since it (was respected on (main) flux subscription
        fun fluxOther2() {
            Flux.range(1, 10)
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .subscribeOn(Schedulers.boundedElastic())
                         }, 3)
                .publishOn(Schedulers.single())
                .subscribeOn(Schedulers.parallel())
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }

        // parallel scheduler from subscribe is immediately overruled by publish on - parallel
        // flatmap instructs internal publishers to run on bounded elastic
        // subscribe on is irrelevant here
        // forward would make the values continue on their current threads, but we switch to single thread scheduler
        fun fluxFinal() {
            Flux.range(1, 10)
                .publishOn(Schedulers.parallel())
                .map {
                    println("(${Thread.currentThread().name}) - mappity map on: $it")
                    it
                }
                .flatMap({ value -> Mono.fromCallable { task(value) }
                    .publishOn(Schedulers.boundedElastic())
                         }, 3)

//                .flatMap({ value -> Mono
//                    .just(task(value))
//                    .publishOn(Schedulers.boundedElastic())
//                }, 3)

                .subscribeOn(Schedulers.single())
                .publishOn(Schedulers.single()) // switch to single thread explicitly
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }

        val task = { value: Int ->
            val computed = value //2 * value
            println("(${Thread.currentThread().name}) - working on: $computed")
            Thread.sleep(500)
            computed
        }
    }
}