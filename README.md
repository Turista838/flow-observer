# flow-observer

Kotlin library + compiler plugin for observing `StateFlow` and `SharedFlow` changes in Android ViewModels.

Annotate the flows you care about. The compiler plugin injects `.addObservable(tag)` into the property initializer so logging runs on the **same collect chain** as your UI (no extra forever subscriber, no `attachFlowObserver()`).

> **Logging only happens while something is collecting the flow.**  
> No collector (UI, `LaunchedEffect`, tests, …) → no logs, even if the upstream emits.  
> That keeps `SharingStarted.WhileSubscribed` correct: the observer is not a hidden subscriber.

## Setup

```kotlin
dependencies {
    implementation("dev.goncaloramalho:flow-observer:1.0.0")
    kotlinCompilerPluginClasspath("dev.goncaloramalho:flow-observer-compiler:1.0.0")
}
```

Use a Kotlin version compatible with the compiler plugin (this project targets Kotlin **2.2.x**).

## Usage

### Annotate flows

```kotlin
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    @ObserveFlow
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    // compiler rewrites to: _uiState.asStateFlow().addObservable(tag = "LoginViewModel.uiState")
}
```

- Annotate a **public** `StateFlow` or `SharedFlow` only.
- If `tag` is omitted, defaults to `ClassName.propertyName`.
- Do **not** call `attachFlowObserver()` — that API is gone.
- You may call `addObservable(tag)` manually if you prefer; the plugin skips already-wrapped initializers.
- **Collect the flow** (e.g. `collectAsStateWithLifecycle()`) or nothing will be logged.

### When logging runs

| Situation | Logs? |
|-----------|--------|
| UI / code is collecting the annotated flow | **Yes** (on delivered emissions) |
| Annotated flow has zero collectors | **No** |
| `FlowObserverSettings.enabled = false` | **No** |

### What gets logged

| Flow | Message shape |
|------|----------------|
| `StateFlow` | `change { previousState: …, currentState: … }` |
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

If you never call `configure`, defaults apply: observation is **enabled**, and messages go to **`Log.i`** on Android (or stdout on JVM).

### `FlowObserverSettings`

| Option | Default | Description |
|--------|---------|-------------|
| `enabled` | `true` | When `false`, collectors still run but nothing is logged. Use `BuildConfig.DEBUG` to keep release builds quiet. |
| `logger` | `null` | Custom log sink. When `null`, uses `Log.i` on Android or stdout on JVM. |

## Sample

The `:sample` module shows Activity-scoped ViewModels, a Nav destination with a screen-scoped ViewModel, `WhileSubscribed` sharing, and configuration with `BuildConfig.DEBUG` plus a `Log.d` logger.

## License

[MIT](LICENSE)
