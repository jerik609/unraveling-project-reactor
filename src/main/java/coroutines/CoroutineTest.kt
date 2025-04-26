package coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CoroutineTest {

    fun run() = runBlocking {

        launch {
            while (true) {
                println("hello!!!")
                delay(1000)
            }
        }

        launch {
            printer()
        }

    }

    fun printer() = {

        while (true) {
            println("hello!!!")

        }

    }


}