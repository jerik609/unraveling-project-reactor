package org.example

import org.example.subscribers.MySubscriber
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.LogManager
import java.util.logging.Logger


val logger: Logger = Logger.getLogger("MAIN LOGGER")

fun main() {

    // configure logging!
    // https://nozaki.me/roller/kyle/entry/java-util-logging-programmatic-configuration
    val logManager = LogManager.getLogManager()
    MySubscriber::class.java.classLoader.getResourceAsStream("logging.properties").use { inputStream ->
        logManager.readConfiguration(inputStream)
    }

    // === exercises ===

    //ExercisesPart2Runner.run()
    //ExercisesPart3Runner.run()
    //ExercisesPart4Runner.run()

    // === prototypes and tests ===

    val handlers = logger


    hotFluxTest()

    //multiSubscribe()
}

fun hotFluxTest() {
    //val x = MyExample()
    //x.test()

    val hotFlux = HotFlux()

    // a flux which is triggered by first subscription and the multicasts, late subscribers may miss some records
    //testColdHotFlux(hotFlux)

    // a cold hot flux, from which we consume only finite number of items - what will happen to it?
    //testColdHotFlux2(hotFlux)

    // testing non-shared flux with two subscribers, one cancelling after a while
    testColdHotFlux3(hotFlux)

    // a flux which is being pushed data by underlying sink - and the data is lost, later a subscriber joins and consumes,
    // then some time later, the next subscriber joins - both subscribers may miss data
    //testHotHotFlux(hotFlux)

    while(true) {}
}

fun testColdHotFlux(hotFlux: HotFlux) {
    val coldHotFlux = hotFlux.testColdHotFlux().share()
    hotFlux.consumeTwoDelayed(coldHotFlux)
}

fun testColdHotFlux2(hotFlux: HotFlux) {
    val coldHotFlux = hotFlux.testColdHotFlux()//.share()
    hotFlux.consumeFiniteItems(coldHotFlux, 10)
}

fun testColdHotFlux3(hotFlux: HotFlux) {
    val coldHotFlux = hotFlux.testColdHotFlux()//.share()
        .doOnError { logger.info("error! ${it.message}") }
        .doOnEach { logger.info("signal: $it") }
    hotFlux.consumeFiniteItemsAndCancel(coldHotFlux, 10)
}

fun testHotHotFlux(hotFlux: HotFlux) {
    val hotHotFlux = hotFlux.testHotHotFlux()
    hotFlux.consumeTwoDelayed(hotHotFlux)
}

// https://medium.com/@ranjeetk.developer/hot-cold-publishers-java-reactive-programming-5c057c091d63
fun multiSubscribe() {

    //val f = Flux.just(1, 2, 3)

    val f = gimmeFlux()//.share()

    f.subscribe { logger.info("#1: $it") }
    f.subscribe { logger.info("#2: $it") }

    while(true) {

    }
}

fun gimmeFlux(): Flux<String> {
    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    val atomicInt = AtomicInteger(0)

    val emitter = { sink: FluxSink<String> ->
        executor.submit {
            while (true) {
                val value = "hot value #${atomicInt.incrementAndGet()}"
                logger.info("[FLUX] -> emitting: $value")
                sink.next(value)
                Thread.sleep(1500)
            }
        }
        Unit
    }

    return Flux.create(emitter)
}