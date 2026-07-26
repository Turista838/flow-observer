# flow-observer

Observes `MutableStateFlow` and `MutableSharedFlow` writes.

Annotate mutable properties; a Kotlin compiler plugin injects `.addObservable(...)` so each `value` / `emit` / `update` / `tryEmit` is logged once on the emit side.

## Usage

```kotlin
class LoginViewModel : ViewModel() {

    @ObserveFlow
    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
}
```

- Annotate `MutableStateFlow` / `MutableSharedFlow` properties.
- If `tag` is omitted, defaults to `ClassName.propertyName`.
- `addObservable(tag, subscriptionLogging)` can also be called manually; the plugin skips already-wrapped initializers.

### Logcat

| Flow | Message |
|------|---------|
| `MutableStateFlow` | `change { previousState: …, currentState: … }` |
| `MutableSharedFlow` | `event { … }` |

Examples:

```
I/LoginViewModel._uiState: change { previousState: LoginUiState(loading=false), currentState: LoginUiState(loading=true) }
I/LoginViewModel._events: event { NavigateHome }
```

### When writes are logged

By default every write is logged (once, on the emit side). Set `enabled = false` to turn logging off entirely.

To skip writes that have no collectors, set `logOnlyWhenSubscribed = true` in `FlowObserverSettings`. That global rule applies to flows using `SubscriptionLogging.Default` (the annotation default).

Override a single flow with `subscriptionLogging`:

| Value | Behavior |
|-------|----------|
| `Default` | Follows `logOnlyWhenSubscribed` |
| `Always` | Always log, even with no collectors |
| `OnlyWhenSubscribed` | Log only when `subscriptionCount > 0` |

```kotlin
@ObserveFlow(subscriptionLogging = SubscriptionLogging.OnlyWhenSubscribed)
private val _uiState = MutableStateFlow(...)

@ObserveFlow(subscriptionLogging = SubscriptionLogging.Always)
private val _events = MutableSharedFlow<Event>()
```

## Setup

Android only. Requires Kotlin **2.0.0–2.4.10**.

```kotlin
plugins {
    id("dev.goncaloramalho.flow-observer") version "2.0.0"
}
```

## Configuration

Call `FlowObserver.configure` at app startup (for example in `Application.onCreate`):

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FlowObserver.configure(
            FlowObserverSettings(
                enabled = BuildConfig.DEBUG,
                logOnlyWhenSubscribed = false,
                logger = FlowObserverLogger { tag, message -> Log.d(tag, message) },
            ),
        )
    }
}
```

Defaults if `configure` is never called: `enabled = true`, `logOnlyWhenSubscribed = false`, logger = `Log.i` on Android (stdout on JVM).

| Option | Default | Description |
|--------|---------|-------------|
| `enabled` | `true` | Disables logging when `false` |
| `logOnlyWhenSubscribed` | `false` | For `SubscriptionLogging.Default`, log only while collected |
| `logger` | platform default | Custom sink; otherwise `Log.i` / stdout |

## Components

| Piece | Artifact | Role |
|-------|----------|------|
| Runtime | `flow-observer` | `@ObserveFlow`, `addObservable`, settings, logging |
| Kotlin compiler plugins | `flow-observer-compiler-2.x.x` | IR injection of `addObservable`; one artifact per Kotlin line |
| Gradle plugin | `dev.goncaloramalho.flow-observer` | Wires the runtime and the matching compiler artifact for a Kotlin version |

## Sample

The `:sample` module demonstrates Activity- and screen-scoped ViewModels, subscription-logging options, and `BuildConfig.DEBUG` configuration.

## License

[MIT](LICENSE)
