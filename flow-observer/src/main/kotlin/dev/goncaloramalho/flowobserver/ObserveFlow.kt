package dev.goncaloramalho.flowobserver

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ObserveFlow(
    val tag: String = "",
)
