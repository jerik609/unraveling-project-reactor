package org.example.exercises.part5

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Exercise3 {
    companion object {
        fun run() {

            val fallbackUrl = "fallback url"
            val urls = listOf("url1", "url2", "url3", "url4", "url5")

            val fetchJson = { url: String -> "{\"data\": \"$url\"}" }
            val parseJson = { jsonStr: String ->
                if (jsonStr.contains("url3")) {
                    throw RuntimeException("invalid json: $jsonStr")
                } else {
                    jsonStr.uppercase()
                }
            }

            Flux.fromIterable(urls)
                .flatMap { url ->
                    Mono
                        .fromCallable { fetchJson(url) }
                        .map { json -> parseJson(json) }
                        // provides a fallback and cancels this inner publisher (the Mono),
                        // but the others started by flatMap will continue and provide results, NEAT!
                        .onErrorResume { Mono
                            .fromCallable { fetchJson(fallbackUrl) }
                            .map { json -> parseJson(json) } }
                } // long API call
                .subscribe(
                    { println("read sub: $it") },
                    { println("error sub: ${it.message}") })
        }
    }
}