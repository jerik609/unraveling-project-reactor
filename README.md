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