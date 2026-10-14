package com.firmapp.autolink

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView

/**
 * リンクの押下状態を [LinkSpan.pressed] に反映しつつクリックを処理する [LinkMovementMethod]。
 *
 * リンク上を押していないタッチは親(スクロールなど)に委ねるため、
 * その場合は [super] に処理を委譲する。
 */
internal class PressAwareLinkMovementMethod : LinkMovementMethod() {

    private var pressedSpan: LinkSpan? = null

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val span = findSpan(widget, buffer, event)
                if (span != null) {
                    pressedSpan = span.apply { pressed = true }
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(span),
                        buffer.getSpanEnd(span),
                    )
                    widget.invalidate()
                    return true
                }
                return super.onTouchEvent(widget, buffer, event)
            }

            MotionEvent.ACTION_MOVE -> {
                val current = pressedSpan ?: return super.onTouchEvent(widget, buffer, event)
                // 指がリンクから外れたら押下状態を解除
                if (findSpan(widget, buffer, event) !== current) {
                    clearPress(widget, buffer)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val span = pressedSpan ?: return super.onTouchEvent(widget, buffer, event)
                clearPress(widget, buffer)
                span.onClick(widget)
                return true
            }

            else -> {
                if (pressedSpan != null) {
                    clearPress(widget, buffer)
                    return true
                }
                return super.onTouchEvent(widget, buffer, event)
            }
        }
    }

    private fun clearPress(widget: TextView, buffer: Spannable) {
        pressedSpan?.pressed = false
        pressedSpan = null
        Selection.removeSelection(buffer)
        widget.invalidate()
    }

    private fun findSpan(widget: TextView, buffer: Spannable, event: MotionEvent): LinkSpan? {
        val layout = widget.layout ?: return null
        val x = event.x.toInt() - widget.totalPaddingLeft + widget.scrollX
        val y = event.y.toInt() - widget.totalPaddingTop + widget.scrollY
        val line = layout.getLineForVertical(y)
        if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)) return null
        val offset = layout.getOffsetForHorizontal(line, x.toFloat())
        return buffer.getSpans(offset, offset, LinkSpan::class.java).firstOrNull()
    }
}
