package me.nikhilchaudhari.library.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.NeuInsets
import me.nikhilchaudhari.library.NeuTheme
import me.nikhilchaudhari.library.expressiveNeumorphic
import me.nikhilchaudhari.library.neumorphic
import me.nikhilchaudhari.library.shapes.NeuShape
import me.nikhilchaudhari.library.shapes.Pressed
import me.nikhilchaudhari.library.shapes.Punched

/**
 * Material 3 Expressive Neumorphic Button
 * 
 * A button with neumorphic styling and spring physics animations.
 * Compatible with Material 3 Expressive design language.
 *
 * @param onClick Callback when button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param colorScheme Neumorphic color scheme to use
 * @param shape Shape of the button
 * @param neuShape Neumorphic shape type
 * @param elevation Shadow elevation
 * @param contentPadding Padding around the content
 * @param content Content of the button
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
    val isPressed by interactionSource.collectIsPressedAsState()
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
            .clip(shape)
            .neumorphic(
                neuShape = neuShape,
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = animatedElevation
            )
            .background(colorScheme.backgroundColor, shape)
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
        val contentColor = if (enabled) {
            colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.onSurface
        } else {
            (colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.onSurface).copy(alpha = 0.38f)
        }
        
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                content()
            }
        }
    }
}

/**
 * Material 3 Expressive Neumorphic Card
 * 
 * A card container with neumorphic styling.
 *
 * @param modifier Modifier to be applied to the card
 * @param colorScheme Neumorphic color scheme to use
 * @param shape Shape of the card
 * @param neuShape Neumorphic shape type
 * @param elevation Shadow elevation
 * @param contentPadding Padding around the content
 * @param content Content of the card
 */
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
            .clip(shape)
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

/**
 * Material 3 Expressive Neumorphic Text Field
 * 
 * A text field with neumorphic pressed styling.
 *
 * @param value Current text value
 * @param onValueChange Callback when value changes
 * @param modifier Modifier to be applied to the text field
 * @param enabled Whether the text field is enabled
 * @param readOnly Whether the text field is read-only
 * @param placeholder Placeholder text
 * @param leadingIcon Leading icon composable
 * @param trailingIcon Trailing icon composable
 * @param colorScheme Neumorphic color scheme to use
 * @param shape Shape of the text field
 * @param textStyle Text style for the input
 * @param keyboardOptions Keyboard options
 * @param keyboardActions Keyboard actions
 * @param singleLine Whether the text field is single line
 * @param maxLines Maximum number of lines
 * @param visualTransformation Visual transformation for the text
 */
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

/**
 * Material 3 Expressive Neumorphic Switch
 * 
 * A toggle switch with neumorphic styling and spring animations.
 *
 * @param checked Whether the switch is checked
 * @param onCheckedChange Callback when checked state changes
 * @param modifier Modifier to be applied to the switch
 * @param enabled Whether the switch is enabled
 * @param colorScheme Neumorphic color scheme to use
 * @param checkedThumbColor Color of the thumb when checked
 * @param uncheckedThumbColor Color of the thumb when unchecked
 */
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
        targetValue = if (checked) {
            checkedThumbColor.copy(alpha = 0.3f)
        } else {
            colorScheme.backgroundColor
        },
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
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

/**
 * Material 3 Expressive Neumorphic Slider
 * 
 * A slider with neumorphic styling.
 *
 * @param value Current slider value (0f to 1f)
 * @param onValueChange Callback when value changes
 * @param modifier Modifier to be applied to the slider
 * @param enabled Whether the slider is enabled
 * @param colorScheme Neumorphic color scheme to use
 * @param trackHeight Height of the track
 * @param thumbSize Size of the thumb
 */
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
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedThumbSize by animateDpAsState(
        targetValue = if (isPressed) thumbSize * 1.2f else thumbSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sliderThumbSize"
    )
    
    val accentColor = colorScheme.accentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbSize + 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track background
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
            
            // Active track
            Box(
                modifier = Modifier
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(accentColor.copy(alpha = 0.6f), RoundedCornerShape(trackHeight / 2))
            )
            
            // Thumb
            Box(
                modifier = Modifier
                    .padding(start = ((value.coerceIn(0f, 1f)) * 200).dp) // This is simplified, should use layout measurement
                    .size(animatedThumbSize)
                    .clip(CircleShape)
                    .neumorphic(
                        neuShape = Punched.Oval(),
                        lightShadowColor = colorScheme.lightShadowColor,
                        darkShadowColor = colorScheme.darkShadowColor,
                        elevation = 6.dp
                    )
                    .background(accentColor, CircleShape)
            )
        }
    }
}

/**
 * Material 3 Expressive Neumorphic Icon Button
 * 
 * A circular icon button with neumorphic styling.
 *
 * @param onClick Callback when button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param colorScheme Neumorphic color scheme to use
 * @param size Size of the button
 * @param content Icon content
 */
@Composable
fun NeuIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme = NeuTheme.LightColorScheme,
    size: Dp = 48.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
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

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .expressiveNeumorphic(
                neuShape = Punched.Oval(),
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = 6.dp,
                pressed = isPressed,
                hovered = isHovered
            )
            .background(colorScheme.backgroundColor, CircleShape)
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
            colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.onSurface
        } else {
            (colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.onSurface).copy(alpha = 0.38f)
        }
        
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

/**
 * Material 3 Expressive Neumorphic Chip
 * 
 * A chip/tag component with neumorphic styling.
 *
 * @param onClick Callback when chip is clicked
 * @param modifier Modifier to be applied to the chip
 * @param selected Whether the chip is selected
 * @param enabled Whether the chip is enabled
 * @param colorScheme Neumorphic color scheme to use
 * @param leadingIcon Leading icon composable
 * @param content Content of the chip
 */
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
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            (colorScheme.accentColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.15f)
        } else {
            colorScheme.backgroundColor
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipBackground"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chipScale"
    )

    val neuShape = if (selected) Pressed.Rounded(20.dp) else Punched.Rounded(20.dp)
    
    Row(
        modifier = modifier
            .scale(scale)
            .height(36.dp)
            .clip(RoundedCornerShape(20.dp))
            .neumorphic(
                neuShape = neuShape,
                lightShadowColor = colorScheme.lightShadowColor,
                darkShadowColor = colorScheme.darkShadowColor,
                elevation = if (selected) 3.dp else 5.dp,
                strokeWidth = 3.dp
            )
            .background(backgroundColor, RoundedCornerShape(20.dp))
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
        leadingIcon?.invoke()
        
        val contentColor = if (selected) {
            colorScheme.accentColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.primary
        } else {
            colorScheme.onBackgroundColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.onSurface
        }
        
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                content()
            }
        }
    }
}
