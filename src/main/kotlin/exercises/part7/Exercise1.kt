package org.example.exercises.part7

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.context.Context
import java.math.BigDecimal
import java.math.RoundingMode

class Exercise1 {

    companion object {

        val divide = { value: Int, divider: BigDecimal ->
            println("(${Thread.currentThread().name}) - working on: $value")
            BigDecimal.valueOf(value.toLong()).divide(divider, 3, RoundingMode.HALF_UP)
        }

        fun run() {
            Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .transformDeferredContextual { input, context ->
                    val divider = context.getOrDefault("divider", BigDecimal.valueOf(1.0))!!
                    input.flatMapSequential { value ->
                        Mono
                            .fromCallable { divide(value, divider) }
                            .publishOn(Schedulers.boundedElastic())
                    }
                }
                .contextWrite(Context.of("divider", BigDecimal.valueOf(3.0)))
                .contextWrite(Context.of("divider", BigDecimal.valueOf(10.0)))
                .subscribe {
                    println("result: $it")
                }
        }
    }

}