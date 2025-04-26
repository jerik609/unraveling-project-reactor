package org.example.exercises.part6

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class Exercise2 {
    companion object {
        fun run() {

            val slowConsumer = { input: Int ->
                val sleepDuration = (3000 - input * 10).toLong()
                println("(${Thread.currentThread().name}) - START: slowly consuming: $input, delay: $sleepDuration")
                Thread.sleep(sleepDuration)
                println("(${Thread.currentThread().name}) - DONE: slowly consuming: $input")
                input
            }

            Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
                .publishOn(Schedulers.single())
                .map { input ->
                    println("(${Thread.currentThread().name}) - observing value: $input")
                    input
                }
                .flatMap(
                    { value -> Mono
                        .fromCallable { slowConsumer(value) } // this fella is SLOW, but we will tackle it in parallel, we can afford 5 threads at a time!
                        .publishOn(Schedulers.boundedElastic()) // this is important - it tells the flat map on which scheduler to run - this enables parallelization
                    },
                    5)
                .publishOn(Schedulers.single()) // changing again to single thread executor
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }
    }
}