package me.nikhilchaudhari.library.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlin.math.*

/** Circular control with TalkBack set-progress and keyboard support. Values remain caller-owned. */
@Composable
fun NeuRotaryKnob(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f, enabled: Boolean = true,
    label: String = "Value", onValueChangeFinished: (() -> Unit)? = null) {
    require(value.isFinite() && valueRange.start.isFinite() && valueRange.endInclusive.isFinite() && valueRange.start < valueRange.endInclusive)
    val current by rememberUpdatedState(value.coerceIn(valueRange))
    val change by rememberUpdatedState(onValueChange)
    val finished by rememberUpdatedState(onValueChangeFinished)
    val fraction = (current - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.38f)
    NeuSurface(modifier.sizeIn(minWidth = 64.dp, minHeight = 64.dp).size(96.dp)
        .semantics {
            contentDescription = label
            progressBarRangeInfo = ProgressBarRangeInfo(current, valueRange)
            if (!enabled) disabled()
            setProgress { target ->
                if (enabled && target.isFinite()) {
                    change(target.coerceIn(valueRange)); finished?.invoke(); true
                } else false
            }
        }.onKeyEvent { event ->
            if (!enabled || event.type != KeyEventType.KeyDown) false else {
                val step = (valueRange.endInclusive - valueRange.start) / 100f
                val target = when (event.key) {
                    Key.DirectionUp, Key.DirectionRight -> current + step
                    Key.DirectionDown, Key.DirectionLeft -> current - step
                    else -> return@onKeyEvent false
                }
                change(target.coerceIn(valueRange)); finished?.invoke(); true
            }
        }.focusable(enabled).pointerInput(enabled, valueRange) {
            if (enabled) detectDragGestures(onDragEnd = { finished?.invoke() }) { pointer, _ ->
                pointer.consume()
                val x = pointer.position.x - size.width / 2f
                val y = pointer.position.y - size.height / 2f
                val degrees = ((atan2(y, x) * 180f / PI.toFloat() - 135f) + 360f) % 360f
                val progress = if (degrees <= 270f) degrees / 270f else if (degrees < 315f) 1f else 0f
                change(valueRange.start + progress * (valueRange.endInclusive - valueRange.start))
            }
        }, shape = CircleShape) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            drawArc(color.copy(alpha = 0.2f), 135f, 270f, false, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            drawArc(color, 135f, 270f * fraction, false, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            val angle = (135f + 270f * fraction) * PI.toFloat() / 180f
            val radius = size.minDimension * 0.32f
            drawLine(color, center, Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius),
                strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}
