package com.firmapp.blur.compose

import android.graphics.Picture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.firmapp.blur.BackdropBlurRenderer

/**
 * Compose 版の背景ぼかしで、ぼかし元([backdropSource])と
 * ぼかすビュー([backdropBlur])の間で共有する状態。
 *
 * [rememberBackdropBlurState] で生成する。内部的に [BackdropBlurRenderer]（View 版と共通の
 * ぼかしエンジン）へ渡す背景スナップショット([Picture])と、座標・フレーム同期を保持する。
 */
@Stable
class BackdropBlurState {
    internal val picture = Picture()
    internal var sourcePosition by mutableStateOf(Offset.Zero)
    internal var frame by mutableStateOf(0)
}

/**
 * [BackdropBlurState] を生成し、毎フレーム再描画を駆動する。
 */
@Composable
fun rememberBackdropBlurState(): BackdropBlurState {
    val state = remember { BackdropBlurState() }
    LaunchedEffect(state) {
        while (true) {
            withFrameNanos { }
            state.frame++
        }
    }
    return state
}

/**
 * このコンポーザブルをぼかし元の背景として指定する。
 * 描画内容を毎フレーム [Picture] に記録し、[backdropBlur] がそれをぼかして使う。
 *
 * スクロールする背景をぼかす場合は、スクロールコンテナ自体ではなく、
 * その中で実際に移動するコンテンツにこの修飾子を付けること。
 */
fun Modifier.backdropSource(state: BackdropBlurState): Modifier = this
    .onGloballyPositioned { state.sourcePosition = it.positionInWindow() }
    .drawWithCache {
        onDrawWithContent {
            state.frame // 毎フレーム再記録するため読む
            val w = size.width.toInt()
            val h = size.height.toInt()
            if (w > 0 && h > 0) {
                // 中身を Picture に記録してから、それを画面にも描く
                val recording = Canvas(state.picture.beginRecording(w, h))
                draw(this, layoutDirection, recording, size) {
                    this@onDrawWithContent.drawContent()
                }
                state.picture.endRecording()
                drawIntoCanvas { it.nativeCanvas.drawPicture(state.picture) }
            } else {
                drawContent()
            }
        }
    }

/**
 * このコンポーザブルの**背後**にある [backdropSource] の内容をリアルタイムにぼかして敷く。
 * すりガラス風のヘッダー／カード／パネルを作れる。
 *
 * @param radius ぼかしの強さ
 * @param overlayColor ぼかしの上に重ねる色(半透明の白など)。[Color.Transparent] で無効
 * @param shape クリップ形状(角丸・円など)
 * @param downscale 取り込み時の縮小率(0.05〜1.0)。小さいほど高速だが粗い
 */
fun Modifier.backdropBlur(
    state: BackdropBlurState,
    radius: Dp = 16.dp,
    overlayColor: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    downscale: Float = 1f,
): Modifier = composed {
    val renderer = remember { BackdropBlurRenderer() }
    val density = LocalDensity.current
    var position by remember { mutableStateOf(Offset.Zero) }
    DisposableEffect(renderer) {
        onDispose { renderer.release() }
    }
    this
        .onGloballyPositioned { position = it.positionInWindow() }
        .clip(shape)
        .drawBehind {
            state.frame // 毎フレーム再描画するため読む
            val w = size.width.toInt()
            val h = size.height.toInt()
            if (w <= 0 || h <= 0 || state.picture.width <= 0) return@drawBehind
            val offsetX = position.x - state.sourcePosition.x
            val offsetY = position.y - state.sourcePosition.y
            val radiusPx = with(density) { radius.toPx() }
            renderer.capture(offsetX, offsetY, w, h, radiusPx, downscale) { canvas ->
                canvas.drawPicture(state.picture)
            }
            drawIntoCanvas { renderer.drawInto(it.nativeCanvas, w, h) }
            if (overlayColor != Color.Transparent) {
                drawRect(overlayColor)
            }
        }
}
