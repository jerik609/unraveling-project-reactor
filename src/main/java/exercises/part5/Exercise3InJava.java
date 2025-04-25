package exercises.part5;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Exercise3InJava {
    public static void run() {
        String fallbackUrl = "fallbackUrl";

        Flux.just("url1", "url2", "url3", "url4")
                .flatMap(url -> Mono.fromCallable(() -> fetchData(url))
                        .map(Exercise3InJava::parseJson)
                        .onErrorResume(RuntimeException.class, e -> {
                            String fallbackJson = fetchData(fallbackUrl);
                            return Mono.just(parseJson(fallbackJson));
                        }))
                .subscribe(System.out::println);
    }

    public static String fetchData(String url) {
        // Simulates fetching JSON data from the URL
        return "{\"data\": \"" + url + "\"}";
    }

    public static String parseJson(String jsonString) {
        // Simulates the parsing of jsonString and throwing an exception if it is invalid
        String str = "";
        if(jsonString.contains("url3"))
            throw new RuntimeException("Invalid JSON");
        else
            str = jsonString.toUpperCase();
        return str;
    }
}