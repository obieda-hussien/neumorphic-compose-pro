package me.nikhilchaudhari.library.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import me.nikhilchaudhari.library.LightSource
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.internal.stackBlur
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A Button with neumorphic styling for traditional Android Views/XML layouts
 * 
 * Usage in XML:
 * ```xml
 * <me.nikhilchaudhari.library.views.NeumorphicButton
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:text="Click Me"
 *     app:neuShape="punched"
 *     app:neuCornerRadius="12dp"
 *     app:neuElevation="6dp"
 *     app:neuLightShadowColor="@color/white"
 *     app:neuDarkShadowColor="@color/gray" />
 * ```
 * 
 * Usage in Java:
 * ```java
 * NeumorphicButton button = new NeumorphicButton(context);
 * button.setNeuShapeType(NeuShapeType.PUNCHED);
 * button.setNeuElevation(dpToPx(8));
 * ```
 */
class NeumorphicButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

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

    // Animation properties
    var enablePressAnimation: Boolean = true
    private var isPressed = false
    private var currentElevation: Float = neuElevation

    // Internal blur maker
    private var blurMaker: BlurMaker? = null

    // Cached bitmaps
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
        blurMaker = BlurMaker(context, calculateDefaultBlurRadius())

        // Parse XML attributes
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.NeumorphicButton)
            
            neuShapeType = when (typedArray.getInt(R.styleable.NeumorphicButton_neuShape, 0)) {
                0 -> NeuShapeType.PUNCHED
                1 -> NeuShapeType.PRESSED
                2 -> NeuShapeType.POT
                else -> NeuShapeType.PUNCHED
            }
            
            neuCornerType = when (typedArray.getInt(R.styleable.NeumorphicButton_neuCornerType, 0)) {
                0 -> NeuCornerType.ROUNDED
                1 -> NeuCornerType.OVAL
                else -> NeuCornerType.ROUNDED
            }
            
            neuCornerRadius = typedArray.getDimension(
                R.styleable.NeumorphicButton_neuCornerRadius, 12f.dpToPx()
            )
            
            neuLightShadowColor = typedArray.getColor(
                R.styleable.NeumorphicButton_neuLightShadowColor, Color.WHITE
            )
            
            neuDarkShadowColor = typedArray.getColor(
                R.styleable.NeumorphicButton_neuDarkShadowColor, Color.LTGRAY
            )
            
            neuElevation = typedArray.getDimension(
                R.styleable.NeumorphicButton_neuElevation, 6f.dpToPx()
            )
            currentElevation = neuElevation
            
            neuStrokeWidth = typedArray.getDimension(
                R.styleable.NeumorphicButton_neuStrokeWidth, 6f.dpToPx()
            )
            
            neuInsetHorizontal = typedArray.getDimension(
                R.styleable.NeumorphicButton_neuInsetHorizontal, 6f.dpToPx()
            )
            
            neuInsetVertical = typedArray.getDimension(
                R.styleable.NeumorphicButton_neuInsetVertical, 6f.dpToPx()
            )
            
            neuLightSource = when (typedArray.getInt(R.styleable.NeumorphicButton_neuLightSource, 0)) {
                0 -> LightSource.TOP_LEFT
                1 -> LightSource.TOP_RIGHT
                2 -> LightSource.BOTTOM_LEFT
                3 -> LightSource.BOTTOM_RIGHT
                else -> LightSource.TOP_LEFT
            }
            
            neuBackgroundColor = typedArray.getColor(
                R.styleable.NeumorphicButton_neuBackgroundColor, Color.parseColor("#ECEAEB")
            )
            
            typedArray.recycle()
        }

        // Remove default button background
        background = null
        stateListAnimator = null
        
        // Set layer type for proper shadow rendering
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        needsRedraw = true
        invalidateShadowBitmaps()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (enablePressAnimation && isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isPressed = true
                    currentElevation = neuElevation * 0.5f
                    needsRedraw = true
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPressed = false
                    currentElevation = neuElevation
                    needsRedraw = true
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
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
        drawBackgroundShadows(canvas)
        drawNeumorphicBackground(canvas)
    }

    private fun drawPressed(canvas: Canvas) {
        drawNeumorphicBackground(canvas)
        drawForegroundShadows(canvas)
    }

    private fun drawPot(canvas: Canvas) {
        drawBackgroundShadows(canvas)
        drawNeumorphicBackground(canvas)
        drawForegroundShadows(canvas)
    }

    private fun drawBackgroundShadows(canvas: Canvas) {
        val (lightOffsetX, lightOffsetY) = getLightOffset()
        val (darkOffsetX, darkOffsetY) = getDarkOffset()

        lightShadowBitmap?.let {
            canvas.drawBitmap(
                it,
                -neuInsetHorizontal - currentElevation + lightOffsetX,
                -neuInsetVertical - currentElevation + lightOffsetY,
                shadowPaint
            )
        }

        darkShadowBitmap?.let {
            canvas.drawBitmap(
                it,
                neuInsetHorizontal - currentElevation + darkOffsetX,
                neuInsetVertical - currentElevation + darkOffsetY,
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

        lightShadowBitmap = generateShadowBitmap(neuLightShadowColor)
        darkShadowBitmap = generateShadowBitmap(neuDarkShadowColor)

        if (neuShapeType == NeuShapeType.PRESSED || neuShapeType == NeuShapeType.POT) {
            foregroundShadowBitmap = generateForegroundShadowBitmap()
        }
    }

    private fun generateShadowBitmap(shadowColor: Int): Bitmap? {
        val bitmapWidth = (width + currentElevation * 2).roundToInt()
        val bitmapHeight = (height + currentElevation * 2).roundToInt()
        
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
        canvas.translate(currentElevation, currentElevation)
        drawable.draw(canvas)

        return blurMaker?.blur(bitmap) ?: bitmap.stackBlur(calculateDefaultBlurRadius())
    }

    private fun generateForegroundShadowBitmap(): Bitmap? {
        if (width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val (lightOffsetX, lightOffsetY) = getLightOffset()

        val lightDrawable = GradientDrawable().apply {
            setSize(width + currentElevation.toInt(), height + currentElevation.toInt())
            setStroke(neuStrokeWidth.toInt(), neuLightShadowColor)
            setBounds(0, 0, width + currentElevation.toInt(), height + currentElevation.toInt())
            setColor(Color.TRANSPARENT)
            when (neuCornerType) {
                NeuCornerType.OVAL -> shape = GradientDrawable.OVAL
                NeuCornerType.ROUNDED -> {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = neuCornerRadius
                }
            }
        }

        val darkDrawable = GradientDrawable().apply {
            setSize(width + currentElevation.toInt(), height + currentElevation.toInt())
            setStroke(neuStrokeWidth.toInt(), neuDarkShadowColor)
            setBounds(0, 0, width + currentElevation.toInt(), height + currentElevation.toInt())
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
        canvas.translate(lightOffsetX * 0.5f, lightOffsetY * 0.5f)
        lightDrawable.draw(canvas)
        canvas.restore()

        darkDrawable.draw(canvas)

        return blurMaker?.blur(bitmap) ?: bitmap.stackBlur(calculateDefaultBlurRadius())
    }

    private fun getLightOffset(): Pair<Float, Float> {
        return when (neuLightSource) {
            LightSource.TOP_LEFT -> Pair(-currentElevation * 0.5f, -currentElevation * 0.5f)
            LightSource.TOP_RIGHT -> Pair(currentElevation * 0.5f, -currentElevation * 0.5f)
            LightSource.BOTTOM_LEFT -> Pair(-currentElevation * 0.5f, currentElevation * 0.5f)
            LightSource.BOTTOM_RIGHT -> Pair(currentElevation * 0.5f, currentElevation * 0.5f)
        }
    }

    private fun getDarkOffset(): Pair<Float, Float> {
        return when (neuLightSource) {
            LightSource.TOP_LEFT -> Pair(currentElevation * 0.5f, currentElevation * 0.5f)
            LightSource.TOP_RIGHT -> Pair(-currentElevation * 0.5f, currentElevation * 0.5f)
            LightSource.BOTTOM_LEFT -> Pair(currentElevation * 0.5f, -currentElevation * 0.5f)
            LightSource.BOTTOM_RIGHT -> Pair(-currentElevation * 0.5f, -currentElevation * 0.5f)
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

    fun refresh() {
        needsRedraw = true
        invalidate()
    }
}
