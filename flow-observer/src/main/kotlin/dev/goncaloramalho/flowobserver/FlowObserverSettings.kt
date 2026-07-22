package dev.goncaloramalho.flowobserver

/**
 * Runtime options for flow observers.
 *
 * @property enabled When `false`, state/event emissions are not logged.
 * @property logger Custom sink for log lines. When `null`, defaults to `Log.i` on Android
 * (via reflection) or stdout on JVM.
 */
data class FlowObserverSettings(
    val enabled: Boolean = true,
    val logger: FlowObserverLogger? = null,
)
