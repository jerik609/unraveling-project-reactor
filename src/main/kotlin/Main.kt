package org.example

import coroutines.CoroutineTest
import org.example.context.ContextTests
import org.example.exercises.part5.ExercisesPart5Runner
import org.example.exercises.part6.ExercisesPart6Runner
import org.example.exercises.part7.ExercisesPart7Runner
import org.example.exercises.part8.ExercisesPart8Runner
import org.example.parallel.ParallelFluxTests
import org.example.parallel.ParallelizationOnFlatMap
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
    //ExercisesPart5Runner.run()
    //ExercisesPart6Runner.run()
    //ExercisesPart7Runner.run()
    ExercisesPart8Runner.run()

    // === prototypes and tests ===

    //hotFluxTest()

    //multiSubscribe()

//    val c = CoroutineTest()
//    c.run()

    //ParallelFluxTests.run()
    //ParallelizationOnFlatMap.run()

    //ContextTests.streamTestRun()
    //ContextTests.funnyContext()

}

fun hotFluxTest() {
    //val x = MyExample()
    //x.test()

    val fluxRepo = FluxRepo()

    // a flux which is triggered by first subscription and the multicasts, late subscribers may miss some records
    //testColdHotFlux(fluxRepo)

    // a flux which is triggered by first subscription and the multicasts, late subscribers may miss some records
    //testColdHotFluxX(fluxRepo)

    // a flux created using sinks, otherwise same as testColdHotFluxX
    testHotHotFluxX(fluxRepo)

    // a cold hot flux, from which we consume only finite number of items - what will happen to it?
    //testColdHotFlux2(fluxRepo)

    // testing non-shared flux with two subscribers, one cancelling after a while
    // IT DOES CREATE A SECOND FLUX WHEN NOT "SHARED"!!!
    //testColdHotFlux3(fluxRepo)

    // a flux which is being pushed data by underlying sink - and the data is lost, later a subscriber joins and consumes,
    // then some time later, the next subscriber joins - both subscribers may miss data
    //testHotHotFlux(fluxRepo)

    while(true) {}
}

fun testColdHotFlux(fluxRepo: FluxRepo) {
    val coldHotFlux = fluxRepo.testColdHotFlux().share()
    fluxRepo.consumeTwoDelayed(coldHotFlux)
}

fun testColdHotFluxX(fluxRepo: FluxRepo) {
    val coldHotFlux = fluxRepo.testColdHotFlux().share()
        .doOnError { logger.info("error! ${it.message}") }
        .doOnEach { logger.info("signal: $it") }
        .doOnSubscribe { logger.info("'SUBSCRIBED' signal received") }
        .doOnCancel { logger.info("'CANCEL' signal received") }
        .doOnTerminate { logger.info("'TERMINATE' signal received") }
    fluxRepo.consumeFiniteItemsAndCancel(coldHotFlux, 10)
}

fun testHotHotFluxX(fluxRepo: FluxRepo) {
    val coldHotFlux = fluxRepo.testHotHotFlux().share()
        .doOnError { logger.info("error! ${it.message}") }
        .doOnEach { logger.info("signal: $it") }
        .doOnSubscribe { logger.info("'SUBSCRIBED' signal received") }
        .doOnCancel { logger.info("'CANCEL' signal received") }
        .doOnTerminate { logger.info("'TERMINATE' signal received") }
    fluxRepo.consumeFiniteItemsAndCancel(coldHotFlux, 10)
}

fun testColdHotFlux2(fluxRepo: FluxRepo) {
    val coldHotFlux = fluxRepo.testColdHotFlux()//.share()
    fluxRepo.consumeFiniteItems(coldHotFlux, 10)
}

fun testColdHotFlux3(fluxRepo: FluxRepo) {
    val coldHotFlux = fluxRepo.testColdHotFlux() //.share()
        .doOnError { logger.info("error! ${it.message}") }
        .doOnEach { logger.info("signal: $it") }
        .doOnSubscribe { logger.info("'SUBSCRIBED' signal received") }
        .doOnCancel { logger.info("'CANCEL' signal received") }
        .doOnTerminate { logger.info("'TERMINATE' signal received") }
    fluxRepo.consumeFiniteItemsAndCancel(coldHotFlux, 10)
}

fun testHotHotFlux(fluxRepo: FluxRepo) {
    val hotHotFlux = fluxRepo.testHotHotFlux()
    fluxRepo.consumeTwoDelayed(hotHotFlux)
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