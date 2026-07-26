package dev.goncaloramalho.flowobserver

/**
 * Runtime options for flow observers.
 *
 * @property enabled When `false`, writes are not logged.
 * @property logOnlyWhenSubscribed When `true` and a flow uses [SubscriptionLogging.Default],
 * log only if the mutable flow has at least one collector. Default `false` (always log).
 * @property logger Custom sink for log lines. When `null`, defaults to `Log.i` on Android
 * (via reflection) or stdout on JVM.
 */
data class FlowObserverSettings(
    val enabled: Boolean = true,
    val logOnlyWhenSubscribed: Boolean = false,
    val logger: FlowObserverLogger? = null,
)
