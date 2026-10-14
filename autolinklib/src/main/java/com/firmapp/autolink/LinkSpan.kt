package com.firmapp.autolink

import android.graphics.Color
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

/**
 * 1 件のリンクに対応するクリック可能なスパン。
 * 押下状態([pressed])に応じて色を切り替える。
 */
internal class LinkSpan(
    val link: Link,
    private val style: LinkStyle,
    private val onClick: (Link) -> Unit,
) : ClickableSpan() {

    var pressed: Boolean = false

    override fun onClick(widget: View) = onClick(link)

    override fun updateDrawState(ds: TextPaint) {
        ds.color = if (pressed) style.pressedColor else style.color
        ds.isUnderlineText = style.underline
        if (style.bold) {
            ds.typeface = Typeface.create(ds.typeface, Typeface.BOLD)
        }
        ds.bgColor = Color.TRANSPARENT
    }
}
