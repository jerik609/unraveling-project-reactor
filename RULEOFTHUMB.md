# Rule of thumb for dummies (at this point in my learning journey)

1. always assume that thread context switching may happen, even if not using publishOn or subscribeOn explicitly, 
e.g. when we use `Flux.fromFuture`, `flatMap` or `delayElements` - therefore we should not rely on thread affinity
and the statement "operation will forward to next operator using the same thread is only `mostly` correct"
2. still understanding how publishOn and subscribeOn work is crucial to avoid unnecessary thread context switches caused
by us
3. when not having an explicit need, we should not use thread context switching
4. if we need to rely on something spanning a particular subscription processing - it's the Context object