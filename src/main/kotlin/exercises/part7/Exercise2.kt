package org.example.exercises.part7

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.context.Context
import kotlin.times

class Exercise2 {


    companion object {
        fun run() {

            Flux
                .range(1, 5)
                .flatMapSequential { value ->
                    Mono
                        // ensures we get the context from current subscription
                        .deferContextual { context -> Mono.just(context.getOrDefault("multiplier", 1)!!) }
                        // with the value,
                        .flatMap { multiplier ->
                            Mono.fromCallable {
                                val result = value * multiplier
                                println("(${Thread.currentThread().name}) - working on: $result")
                                Thread.sleep(500)
                                result
                            }.publishOn(Schedulers.parallel())
                        }
                }
                .contextWrite(Context.of("multiplier", 3))
                .subscribe {
                    println("(${Thread.currentThread().name}) - result: $it")
                }

        }
    }

}