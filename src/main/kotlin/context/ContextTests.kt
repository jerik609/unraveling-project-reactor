package org.example.context

import reactor.core.publisher.Flux
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
    }


}