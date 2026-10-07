package com.example.healthsync

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.sin

// A progress bar that looks like layered, translucent water filling left -> right.
//  - Five overlapping waves, from pale sky-blue (furthest ahead) to solid navy (closest to the left).
//  - Each wave has its own size, length, color, and ripple direction, so they drift against each other.
//  - Colors are semi-transparent, so where waves overlap you see the blend, like the reference design.
//  - The navy front layer has a gradient that slides left -> right to suggest flow.
class WaterProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    // One translucent wave.
    //  lead   = how far ahead of the navy edge this wave reaches
    //  amp    = how far the edge swings left/right
    //  length = vertical distance of one full S-curve
    //  speed  = ripple speed/direction (whole numbers keep the loop seamless; negative = other way)
    //  shift  = starting offset so the waves don't line up
    private class WaveLayer(
        color: Int,
        val lead: Float,
        val amp: Float,
        val length: Float,
        val speed: Int,
        val shift: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    }

    // ----- Tunable look & feel -----
    // Listed back (furthest ahead, palest) to front (navy). Change colors/alpha here.
    // Color format is 0xAARRGGBB: AA = opacity (FF solid, 80 about half).
    private val layers = listOf(
        WaveLayer(0xB3A9D0EE.toInt(), dp(32f), dp(11f), dp(96f), -1, 0.0f),  // pale sky blue
        WaveLayer(0xCC6FB1E2.toInt(), dp(23f), dp(10f), dp(76f),  1, 1.8f),  // light blue
        WaveLayer(0xD93F86CE.toInt(), dp(15f), dp(9f),  dp(86f), -2, 3.4f),  // mid blue
        WaveLayer(0xE62254B3.toInt(), dp(7f),  dp(7f),  dp(64f),  1, 5.0f),  // deep blue
        WaveLayer(0xFF0A1F6B.toInt(), 0f,      dp(5f),  dp(54f), -1, 0.9f)   // navy (front)
    )
    private val cornerRadius = dp(10f)
    private val flowTileWidth = dp(120f)
    private val fillDurationMs = 900L
    private val waveDurationMs = 3200L   // lower = faster ripple
    private val flowDurationMs = 2600L   // lower = faster flow

    private val colorTrack = 0xFFF1F1F1.toInt()
    private val colorNavy = 0xFF1E5BC0.toInt()
    private val colorNavyLight = 0xFF2A6FD6.toInt()

    // ----- Drawing tools (created once, reused every frame) -----
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorTrack }
    private val clipPath = Path()
    private val wavePath = Path()
    private val shaderMatrix = Matrix()
    private val twoPi = (2 * PI).toFloat()

    // ----- Animation state -----
    private var shownProgress = 0f     // what's currently drawn (0..1)
    private var wavePhase = 0f         // 0..2π, drives the ripple
    private var flowOffset = 0f        // 0..1, drives the sliding gradient

    private var fillAnimator: ValueAnimator? = null
    private val waveAnimator = ValueAnimator.ofFloat(0f, twoPi).apply {
        duration = waveDurationMs
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { wavePhase = it.animatedValue as Float; invalidate() }
    }
    private val flowAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = flowDurationMs
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { flowOffset = it.animatedValue as Float }
    }

    // Public API: fraction is 0.0 (empty) to 1.0 (goal reached)
    fun setProgress(fraction: Float, animate: Boolean = true) {
        val target = fraction.coerceIn(0f, 1f)
        fillAnimator?.cancel()
        if (animate) {
            fillAnimator = ValueAnimator.ofFloat(shownProgress, target).apply {
                duration = fillDurationMs
                interpolator = DecelerateInterpolator()
                addUpdateListener { shownProgress = it.animatedValue as Float; invalidate() }
                start()
            }
        } else {
            shownProgress = target
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Rounded-rectangle outline that everything is clipped to
        clipPath.reset()
        clipPath.addRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), cornerRadius, cornerRadius, Path.Direction.CW)

        // Front (navy) layer gets a repeating gradient; sliding it sideways makes it "flow"
        layers.last().paint.shader = LinearGradient(
            0f, 0f, flowTileWidth, 0f,
            intArrayOf(colorNavy, colorNavyLight, colorNavy),
            null,
            Shader.TileMode.REPEAT
        )
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawRect(0f, 0f, w, h, trackPaint)

        if (shownProgress > 0f) {
            shaderMatrix.setTranslate(flowOffset * flowTileWidth, 0f)   // slides left -> right
            layers.last().paint.shader?.setLocalMatrix(shaderMatrix)

            // Back to front, so the navy ends up on top
            for (layer in layers) drawWave(canvas, w, h, layer)
        }
        canvas.restore()
    }

    // Fills everything left of a wavy vertical edge.
    // At 0% the edge is fully off-screen left; at 100% it's fully past the right side.
    private fun drawWave(canvas: Canvas, w: Float, h: Float, layer: WaveLayer) {
        val baseX = shownProgress * (w + 2 * layer.amp + layer.lead) - layer.amp
        val k = twoPi / layer.length
        val step = dp(2f)

        wavePath.reset()
        wavePath.moveTo(0f, 0f)
        var y = 0f
        while (y < h) {
            wavePath.lineTo(baseX + layer.amp * sin(k * y + layer.speed * wavePhase + layer.shift), y)
            y += step
        }
        wavePath.lineTo(baseX + layer.amp * sin(k * h + layer.speed * wavePhase + layer.shift), h)
        wavePath.lineTo(0f, h)
        wavePath.close()
        canvas.drawPath(wavePath, layer.paint)
    }

    // Only run the endless animations while the view is on screen (saves battery)
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!waveAnimator.isStarted) waveAnimator.start()
        if (!flowAnimator.isStarted) flowAnimator.start()
    }

    override fun onDetachedFromWindow() {
        waveAnimator.cancel()
        flowAnimator.cancel()
        fillAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) {
            if (!waveAnimator.isStarted) waveAnimator.start()
            if (!flowAnimator.isStarted) flowAnimator.start()
        } else {
            waveAnimator.cancel()
            flowAnimator.cancel()
        }
    }
}