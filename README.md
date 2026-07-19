# flow-observer

KSP library for observing `StateFlow` and `SharedFlow` changes in Android ViewModels.

Annotate the flows you care about, call the generated `attachFlowObserver()` from `init`, and optionally configure logging once at app startup.

## Setup

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("dev.goncaloramalho:flow-observer:1.0.0")
    ksp("dev.goncaloramalho:flow-observer-compiler:1.0.0")
}
```

## Usage

### Annotate flows and attach in `init`

```kotlin
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    @ObserveFlow
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    @ObserveFlow(tag = "Login.events")
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        attachFlowObserver()
    }
}
```

- Prefer the **public** `StateFlow` / `SharedFlow`, not the private `Mutable*` backing property.
- If `tag` is omitted, it defaults to `ViewModelName.propertyName`.
- Call `attachFlowObserver()` from `init` so observation follows the **real** ViewModel instance (Activity-, Fragment-, or Nav-scoped).

### What gets logged

| Flow | Message shape |
|------|----------------|
| `StateFlow` | `change { previousState: …, currentState: … }` (skips the initial value) |
| `SharedFlow` | `event { … }` |

## Configuration

Configure once at app startup — typically in `Application.onCreate` — via `FlowObserver.configure`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FlowObserver.configure(
            FlowObserverSettings(
                enabled = BuildConfig.DEBUG,
                logger = FlowObserverLogger { tag, message -> Log.d(tag, message) },
            ),
        )
    }
}
```

If you never call `configure`, defaults apply: observation is **enabled**, and messages go to **`Log.i`**.

### `FlowObserverSettings`

| Option | Default | Description |
|--------|---------|-------------|
| `enabled` | `true` | When `false`, collectors still run but nothing is logged. Use `BuildConfig.DEBUG` to keep release builds quiet. |
| `logger` | `null` | Custom log sink. When `null`, generated code uses `Log.i(tag, message)`. |

### `enabled`

Turn logging off without removing annotations or `attachFlowObserver()` calls:

```kotlin
FlowObserver.configure(
    FlowObserverSettings(enabled = BuildConfig.DEBUG),
)
```

### `logger`

Redirect output anywhere you like (Logcat level, Timber, file, etc.):

```kotlin
// Log.d instead of the default Log.i
FlowObserver.configure(
    FlowObserverSettings(
        enabled = BuildConfig.DEBUG,
        logger = FlowObserverLogger { tag, message -> Log.d(tag, message) },
    ),
)

// Timber
FlowObserver.configure(
    FlowObserverSettings(
        enabled = BuildConfig.DEBUG,
        logger = FlowObserverLogger { tag, message -> Timber.tag(tag).d(message) },
    ),
)
```

`FlowObserverLogger` is a `fun interface` with a single method:

```kotlin
fun interface FlowObserverLogger {
    fun log(tag: String, message: String)
}
```

## Sample

The `:sample` module shows Activity-scoped ViewModels, a Nav destination with a screen-scoped ViewModel, and configuration with `BuildConfig.DEBUG` plus a `Log.d` logger.

## License

[MIT](LICENSE)
