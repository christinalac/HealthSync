//******************************************
// Program Name: WaterProgressBar.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 1.3
// Purpose: Custom animated progress bar that fills left to right like layered, flowing water. Used for the hydration meter.
//******************************************

package com.example.healthsync;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

// A progress bar that looks like layered, translucent water filling left -> right.
//  - Five overlapping waves, from pale sky-blue (furthest ahead) to solid navy (closest to the left).
//  - Each wave has its own size, length, color, and ripple direction, so they drift against each other.
//  - Colors are semi-transparent, so where waves overlap you see the blend, like the reference design.
//  - The navy front layer has a gradient that slides left -> right to suggest flow.
public class WaterProgressBar extends View {

    private final float density = getResources().getDisplayMetrics().density;

    private float dp(float v) {
        return v * density;
    }

    // One translucent wave.
    //  lead   = how far ahead of the navy edge this wave reaches
    //  amp    = how far the edge swings left/right
    //  length = vertical distance of one full S-curve
    //  speed  = ripple speed/direction (whole numbers keep the loop seamless; negative = other way)
    //  shift  = starting offset so the waves don't line up
    private static final class WaveLayer {
        final float lead;
        final float amp;
        final float length;
        final int speed;
        final float shift;
        final Paint paint;

        WaveLayer(int color, float lead, float amp, float length, int speed, float shift) {
            this.lead = lead;
            this.amp = amp;
            this.length = length;
            this.speed = speed;
            this.shift = shift;
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.paint.setColor(color);
        }
    }

    // ----- Tunable look & feel -----
    // Listed back (furthest ahead, palest) to front (navy). Change colors/alpha here.
    // Color format is 0xAARRGGBB: AA = opacity (FF solid, 80 about half).
    private final List<WaveLayer> layers = new ArrayList<>();

    private final float cornerRadius = dp(10f);
    private final float flowTileWidth = dp(120f);
    private final long fillDurationMs = 900L;
    private final long waveDurationMs = 3200L;   // lower = faster ripple
    private final long flowDurationMs = 2600L;   // lower = faster flow

    private final int colorTrack = 0xFFF1F1F1;
    private final int colorNavy = 0xFF1E5BC0;
    private final int colorNavyLight = 0xFF2A6FD6;

    // ----- Drawing tools (created once, reused every frame) -----
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();
    private final Path wavePath = new Path();
    private final Matrix shaderMatrix = new Matrix();
    private final float twoPi = (float) (2 * Math.PI);

    // ----- Animation state -----
    private float shownProgress = 0f;     // what's currently drawn (0..1)
    private float wavePhase = 0f;         // 0..2π, drives the ripple
    private float flowOffset = 0f;        // 0..1, drives the sliding gradient

    @Nullable private ValueAnimator fillAnimator;
    private final ValueAnimator waveAnimator;
    private final ValueAnimator flowAnimator;

    public WaterProgressBar(Context context) {
        this(context, null);
    }

    public WaterProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        trackPaint.setColor(colorTrack);

        layers.add(new WaveLayer(0xB3A9D0EE, dp(32f), dp(11f), dp(96f), -1, 0.0f));  // pale sky blue
        layers.add(new WaveLayer(0xCC6FB1E2, dp(23f), dp(10f), dp(76f), 1, 1.8f));   // light blue
        layers.add(new WaveLayer(0xD93F86CE, dp(15f), dp(9f), dp(86f), -2, 3.4f));   // mid blue
        layers.add(new WaveLayer(0xE62254B3, dp(7f), dp(7f), dp(64f), 1, 5.0f));     // deep blue
        layers.add(new WaveLayer(0xFF0A1F6B, 0f, dp(5f), dp(54f), -1, 0.9f));        // navy (front)

        waveAnimator = ValueAnimator.ofFloat(0f, twoPi);
        waveAnimator.setDuration(waveDurationMs);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.addUpdateListener(a -> {
            wavePhase = (float) a.getAnimatedValue();
            invalidate();
        });

        flowAnimator = ValueAnimator.ofFloat(0f, 1f);
        flowAnimator.setDuration(flowDurationMs);
        flowAnimator.setInterpolator(new LinearInterpolator());
        flowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flowAnimator.addUpdateListener(a -> flowOffset = (float) a.getAnimatedValue());
    }

    // Public API: fraction is 0.0 (empty) to 1.0 (goal reached)
    public void setProgress(float fraction) {
        setProgress(fraction, true);
    }

    public void setProgress(float fraction, boolean animate) {
        final float target = Math.max(0f, Math.min(1f, fraction));
        if (fillAnimator != null) fillAnimator.cancel();
        if (animate) {
            fillAnimator = ValueAnimator.ofFloat(shownProgress, target);
            fillAnimator.setDuration(fillDurationMs);
            fillAnimator.setInterpolator(new DecelerateInterpolator());
            fillAnimator.addUpdateListener(a -> {
                shownProgress = (float) a.getAnimatedValue();
                invalidate();
            });
            fillAnimator.start();
        } else {
            shownProgress = target;
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Rounded-rectangle outline that everything is clipped to
        clipPath.reset();
        clipPath.addRoundRect(new RectF(0f, 0f, w, h), cornerRadius, cornerRadius, Path.Direction.CW);

        // Front (navy) layer gets a repeating gradient; sliding it sideways makes it "flow"
        front().paint.setShader(new LinearGradient(
                0f, 0f, flowTileWidth, 0f,
                new int[]{colorNavy, colorNavyLight, colorNavy},
                null,
                Shader.TileMode.REPEAT));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        canvas.save();
        canvas.clipPath(clipPath);
        canvas.drawRect(0f, 0f, w, h, trackPaint);

        if (shownProgress > 0f) {
            shaderMatrix.setTranslate(flowOffset * flowTileWidth, 0f);   // slides left -> right
            Shader shader = front().paint.getShader();
            if (shader != null) shader.setLocalMatrix(shaderMatrix);

            // Back to front, so the navy ends up on top
            for (WaveLayer layer : layers) drawWave(canvas, w, h, layer);
        }
        canvas.restore();
    }

    // Fills everything left of a wavy vertical edge.
    // At 0% the edge is fully off-screen left; at 100% it's fully past the right side.
    private void drawWave(Canvas canvas, float w, float h, WaveLayer layer) {
        float baseX = shownProgress * (w + 2 * layer.amp + layer.lead) - layer.amp;
        float k = twoPi / layer.length;
        float step = dp(2f);

        wavePath.reset();
        wavePath.moveTo(0f, 0f);
        float y = 0f;
        while (y < h) {
            wavePath.lineTo(baseX + layer.amp
                    * (float) Math.sin(k * y + layer.speed * wavePhase + layer.shift), y);
            y += step;
        }
        wavePath.lineTo(baseX + layer.amp
                * (float) Math.sin(k * h + layer.speed * wavePhase + layer.shift), h);
        wavePath.lineTo(0f, h);
        wavePath.close();
        canvas.drawPath(wavePath, layer.paint);
    }

    private WaveLayer front() {
        return layers.get(layers.size() - 1);
    }

    // Only run the endless animations while the view is on screen (saves battery)
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!waveAnimator.isStarted()) waveAnimator.start();
        if (!flowAnimator.isStarted()) flowAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        waveAnimator.cancel();
        flowAnimator.cancel();
        if (fillAnimator != null) fillAnimator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            if (!waveAnimator.isStarted()) waveAnimator.start();
            if (!flowAnimator.isStarted()) flowAnimator.start();
        } else {
            waveAnimator.cancel();
            flowAnimator.cancel();
        }
    }
}
