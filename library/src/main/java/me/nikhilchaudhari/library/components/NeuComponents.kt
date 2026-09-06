package me.nikhilchaudhari.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.NeuInsets
import me.nikhilchaudhari.library.NeuTheme
import me.nikhilchaudhari.library.expressiveNeumorphic
import me.nikhilchaudhari.library.rememberMinHoldPressedState
import me.nikhilchaudhari.library.neumorphic
import me.nikhilchaudhari.library.shapes.NeuShape
import me.nikhilchaudhari.library.shapes.Pressed
import me.nikhilchaudhari.library.shapes.Punched
import kotlin.math.roundToInt

/**
 * Material 3 Expressive Neumorphic Button
 *
 * A button with neumorphic styling and spring physics animations.
 * Compatible with Material 3 Expressive design language.
 */
@Composable
fun NeuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    shape: Shape = RoundedCornerShape(16.dp),
    neuShape: NeuShape = Punched.Rounded(16.dp),
    elevation: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberMinHoldPressedState(interactionSource)
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = when {
            isPressed -> elevation * 0.3f
            isHovered -> elevation * 1.2f
            else -> elevation
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonElevation"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .let { if (neuShape is Pressed) it.clip(shape) else it }
            .neumorphic(
                neuShape = neuShape,
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = animatedElevation
            )
            .background(colorScheme.backgroundColor, shape)
            .hoverable(
                interactionSource = interactionSource,
                enabled = enabled
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        val baseContentColor = colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
            ?: MaterialTheme.colorScheme.onSurface
        val contentColor = if (enabled) baseContentColor else baseContentColor.copy(alpha = 0.38f)

        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                content()
            }
        }
    }
}

/** Material 3 Expressive Neumorphic Card. */
@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    shape: Shape = RoundedCornerShape(20.dp),
    neuShape: NeuShape = Punched.Rounded(20.dp),
    elevation: Dp = 10.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .let { if (neuShape is Pressed) it.clip(shape) else it }
            .neumorphic(
                neuShape = neuShape,
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = elevation
            )
            .background(colorScheme.backgroundColor, shape)
            .padding(contentPadding)
    ) {
        val contentColor = colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
            ?: MaterialTheme.colorScheme.onSurface

        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

/** Material 3 Expressive Neumorphic Text Field. */
@Composable
fun NeuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    shape: Shape = RoundedCornerShape(12.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "textFieldElevation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) {
            colorScheme.accentColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "textFieldBorder"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .neumorphic(
                neuShape = Pressed.Rounded(12.dp),
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = animatedElevation,
                strokeWidth = 4.dp
            )
            .background(colorScheme.backgroundColor, shape)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = borderColor,
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leadingIcon?.invoke()

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = (colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
                            ?: MaterialTheme.colorScheme.onSurface).copy(alpha = 0.5f)
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = textStyle.copy(
                        color = colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
                            ?: MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = singleLine,
                    maxLines = maxLines,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    cursorBrush = SolidColor(
                        colorScheme.accentColor.takeIf { it != Color.Unspecified }
                            ?: MaterialTheme.colorScheme.primary
                    )
                )
            }

            trailingIcon?.invoke()
        }
    }
}

/** Material 3 Expressive Neumorphic Switch. */
@Composable
fun NeuSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    checkedThumbColor: Color = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary,
    uncheckedThumbColor: Color = colorScheme.darkShadowColor
) {
    val interactionSource = remember { MutableInteractionSource() }

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switchThumbOffset"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked) checkedThumbColor else uncheckedThumbColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switchThumbColor"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) checkedThumbColor.copy(alpha = 0.3f) else colorScheme.backgroundColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switchTrackColor"
    )

    Box(
        modifier = modifier
            .width(56.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .neumorphic(
                neuShape = Pressed.Rounded(16.dp),
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = 4.dp,
                strokeWidth = 3.dp
            )
            .background(trackColor, RoundedCornerShape(16.dp))
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = onCheckedChange
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .neumorphic(
                    neuShape = Punched.Oval(),
                    lightShadowColor = colorScheme.lightShadowColor,
                    darkShadowColor = colorScheme.darkShadowColor,
                    elevation = 4.dp
                )
                .background(thumbColor, CircleShape)
        )
    }
}

