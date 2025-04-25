package org.example.subscribers

import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.SignalType
import java.util.logging.Logger

open class MySubscriber(protected val name: String): BaseSubscriber<String>() {

    companion object {
        val logger: Logger = Logger.getLogger("subscriber")
    }

    override fun hookOnNext(value: String) {
        logger.info("(sub: $name) - consuming: $value")
    }

    override fun hookOnError(throwable: Throwable) {
        logger.info("(sub: $name) - encountered an error: ${throwable.message}")
        throw RuntimeException("BOOM: ${throwable.message}")
    }

    override fun hookOnComplete() {
        logger.info("(sub: $name) - completed")
    }

}