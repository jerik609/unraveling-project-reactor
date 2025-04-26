package org.example.exercises.part6

import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

class Exercise3 {
    companion object {
        fun run() {

            Flux.create { sink ->
                (1..10).asIterable().forEach {
                    Thread.sleep(1000)
                    sink.next(it)
                }
                sink.complete()
            }
                .subscribeOn(Schedulers.newBoundedElastic(
                    2, 3, "our-bounded-elastic", 30, true
                ))
                .doOnNext { println("(${Thread.currentThread().name}) - next requested: $it") }
                .subscribe(
                    {println("(${Thread.currentThread().name}) - next: $it")},
                    {println("(${Thread.currentThread().name}) - error: $it")},
                    {println("(${Thread.currentThread().name}) - done")})
        }
    }
}