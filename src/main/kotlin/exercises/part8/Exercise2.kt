package org.example.exercises.part8

import reactor.core.publisher.Flux
import java.time.Duration

class Exercise2 {

    companion object {
        fun run() {

            Flux.range(1, 10)
                .delayElements(Duration.ofSeconds(1))
                .toIterable()
                .forEach { println(it) }

        }
    }

}