/** Material 3 Expressive Neumorphic Slider. */
@Composable
fun NeuSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    trackHeight: Dp = 8.dp,
    thumbSize: Dp = 24.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberMinHoldPressedState(interactionSource)
    val density = LocalDensity.current
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val coercedValue = value.coerceIn(0f, 1f)

    val animatedThumbScale by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sliderThumbScale"
    )

    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbSize + 8.dp)
                .progressSemantics(coercedValue, 0f..1f)
                .pointerInput(enabled, thumbSize) {
                    if (!enabled) return@pointerInput

                    fun updateFromPosition(x: Float) {
                        val availableWidth = (size.width.toFloat() - thumbSizePx).coerceAtLeast(0f)
                        val trackX = (x - thumbSizePx / 2f).coerceIn(0f, availableWidth)
                        val newValue = if (availableWidth > 0f) trackX / availableWidth else 0f
                        onValueChange(newValue.coerceIn(0f, 1f))
                    }

                    detectTapGestures { offset ->
                        updateFromPosition(offset.x)
                    }
                }
                .pointerInput(enabled, thumbSize) {
                    if (!enabled) return@pointerInput

                    detectDragGestures(
                        onDrag = { change, _ ->
                            change.consume()
                            val availableWidth = (size.width.toFloat() - thumbSizePx).coerceAtLeast(0f)
                            val trackX = (change.position.x - thumbSizePx / 2f).coerceIn(0f, availableWidth)
                            val newValue = if (availableWidth > 0f) trackX / availableWidth else 0f
                            onValueChange(newValue.coerceIn(0f, 1f))
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val availableWidth = (maxWidth - thumbSize).coerceAtLeast(0.dp)
            val thumbOffset = availableWidth * coercedValue

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .neumorphic(
                        neuShape = Pressed.Rounded(trackHeight / 2),
                        lightShadowColor = colorScheme.lightShadowColor,
                        darkShadowColor = colorScheme.darkShadowColor,
                        elevation = 3.dp,
                        strokeWidth = 2.dp
                    )
                    .background(colorScheme.backgroundColor, RoundedCornerShape(trackHeight / 2))
            )

            Box(
                modifier = Modifier
                    .width(availableWidth * coercedValue)
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(accentColor.copy(alpha = 0.6f), RoundedCornerShape(trackHeight / 2))
            )

            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(thumbSize)
                    .neumorphic(
                        neuShape = Punched.Oval(),
                        lightShadowColor = colorScheme.lightShadowColor,
                        darkShadowColor = colorScheme.darkShadowColor,
                        elevation = 6.dp
                    )
                    .background(accentColor, CircleShape)
                    .scale(animatedThumbScale)
            )
        }
    }
}

/** Material 3 Expressive Neumorphic Icon Button. */
@Composable
fun NeuIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    size: Dp = 48.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberMinHoldPressedState(interactionSource)
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.9f
            isHovered -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconButtonScale"
    )

    val selectedAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "iconButtonSelectedAlpha"
    )

    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary
    val unselectedContentColor = if (enabled) {
        colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
            ?: MaterialTheme.colorScheme.onSurface
    } else {
        (colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
            ?: MaterialTheme.colorScheme.onSurface).copy(alpha = 0.38f)
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 1f - selectedAlpha }
                .expressiveNeumorphic(
                    neuShape = Punched.Oval(),
                    lightShadowColor = colorScheme.lightShadowColor,
                    darkShadowColor = colorScheme.darkShadowColor,
                    elevation = 6.dp,
                    pressed = isPressed,
                    hovered = isHovered
                )
                .background(colorScheme.backgroundColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = selectedAlpha }
                .clip(CircleShape)
                .neumorphic(
                    neuShape = Pressed.Oval(),
                    lightShadowColor = colorScheme.lightShadowColor,
                    darkShadowColor = colorScheme.darkShadowColor,
                    elevation = 4.dp,
                    strokeWidth = 3.dp
                )
                .background(accentColor.copy(alpha = 0.15f), CircleShape)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .hoverable(
                    interactionSource = interactionSource,
                    enabled = enabled
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            val contentColor = if (enabled) {
                lerp(unselectedContentColor, accentColor, selectedAlpha)
            } else {
                unselectedContentColor
            }

            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

/** Material 3 Expressive Neumorphic Chip. */
@Composable
fun NeuChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    leadingIcon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberMinHoldPressedState(interactionSource)

    val selectedAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipSelectedAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chipScale"
    )

    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary
    val unselectedContentColor = colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .height(36.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 1f - selectedAlpha }
                .neumorphic(
                    neuShape = Punched.Rounded(20.dp),
                    lightShadowColor = colorScheme.lightShadowColor,
                    darkShadowColor = colorScheme.darkShadowColor,
                    elevation = 5.dp,
                    strokeWidth = 3.dp
                )
                .background(colorScheme.backgroundColor, shape)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = selectedAlpha }
                .clip(shape)
                .neumorphic(
                    neuShape = Pressed.Rounded(20.dp),
                    lightShadowColor = colorScheme.lightShadowColor,
                    darkShadowColor = colorScheme.darkShadowColor,
                    elevation = 3.dp,
                    strokeWidth = 3.dp
                )
                .background(accentColor.copy(alpha = 0.15f), shape)
        )

        Row(
            modifier = Modifier
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = leadingIcon != null,
                enter = fadeIn(tween(200)) + expandHorizontally(tween(200)),
                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
            ) {
                leadingIcon?.invoke()
            }

            val contentColor = lerp(unselectedContentColor, accentColor, selectedAlpha)

            CompositionLocalProvider(LocalContentColor provides contentColor) {
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    content()
                }
            }
        }
    }
}

