package org.example.parallel

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

class ParallelFluxTests {

    companion object {

        val logger: Logger = Logger.getLogger(ParallelFluxTests::class.java.name)

        // example of "groups" usage

        fun run() {
            // produces elements in a separate thread, until cancelled
            Flux.create { sink ->

                val atomicInteger = AtomicInteger(0)

                Thread {
                    while (!sink.isCancelled) {
                        val count = atomicInteger.incrementAndGet()
                        logger.info("producing value: $count")
                        sink.next(count)
                    }
                }.start()

                logger.info("producer thread stopped")

            }
                // just to catch the event of cancellation
                .doOnCancel { logger.info("cancelled") }
                // take 50 elements and then cancel
                .take(50)
                // limit parallelism to 4 (4 rail), we can have more or less rails than actual available threads
                .parallel(4)
                // a scheduler to run on, custom in this case - to demonstrate the that parallelism and number of threads can differ
                .runOn(Schedulers.fromExecutor(Executors.newFixedThreadPool(6)))
                // a flat map which will multiply the values by 2, simulating "real work"
                .flatMap { value ->
                    val product = value * 2
                    logger.info("(${Thread.currentThread().name} - ${Thread.currentThread().threadId()}) - processing: $product")
                    Mono.just(product)
                }
                // example of groups usage - exposes the rails
                .groups()
                // collecting the results of individual rails into a list:
                // we're dealing with a flux of fluxes (inner = "railId and itemValue") - which we want to flatten into a flux of pairs of "railId and list"
                .flatMap { group ->
                    // we will flatten each individual inner flux
                    // collectList does to collection of itemValues, but it returns a Mono<List>
                    // This would create a Pair<railId, Mono<List> - thus one more flattening is needed - of the Mono<List> into a Mono<Pair>
                    group.collectList().flatMap { list ->
                        logger.info("(${Thread.currentThread().name} - ${Thread.currentThread().threadId()}) - collected list for rail id: ${group.key()}: $list)")
                        // into a Mono (since we're working with flatMap) of the desired pairs
                        Mono.just(Pair(group.key(), list))
                    }
                    // so we transformed the Flux of Fluxes into a Flux of Pairs (flatMap will subscribe to the Mono pair and wait for the pair, then pack it to flux)
                }
                .subscribe(
                    {
                        logger.info("(${Thread.currentThread().name} - ${Thread.currentThread().threadId()}) - rail id: ${it.first} consumed: ${it.second})")
                    },
                    {
                        logger.info("(${Thread.currentThread().name} - ${Thread.currentThread().threadId()}) - error: ${it.message}")
                    },
                    {
                        logger.info("(${Thread.currentThread().name} - ${Thread.currentThread().threadId()}) - completed")
                    }
                )

            while(true) {

            }

        }
    }
}