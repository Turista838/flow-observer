# flow-observer

Kotlin library + compiler plugin for observing `MutableStateFlow` and `MutableSharedFlow` writes in Android ViewModels.

Annotate the **mutable** backing properties. The compiler plugin injects `.addObservable(...)` so each `value` / `emit` / `update` / `tryEmit` is logged **once on the emit side** — not once per collector.

## Setup

```kotlin
dependencies {
    implementation("dev.goncaloramalho:flow-observer:1.0.0")
    // Pick the compiler artifact for your Kotlin version (see table below).
    kotlinCompilerPluginClasspath("dev.goncaloramalho:flow-observer-compiler-2.2.21:1.0.0")
}
```

The runtime library is shared. The compiler plugin is **versioned per compatible Kotlin line** because it loads into the host compiler.

| App Kotlin | Compiler artifact |
|------------|-------------------|
| 2.0.0, 2.0.10 | `flow-observer-compiler-2.0.10` |
| 2.0.20, 2.0.21 | `flow-observer-compiler-2.0.21` |
| 2.1.0 – 2.1.21 | `flow-observer-compiler-2.1.10` |
| 2.2.0, 2.2.10 | `flow-observer-compiler-2.2.10` |
| 2.2.20, 2.2.21 | `flow-observer-compiler-2.2.21` |
| 2.3.0 – 2.3.21 | `flow-observer-compiler-2.3.21` |

In this repo, IR/registrar sources are split under `flow-observer-compiler/src/` (`common`, `ir-legacy`, `ir-legacy21`, `ir-modern`, `registrar`, `registrar23`), and thin modules under `flow-observer-compiler/<kotlin>/` publish as `flow-observer-compiler-<kotlin>`.

## Usage

### Annotate mutable flows

```kotlin
class LoginViewModel : ViewModel() {

    @ObserveFlow
    private val _uiState = MutableStateFlow(LoginUiState())
    // → MutableStateFlow(...).addObservable("LoginViewModel._uiState", SubscriptionLogging.Default)

    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
}
```

- Annotate **`MutableStateFlow` / `MutableSharedFlow`** (typically the private `_uiState` / `_events`).
- Expose read-only `asStateFlow()` / `asSharedFlow()` as usual (no annotation on those).
- If `tag` is omitted, defaults to `ClassName.propertyName`.
- You may call `addObservable(tag, subscriptionLogging)` manually; the plugin skips already-wrapped initializers.
- `stateIn` / `shareIn` results are **not** supported (no mutable write path to hook).

### When logging runs

By default (`SubscriptionLogging.Default` + `logOnlyWhenSubscribed = false`): **log every write**, even with zero collectors.

| Situation | Default settings | `logOnlyWhenSubscribed = true` |
|-----------|------------------|--------------------------------|
| Write on annotated mutable | **Yes** (once) | Only if `subscriptionCount > 0` |
| Many collectors | Still **one** log per write | Still **one** log per write |
| `enabled = false` | **No** | **No** |

Per-flow override via annotation (wins over settings):

```kotlin
@ObserveFlow(subscriptionLogging = SubscriptionLogging.OnlyWhenSubscribed)
private val _uiState = MutableStateFlow(...)

@ObserveFlow(subscriptionLogging = SubscriptionLogging.Always)
private val _events = MutableSharedFlow<Event>()
```

| `subscriptionLogging` | Meaning |
|-----------------------|---------|
| `Default` | Use `FlowObserverSettings.logOnlyWhenSubscribed` at log time |
| `OnlyWhenSubscribed` | Always require collectors (ignore settings) |
| `Always` | Always log writes (ignore settings) |

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
                logOnlyWhenSubscribed = false, // log every write by default
                logger = FlowObserverLogger { tag, message -> Log.d(tag, message) },
            ),
        )
    }
}
```

If you never call `configure`, defaults apply: observation is **enabled**, `logOnlyWhenSubscribed` is **false**, and messages go to **`Log.i`** on Android (or stdout on JVM).

### `FlowObserverSettings`

| Option | Default | Description |
|--------|---------|-------------|
| `enabled` | `true` | When `false`, writes are not logged. Use `BuildConfig.DEBUG` to keep release builds quiet. |
| `logOnlyWhenSubscribed` | `false` | When `true`, flows with `SubscriptionLogging.Default` log only if someone is collecting. |
| `logger` | `null` | Custom log sink. When `null`, uses `Log.i` on Android or stdout on JVM. |

## Sample

The `:sample` module shows Activity-scoped ViewModels, a Nav destination with a screen-scoped ViewModel, a **subscription-logging** demo (`logOnlyWhenSubscribed` vs `SubscriptionLogging.Always`), and configuration with `BuildConfig.DEBUG` plus a `Log.d` logger.

## License

[MIT](LICENSE)
