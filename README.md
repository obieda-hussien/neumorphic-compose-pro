# Neumorphism UI Library for Android

A modern, flexible Neumorphism UI library for Android supporting both **Jetpack Compose** and traditional **XML/Java Android Views**.

[![](https://img.shields.io/badge/mavencentral-2.0.0-brightgreen)]() ![List of Awesome List Badge](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg) [![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

<p align="center">
<img src="https://github.com/CuriousNikhil/neumorphic-compose/blob/main/static/complete_screen.png?raw=true" height=400>
</p>

## Features ✨

- 🎨 **Jetpack Compose Support** - Use the `neumorphic()` modifier with any composable
- 📱 **XML/Java Views Support** - Traditional Android Views (NeumorphicView, NeumorphicButton, NeumorphicCardView)
- 🌓 **Dark Theme Support** - Built-in light and dark theme color schemes
- 🎭 **Material You Integration** - Dynamic colors on Android 12+
- 💫 **Animation Support** - Smooth press animations
- 🔆 **Configurable Light Source** - TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
- 🛠 **Modern Implementation** - Migrated from deprecated RenderScript to StackBlur

## Installation

### Jetpack Compose Library

Add to your app level `build.gradle`:

```kotlin
implementation("me.nikhilchaudhari:composeNeumorphism:2.0.0")
```

### XML/Views Library

```kotlin
implementation("me.nikhilchaudhari:neumorphismViews:2.0.0")
```

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
5. **Clip for Pressed shape**: Use `Modifier.clip()` when using `Pressed` shape

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

- **Minimum SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Compose**: 1.5.4+
- **Kotlin**: 1.9.20+

## License

Licensed under Apache License, Version 2.0 [here](https://github.com/CuriousNikhil/neumorphic-compose/blob/main/LICENSE)

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## Acknowledgments

- Stack Blur Algorithm by Mario Klingemann
- Original Neumorphism design concept from Alexander Plyuto
