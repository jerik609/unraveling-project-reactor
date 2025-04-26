package org.example.exercises.part6

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class Exercise4 {
    companion object {
        fun run() {

            val task = { value: Int ->
                println("(${Thread.currentThread().name}) - working on: $value")
                Thread.sleep(500)
                2 * value
            }

            // this example contains various settings, see commments to play with it

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