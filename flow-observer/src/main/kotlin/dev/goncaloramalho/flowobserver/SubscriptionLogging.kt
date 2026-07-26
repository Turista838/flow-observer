package dev.goncaloramalho.flowobserver

/**
 * Controls whether emit-side logging requires active collectors.
 *
 * When [Default], [FlowObserverSettings.logOnlyWhenSubscribed] is used at log time
 * (so [FlowObserver.configure] can change behavior without recompiling).
 * Explicit [OnlyWhenSubscribed] / [Always] override the global setting for that flow.
 */
enum class SubscriptionLogging {
    /** Follow [FlowObserverSettings.logOnlyWhenSubscribed]. */
    Default,

    /** Log only when `subscriptionCount > 0`. */
    OnlyWhenSubscribed,

    /** Log on every write, even with zero collectors. */
    Always,
}
