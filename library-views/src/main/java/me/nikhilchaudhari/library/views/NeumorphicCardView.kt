package me.nikhilchaudhari.library.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import me.nikhilchaudhari.library.LightSource
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.internal.stackBlur
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A CardView with neumorphic styling for traditional Android Views/XML layouts
 * 
 * Usage in XML:
 * ```xml
 * <me.nikhilchaudhari.library.views.NeumorphicCardView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:neuShape="punched"
 *     app:neuCornerRadius="16dp"
 *     app:neuElevation="8dp">
 *     
 *     <!-- Your content here -->
 *     <LinearLayout ... />
 *     
 * </me.nikhilchaudhari.library.views.NeumorphicCardView>
 * ```
 * 
 * Usage in Java:
 * ```java
 * NeumorphicCardView cardView = new NeumorphicCardView(context);
 * cardView.setNeuShapeType(NeuShapeType.POT);
 * cardView.setNeuCornerRadius(dpToPx(16));
 * ```
 */
class NeumorphicCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

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

    var neuCornerRadius: Float = 16f.dpToPx()
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

    var neuElevation: Float = 8f.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var neuStrokeWidth: Float = 6f.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var neuInsetHorizontal: Float = 8f.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var neuInsetVertical: Float = 8f.dpToPx()
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
            val typedArray = context.obtainStyledAttributes(it, R.styleable.NeumorphicCardView)
            
            neuShapeType = when (typedArray.getInt(R.styleable.NeumorphicCardView_neuShape, 0)) {
                0 -> NeuShapeType.PUNCHED
                1 -> NeuShapeType.PRESSED
                2 -> NeuShapeType.POT
                else -> NeuShapeType.PUNCHED
            }
            
            neuCornerType = when (typedArray.getInt(R.styleable.NeumorphicCardView_neuCornerType, 0)) {
                0 -> NeuCornerType.ROUNDED
                1 -> NeuCornerType.OVAL
                else -> NeuCornerType.ROUNDED
            }
            
            neuCornerRadius = typedArray.getDimension(
                R.styleable.NeumorphicCardView_neuCornerRadius, 16f.dpToPx()
            )
            
            neuLightShadowColor = typedArray.getColor(
                R.styleable.NeumorphicCardView_neuLightShadowColor, Color.WHITE
            )
            
            neuDarkShadowColor = typedArray.getColor(
                R.styleable.NeumorphicCardView_neuDarkShadowColor, Color.LTGRAY
            )
            
            neuElevation = typedArray.getDimension(
                R.styleable.NeumorphicCardView_neuElevation, 8f.dpToPx()
            )
            
            neuStrokeWidth = typedArray.getDimension(
                R.styleable.NeumorphicCardView_neuStrokeWidth, 6f.dpToPx()
            )
            
            neuInsetHorizontal = typedArray.getDimension(
                R.styleable.NeumorphicCardView_neuInsetHorizontal, 8f.dpToPx()
            )
            
            neuInsetVertical = typedArray.getDimension(
                R.styleable.NeumorphicCardView_neuInsetVertical, 8f.dpToPx()
            )
            
            neuLightSource = when (typedArray.getInt(R.styleable.NeumorphicCardView_neuLightSource, 0)) {
                0 -> LightSource.TOP_LEFT
                1 -> LightSource.TOP_RIGHT
                2 -> LightSource.BOTTOM_LEFT
                3 -> LightSource.BOTTOM_RIGHT
                else -> LightSource.TOP_LEFT
            }
            
            neuBackgroundColor = typedArray.getColor(
                R.styleable.NeumorphicCardView_neuBackgroundColor, Color.parseColor("#ECEAEB")
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

        // Add padding for shadows
        setPadding(
            (neuInsetHorizontal + neuElevation).toInt(),
            (neuInsetVertical + neuElevation).toInt(),
            (neuInsetHorizontal + neuElevation).toInt(),
            (neuInsetVertical + neuElevation).toInt()
        )
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

        val contentLeft = neuInsetHorizontal + neuElevation
        val contentTop = neuInsetVertical + neuElevation
        val contentWidth = width - 2 * contentLeft
        val contentHeight = height - 2 * contentTop

        if (contentWidth <= 0 || contentHeight <= 0) return

        lightShadowBitmap?.let {
            canvas.drawBitmap(
                it,
                contentLeft - neuElevation + lightOffsetX,
                contentTop - neuElevation + lightOffsetY,
                shadowPaint
            )
        }

        darkShadowBitmap?.let {
            canvas.drawBitmap(
                it,
                contentLeft - neuElevation + darkOffsetX + neuInsetHorizontal * 2,
                contentTop - neuElevation + darkOffsetY + neuInsetVertical * 2,
                shadowPaint
            )
        }
    }

    private fun drawForegroundShadows(canvas: Canvas) {
        val contentLeft = neuInsetHorizontal + neuElevation
        val contentTop = neuInsetVertical + neuElevation
        
        foregroundShadowBitmap?.let {
            canvas.drawBitmap(it, contentLeft, contentTop, shadowPaint)
        }
    }

    private fun drawNeumorphicBackground(canvas: Canvas) {
        backgroundPaint.color = neuBackgroundColor
        
        val contentLeft = neuInsetHorizontal + neuElevation
        val contentTop = neuInsetVertical + neuElevation
        val contentWidth = width - 2 * contentLeft
        val contentHeight = height - 2 * contentTop

        if (contentWidth <= 0 || contentHeight <= 0) return

        when (neuCornerType) {
            NeuCornerType.OVAL -> {
                canvas.drawOval(
                    contentLeft, contentTop,
                    contentLeft + contentWidth, contentTop + contentHeight,
                    backgroundPaint
                )
            }
            NeuCornerType.ROUNDED -> {
                canvas.drawRoundRect(
                    contentLeft, contentTop,
                    contentLeft + contentWidth, contentTop + contentHeight,
                    neuCornerRadius, neuCornerRadius,
                    backgroundPaint
                )
            }
        }
    }

    private fun generateShadowBitmaps() {
        invalidateShadowBitmaps()
        
        val contentLeft = neuInsetHorizontal + neuElevation
        val contentTop = neuInsetVertical + neuElevation
        val contentWidth = (width - 2 * contentLeft).toInt()
        val contentHeight = (height - 2 * contentTop).toInt()

        if (contentWidth <= 0 || contentHeight <= 0) return

        lightShadowBitmap = generateShadowBitmap(neuLightShadowColor, contentWidth, contentHeight)
        darkShadowBitmap = generateShadowBitmap(neuDarkShadowColor, contentWidth, contentHeight)

        if (neuShapeType == NeuShapeType.PRESSED || neuShapeType == NeuShapeType.POT) {
            foregroundShadowBitmap = generateForegroundShadowBitmap(contentWidth, contentHeight)
        }
    }

    private fun generateShadowBitmap(shadowColor: Int, contentWidth: Int, contentHeight: Int): Bitmap? {
        val bitmapWidth = (contentWidth + neuElevation * 2).roundToInt()
        val bitmapHeight = (contentHeight + neuElevation * 2).roundToInt()
        
        if (bitmapWidth <= 0 || bitmapHeight <= 0) return null

        val drawable = GradientDrawable().apply {
            setColor(shadowColor)
            setSize(contentWidth, contentHeight)
            setBounds(0, 0, contentWidth, contentHeight)
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

    private fun generateForegroundShadowBitmap(contentWidth: Int, contentHeight: Int): Bitmap? {
        if (contentWidth <= 0 || contentHeight <= 0) return null

        val bitmap = Bitmap.createBitmap(contentWidth, contentHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val (lightOffsetX, lightOffsetY) = getLightOffset()

        val lightDrawable = GradientDrawable().apply {
            setSize(contentWidth + neuElevation.toInt(), contentHeight + neuElevation.toInt())
            setStroke(neuStrokeWidth.toInt(), neuLightShadowColor)
            setBounds(0, 0, contentWidth + neuElevation.toInt(), contentHeight + neuElevation.toInt())
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
            setSize(contentWidth + neuElevation.toInt(), contentHeight + neuElevation.toInt())
            setStroke(neuStrokeWidth.toInt(), neuDarkShadowColor)
            setBounds(0, 0, contentWidth + neuElevation.toInt(), contentHeight + neuElevation.toInt())
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

    fun refresh() {
        needsRedraw = true
        invalidate()
    }
}
