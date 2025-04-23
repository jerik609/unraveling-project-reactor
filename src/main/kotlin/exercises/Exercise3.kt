package org.example.exercises

import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class Exercise3 {

    companion object {

        fun runExercise3() {

            Mono.fromFuture(createCompletableFuture()).subscribe(
                { value -> println("thread: ${Thread.currentThread().threadId()}"); println("Received: $value") },
                { error -> println("Error: ${error.message}") },
                { println("thread: ${Thread.currentThread().threadId()}"); println("Completed") }
            )

        }

        fun createCompletableFuture(): CompletableFuture<Int> {
            val executor = Executors.newSingleThreadExecutor()
            return CompletableFuture.supplyAsync({ println("Executor thread: ${Thread.currentThread().threadId()}"); 123 }, executor)
        }
    }

}