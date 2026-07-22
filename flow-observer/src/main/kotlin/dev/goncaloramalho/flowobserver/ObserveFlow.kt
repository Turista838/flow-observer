package dev.goncaloramalho.flowobserver

/**
 * Marks a public `StateFlow` or `SharedFlow` property for observation.
 *
 * The Flow Observer compiler plugin rewrites the property initializer to chain
 * [addObservable] with [tag], so logging runs on the same collect chain as UI collectors
 * (no extra `launchIn` subscriber).
 *
 * @property tag Logcat tag. When blank, defaults to `ClassName.propertyName`.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class ObserveFlow(
    val tag: String = "",
)
