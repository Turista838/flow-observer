package dev.goncaloramalho.flowobserver

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class FlowObserver(
    val tag: String = "",
    val logInitial: Boolean = false,
)
