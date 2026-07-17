package dev.goncaloramalho.flowobserver

/**
 * Runtime options for generated flow observers.
 *
 * @property enabled When `false`, state/event emissions are not logged.
 * @property logger Custom sink for log lines. When `null`, generated code uses `Log.i`.
 */
data class FlowObserverSettings(
    val enabled: Boolean = true,
    val logger: FlowObserverLogger? = null,
)
