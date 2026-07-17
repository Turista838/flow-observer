package dev.goncaloramalho.flowobserver

data class FlowObserverSettings(
    val enabled: Boolean = true,
    val logger: FlowObserverLogger? = null,
)
