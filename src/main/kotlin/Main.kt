package org.example

import org.example.exercises.Exercise1
import org.example.exercises.Exercise2
import org.example.exercises.Exercise3
import org.example.exercises.Exercise4
import org.example.exercises.Exercise5
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger


fun main() {

    //Exercise1.runExercise1()
    //Exercise2.runExercise2()
    //Exercise3.runExercise3()
    //Exercise4.runExercise4()
    //Exercise5.runExercise5()



    //hotFluxTest()

    multiSubscribe()

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
        .doOnError { println("error! ${it.message}") }
        .doOnEach { println("signal: $it") }
    hotFlux.consumeFiniteItemsAndCancel(coldHotFlux, 10)
}

fun testHotHotFlux(hotFlux: HotFlux) {
    val hotHotFlux = hotFlux.testHotHotFlux()
    hotFlux.consumeTwoDelayed(hotHotFlux)
}

// https://medium.com/@ranjeetk.developer/hot-cold-publishers-java-reactive-programming-5c057c091d63
fun multiSubscribe() {
    //val f = Flux.just(1, 2, 3)

    val f = gimmeFlux().share()

    f.subscribe { println("#1: $it") }
    f.subscribe { println("#2: $it") }

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
                println("[FLUX] -> emitting: $value")
                sink.next(value)
                Thread.sleep(1500)
            }
        }
        Unit
    }

    return Flux.create(emitter)
}