package dev.goncaloramalho.flowobserver

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow

internal class LoggingSharedFlow<T>(
    private val source: SharedFlow<T>,
    private val tag: String,
) : SharedFlow<T> by source {

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        source.collect { value ->
            FlowObserverLog.log(tag, "event { $value }")
            collector.emit(value)
        }
    }
}
