package org.example.exercises.part6

import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

class Exercise5 {
    companion object {
        fun run() {

            val task = { value: Int ->
                val x = 2 * value
                println("(${Thread.currentThread().name}) - working on: $x")
                Thread.sleep(5000)
                println("(${Thread.currentThread().name}) - DONE working on: $x")
                x
            }

            Flux.range(1, 10)
                .parallel() // run in parallel
                .runOn(Schedulers.parallel())
                .map { value -> task(value) }
                .sequential()
                .publishOn(Schedulers.single())
                .subscribe(
                    { println("(${Thread.currentThread().name}) - consumed: $it") },
                    { println("(${Thread.currentThread().name}) - error: ${it.message}") },
                    { println("(${Thread.currentThread().name}) - DONE") })
        }
    }
}