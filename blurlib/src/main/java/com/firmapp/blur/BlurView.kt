package com.firmapp.blur

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes

/**
 * 背後のビューをリアルタイムにぼかして「すりガラス」のように見せる [FrameLayout]。
 *
 * RenderScript を使わず、[RenderEffect][android.graphics.RenderEffect] による
 * ハードウェアぼかしのみで実装しているため API 31 以降が必要。
 *
 * 使い方:
 * ```
 * blurView.blurBehind(backgroundView)   // ぼかし元を指定
 * blurView.blurRadiusDp = 20f
 * blurView.cornerRadiusDp = 16f
 * ```
 *
 * 注意: [blurBehind] に渡す背景ビューは、この [BlurView] を**子孫に含まない**こと。
 * 含めると描画が再帰してしまう。レイアウト上は別の兄弟コンテナを指定する。
 */
class BlurView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val renderer = BackdropBlurRenderer()
    private val backdropLocation = IntArray(2)
    private val selfLocation = IntArray(2)

    private var backdrop: View? = null

    private var lastOffsetX = Float.NaN
    private var lastOffsetY = Float.NaN

    /** ぼかしの有効/無効。false の間は通常の [FrameLayout] として振る舞う。 */
    var blurEnabled: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    /** ぼかしの強さ(dp)。 */
    var blurRadiusDp: Float = DEFAULT_RADIUS_DP
        set(value) {
            field = value.coerceAtLeast(0f)
            invalidate()
        }

    /** ぼかしの上に重ねるオーバーレイ色(ARGB)。0 で無効。 */
    @ColorInt
    var overlayColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            invalidate()
        }

    /** 角丸半径(dp)。[circle] が true の場合は無視される。 */
    var cornerRadiusDp: Float = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
            invalidateOutline()
            invalidate()
        }

    /** true で円形にクリップする。 */
    var circle: Boolean = false
        set(value) {
            field = value
            invalidateOutline()
            invalidate()
        }

    /** 取り込み時の縮小率(0.0〜1.0)。小さいほど高速だが粗くなる。 */
    var downscaleFactor: Float = DEFAULT_DOWNSCALE
        set(value) {
            field = value.coerceIn(0.05f, 1f)
            invalidate()
        }

    // 背景が更新されるたびに自分も描き直す
    private val onPreDraw = ViewTreeObserver.OnPreDrawListener {
        if (blurEnabled && backdrop != null) invalidate()
        true
    }

    init {
        setWillNotDraw(false)
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                when {
                    circle -> {
                        val d = minOf(view.width, view.height)
                        val left = (view.width - d) / 2
                        val top = (view.height - d) / 2
                        outline.setOval(left, top, left + d, top + d)
                    }
                    cornerRadiusDp > 0f ->
                        outline.setRoundRect(0, 0, view.width, view.height, dp(cornerRadiusDp))
                    else ->
                        outline.setRect(0, 0, view.width, view.height)
                }
            }
        }

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.BlurView) {
                blurRadiusDp = getDimension(R.styleable.BlurView_blurRadius, dp(DEFAULT_RADIUS_DP)) / density
                overlayColor = getColor(R.styleable.BlurView_blurOverlayColor, Color.TRANSPARENT)
                cornerRadiusDp = getDimension(R.styleable.BlurView_blurCornerRadius, 0f) / density
                circle = getBoolean(R.styleable.BlurView_blurCircle, false)
                downscaleFactor = getFloat(R.styleable.BlurView_blurDownscale, DEFAULT_DOWNSCALE)
            }
        }
    }

    /**
     * ぼかしの元になる背景ビューを指定する。
     *
     * スクロールする背景をぼかす場合は、スクロールコンテナ([ScrollView] など)ではなく
     * **実際に移動する中身のビュー**(その中の単一コンテンツ)を渡すこと。
     * コンテナを直接 [View.draw] すると、描画がスクロール位置とずれる場合がある。
     *
     * @return メソッドチェーン用に自身を返す
     */
    fun blurBehind(target: View): BlurView {
        backdrop = target
        invalidate()
        return this
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(onPreDraw)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnPreDrawListener(onPreDraw)
        renderer.release()
        super.onDetachedFromWindow()
    }

    override fun draw(canvas: Canvas) {
        val target = backdrop
        if (blurEnabled && target != null && width > 0 && height > 0 && canvas.isHardwareAccelerated) {
            getLocationInWindow(selfLocation)
            target.getLocationInWindow(backdropLocation)
            val offsetX = (selfLocation[0] - backdropLocation[0]).toFloat()
            val offsetY = (selfLocation[1] - backdropLocation[1]).toFloat()

            // 取り込み位置が前フレームから変われば、もう1フレーム描き直して追従を取りこぼさない。
            // 位置が安定したら止まる(常時再描画にはならない)。
            if (offsetX != lastOffsetX || offsetY != lastOffsetY) {
                lastOffsetX = offsetX
                lastOffsetY = offsetY
                postInvalidateOnAnimation()
            }

            renderer.capture(offsetX, offsetY, width, height, dp(blurRadiusDp), downscaleFactor) { target.draw(it) }
            renderer.drawInto(canvas, width, height)

            if (overlayColor != Color.TRANSPARENT) {
                canvas.drawColor(overlayColor)
            }
        }
        // 子ビュー(カードの中身など)を上に描画
        super.draw(canvas)
    }

    private val density: Float get() = resources.displayMetrics.density
    private fun dp(value: Float): Float = value * density

    companion object {
        private const val DEFAULT_RADIUS_DP = 16f
        private const val DEFAULT_DOWNSCALE = 1f
    }
}
