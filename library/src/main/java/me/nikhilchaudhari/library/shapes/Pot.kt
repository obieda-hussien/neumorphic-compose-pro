package me.nikhilchaudhari.library.shapes

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.internal.BlurMaker


/**
 * Pot / Basin shape
 *    /\___________/\
 * __/               \__
 */
open class Pot(private val cornerType: CornerType = CornerType.Rounded()) : NeuShape {

    override fun drawShadows(
        drawScope: ContentDrawScope,
        blurMaker: BlurMaker,
        shapeConfig: ShapeConfig
    ) {
        shapeConfig.cornerType = cornerType
        // The outer (raised/"Punched"-style) shadow is meant to extend past
        // the shape's own bounds - draw it unclipped.
        drawScope.drawOnBackground(shapeConfig, blurMaker)
        drawScope.drawContent()
        // The inner (recessed/"Pressed"-style) shadow, on the other hand,
        // must stay within the shape's bounds or it visibly bleeds past the
        // rounded corners onto the flat surface around it. A Modifier.clip()
        // on the whole component can't distinguish between these two draws -
        // both happen inside this single draw() call - so the clip is scoped
        // to just this one call instead.
        drawScope.clippedToCornerType(cornerType) {
            drawOnForeground(shapeConfig, blurMaker)
        }
    }

    class Oval(): Pot(CornerType.Oval)

    class Rounded(radius: Dp = 12.dp): Pot(CornerType.Rounded(radius))
}

/**
 * Clips [block]'s drawing to this shape's own bounds, matching [cornerType]
 * (a rounded rect or an oval/circle). Shared by any [NeuShape] that needs to
 * clip only part of its drawing - see [Pot] above for why that can't be done
 * with an external `Modifier.clip()`.
 */
internal fun DrawScope.clippedToCornerType(cornerType: CornerType, block: DrawScope.() -> Unit) {
    val bounds = Rect(Offset.Zero, size)
    val path = Path()
    when (cornerType) {
        is CornerType.Oval -> path.addOval(bounds)
        is CornerType.Rounded -> {
            val radiusPx = density.run { cornerType.radius.toPx() }
            path.addRoundRect(RoundRect(bounds, CornerRadius(radiusPx)))
        }
    }
    clipPath(path) {
        block()
    }
}