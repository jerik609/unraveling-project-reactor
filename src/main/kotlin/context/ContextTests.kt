package org.example.context

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.context.Context
import reactor.util.context.ContextView
import java.util.stream.Stream

class ContextTests {

    // overit ze se kontext nastavuje odspoda, t.j. ten "nahore" ma vsechny zmeny, ale kdyz dame flatmap mezi nastavovani,
    // vidime kontext v tom stavu jak byl v danem miste, ne ze mame ten mergnuty velky kontext ktery prepadava dolu
    // proste tam mame ty kontext objekty ktere jsme vytvorili "po ceste" (= pri zmenach se vzdy vytvari novy kontext, kontext je immutable)

    // kontext se vytvari pri subscripci, resp. kazda subskripce ma svuj vlastni (napr proto se z flatmap kontext nepropaguje - vznika pro interni
    // subskripce a nema nic hlavni subskripci spolecnyho)

    // kdyz menime konkretni klic, tak plati nejvyssi hodnota, ale zas, mezi dvema zmenama plati ten kontext co tam byl vytvoreny, ten nezanika!

    // kontext je deferred, t.j. podobne jako vytvareni flux ze streamu - kdyz pouzijeme non deferrer flux ze stream, tak to hodi chybu (stream skonzumovan),
    // takze musime dat deferred flux creation

    // podobne context, nebyt deferred, tak bychom pouzivali jeden kontext pro vsechny subsckripce - takze musime mit deferred, aby se
    // pri subskripci vzala vzdy ta nove vytvorena

    companion object {
        fun streamTestRun() {

            try {
                // won't work, stream is consumed on 2nd subscribe
                val f = Flux.fromStream(Stream.of(1, 2, 3, 4))
                f.subscribe({}, { println("sub1: error: ${it.message}")}, { println("sub1: completed")})
                f.subscribe({}, { println("sub2: error: ${it.message}")}, { println("sub2: completed")})
            } catch (e: Exception) {
                println("error: ${e.message}")
            }

            println("======================")

            try {
                // works fine, new stream always provided by supplier
                val f = Flux.fromStream {Stream.of(1, 2, 3, 4) } // new stream created by supplier
                f.subscribe({}, { println("sub1: error: ${it.message}")}, { println("sub1: completed")})
                f.subscribe({}, { println("sub2: error: ${it.message}")}, { println("sub2: completed")})
                println("this works fine, always new stream provided by supplier")
            } catch (e: Exception) {
                println("error: ${e.message}")
            }

        }

        fun funnyContext() {

            val getConcatLevels = { context: ContextView ->
                val l1 = context.getOrDefault("level1", "default 1 ")!!
                val l2 = context.getOrDefault("level2", "default 1 ")!!
                val l3 = context.getOrDefault("level3", "default 1 ")!!
                val l4 = context.getOrDefault("level4", "default 1 ")!!
                val l5 = context.getOrDefault("level5", "default 1 ")!!
                l1 + l2 + l3 + l4 + l5
            }

            val flux = Flux.just("")
                .contextWrite(Context.of("level5", "l5 "))
                .transformDeferredContextual { flux, context ->
                    val suffix = getConcatLevels(context)
                    flux.flatMapSequential { value ->
                        Mono<String>.fromCallable { "$value + $suffix <<< TOP \n" }
                            .publishOn(Schedulers.parallel())
                    }
                }
                .contextWrite(Context.of("level4", "l4 "))
                .transformDeferredContextual { flux, context ->
                    val suffix = getConcatLevels(context)
                    flux.flatMapSequential { value ->
                        Mono<String>.fromCallable { "$value + $suffix <<< TOP-1 \n" }
                            .publishOn(Schedulers.parallel())
                    }
                }
                .contextWrite(Context.of("level3", "l3 "))
                .transformDeferredContextual { flux, context ->
                    val suffix = getConcatLevels(context)
                    flux.flatMapSequential { value ->
                        Mono<String>.fromCallable { "$value + $suffix TOP-2 \n" }
                            .publishOn(Schedulers.parallel())
                    }
                }
                .contextWrite(Context.of("level2", "l2 "))
                .transformDeferredContextual { flux, context ->
                    val suffix = getConcatLevels(context)
                    flux.flatMapSequential { value ->
                        Mono<String>.fromCallable { "$value + $suffix TOP-3 \n" }
                            .publishOn(Schedulers.parallel())
                    }
                }
                .contextWrite(Context.of("level1", "l1 "))
                .transformDeferredContextual { flux, context ->
                    val suffix = getConcatLevels(context)
                    flux.flatMapSequential { value ->
                        Mono<String>.fromCallable { "$value + $suffix TOP-4 \n" }
                            .publishOn(Schedulers.parallel())
                    }
                }

            flux
                .subscribe { println(it) }

            while (true) {

            }
        }

    }


}