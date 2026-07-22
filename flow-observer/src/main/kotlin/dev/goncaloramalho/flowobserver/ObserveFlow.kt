package dev.goncaloramalho.flowobserver

/**
 * Marks a `MutableStateFlow` or `MutableSharedFlow` property for observation.
 *
 * The Flow Observer compiler plugin rewrites the property initializer to chain
 * [addObservable] with [tag], so writes (`value` / `emit` / `update` / …) are logged once
 * per emission — BlocObserver-style, independent of how many collectors listen.
 *
 * Annotate the **mutable** backing property, then expose `asStateFlow()` / `asSharedFlow()` as usual.
 *
 * @property tag Logcat tag. When blank, defaults to `ClassName.propertyName`.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class ObserveFlow(
    val tag: String = "",
)