/** Material 3 Expressive Neumorphic Progress Bar. */
@Composable
fun NeuProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    trackHeight: Dp = 12.dp,
    animated: Boolean = true
) {
    val targetProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = if (animated) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            spring(stiffness = Spring.StiffnessHigh)
        },
        label = "progressAnimation"
    )

    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(RoundedCornerShape(trackHeight / 2))
            .neumorphic(
                neuShape = Pressed.Rounded(trackHeight / 2),
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = 4.dp,
                strokeWidth = 3.dp
            )
            .background(colorScheme.backgroundColor, RoundedCornerShape(trackHeight / 2))
            .progressSemantics(targetProgress, 0f..1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(
                    accentColor.copy(alpha = 0.8f),
                    RoundedCornerShape(trackHeight / 2)
                )
        )
    }
}

/** Material 3 Expressive Neumorphic Circular Progress Indicator. */
@Composable
fun NeuCircularProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp
) {
    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary
    val determinateTarget = progress?.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = determinateTarget ?: 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "circularProgressAnimation"
    )

    val indeterminateRotation = if (progress == null) {
        val transition = rememberInfiniteTransition(label = "circularProgressIndeterminate")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100),
                repeatMode = RepeatMode.Restart
            ),
            label = "indeterminateRotation"
        ).value
    } else {
        0f
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .neumorphic(
                neuShape = Pressed.Oval(),
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = 4.dp,
                strokeWidth = 4.dp
            )
            .background(colorScheme.backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((size - 8.dp).coerceAtLeast(0.dp))) {
            val strokeWidthPx = strokeWidth.toPx().coerceAtMost(size.minDimension / 2f)
            val arcDimension = (this.size.minDimension - strokeWidthPx).coerceAtLeast(0f)
            val arcSize = Size(arcDimension, arcDimension)
            val topLeft = Offset(
                (this.size.width - arcDimension) / 2f,
                (this.size.height - arcDimension) / 2f
            )

            drawArc(
                color = colorScheme.darkShadowColor.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            if (progress != null) {
                if (animatedProgress > 0f && strokeWidthPx > 0f) {
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }
            } else if (strokeWidthPx > 0f) {
                drawArc(
                    color = accentColor,
                    startAngle = indeterminateRotation - 90f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        if (progress != null) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Material 3 Expressive Neumorphic Radio Button. */
@Composable
fun NeuRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    size: Dp = 24.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberMinHoldPressedState(interactionSource)

    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "radioScale"
    )

    val innerSize by animateDpAsState(
        targetValue = if (selected) size * 0.5f else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "radioInnerSize"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.15f) else colorScheme.backgroundColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "radioBackground"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .let { if (selected) it.clip(CircleShape) else it }
            .neumorphic(
                neuShape = if (selected) Pressed.Oval() else Punched.Oval(),
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = 4.dp,
                strokeWidth = 2.dp
            )
            .background(backgroundColor, CircleShape)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
                .background(accentColor, CircleShape)
        )
    }
}

/** Material 3 Expressive Neumorphic Checkbox. */
@Composable
fun NeuCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    size: Dp = 24.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberMinHoldPressedState(interactionSource)

    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkboxScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (checked) accentColor else colorScheme.backgroundColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkboxBackground"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) accentColor else colorScheme.darkShadowColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkboxBorder"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .let { if (checked) it.clip(RoundedCornerShape(6.dp)) else it }
            .neumorphic(
                neuShape = if (checked) Pressed.Rounded(6.dp) else Punched.Rounded(6.dp),
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = 4.dp,
                strokeWidth = 2.dp
            )
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .border(
                width = if (!checked) 2.dp else 0.dp,
                color = if (!checked) borderColor else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = onCheckedChange
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Text(
                text = "✓",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Material 3 Expressive Neumorphic Floating Action Button. */
@Composable
fun NeuFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    size: Dp = 56.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberMinHoldPressedState(interactionSource)
    val isHovered by interactionSource.collectIsHoveredAsState()

    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isHovered -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 4.dp
            isHovered -> 12.dp
            else -> 8.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabElevation"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .neumorphic(
                neuShape = Punched.Oval(),
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = elevation
            )
            .background(colorScheme.backgroundColor, CircleShape)
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides accentColor) {
            content()
        }
    }
}

