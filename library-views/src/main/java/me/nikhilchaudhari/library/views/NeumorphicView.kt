package me.nikhilchaudhari.library.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import me.nikhilchaudhari.library.LightSource
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.internal.stackBlur
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Shape types for neumorphic views
 */
enum class NeuShapeType {
    PUNCHED,
    PRESSED,
    POT
}

/**
 * Corner types for neumorphic views
 */
enum class NeuCornerType {
    ROUNDED,
    OVAL
}

/**
 * Base class for neumorphic views that can be used in XML layouts
 * Supports Java/Kotlin Android development with traditional Views
 */
open class NeumorphicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Neumorphic properties
    var neuShapeType: NeuShapeType = NeuShapeType.PUNCHED
        set(value) {
            field = value
            invalidate()
        }

    var neuCornerType: NeuCornerType = NeuCornerType.ROUNDED
        set(value) {
            field = value
            invalidate()
        }

    var neuCornerRadius: Float = 12f.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var neuLightShadowColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var neuDarkShadowColor: Int = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }

    var neuElevation: Float = 6f.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var neuStrokeWidth: Float = 6f.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var neuInsetHorizontal: Float = 6f.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var neuInsetVertical: Float = 6f.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var neuLightSource: LightSource = LightSource.TOP_LEFT
        set(value) {
            field = value
            invalidate()
        }

    var neuBackgroundColor: Int = Color.parseColor("#ECEAEB")
        set(value) {
            field = value
            invalidate()
        }

    // Internal blur maker
    private var blurMaker: BlurMaker? = null

    // Cached bitmaps for performance
    private var lightShadowBitmap: Bitmap? = null
    private var darkShadowBitmap: Bitmap? = null
    private var foregroundShadowBitmap: Bitmap? = null
    private var needsRedraw = true

    // Paint objects
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    init {
        // Initialize blur maker
        blurMaker = BlurMaker(context, calculateDefaultBlurRadius())
        
        // Parse XML attributes
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.NeumorphicView)
            
            neuShapeType = when (typedArray.getInt(R.styleable.NeumorphicView_neuShape, 0)) {
                0 -> NeuShapeType.PUNCHED
                1 -> NeuShapeType.PRESSED
                2 -> NeuShapeType.POT
                else -> NeuShapeType.PUNCHED
            }
            
            neuCornerType = when (typedArray.getInt(R.styleable.NeumorphicView_neuCornerType, 0)) {
                0 -> NeuCornerType.ROUNDED
                1 -> NeuCornerType.OVAL
                else -> NeuCornerType.ROUNDED
            }
            
            neuCornerRadius = typedArray.getDimension(
                R.styleable.NeumorphicView_neuCornerRadius, 12f.dpToPx()
            )
            
            neuLightShadowColor = typedArray.getColor(
                R.styleable.NeumorphicView_neuLightShadowColor, Color.WHITE
            )
            
            neuDarkShadowColor = typedArray.getColor(
                R.styleable.NeumorphicView_neuDarkShadowColor, Color.LTGRAY
            )
            
            neuElevation = typedArray.getDimension(
                R.styleable.NeumorphicView_neuElevation, 6f.dpToPx()
            )
            
            neuStrokeWidth = typedArray.getDimension(
                R.styleable.NeumorphicView_neuStrokeWidth, 6f.dpToPx()
            )
            
            neuInsetHorizontal = typedArray.getDimension(
                R.styleable.NeumorphicView_neuInsetHorizontal, 6f.dpToPx()
            )
            
            neuInsetVertical = typedArray.getDimension(
                R.styleable.NeumorphicView_neuInsetVertical, 6f.dpToPx()
            )
            
            neuLightSource = when (typedArray.getInt(R.styleable.NeumorphicView_neuLightSource, 0)) {
                0 -> LightSource.TOP_LEFT
                1 -> LightSource.TOP_RIGHT
                2 -> LightSource.BOTTOM_LEFT
                3 -> LightSource.BOTTOM_RIGHT
                else -> LightSource.TOP_LEFT
            }
            
            neuBackgroundColor = typedArray.getColor(
                R.styleable.NeumorphicView_neuBackgroundColor, Color.parseColor("#ECEAEB")
            )
            
            typedArray.recycle()
        }
        
        // Enable drawing
        setWillNotDraw(false)
        
        // Set layer type for proper shadow rendering
        // Use hardware layer on modern devices, fall back to software for complex operations
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        } else {
            // Software layer required for blur effects on older devices
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        needsRedraw = true
        invalidateShadowBitmaps()
    }

    override fun onDraw(canvas: Canvas) {
        if (width <= 0 || height <= 0) return

        if (needsRedraw) {
            generateShadowBitmaps()
            needsRedraw = false
        }

        when (neuShapeType) {
            NeuShapeType.PUNCHED -> drawPunched(canvas)
            NeuShapeType.PRESSED -> drawPressed(canvas)
            NeuShapeType.POT -> drawPot(canvas)
        }

        super.onDraw(canvas)
    }

    private fun drawPunched(canvas: Canvas) {
        // Draw background shadows first
        drawBackgroundShadows(canvas)
        // Draw the actual background
        drawNeumorphicBackground(canvas)
    }

    private fun drawPressed(canvas: Canvas) {
        // Draw the actual background first
        drawNeumorphicBackground(canvas)
        // Draw foreground shadows
        drawForegroundShadows(canvas)
    }

    private fun drawPot(canvas: Canvas) {
        // Draw background shadows
        drawBackgroundShadows(canvas)
        // Draw the actual background
        drawNeumorphicBackground(canvas)
        // Draw foreground shadows
        drawForegroundShadows(canvas)
    }

    private fun drawBackgroundShadows(canvas: Canvas) {
        val (lightOffsetX, lightOffsetY) = getLightOffset()
        val (darkOffsetX, darkOffsetY) = getDarkOffset()

        lightShadowBitmap?.let {
            canvas.drawBitmap(
                it,
                -neuInsetHorizontal - neuElevation + lightOffsetX,
                -neuInsetVertical - neuElevation + lightOffsetY,
                shadowPaint
            )
        }

        darkShadowBitmap?.let {
            canvas.drawBitmap(
                it,
                neuInsetHorizontal - neuElevation + darkOffsetX,
                neuInsetVertical - neuElevation + darkOffsetY,
                shadowPaint
            )
        }
    }

    private fun drawForegroundShadows(canvas: Canvas) {
        foregroundShadowBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, shadowPaint)
        }
    }

    private fun drawNeumorphicBackground(canvas: Canvas) {
        backgroundPaint.color = neuBackgroundColor
        
        when (neuCornerType) {
            NeuCornerType.OVAL -> {
                canvas.drawOval(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    backgroundPaint
                )
            }
            NeuCornerType.ROUNDED -> {
                canvas.drawRoundRect(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    neuCornerRadius, neuCornerRadius,
                    backgroundPaint
                )
            }
        }
    }

    private fun generateShadowBitmaps() {
        invalidateShadowBitmaps()
        
        if (width <= 0 || height <= 0) return

        // Generate background shadows
        lightShadowBitmap = generateShadowBitmap(neuLightShadowColor)
        darkShadowBitmap = generateShadowBitmap(neuDarkShadowColor)

        // Generate foreground shadows for pressed and pot shapes
        if (neuShapeType == NeuShapeType.PRESSED || neuShapeType == NeuShapeType.POT) {
            foregroundShadowBitmap = generateForegroundShadowBitmap()
        }
    }

    private fun generateShadowBitmap(shadowColor: Int): Bitmap? {
        val bitmapWidth = (width + neuElevation * 2).roundToInt()
        val bitmapHeight = (height + neuElevation * 2).roundToInt()
        
        if (bitmapWidth <= 0 || bitmapHeight <= 0) return null

        val drawable = GradientDrawable().apply {
            setColor(shadowColor)
            setSize(width, height)
            setBounds(0, 0, width, height)
            when (neuCornerType) {
                NeuCornerType.OVAL -> shape = GradientDrawable.OVAL
                NeuCornerType.ROUNDED -> {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = neuCornerRadius
                }
            }
        }

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.translate(neuElevation, neuElevation)
        drawable.draw(canvas)

        return blurMaker?.blur(bitmap) ?: bitmap.stackBlur(calculateDefaultBlurRadius())
    }

    private fun generateForegroundShadowBitmap(): Bitmap? {
        if (width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val (lightOffsetX, lightOffsetY) = getLightOffset()

        // Light shadow stroke
        val lightDrawable = GradientDrawable().apply {
            setSize(width + neuElevation.toInt(), height + neuElevation.toInt())
            setStroke(neuStrokeWidth.toInt(), neuLightShadowColor)
            setBounds(0, 0, width + neuElevation.toInt(), height + neuElevation.toInt())
            setColor(Color.TRANSPARENT)
            when (neuCornerType) {
                NeuCornerType.OVAL -> shape = GradientDrawable.OVAL
                NeuCornerType.ROUNDED -> {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = neuCornerRadius
                }
            }
        }

        // Dark shadow stroke
        val darkDrawable = GradientDrawable().apply {
            setSize(width + neuElevation.toInt(), height + neuElevation.toInt())
            setStroke(neuStrokeWidth.toInt(), neuDarkShadowColor)
            setBounds(0, 0, width + neuElevation.toInt(), height + neuElevation.toInt())
            setColor(Color.TRANSPARENT)
            when (neuCornerType) {
                NeuCornerType.OVAL -> shape = GradientDrawable.OVAL
                NeuCornerType.ROUNDED -> {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = neuCornerRadius
                }
            }
        }

        canvas.save()
        canvas.translate(lightOffsetX, lightOffsetY)
        lightDrawable.draw(canvas)
        canvas.restore()

        darkDrawable.draw(canvas)

        return blurMaker?.blur(bitmap) ?: bitmap.stackBlur(calculateDefaultBlurRadius())
    }

    private fun getLightOffset(): Pair<Float, Float> {
        return when (neuLightSource) {
            LightSource.TOP_LEFT -> Pair(-neuElevation * 0.5f, -neuElevation * 0.5f)
            LightSource.TOP_RIGHT -> Pair(neuElevation * 0.5f, -neuElevation * 0.5f)
            LightSource.BOTTOM_LEFT -> Pair(-neuElevation * 0.5f, neuElevation * 0.5f)
            LightSource.BOTTOM_RIGHT -> Pair(neuElevation * 0.5f, neuElevation * 0.5f)
        }
    }

    private fun getDarkOffset(): Pair<Float, Float> {
        return when (neuLightSource) {
            LightSource.TOP_LEFT -> Pair(neuElevation * 0.5f, neuElevation * 0.5f)
            LightSource.TOP_RIGHT -> Pair(-neuElevation * 0.5f, neuElevation * 0.5f)
            LightSource.BOTTOM_LEFT -> Pair(neuElevation * 0.5f, -neuElevation * 0.5f)
            LightSource.BOTTOM_RIGHT -> Pair(-neuElevation * 0.5f, -neuElevation * 0.5f)
        }
    }

    private fun invalidateShadowBitmaps() {
        lightShadowBitmap?.recycle()
        darkShadowBitmap?.recycle()
        foregroundShadowBitmap?.recycle()
        lightShadowBitmap = null
        darkShadowBitmap = null
        foregroundShadowBitmap = null
    }

    private fun calculateDefaultBlurRadius(): Int {
        val density = resources.displayMetrics.density
        return min(25, (density * 10).roundToInt())
    }

    private fun Float.dpToPx(): Float {
        return this * resources.displayMetrics.density
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        invalidateShadowBitmaps()
    }

    /**
     * Refresh the neumorphic effect
     * Call this after changing properties programmatically
     */
    fun refresh() {
        needsRedraw = true
        invalidate()
    }
}
