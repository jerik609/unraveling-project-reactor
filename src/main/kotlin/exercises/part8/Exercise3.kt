package org.example.exercises.part8

import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class Exercise3 {

    companion object {
        fun run() {

            val blockingOp = { delay: Long ->
                println("running on thread: ${Thread.currentThread().name} with delay: $delay ms")
                Thread.sleep(delay)
                "that took too long"
            }

            Mono.fromCallable { blockingOp(3000) }
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe { println("result: $it") }
        }
    }

}