package org.example.exercises.part8

import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration

class Exercise1 {

    companion object {
        fun run() {

            val url = "https://example.com"

            val gimmeUrl = { url: String ->
                HttpClient.create()
                    .get()
                    .uri(url)
                    .responseSingle { response, body -> body.asString() }
            }

            Mono.fromCallable { gimmeUrl(url) }
                //.delayElement(Duration.ofSeconds(3))
                .flatMap { it }
                .blockOptional(Duration.ofSeconds(2))
                .ifPresentOrElse({ println(it) }, { println("BOOM!")})
        }
    }

}