# Neumorphism UI for Android

A modern neumorphic UI library for Android with support for **Jetpack Compose** and traditional **XML/Java Views**.

This fork focuses on three things: **neumorphic visuals, predictable interaction, and efficient shadow rendering**. Version `4.0.1` hardens the renderer, cache, View invalidation, Compose semantics, concurrency, and performance test coverage while keeping the public component API stable.

[![JitPack](https://jitpack.io/v/obieda-hussien/neumorphic-compose-pro.svg)](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro)

<p align="center">
  <img src="https://github.com/obieda-hussien/neumorphic-compose-pro/blob/main/static/complete_screen.png?raw=true" height="400" alt="Neumorphic Compose demo">
</p>

## Why this fork?

The upstream renderer could repeatedly recreate expensive shadow resources and blur work. This fork introduces shared rendering infrastructure and caching so repeated shadows can be reused instead of recomputed unnecessarily.

The goal is not to replace neumorphism with a different visual system. The goal is to make the existing effect **more reliable and cheaper to render**, especially when many neumorphic components are present or when elevation is animated.

## Highlights

- **Jetpack Compose**: `Modifier.neumorphic()` plus animated and expressive variants.
- **XML / Java Views**: `NeumorphicView`, `NeumorphicButton`, `NeumorphicCardView`, and related APIs.
- **Three shapes**: `Punched`, `Pressed`, and `Pot`.
- **Four light directions**: top-left, top-right, bottom-left, bottom-right.
- **Light and dark theme support** plus Material You dynamic colors.
- **Press and hover interaction support**.
- **Accessibility semantics** for interactive Compose controls.
- **Shared shadow caching** and reusable blur infrastructure.
- **StackBlur fallback** for the legacy RenderScript path.
- **Memory-pressure-aware cache management**.
- **Regression tests and Macrobenchmark smoke coverage**.

## What's new in 4.0.1

`4.0.1` is a hardening release built on the 4.0 renderer changes. It does not introduce a breaking public API migration.

### Rendering and performance

- Shared `BlurMaker` infrastructure is protected for concurrent use.
- Blur backends are separated into RenderScript and StackBlur implementations, with fallback behavior when the legacy path fails.
- RenderScript resources are reused instead of recreated for every blur operation.
- Allocation caching uses access-order LRU behavior.
- Shadow-cache keys preserve exact color/float identity and include the blur-downsampling setting.
- Blur downsampling uses ceil-based dimensions so partial sampling regions are not dropped.
- Cache entries are released and re-budgeted in response to Android memory-pressure callbacks.
- Compose shadow cache hits avoid unnecessary shadow drawable allocation.
- View shadow invalidation is centralized so runtime shadow changes consistently trigger redraws.
- `NeumorphicCardView` keeps content padding separate from shadow geometry.
- `NeuAnimationType.NONE` applies its target elevation immediately.

### Compose interaction and accessibility

- `NeuSlider` now responds to tap and drag gestures and reports values in the valid range.
- `NeuButton`, `NeuIconButton`, and `NeuFloatingActionButton` use `hoverable` with the same interaction source used by hover-state collection.
- `NeuCircularProgress(progress = null)` now behaves as an indeterminate animated indicator.
- Switch, Checkbox, and RadioButton expose state-aware semantics.
- Slider and SeekBar expose progress-range semantics.
- Progress and slider geometry are clamped safely, including zero-width layouts.
- Circular progress calculations use the actual drawing bounds rather than the `size` parameter name that previously caused a shadowing hazard.

### Validation and CI

- Compose UI regression tests cover interaction and semantics.
- Unit tests cover shadow-cache identity and shared blur behavior.
- Concurrent blur and cache-pressure paths have dedicated coverage.
- A device blur timing smoke benchmark is included.
- A dedicated Macrobenchmark module covers startup/frame smoke paths.
- The demo app is profileable for performance diagnostics.
- CI separates build, unit-test, lint, instrumentation, and Macrobenchmark work into independent jobs.

## Installation

The artifacts are published through [JitPack](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro).

Add JitPack to your project repositories:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Jetpack Compose

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library:4.0.1")
```

### XML / Views

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library-views:4.0.1")
```

The two artifacts use the same release version.

> Coming from the upstream `me.nikhilchaudhari:composeNeumorphism` artifact? The public neumorphic API remains compatible with the existing usage patterns. Replace the dependency coordinates first, then verify your build against your app's dependency graph.

## Quick start

### Jetpack Compose

```kotlin
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.neumorphic

@Composable
fun Example() {
    Text(
        text = "Hello Neumorphism",
        modifier = Modifier
            .padding(16.dp)
            .neumorphic()
    )
}
```

### Customized Compose effect

```kotlin
Modifier.neumorphic(
    neuShape = Punched.Rounded(radius = 12.dp),
    elevation = 8.dp,
    lightShadowColor = Color.White,
    darkShadowColor = Color.Gray,
    lightSource = LightSource.TOP_LEFT
)
```

### Animated press effect

```kotlin
Modifier.animatedNeumorphic(
    neuShape = Punched.Rounded(),
    elevation = 8.dp,
    pressed = isPressed,
    animationDuration = 150
)
```

### Spring-based animation

```kotlin
Modifier.springNeumorphic(
    neuShape = Punched.Rounded(),
    elevation = 8.dp,
    pressed = isPressed,
    animationType = NeuAnimationType.SPRING_BOUNCY
)
```

### XML

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
```

### Java

```java
NeumorphicButton button = new NeumorphicButton(context);
button.setNeuShapeType(NeuShapeType.PUNCHED);
button.setNeuCornerRadius(dpToPx(12));
button.setNeuElevation(dpToPx(8));
button.setNeuLightSource(LightSource.TOP_LEFT);
```

## Shapes

| Shape | Appearance | Use case |
| --- | --- | --- |
| `Punched` | Raised / elevated | Cards, buttons, surfaces |
| `Pressed` | Recessed / sunken | Inputs, pressed states, wells |
| `Pot` | Combined raised + recessed | Containers and compound surfaces |

Every shape supports rounded and oval variants:

```kotlin
Punched.Rounded(radius = 12.dp)
Punched.Oval()
Pressed.Rounded(radius = 8.dp)
Pressed.Oval()
Pot.Rounded(radius = 16.dp)
Pot.Oval()
```

## Main parameters

| Parameter | Default | Description |
| --- | --- | --- |
| `neuShape` | `Punched.Rounded()` | Shadow geometry |
| `lightShadowColor` | `Color.White` | Light-side shadow color |
| `darkShadowColor` | `Color.LightGray` | Dark-side shadow color |
| `elevation` | `6.dp` | Shadow depth |
| `strokeWidth` | `6.dp` | Inner-shadow stroke width |
| `neuInsets` | `NeuInsets(6.dp, 6.dp)` | Horizontal and vertical shadow insets |
| `lightSource` | `TOP_LEFT` | Light direction |

## Light source

```kotlin
Modifier.neumorphic(
    lightSource = LightSource.TOP_LEFT
)
```

Available values:

`TOP_LEFT` · `TOP_RIGHT` · `BOTTOM_LEFT` · `BOTTOM_RIGHT`

Keep one light direction across the screen or design system for a more coherent result.

## Theme support

The library provides theme helpers for light/dark color schemes and Material You dynamic colors.

```kotlin
val colorScheme = NeuTheme.colorScheme()
```

For Android 12+ dynamic colors:

```kotlin
val dynamicScheme = NeuTheme.dynamicColorScheme()
```

Custom schemes are also supported:

```kotlin
val customScheme = NeuTheme.customColorScheme(
    backgroundColor = Color(0xFFE0E5EC)
)
```

## Utility extensions

```kotlin
Modifier.softNeumorphic()
Modifier.deepNeumorphic()

val (lightShadow, darkShadow) = backgroundColor.toNeuColors()

val lighter = color.lighten(0.2f)
val darker = color.darken(0.2f)
```

## Performance

The main performance work in 4.0.x is about **avoiding repeated shadow work**.

### Shared blur infrastructure

A shared blur pipeline avoids creating an independent RenderScript context for every neumorphic component. The legacy RenderScript implementation is isolated from the CPU StackBlur fallback, making failure recovery local to the backend instead of permanently poisoning the shared path.

### Shadow cache

Generated shadows are stored in a process-wide LRU cache keyed by the parameters that affect the rendered result, including size, elevation, stroke, colors, shape/light configuration, and blur quality settings.

This is especially useful for repeated UI such as:

- rows in a `LazyColumn`
- repeated cards
- buttons returning to the same resting elevation
- animated elevation values that revisit the same cache buckets

### Blur downsampling

`NeuPerformanceConfig.blurDownsampling` controls the working resolution used by the blur pipeline. Higher downsampling values reduce blur work and memory use at the cost of softer / lower-detail shadow rendering.

Example:

```kotlin
NeuPerformanceConfig.blurDownsampling = 3
```

### Shadow-cache budget

`NeuPerformanceConfig.shadowCacheBudgetKB` controls the approximate cache budget:

```kotlin
NeuPerformanceConfig.shadowCacheBudgetKB = 12 * 1024
```

Tune these values early in application startup when possible. They can also be changed later; newly generated shadows use the current configuration.

### Baseline profiles

The library includes a small hand-authored baseline profile focused on frequently used internal classes. It is intentionally conservative.

For the strongest application-level startup optimization, generate a device-backed baseline profile with the AndroidX Macrobenchmark/Baseline Profile tooling for your actual app flows.

## Best practices

### Keep the palette coherent

Use a background and shadow palette that belong to the same tonal family. Slightly off-white and gray values generally produce a more natural neumorphic result than pure white/black pairs.

### Keep the light direction consistent

Pick one `LightSource` for a screen or design system unless you have a deliberate reason to change it.

### Keep elevation moderate

Around `4.dp` to `12.dp` is a useful starting range for common controls. Larger values can work, but they increase the shadow footprint and rendering cost.

### Be careful with clipping

Raised `Punched` and combined `Pot` shadows are meant to extend beyond the component bounds. Applying `Modifier.clip()` before `Modifier.neumorphic()` can clip that soft overflow.

Use clipping intentionally for `Pressed` shapes when you need the inner shadow contained within the component. The library's built-in `NeuXxx` components already handle their internal shadow geometry accordingly.

## Migration

`4.0.1` keeps the public component signatures stable. Existing `neumorphic()` usage does not require a migration step.

For older consumers, the important version change is simply the dependency coordinate:

```kotlin
implementation("com.github.obieda-hussien.neumorphic-compose-pro:library:4.0.1")
```

## Requirements

- **Minimum SDK**: 24
- **Compile / Target SDK**: 36
- **Compose BOM**: `2026.04.01`
- **Kotlin**: `2.3.0`
- **AGP**: `8.13.2` for this repository's build
- **Java**: 17 source/target compatibility

The published library does not require consumers to use the repository's exact build-tool versions, but your project must satisfy the dependency constraints imposed by the Android/Compose stack you select.

## Project modules

| Module | Purpose |
| --- | --- |
| `library` | Jetpack Compose implementation |
| `library-views` | XML / Android Views implementation |
| `app` | Demo application |
| `macrobenchmark` | Startup/frame performance smoke tests |

## Contributing

Issues and pull requests are welcome. When reporting a rendering or performance problem, include the Android version, device/emulator, component involved, shape, elevation, and a small reproduction where possible.

## License

Apache License 2.0. See [LICENSE](LICENSE).

## Current maintainer

**Abdelrahman Hussein (Obieda)** is the current maintainer of this fork and is responsible for its ongoing development, maintenance, modernization, performance improvements, bug fixes, testing, and future releases.

This project builds upon the original work of the upstream authors and contributors. Their work remains credited and preserved. This fork continues active development after a long period of limited maintenance, with a focus on modern Android, Jetpack Compose, performance, reliability, accessibility, and long-term maintainability.

## Credits

- Stack Blur algorithm by Mario Klingemann
- Original neumorphism concept by Alexander Plyuto
