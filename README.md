# Neumorphism UI Library Pro for Android 🚀

[![JitPack](https://jitpack.io/v/obieda-hussien/neumorphic-compose-pro.svg)](https://jitpack.io/#obieda-hussien/neumorphic-compose-pro)
![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)
![Android](https://img.shields.io/badge/Android-5.0%2B-brightgreen.svg)

An optimized, ultra-high performance Neumorphism UI library for Android supporting **Jetpack Compose** and traditional **XML/Java Views**.

Refactored for zero lag, ultra-low battery consumption, LRU shadow bitmap caching, and Material 3 Expressive UI.

<p align="center">
<img src="static/complete_screen.png" height=400 alt="Neumorphism Demo">
</p>

## Highlights & Performance Improvements⚡

- ⚡ **Zero Lag & Fast Launch**: Instant rendering with thread-safe `NeuCache` (LruCache). Shadows are computed once and reused seamlessly during recompositions and list scrolling.
- 🔋 **Battery & Memory Efficient**: Drastically reduced Garbage Collection (GC) pressure by eliminating redundant Bitmap allocations.
- 🎨 **Jetpack Compose Support**: Use `neumorphic()`, `animatedNeumorphic()`, `springNeumorphic()`, and `expressiveNeumorphic()` modifiers on any composable.
- 📱 **XML/Java Views Support**: High-performance `NeumorphicView`, `NeumorphicButton`, and `NeumorphicCardView`.
- 🎭 **Material Design 3 & Expressive**: Integrated M3 Expressive design tokens, spring physics, and dynamic colors (Android 12+).
- 🔆 **Configurable Light Source**: `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`.

---

## Installation 📦

### 1. Add JitPack Repository

Add JitPack to your `settings.gradle.kts` or root `settings.gradle`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add Dependencies

Add the library dependency to your app's `build.gradle.kts` / `build.gradle`:

#### Jetpack Compose Library
```kotlin
implementation("com.github.obieda-hussien:neumorphic-compose-pro:3.0.0")
```

#### XML / Android Views Library
```kotlin
implementation("com.github.obieda-hussien:neumorphic-views-pro:3.0.0")
```

---

## Quick Start 🏁

### Jetpack Compose

```kotlin
// Basic Neumorphic Card
Box(
    modifier = Modifier
        .size(150.dp)
        .neumorphic(
            neuShape = Punched.Rounded(16.dp),
            elevation = 6.dp,
            lightSource = LightSource.TOP_LEFT
        )
) {
    Text("Pro Neumorphic Card", modifier = Modifier.align(Alignment.Center))
}

// Interactive Animated Button
Button(
    modifier = Modifier
        .expressiveNeumorphic(
            neuShape = Punched.Rounded(12.dp),
            elevation = 8.dp,
            pressed = isPressed
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
    app:neuLightSource="topLeft" />
```

---

## Shapes 📐

| Shape | Description | Visual Effect |
|-------|-------------|---------------|
| `Punched` | Raised/bumped effect | `__/‾‾‾‾‾‾‾\__` |
| `Pressed` | Sunken/depressed effect | `‾‾\________/‾‾` |
| `Pot` | Dual convex + concave effect | `_/\‾‾‾‾‾/\_` |

```kotlin
Punched.Rounded(radius = 16.dp)
Punched.Oval()
Pressed.Rounded(radius = 12.dp)
Pressed.Oval()
Pot.Rounded(radius = 16.dp)
Pot.Oval()
```

---

## Requirements 📋

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Kotlin**: 2.0.21+
- **Compose Compiler**: Included with Kotlin 2.0+

---

## Author & Maintainer 👨‍💻

**Obieda Hussien**
GitHub: [obieda-hussien](https://github.com/obieda-hussien)
Repository: [neumorphic-compose-pro](https://github.com/obieda-hussien/neumorphic-compose-pro)

---

## License 📜

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
