package org.example.exercises.part7

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.context.Context

class Exercise3 {

    companion object {
        fun run() {

            val flux = Flux.just("Hello", ", ", "World")
                .transformDeferredContextual { flux, context ->
                    val suffix = context.getOrDefault("suffix", "")!!
                    flux.flatMapSequential { value ->
                        Mono<String>.fromCallable { "$value + $suffix" }
                            .publishOn(Schedulers.parallel())
                    }
                }

            flux
                .contextWrite(Context.of("suffix", "!!"))
                .subscribe { println(it) }

            Thread.sleep(250)

            flux
                .contextWrite(Context.of("suffix", "??"))
                .subscribe { println(it) }
        }
    }
}