package org.example.subscribers

import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.SignalType

open class MySubscriber(private val name: String): BaseSubscriber<String>() {

    override fun hookOnNext(value: String) {
        println("(sub: $name) - consuming: $value")
    }

    override fun hookOnError(throwable: Throwable) {
        println("(sub: $name) - encountered an error: ${throwable.message}")
        throw RuntimeException("BOOM: ${throwable.message}")
    }

    override fun hookOnComplete() {
        println("(sub: $name) - completed")
    }

}