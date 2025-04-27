## Code Description: ParallelFluxTests (Gemini generated & checked to match the behavior)

This code defines a class `ParallelFluxTests` within the `org.example.parallel` package, utilizing Project Reactor for parallel stream processing.

1.  **Setup**:
    *   It imports necessary classes from Reactor (`Flux`, `Mono`, `Schedulers`), Java concurrency (`Executors`, `AtomicInteger`), and Java logging (`Logger`).
    *   Inside a `companion object`, it initializes a standard Java `Logger`.

2.  **`run()` Function**: This function encapsulates the core reactive stream logic:
    *   **Flux Creation (`Flux.create`)**: A `Flux` is created programmatically.
        *   An `AtomicInteger` is initialized.
        *   A new `Thread` is started. Inside this thread, a loop runs as long as the `Flux` sink (`sink`) is not cancelled.
        *   In the loop, the `AtomicInteger` is incremented, the new value is logged, and then emitted into the `Flux` via `sink.next()`.
        *   A log message indicates when the producer thread stops (upon cancellation).
    *   **Cancellation Hook (`doOnCancel`)**: A side-effect logs a message if the stream receives a cancellation signal.
    *   **Limiting Elements (`take`)**: The stream is limited to the first 50 elements. After 50 elements are processed, it automatically cancels the upstream source.
    *   **Parallelization (`parallel`)**: The `Flux` is converted into a `ParallelFlux` configured to run on 4 parallel rails (logical units of parallelism).
    *   **Scheduler Assignment (`runOn`)**: It specifies that the parallel operations should execute on threads provided by a custom scheduler. This scheduler is created from a fixed-size thread pool of 6 threads (`Executors.newFixedThreadPool(6)`).
    *   **Parallel Processing (`flatMap` - First)**: This operation is applied to each parallel rail.
        *   For each value received, it calculates `value * 2`.
        *   It logs the calculated `product` along with the name and ID of the executing thread.
        *   The result (`product`) is wrapped in a `Mono.just()` and returned.
    *   **Grouping (`groups`)**: The `ParallelFlux` is converted back into a sequential `Flux`. Each element emitted by this `Flux` is a `GroupedFlux<Int, Int>`, representing one of the original parallel rails and containing the items processed on that rail. The key of the `GroupedFlux` is the rail ID.
    *   **Collecting Group Results (`flatMap` - Second)**: This operation processes each `GroupedFlux` (each rail) individually.
        *   `group.collectList()`: Collects all integer values processed on the current rail (`group`) into a `Mono<List<Int>>`.
        *   An inner `.flatMap` processes the `Mono<List<Int>>`. Once the list is available:
            *   It logs the collected list, the rail ID (`group.key()`), and the current thread's name/ID.
            *   It creates a `Pair` containing the rail ID and the collected list (`Pair(group.key(), list)`).
            *   This `Pair` is wrapped in `Mono.just()`.
        *   The outer `flatMap` subscribes to this inner `Mono<Pair>` and emits the resulting `Pair` downstream.
    *   **Subscription (`subscribe`)**: The final `Flux<Pair<Int, List<Int>>>` is subscribed to.
        *   The `onNext` lambda receives each `Pair` and logs the rail ID (`it.first`), the consumed list (`it.second`), and the current thread's name/ID.
        *   The `onError` lambda logs any error message along with the thread info.
        *   The `onComplete` lambda logs a completion message with thread info when the entire stream finishes successfully.
    *   **Keep Alive Loop (`while(true)`)**: An infinite loop at the end prevents the main thread from exiting immediately, allowing the background threads managed by the Reactor scheduler and the custom executor to complete their work and produce output.

In summary, the code sets up a number producer, processes its first 50 numbers in parallel across 4 logical rails using a pool of 6 threads, simulates work, groups the results by rail, collects these results into lists per rail, and finally logs the grouped results.