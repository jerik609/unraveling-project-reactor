package org.example

import org.example.exercises.Exercise1
import org.example.exercises.Exercise2
import org.example.exercises.Exercise3
import org.example.exercises.Exercise4
import org.example.exercises.Exercise5


fun main() {

    //Exercise1.runExercise1()
    //Exercise2.runExercise2()
    //Exercise3.runExercise3()
    //Exercise4.runExercise4()
    Exercise5.runExercise5()

    //hotFluxTest()

}

fun hotFluxTest() {
    val x = MyExample()
    //x.test()

    val hotFlux = HotFlux()

    // a flux which is triggered by first subscription and the multicasts, late subscribers may miss some records
    //testColdHotFlux(hotFlux)

    // a flux which is being pushed data by underlying sink - and the data is lost, later a subscriber joins and consumes,
    // then some time later, the next subscriber joins - both subscribers may miss data
    testHotHotFlux(hotFlux)

    while(true) {}
}

fun testColdHotFlux(hotFlux: HotFlux) {
    val coldHotFlux = hotFlux.testColdHotFlux().share()
    hotFlux.consumeTwoDelayed(coldHotFlux)
}

fun testHotHotFlux(hotFlux: HotFlux) {
    val hotHotFlux = hotFlux.testHotHotFlux()
    hotFlux.consumeTwoDelayed(hotHotFlux)
}