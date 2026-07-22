# flow-observer

Kotlin library + compiler plugin for observing `MutableStateFlow` and `MutableSharedFlow` writes in Android ViewModels (BlocObserver-style).

Annotate the **mutable** backing properties. The compiler plugin injects `.addObservable(tag)` so each `value` / `emit` / `update` / `tryEmit` is logged **once on the emit side** — not once per collector.

## Setup

```kotlin
dependencies {
    implementation("dev.goncaloramalho:flow-observer:1.0.0")
    kotlinCompilerPluginClasspath("dev.goncaloramalho:flow-observer-compiler:1.0.0")
}
```

Use a Kotlin version compatible with the compiler plugin (this project targets Kotlin **2.2.x**).

## Usage

### Annotate mutable flows

```kotlin
class LoginViewModel : ViewModel() {

    @ObserveFlow
    private val _uiState = MutableStateFlow(LoginUiState())
    // compiler rewrites to: MutableStateFlow(...).addObservable(tag = "LoginViewModel._uiState")

    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
}
```

- Annotate **`MutableStateFlow` / `MutableSharedFlow`** (typically the private `_uiState` / `_events`).
- Expose read-only `asStateFlow()` / `asSharedFlow()` as usual (no annotation on those).
- If `tag` is omitted, defaults to `ClassName.propertyName`.
- You may call `addObservable(tag)` manually; the plugin skips already-wrapped initializers.
- `stateIn` / `shareIn` results are **not** supported (no mutable write path to hook).

### When logging runs

| Situation | Logs? |
|-----------|--------|
| You write via `value` / `update` / `emit` / `tryEmit` on the annotated mutable | **Yes** (once per write) |
| Many collectors listen to `asStateFlow()` / `asSharedFlow()` | Still **one** log per write |
| Nobody is collecting | Still **yes** (same as BlocObserver) |
| `FlowObserverSettings.enabled = false` | **No** |

### What gets logged

| Flow | Message shape |
|------|----------------|
| `MutableStateFlow` | `change { previousState: …, currentState: … }` |
| `MutableSharedFlow` | `event { … }` |

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
| `enabled` | `true` | When `false`, writes are not logged. Use `BuildConfig.DEBUG` to keep release builds quiet. |
| `logger` | `null` | Custom log sink. When `null`, uses `Log.i` on Android or stdout on JVM. |

## Sample

The `:sample` module shows Activity-scoped ViewModels, a Nav destination with a screen-scoped ViewModel, and configuration with `BuildConfig.DEBUG` plus a `Log.d` logger.

## License

[MIT](LICENSE)
