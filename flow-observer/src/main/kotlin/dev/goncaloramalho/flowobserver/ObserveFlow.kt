package dev.goncaloramalho.flowobserver

/**
 * Marks a `MutableStateFlow` or `MutableSharedFlow` property for observation.
 *
 * The Flow Observer compiler plugin rewrites the property initializer to chain
 * [addObservable], so writes (`value` / `emit` / `update` / …) are logged once per emission.
 *
 * Annotate the **mutable** backing property, then expose `asStateFlow()` / `asSharedFlow()` as usual.
 *
 * @property tag Logcat tag. When blank, defaults to `ClassName.propertyName`.
 * @property subscriptionLogging Per-flow policy; [SubscriptionLogging.Default] follows
 * [FlowObserverSettings.logOnlyWhenSubscribed]. Explicit values override the global setting.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class ObserveFlow(
    val tag: String = "",
    val subscriptionLogging: SubscriptionLogging = SubscriptionLogging.Default,
)
