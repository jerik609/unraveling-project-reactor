package org.example

fun main() {

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