/** Material 3 Expressive Neumorphic SeekBar. */
@Composable
fun NeuSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    trackHeight: Dp = 10.dp,
    thumbSize: Dp = 28.dp
) {
    var isDragging by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val coercedValue = value.coerceIn(0f, 1f)

    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary

    // Keep the thumb's layout size constant while animating only its visual scale.
    // Changing the layout size invalidated the shadow bitmap and forced a new blur
    // at drag start, which was especially expensive after returning from background.
    val animatedThumbScale by animateFloatAsState(
        targetValue = if (isDragging > 0f) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "seekbarThumbScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize + 16.dp)
            .progressSemantics(coercedValue, 0f..1f)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectTapGestures { offset ->
                    val availableWidth = (size.width.toFloat() - thumbSizePx).coerceAtLeast(0f)
                    val tapX = (offset.x - thumbSizePx / 2f).coerceIn(0f, availableWidth)
                    val newValue = if (availableWidth > 0f) tapX / availableWidth else 0f
                    onValueChange(newValue.coerceIn(0f, 1f))
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = { isDragging = 1f },
                    onDragEnd = { isDragging = 0f },
                    onDragCancel = { isDragging = 0f },
                    onDrag = { change, _ ->
                        change.consume()
                        val availableWidth = (size.width.toFloat() - thumbSizePx).coerceAtLeast(0f)
                        val dragX = (change.position.x - thumbSizePx / 2f).coerceIn(0f, availableWidth)
                        val newValue = if (availableWidth > 0f) dragX / availableWidth else 0f
                        onValueChange(newValue.coerceIn(0f, 1f))
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val trackWidthDp = (maxWidth - thumbSize).coerceAtLeast(0.dp)
        val thumbOffsetX = with(density) { (trackWidthDp * coercedValue).toPx() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .padding(horizontal = thumbSize / 2)
                .clip(RoundedCornerShape(trackHeight / 2))
                .neumorphic(
                    neuShape = Pressed.Rounded(trackHeight / 2),
                    lightShadowColor = colorScheme.lightShadowColor,
                    darkShadowColor = colorScheme.darkShadowColor,
                    elevation = 3.dp,
                    strokeWidth = 2.dp
                )
                .background(colorScheme.backgroundColor, RoundedCornerShape(trackHeight / 2))
        )

        Box(
            modifier = Modifier
                .padding(start = thumbSize / 2)
                .width(trackWidthDp * coercedValue)
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(accentColor.copy(alpha = 0.7f), RoundedCornerShape(trackHeight / 2))
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetX.roundToInt(), 0) }
                .size(thumbSize)
                .neumorphic(
                    neuShape = Punched.Oval(),
                    lightShadowColor = colorScheme.lightShadowColor,
                    darkShadowColor = colorScheme.darkShadowColor,
                    elevation = 8.dp
                )
                .background(accentColor, CircleShape)
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .scale(animatedThumbScale)
        )
    }
}
