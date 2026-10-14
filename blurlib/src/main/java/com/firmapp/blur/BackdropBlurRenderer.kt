package com.firmapp.blur

import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader

/**
 * 背景のスナップショットを [RenderNode] に記録し、[RenderEffect] で
 * ハードウェア支援のぼかしをかけて描画する内部レンダラ。
 *
 * 背景の描画そのものは [capture] に渡す drawBackdrop ラムダに委譲するため、
 * View(`view.draw(canvas)`)でも Compose(録画した Picture/RenderNode の再生)でも共有できる。
 *
 * RenderScript を使わず、API 31 以降の標準描画パイプラインのみで完結する。
 * 1フレームごとに [capture] → [drawInto] の順で呼び出す想定。
 */
internal class BackdropBlurRenderer {

    private val node = RenderNode("firmapp-backdrop-blur")
    private var appliedRadiusPx = Float.NaN

    /**
     * 背景の描画内容を、[width]×[height] の領域ぶんだけ切り出して記録し、
     * ぼかしエフェクトを適用する。背景の描画は [drawBackdrop] に委譲する。
     *
     * @param offsetX backdrop 座標系における切り出し開始位置(X)
     * @param offsetY backdrop 座標系における切り出し開始位置(Y)
     * @param radiusPx ぼかし半径(px)
     * @param downscale 取り込み時の縮小率(0.0〜1.0)
     * @param drawBackdrop 背景を渡された [Canvas] に描く処理(例: `view.draw(it)` / `it.drawPicture(pic)`)
     */
    fun capture(
        offsetX: Float,
        offsetY: Float,
        width: Int,
        height: Int,
        radiusPx: Float,
        downscale: Float,
        drawBackdrop: (Canvas) -> Unit,
    ) {
        val scale = downscale.coerceIn(0.05f, 1f)
        val nodeWidth = (width * scale).toInt().coerceAtLeast(1)
        val nodeHeight = (height * scale).toInt().coerceAtLeast(1)

        node.setPosition(0, 0, nodeWidth, nodeHeight)
        val recording = node.beginRecording(nodeWidth, nodeHeight)
        try {
            recording.scale(scale, scale)
            recording.translate(-offsetX, -offsetY)
            drawBackdrop(recording)
        } finally {
            node.endRecording()
        }

        // 縮小して記録しているぶん、ぼかし半径も同じ比率に合わせる
        val effectiveRadius = (radiusPx * scale).coerceAtLeast(0.1f)
        if (effectiveRadius != appliedRadiusPx) {
            node.setRenderEffect(
                RenderEffect.createBlurEffect(
                    effectiveRadius,
                    effectiveRadius,
                    Shader.TileMode.CLAMP,
                )
            )
            appliedRadiusPx = effectiveRadius
        }
    }

    /** 記録済みのぼかしノードを [canvas] 全面に引き伸ばして描画する。 */
    fun drawInto(canvas: Canvas, width: Int, height: Int) {
        if (!canvas.isHardwareAccelerated || !node.hasDisplayList()) return
        canvas.save()
        canvas.scale(width / node.width.toFloat(), height / node.height.toFloat())
        canvas.drawRenderNode(node)
        canvas.restore()
    }

    /** 保持しているネイティブリソースを解放する。 */
    fun release() {
        node.discardDisplayList()
        appliedRadiusPx = Float.NaN
    }
}
