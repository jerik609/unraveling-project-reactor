package org.example.exercises.part5

import reactor.core.publisher.Flux

class Exercise2 {    companion object {
    fun run() {

        Flux.fromIterable(listOf(1, 2, -3, 4, 5)).flatMap {
            if (it < 0) {
                throw IllegalArgumentException("really an illegal (negative) argument")
            } else {
                Flux.just(it * it)
            }
        }
            .doOnEach { println("error from flux: $it.message") }
            .doFinally { println("finished from flux: processing the list") }
            .subscribe({ println("read sub: $it") }, { println("error sub: ${it.message}") })
    }
}
}