package exercises.part4;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class Exercise1InJava {

    public static void runMe() throws InterruptedException {
        List<String> stockSymbols = Arrays.asList("AAPL", "GOOG", "MSFT", "AMZN", "FB");

        // Create a Flux from stockSymbols
        // Fetch historical prices
        // Subscribe to the Flux
        Flux.fromIterable(stockSymbols)
                .concatMap(stockSymbol -> fetchHistoricalPrices(stockSymbol))
                .subscribe(System.out::println);

        Thread.sleep(5500);
    }


    public static Mono<List<Double>> fetchHistoricalPrices(String stockSymbol) {
        int c = stockSymbol.charAt(0);
        return Mono.just(Arrays.asList(c*10.0, c*20.0, c*30.0))
                .delayElement(Duration.ofMillis(1000));
    }

}
