package org.example.exercises.part2

import reactor.core.publisher.Flux
import java.time.LocalDateTime

class Exercise4 {

    companion object {

        fun runExercise() {

            val publisherSupplier = { Flux.just("Current time: ${LocalDateTime.now()}") }

            val flux = Flux.defer(publisherSupplier)
            val nonDeferredFlux = Flux.from(publisherSupplier.invoke())

            flux.subscribe({ println(it) })
            Thread.sleep(555)
            flux.subscribe({ println(it) })

            println("=======================")

            nonDeferredFlux.subscribe({ println(it) })
            Thread.sleep(555)
            nonDeferredFlux.subscribe({ println(it) })
        }
    }

}