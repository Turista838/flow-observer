package dev.goncaloramalho.flowobserver

/**
 * Marks a public `StateFlow` or `SharedFlow` property on a ViewModel for observation.
 *
 * Call the generated `attachFlowObserver()` from the ViewModel `init` block so collectors
 * bind to the correct instance (Activity-, Fragment-, or navigation-scoped).
 *
 * @property tag Logcat tag. When blank, defaults to `ViewModelName.propertyName`.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ObserveFlow(
    val tag: String = "",
)
