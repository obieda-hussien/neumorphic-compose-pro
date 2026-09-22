package me.nikhilchaudhari.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.nikhilchaudhari.library.*
import me.nikhilchaudhari.library.shapes.ComposeNeuShape

@Composable
fun NeuSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LocalNeuTokens.current.cornerRadius),
    recessed: Boolean = false,
    colors: NeuTheme.NeuColorScheme = NeuTheme.colorScheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalNeuTokens.current
    val neuShape = remember(shape, recessed) { ComposeNeuShape(shape, recessed) }
    val foreground = colors.onBackgroundColor.takeUnless { it == Color.Unspecified }
        ?: neuContentColor(colors.backgroundColor)
    Box(modifier.neumorphic(neuShape = neuShape, elevation = tokens.elevation,
        lightSource = tokens.lightSource, lightShadowColor = colors.lightShadowColor,
        darkShadowColor = colors.darkShadowColor).background(colors.backgroundColor, shape)
        .then(if (tokens.highContrast) Modifier.border(1.dp, foreground, shape) else Modifier)) {
        CompositionLocalProvider(LocalContentColor provides foreground) { content() }
    }
}

@Composable
fun NeuSegmentedButton(options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true) {
    require(options.isNotEmpty() && selectedIndex in options.indices)
    Row(modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, text ->
            NeuSurface(Modifier.weight(1f).heightIn(min = 48.dp).selectable(index == selectedIndex, enabled = enabled,
                role = Role.RadioButton, onClick = { onSelected(index) }), recessed = index == selectedIndex) {
                Text(text, Modifier.align(androidx.compose.ui.Alignment.Center).padding(horizontal = 8.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = LocalContentColor.current.copy(alpha = if (enabled) 1f else 0.38f))
            }
        }
    }
}

@Composable
fun NeuDialog(onDismissRequest: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest) {
        NeuSurface(modifier) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuBottomSheet(onDismissRequest: () -> Unit, modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier,
        containerColor = NeuTheme.colorScheme().backgroundColor) {
        NeuSurface(Modifier.fillMaxWidth(), recessed = true) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
    }
}

@Composable
fun NeuNavigationBar(labels: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier) {
    NeuSurface(modifier) {
        NeuSegmentedButton(labels, selectedIndex, onSelected, Modifier.fillMaxWidth().padding(12.dp))
    }
}

@Composable
fun NeuTabs(labels: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier) = NeuSegmentedButton(labels, selectedIndex, onSelected, modifier)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuRangeSlider(value: ClosedFloatingPointRange<Float>, onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier, valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true, steps: Int = 0, onValueChangeFinished: (() -> Unit)? = null) {
    require(valueRange.start.isFinite() && valueRange.endInclusive.isFinite() && valueRange.start < valueRange.endInclusive)
    require(value.start.isFinite() && value.endInclusive.isFinite() && value.start <= value.endInclusive)
    NeuSurface(modifier, recessed = true) {
        RangeSlider(value = value.start.coerceIn(valueRange)..value.endInclusive.coerceIn(valueRange),
            onValueChange = onValueChange, valueRange = valueRange, enabled = enabled, steps = steps,
            onValueChangeFinished = onValueChangeFinished, modifier = Modifier.padding(horizontal = 12.dp))
    }
}
