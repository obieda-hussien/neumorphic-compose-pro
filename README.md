# Neumorphism UI Library for Android

A modern, flexible Neumorphism UI library for Android supporting both **Jetpack Compose** and traditional **XML/Java Android Views** - optimized in this fork to render shadows with far less CPU/battery cost than the upstream implementation, with no change to visual quality.

[![](https://jitpack.io/v/obieda-hussien/neumorphic-compose-pro.svg)](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro) ![List of Awesome List Badge](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg) [![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

<p align="center">
<img src="https://github.com/obieda-hussien/neumorphic-compose-pro/blob/main/static/complete_screen.png?raw=true" height=400>
</p>

## Features ✨

- 🎨 **Jetpack Compose Support** - Use the `neumorphic()` modifier with any composable
- 📱 **XML/Java Views Support** - Traditional Android Views (NeumorphicView, NeumorphicButton, NeumorphicCardView)
- 🌓 **Dark Theme Support** - Built-in light and dark theme color schemes
- 🎭 **Material You Integration** - Dynamic colors on Android 12+
- 💫 **Animation Support** - Smooth press animations
- 🔆 **Configurable Light Source** - TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
- 🔋 **Low-power rendering** - shadow bitmaps are cached and shared instead of being regenerated on every frame; see [Performance](#performance)
- ✨ **Smooth crossfade transitions** - selection changes (chips, nav bar) fade between states instead of cutting abruptly; see [What's New in 4.0.0](#whats-new-in-400)
- 🎯 **Interactive controls** - Compose Slider, Switch, Checkbox, and RadioButton expose correct interaction and accessibility semantics
- 🧪 **Regression-tested** - Compose UI, concurrent blur, cache-pressure, and Macrobenchmark coverage are included in the project

## What's New in 4.0.1

`4.0.1` is the 4.0 hardening release. It keeps the public API stable while tightening rendering correctness, interaction behavior, accessibility, concurrency, memory handling, and performance validation.

**Renderer and performance hardening:**
- **Thread-safe shared blur pipeline** - the shared `BlurMaker` state is protected for concurrent blur calls and has lifecycle-safe RenderScript recovery.
- **Explicit blur backends** - blur is routed through dedicated StackBlur and RenderScript engines, with StackBlur fallback when the legacy RenderScript path fails.
- **Real warm-up** - `warmUp()` initializes the expensive pre-API 31 blur resources instead of only touching the singleton.
- **True LRU allocation cache** - RenderScript allocations use access-order caching so frequently used sizes stay hot.
- **Exact shadow-cache keys** - float render parameters and color encoding are preserved exactly to avoid approximate-key collisions.
- **Blur-quality-aware cache keys** - changing `NeuPerformanceConfig.blurDownsampling` cannot reuse a bitmap generated with a different quality setting.
- **Memory-pressure handling** - the process-wide shadow cache responds to Android memory-pressure callbacks and restores its configured budget after pressure passes.
- **Correct downsampling dimensions** - ceil-based dimensions preserve bitmap edges instead of losing the last partial sampling region.
- **Fewer cache-hit allocations** - cached Compose shadows avoid unnecessary `GradientDrawable` creation.
- **Runtime View invalidation** - XML/View shadow state is invalidated consistently when shadow-affecting properties change, including detach/reattach flows.
- **Card padding preservation** - shadow geometry is kept separate from user content padding so runtime shadow changes do not corrupt layout padding.
- **Immediate `NeuAnimationType.NONE`** - NONE now applies the target state immediately instead of using a very stiff spring.

**Compose interaction and accessibility fixes:**
- **Interactive `NeuSlider`** - tap and drag gestures update `onValueChange` with clamped 0..1 values.
- **Hover interaction fixes** - `NeuButton`, `NeuIconButton`, and `NeuFloatingActionButton` now attach `hoverable` to the same interaction source used for hover state.
- **Indeterminate `NeuCircularProgress`** - `progress = null` now produces a real animated indeterminate indicator.
- **Accessible controls** - Switch, Checkbox, and RadioButton use state-aware `toggleable`/`selectable` semantics, while Slider/SeekBar expose progress-range semantics.
- **Safe progress geometry** - animated progress values and slider geometry are clamped to valid ranges, including zero-width layouts.
- **Safe circular geometry** - circular progress uses the actual drawing bounds rather than the shadowed `size` parameter.

**Testing and CI:**
- Added Compose UI regression coverage for slider interaction and accessibility semantics.
- Added concurrent blur and cache-pressure coverage.
- Added a repeatable on-device blur timing smoke benchmark.
- Added a dedicated Macrobenchmark module with startup/frame smoke tests against a production-like benchmark build.
- Demo app is profileable for Macrobenchmark diagnostics.
- CI is split into independent build, unit-test, lint, instrumentation, and Macrobenchmark jobs, with modern Gradle/action setup and cancellation of superseded runs.

The 4.0.1 release keeps the public component signatures stable. Existing `neumorphic()` usage does not require migration.

## What's New in 4.0.0

Both modules (`library` and `library-views`) are now versioned together -
previously `library` was on `3.1.0` while `library-views` trailed behind at
`2.1.0`, which was more confusing than useful since they're released
together from the same repo. `4.0.0` is a substantial release; the public
API is unchanged (nothing here requires code changes on your end), but a
lot changed underneath it:

**Fixed:**
- **App freezing on launch** and **battery/CPU drain during animation** -
  see [Performance](#performance) for the full breakdown (shared
  `RenderScript` context, shadow bitmap caching, downsampled blur).
- **A real crash**: `NeuSwitch`'s and `NeuSlider`'s thumb used
  `Modifier.padding()` with a spring-animated offset. Bouncy springs
  overshoot past their target before settling, which briefly pushed the
  padding value negative - and `Modifier.padding()` throws on any negative
  value. Switched to `Modifier.offset()`, which is both the semantically
  correct modifier for animated positioning and one that allows negative
  values without crashing.
- **Raised (`Punched`) shapes losing their shadow when clipped**: several
  components (and demo screens) applied `Modifier.clip()` *before*
  `Modifier.neumorphic()`, which cut away the soft shadow's overflow
  outside the shape's bounds, leaving a flat-looking box. Fixed throughout
  the library and demo; see the updated [Best Practices](#best-practices)
  entry on this. The `Pot` shape (raised + recessed combined) needed a
  deeper fix, since a single external clip can't apply to only one of its
  two shadow passes - it now clips its recessed pass internally instead.
- **Press feedback invisible on quick/light taps**: every component's tap
  animation was driven directly by the raw pressed state, which a fast tap
  can complete faster than the animation has time to visibly show - so it
  only looked like anything happened on a firm, held press. Fixed by
  holding the visual "pressed" state for a guaranteed minimum ~100ms
  regardless of how quick the actual tap was.
- A cache-key collision risk in `NeuShadowCache` (different colors could
  hash to the same key), and a couple of smaller validation/test issues
  caught in review.

**Changed:**
- `NeuFloatingActionButton` and `NeuRadioButton` restyled to match the rest
  of the library's soft, dual-shadow look - both previously mixed in a hard
  solid-fill circle / colored border, which is a Material-standard look,
  not a neumorphic one.
- `NeuChip` and `NeuIconButton` (used by the demo's "Categories" chips and
  bottom navigation bar) now crossfade between their raised/recessed
  treatments on selection changes instead of cutting between them in a
  single frame, and `NeuChip`'s leading icon fades in/out instead of
  popping.

**Added:**
- `NeuPerformanceConfig` - runtime-tunable blur downsampling and shadow
  cache budget; see [Performance](#performance).
- Hand-authored baseline profiles for all three modules; see
  [Performance](#performance).
- A Gradle version catalog (`gradle/libs.versions.toml`) as the single
  source of truth for dependency versions across all three modules, and a
  fix for two `implementation`-vs-`api` visibility bugs that could break
  external JitPack consumers who don't separately declare `material3`/
  `compose-ui`.
- Real unit/instrumented tests for the caching and shared-`BlurMaker`
  logic (previously only placeholder tests existed anywhere in the repo).

## Installation

This fork is published via [JitPack](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro).

Add JitPack to your **project-level** `settings.gradle` (or `build.gradle` for older projects):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Jetpack Compose Library

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library:4.0.1")
```

### XML/Views Library

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library-views:4.0.1")
```

> Coming from the upstream `me.nikhilchaudhari:composeNeumorphism` artifact? The public API is unchanged - swap the dependency coordinates above and everything else keeps working as-is.

## Quick Start

### Jetpack Compose

```kotlin
// Basic usage
Card(
    modifier = Modifier
        .padding(16.dp)
        .size(200.dp)
        .neumorphic()
) {
    // Your content
}

// With customization
Button(
    modifier = Modifier
        .neumorphic(
            neuShape = Punched.Rounded(radius = 12.dp),
            elevation = 8.dp,
            lightShadowColor = Color.White,
            darkShadowColor = Color.Gray,
            lightSource = LightSource.TOP_LEFT
        )
) {
    Text("Click Me")
}
```

### XML Layout

```xml
<me.nikhilchaudhari.library.views.NeumorphicButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Neumorphic Button"
    app:neuShape="punched"
    app:neuCornerRadius="12dp"
    app:neuElevation="8dp"
    app:neuLightShadowColor="@color/white"
    app:neuDarkShadowColor="@color/gray"
    app:neuLightSource="topLeft" />

<me.nikhilchaudhari.library.views.NeumorphicCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:neuShape="pot"
    app:neuCornerRadius="16dp">
    
    <!-- Your content -->
    
</me.nikhilchaudhari.library.views.NeumorphicCardView>
```

### Java Usage

```java
NeumorphicButton button = new NeumorphicButton(context);
button.setNeuShapeType(NeuShapeType.PUNCHED);
button.setNeuCornerRadius(dpToPx(12));
button.setNeuElevation(dpToPx(8));
button.setNeuLightSource(LightSource.TOP_LEFT);
```

## Shapes

Three neumorphic shapes are available:

| Shape | Description | Visual |
|-------|-------------|--------|
| `Punched` | Raised/bumped effect | `__/‾‾‾‾‾‾‾\__` |
| `Pressed` | Depressed/sunken effect | `‾‾\________/‾‾` |
| `Pot` | Combined raised + depressed | `_/\‾‾‾‾‾/\_` |

Each shape supports two corner types:
- **Rounded** - Configurable corner radius
- **Oval** - Circular/elliptical shape

```kotlin
// Compose
Punched.Rounded(radius = 12.dp)
Punched.Oval()
Pressed.Rounded(radius = 8.dp)
Pressed.Oval()
Pot.Rounded(radius = 16.dp)
Pot.Oval()
```

## Configuration Options

| Parameter | Default | Description |
|-----------|---------|-------------|
| `neuShape` | `Punched.Rounded(12.dp)` | Shape type and corner configuration |
| `lightShadowColor` | `Color.White` | Light shadow color |
| `darkShadowColor` | `Color.LightGray` | Dark shadow color |
| `elevation` | `6.dp` | Shadow depth/elevation |
| `strokeWidth` | `6.dp` | Inner shadow stroke width |
| `neuInsets` | `NeuInsets(6.dp, 6.dp)` | Shadow insets (horizontal, vertical) |
| `lightSource` | `LightSource.TOP_LEFT` | Direction of light source |

## Light Source

Configure the direction of the light source to change shadow placement:

```kotlin
// Compose
Modifier.neumorphic(
    lightSource = LightSource.TOP_LEFT    // Default
    // or
    lightSource = LightSource.TOP_RIGHT
    lightSource = LightSource.BOTTOM_LEFT
    lightSource = LightSource.BOTTOM_RIGHT
)
```

## Theme Integration

### Using Theme Colors

```kotlin
@Composable
fun ThemedCard() {
    val colorScheme = NeuTheme.colorScheme() // Auto light/dark
    
    Card(
        backgroundColor = colorScheme.backgroundColor,
        modifier = Modifier.themedNeumorphic(colorScheme)
    ) {
        // Content
    }
}
```

### Material You Dynamic Colors (Android 12+)

```kotlin
@Composable
fun DynamicThemedCard() {
    val colorScheme = NeuTheme.dynamicColorScheme()
    
    Card(
        backgroundColor = colorScheme.backgroundColor,
        modifier = Modifier.themedNeumorphic(colorScheme)
    ) {
        // Content
    }
}
```

### Custom Color Scheme

```kotlin
val customScheme = NeuTheme.customColorScheme(
    backgroundColor = Color(0xFFE0E5EC)
)
```

## Animation

### Animated Press Effect (Compose)

```kotlin
Modifier.animatedNeumorphic(
    neuShape = Punched.Rounded(),
    elevation = 8.dp,
    pressed = isPressed, // from interaction state
    animationDuration = 150
)
```

### Clickable with Animation

```kotlin
Modifier.neumorphicClickable(
    onClick = { /* action */ },
    elevation = 8.dp,
    neuShape = Punched.Rounded()
)
```

### XML Views Animation

Buttons have built-in press animations. Enable/disable with:

```kotlin
button.enablePressAnimation = true
```

## Utility Extensions

```kotlin
// Soft neumorphic (reduced elevation)
Modifier.softNeumorphic()

// Deep neumorphic (increased elevation)
Modifier.deepNeumorphic()

// Generate shadow colors from background
val (lightShadow, darkShadow) = backgroundColor.toNeuColors()

// Lighten/darken colors
val lighter = color.lighten(0.2f)
val darker = color.darken(0.2f)
```

## Best Practices

1. **Use matching colors**: Background and shadow colors should be similar for best effect
2. **Avoid pure white/black**: Use off-white and dark gray for realistic shadows
3. **Consider light source**: Keep light source consistent across your UI
4. **Use appropriate elevation**: 4-12dp works best for most cases
5. **Clip for Pressed shape only**: Use `Modifier.clip()` when using `Pressed` shape - its inner shadow needs to stay within bounds. **Never clip a `Punched` (raised) or `Pot` shape** - their shadow is *meant* to extend past the shape's own bounds, and `Modifier.clip()` placed before `Modifier.neumorphic()` cuts that overflow away, leaving a flat-looking box instead of a soft shadow. All of this library's own `NeuXxx` components already follow this rule internally.

## Performance

Neumorphism is expensive by nature - every soft shadow is a blurred bitmap, and
the upstream implementation regenerated that bitmap **from scratch on every
single draw call**, including every frame of a press animation. This fork
(as of v4.0.1) keeps the exact same visual output but changes *how often*
and *how safely and efficiently* that work happens.

### What was actually draining the battery

1. **A new `RenderScript` context was created on every recomposition.** The
   modifier that owns the blur pipeline was rebuilt every time an animated
   property (like elevation during a press) changed - so on API < 31, a brand
   new `RenderScript` context (one of the heaviest objects on Android) was spun
   up and torn down up to 60 times per second, per neumorphic component.
2. **No caching at all.** Two identical cards in a `LazyColumn`, or a button
   returning to the same resting elevation after a press, each regenerated
   their own `GradientDrawable` + `Bitmap` + full-resolution CPU blur, even
   though the output was pixel-for-pixel identical.
3. **Blur ran at full bitmap resolution**, even though a Gaussian/stack blur
   inherently destroys fine detail - so all that extra resolution was being
   thrown away by the algorithm itself.
4. **Every neumorphic component owned its own `RenderScript` context.** A
   typical screen (header, search bar, a row of quick actions, a couple of
   cards, some switches) easily has 15-20+ neumorphic components. Even with
   fix #1 in place, that's still 15-20+ separate `RenderScript` contexts all
   being created synchronously during the very first frame - which is heavy
   enough on the main thread to show up as the app freezing/hanging for a
   moment on launch.

### What changed

| Fix | Effect |
|---|---|
| `BlurMaker` (and its `RenderScript` context) is now a single app-wide singleton (`NeuBlurMakerHolder`) shared by every `neumorphic()` call and every XML view | At most **one** `RenderScript` context is ever created for the app's entire lifetime, no matter how many neumorphic components exist - this is what fixes the launch freeze |
| Persistent `ScriptIntrinsicBlur` reused across calls | Cuts remaining RenderScript-path overhead further |
| Process-wide LRU shadow-bitmap cache (`NeuShadowCache`), keyed by size/elevation/colors/shape/light source | Identical components (list items, buttons at rest) share one bitmap instead of each generating their own |
| Elevation/stroke quantized to 0.5dp buckets for cache keys | A ~200-frame spring animation collapses into a handful of reusable shadow bitmaps instead of 200 unique ones - imperceptible visually |
| Blur now runs at a configurable downsampled resolution | Fewer pixels are touched by the CPU blur loop while keeping the same visual intent |
| Blur state is thread-safe and lifecycle-safe | Concurrent blur calls do not race shared RenderScript state, and transient failures reset the backend without permanently poisoning the singleton |
| Explicit blur backend abstraction | StackBlur and legacy RenderScript paths are isolated, with safe fallback behavior |
| Exact cache identity + quality namespace | Float/color identity is preserved and blur downsampling is part of the shadow-cache key |
| Memory-pressure-aware cache | Android memory-pressure callbacks clear/rebudget the process-wide cache safely |
| Cache hits avoid unnecessary shadow drawable allocation | Compose can reuse cached shadow bitmaps without recreating a `GradientDrawable` first |

None of this changes the public API - `neumorphic()`, `animatedNeumorphic()`,
`springNeumorphic()`, `expressiveNeumorphic()`, and the XML views all work
exactly as before.

### Optional: warm up before the first frame

The shared `RenderScript` context is still created lazily on first use - by
default that's the first frame that draws a neumorphic component. For an
app whose very first screen is heavy with neumorphic components, you can
shave that off entirely by warming it up on a background thread before the
UI needs it:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Off the main thread - creates the shared RenderScript context
        // ahead of time so the first neumorphic frame never has to wait for it.
        Thread { NeuBlurMakerHolder.warmUp(this) }.start()
    }
}
```

This is a pure optimization - safe to skip, call multiple times, or call
from anywhere; the singleton only does the expensive work once.

### Demo app: smaller, correct APK

The `app` module (the demo/sample app in this repo, not the library itself)
also had some unrelated issues that made it bigger and slower to install
than it needed to be:

- Removed `material-icons-extended` (~5000 icons, several MB) - the demo
  only uses about 10 common icons, all of which live in the much smaller
  `material-icons-core`.
- Removed `navigation-compose` and the three `material3-adaptive` artifacts -
  none of them were used anywhere in the demo's source.
- Enabled `minifyEnabled` + `shrinkResources` for the release build type
  (previously `minifyEnabled false`, so the release APK shipped completely
  unshrunk).
- Fixed a dead conditional in the header's notification icon that picked the
  same icon in both the on/off branches regardless of state.

### Tuning the cache/blur cost with `NeuPerformanceConfig`

The defaults (2x downsampling, 6MB shadow cache) are chosen to be safe for
typical UI. If your app targets low-end devices, or has an unusually large
number of *distinct* neumorphic shapes on screen (so the shared cache gets
little reuse), you can tune these:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NeuPerformanceConfig.blurDownsampling = 3        // lower-end devices: less CPU, slightly softer shadows
        NeuPerformanceConfig.shadowCacheBudgetKB = 12 * 1024  // large/varied UI: fewer cache evictions
    }
}
```

Set these once, early, before any neumorphic composable draws. They're safe
to change later too - a change only affects shadows generated *after* it, it
doesn't retroactively re-render what's already on screen.

### Baseline profiles

This library ships a bundled `baseline-prof.txt` (in `library/src/main/`)
listing its always-hot internal classes (the blur pipeline, the shadow
cache, the shape hierarchy). AGP automatically merges this into any
consuming app's own baseline profile at build time - no setup needed on your
end beyond depending on the library normally.

To be transparent about what this is: it's a hand-authored, class-level-only
profile, not one generated from an actual device run. It tells ART "expect
these classes early, preload/verify them ahead of time" - a real but modest
win. It deliberately does not include method-level rules for the
`neumorphic()`/`animatedNeumorphic()`/`springNeumorphic()` composable
functions, since Compose's compiler generates synthetic default-argument and
mangled internal names for those that are impractical to guess correctly by
hand. For a more complete profile (including those hot composable entry
points), generate one from a real device using the
[Macrobenchmark + Baseline Profile Gradle plugin](https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile)
against the demo app, and merge the relevant lines into the bundled file.

### Roadmap

- Migrate to AGP 9.1.0+ / Compose 1.12+ / `compileSdk 37` once AGP 9's
  migration surface (it changes how the Kotlin Gradle plugin is applied) has
  been verified against a real build - this project deliberately deferred
  that jump for now (see [Requirements](#requirements) above).
- Investigate `RenderEffect`-based GPU compositor blur on API 31+ as a
  zero-CPU-cost alternative to the downsampled StackBlur path (higher
  implementation risk - needs on-device verification before shipping).
- Generate a real Macrobenchmark-based baseline profile (the current one is
  hand-authored and class-level-only, see [Baseline profiles](#baseline-profiles) above) once a
  device/emulator is available to run it against the demo app.
- Extend `NeuPerformanceConfig` further if real-world usage shows a need
  (e.g. per-shape overrides, disabling downsampling for very large elevated
  surfaces where the softness difference could be visible).

## Migration from v1.x

```kotlin
// v1.x
Modifier.neumorphic(
    neuShape = Punched.Rounded(),
    elevation = 6.dp
)

// v2.0 - same API, new features
Modifier.neumorphic(
    neuShape = Punched.Rounded(),
    elevation = 6.dp,
    lightSource = LightSource.TOP_LEFT // NEW
)
```

## Requirements

- **Minimum SDK**: 24 (Android 7.0) for both `library` (Compose) and `library-views` (XML/Views)
- **Compile/Target SDK**: 36
- **Compose BOM**: 2026.04.01 (Compose 1.11)
- **Kotlin**: 2.3.0
- **AGP**: 8.13.2 (build-time requirement for contributors; does not constrain consumers)
- **Java**: 17 source/target compatibility

> **Why not the newest Compose BOM?** Compose 1.12.0 (BOM 2026.08.00 and later)
> requires `compileSdk 37`, which in turn requires AGP 9.1.0+. This project
> deliberately stays on AGP 8.13.x for now (see [Roadmap](#roadmap)), so it's
> pinned to Compose 1.11 (BOM `2026.04.01`), the last line before that jump -
> confirmed compatible with `compileSdk 36` + AGP 8.13 by CI. `material3` is
> no longer pinned to an independent alpha version either, for the same
> reason: an independently-pinned material3 alpha silently requiring
> `compileSdk 37` on its own (regardless of what the rest of the BOM needs)
> is exactly what broke this before - letting the BOM manage `material3`'s
> version keeps everything on one consistent, tested line.

## License

Licensed under Apache License, Version 2.0 [here](https://github.com/obieda-hussien/neumorphic-compose-pro/blob/main/LICENSE)

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## Acknowledgments

- Stack Blur Algorithm by Mario Klingemann
- Original Neumorphism design concept from Alexander Plyuto
