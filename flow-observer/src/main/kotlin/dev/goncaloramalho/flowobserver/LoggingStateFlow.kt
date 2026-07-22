package dev.goncaloramalho.flowobserver

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

internal class LoggingStateFlow<T>(
    private val source: StateFlow<T>,
    private val tag: String,
) : StateFlow<T> by source {

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        var previous = source.value
        var isFirst = true
        source.collect { next ->
            if (isFirst) {
                isFirst = false
                previous = next
            } else {
                FlowObserverLog.log(
                    tag,
                    "change { previousState: $previous, currentState: $next }",
                )
                previous = next
            }
            collector.emit(next)
        }
    }
}
