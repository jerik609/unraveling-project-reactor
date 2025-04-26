package org.example.exercises.part6

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.random.Random

class Exercise1 {
    companion object {

        val randomGenerator = Random(11)

        fun run() {

            val fetchAndCountWords = { url: String ->
                val randomValue = randomGenerator.nextInt(0, 100) * 500 + 100
                println("(${Thread.currentThread().name}) - Word count for $url: $randomValue")
                randomValue
            }

            val urls = listOf("url1", "url2", "url3", "url4", "url5")

            Flux.fromIterable(urls)
                .publishOn(Schedulers.boundedElastic())
                //.delayElements(Duration.ofMillis(1000), Schedulers.single())
                //.publishOn(Schedulers.single())
                .flatMap { Mono.just(fetchAndCountWords(it)) }
                .subscribe(
                    { println("(${Thread.currentThread().name}) - Word count: $it") },
                    { println("(${Thread.currentThread().name}) - Error: ${it.message}") })

            while (true) {

            }
        }
    }
}