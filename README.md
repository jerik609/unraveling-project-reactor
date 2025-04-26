# Comments

## Releasing flux after subscriber cancellation

### From Gemini

**Impact on the Source (Publisher Side) - This is Key:** What happens to the underlying source of the `Flux` depends heavily on how the `Flux` was created and whether it's being shared (multicast):
*   **Cold Flux (e.g., `Flux.just`, `Flux.range`, basic `Flux.create` without sharing):** If the `Flux` is cold, each subscription typically triggers an independent execution of the source logic. Cancelling one subscription usually has **no effect** on the source itself or any *other* active or future subscriptions. If you subscribe again, it will start over. In your `HotFlux.kt`, if `testColdHotFlux()` is *not* followed by `.share()`, cancelling one subscription (like in `testColdHotFlux3`) will stop that flow, but the underlying `ExecutorService` and the `while(true)` loop associated with *that specific subscription* might continue running if not properly handled (which is a potential resource leak!). Another subscriber would start a *new* execution.
*   **Hot/Shared Flux (e.g., using `.share()`, `.publish().refCount()`, `Sinks`):** This is where cancellation often has a more significant impact on the source.
    *   **`.share()` / `.publish().refCount(n)`:** These operators track the number of active subscribers. When a subscriber cancels, the reference count decreases. If the count drops to zero (for `.share()` or `refCount(1)`), the operator automatically **cancels its own subscription to the original upstream source**. This is the mechanism you'd use to signal the original source (like the one created with `Flux.create`) that it's no longer needed and should clean up its resources (e.g., stop the emitting thread, shut down the executor). This cleanup needs to be implemented in the source, often using `sink.onDispose {}` within `Flux.create`.
    *   **`Sinks.many().asFlux()` (like in `testHotHotFlux`):** When a subscriber cancels its subscription to the `Flux` obtained from `sink.asFlux()`, it simply detaches from the sink. The sink itself, and the process feeding it (your `executor.submit` loop), usually **continues running** unaffected by default. The sink might stop emitting *to that specific subscriber*, but it keeps processing and potentially emitting for other current or future subscribers. To stop the *emitter* when all subscribers are gone, the emitter logic itself needs to check `sink.currentSubscriberCount()` periodically and decide to stop, as discussed previously.

### My take

As mentioned above:

A flux with shared/multicast capability is basically a connectable flux. Subscribers may join in (albeit missing some data) and leave. The flux carries on as long as there are subscribers.

A non-shared flux will simply handle each subscription independently ... it will spin up everything "anew". So as a result, the resources will be allocated again, e.g. the thread loop too.

Now the problem is when we have this sort of internal loop (due to connection to the sync world). In such case these "emitter" threads will keep on running. The sink will be informed about the cancellation, but a thread is a separate entity from the reactor framework and thus must be handled explicitly by the developer - the thread will not be automatically "destroyed". This would case a resource leak if not handled, which would be particularly painful in the non-shared scenario.

Same holds for fluxes created out of both `Sinks.many().asFlux()` and `Flux.create(emitter)`.

## Bubbling exception

Basically used for exceptional situations, where we want to make sure the exception is really handled on the highest level, instead of it being lost somewhere down the processing stream.

### From Gemini

BubblingException in Project Reactor Explained
BubblingException is a specialized RuntimeException within Project Reactor, primarily used internally by the framework to wrap and propagate exceptions that are considered "fatal" or critical and should "bubble up" the reactive stream chain. Its main purpose is to ensure that certain types of errors, particularly checked exceptions or those indicating severe issues, are not silently dropped or mishandled within the asynchronous and non-blocking nature of reactive streams.
Project Reactor, adhering to the Reactive Streams specification, has specific rules about how errors are handled. The onError signal is the standard way to propagate errors downstream to a subscriber. However, some exceptions might occur in contexts where the standard onError path is not the most appropriate or even possible, such as within operators that are not directly tied to a downstream subscriber at that moment, or when dealing with exceptions that the Reactive Streams specification limits for onError propagation.
This is where BubblingException, often created via the utility method reactor.core.Exceptions.bubble(Throwable t), comes into play. When a checked exception or another designated "fatal" error occurs within a Reactor operator or internal process, it might be wrapped in a BubblingException. This wrapping effectively turns a checked exception into an unchecked one, allowing it to be re-thrown and handled by a higher level in the stream or the overall error handling mechanism configured for the pipeline.
Key characteristics and implications of BubblingException:
* Unchecked Exception: BubblingException extends ReactiveException, which in turn extends RuntimeException. This means it does not need to be explicitly caught in try-catch blocks, aligning with the common practice in reactive programming to handle errors declaratively using operators.
* Signals "Fatal" Errors: Reactor uses BubblingException to indicate errors that are considered significant and should interrupt the normal flow and bubble up the operator chain. These are often errors that the system cannot reasonably recover from at the point of origin.
* Wrapper for Original Cause: A BubblingException typically wraps the original exception that caused the issue. The original exception can be accessed using the getCause() method.
* Used by Exceptions.bubble(): The primary way BubblingException instances are created is through the static utility method reactor.core.Exceptions.bubble(Throwable t). This method wraps the given Throwable in a BubblingException after potentially performing checks for already fatal exceptions.
* Identifiable: Reactor provides utility methods like Exceptions.isBubbling(Throwable t) to check if an exception is a BubblingException and Exceptions.unwrap(Throwable t) to retrieve the original cause if it is.
  In essence, BubblingException is an internal mechanism within Project Reactor to manage the propagation of certain critical exceptions, ensuring they don't get lost in the asynchronous processing and are instead surfaced in a way that allows for appropriate handling further up the reactive stream or by global error handling configurations. While developers typically interact with error handling through operators like onErrorResume, onErrorReturn, or by subscribing with an error consumer, understanding BubblingException provides insight into Reactor's internal error management strategy, particularly when dealing with unexpected or severe errors.

## subscribeOn

