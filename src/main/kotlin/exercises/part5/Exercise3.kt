package org.example.exercises.part5

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Exercise3 {
    companion object {
        fun run() {

            val fallbackUrl = "fallback url"
            val urls = listOf("url1", "url2", "url3", "url4", "url5")

            val fetchAndParseJson = { jsonStr: String ->
                if (jsonStr == "url3") {
                    throw RuntimeException("invalid json: $jsonStr")
                } else {
                    jsonStr.uppercase()
                }
            }

            Flux.fromIterable(urls)
                .flatMap {
                    Mono.fromCallable { fetchAndParseJson(it) }
                        // provides a fallback and cancels this inner publisher (the Mono), but the others started by flatMap will continue
                        // and provide results, NEAT!
                        .onErrorResume { Mono.just(fallbackUrl) }
                } // long API call

                .subscribe({ println("read sub: $it") }, { println("error sub: ${it.message}") })
        }
    }
}