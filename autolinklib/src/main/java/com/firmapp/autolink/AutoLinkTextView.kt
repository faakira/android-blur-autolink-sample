package com.firmapp.autolink

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.withStyledAttributes

/**
 * 本文中の URL・メール・電話番号・@メンション・#ハッシュタグを自動でリンク化し、
 * タップを受け取れる [AppCompatTextView]。
 *
 * 使い方:
 * ```
 * autoLinkTextView.linkTypes = setOf(LinkType.Web, LinkType.Hashtag)
 * autoLinkTextView.onLinkClick { link -> /* link.rawText などを利用 */ }
 * autoLinkTextView.text = "……"
 * ```
 */
class AutoLinkTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /** 検出対象の種別。変更すると現在のテキストを再処理する。 */
    var linkTypes: Set<LinkType> = setOf(LinkType.Web, LinkType.Hashtag)
        set(value) {
            field = value
            applyLinks()
        }

    private val styles = linkedMapOf<LinkType, LinkStyle>()
    private var onLinkClick: ((Link) -> Unit)? = null
    private var urlTransform: ((String) -> String)? = null

    /** リンク化前の元テキスト。再処理のために保持する。 */
    private var rawSource: CharSequence = ""

    init {
        movementMethod = PressAwareLinkMovementMethod()
        highlightColor = Color.TRANSPARENT

        // 既定スタイル
        var linkColor = DEFAULT_LINK_COLOR
        var tagColor = DEFAULT_TAG_COLOR
        var pressedColor = DEFAULT_PRESSED_COLOR
        var underline = true

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.AutoLinkTextView) {
                linkColor = getColor(R.styleable.AutoLinkTextView_linkColor, linkColor)
                tagColor = getColor(R.styleable.AutoLinkTextView_tagColor, tagColor)
                pressedColor = getColor(R.styleable.AutoLinkTextView_pressedLinkColor, pressedColor)
                underline = getBoolean(R.styleable.AutoLinkTextView_linkUnderline, underline)
            }
        }

        val urlStyle = LinkStyle(linkColor, pressedColor, underline = underline)
        val tagStyle = LinkStyle(tagColor, pressedColor)
        styles[LinkType.Web] = urlStyle
        styles[LinkType.Email] = urlStyle
        styles[LinkType.Phone] = urlStyle
        styles[LinkType.Mention] = LinkStyle(tagColor, pressedColor, bold = true)
        styles[LinkType.Hashtag] = tagStyle
    }

    /** 指定種別の見た目を上書きする。 */
    fun setLinkStyle(type: LinkType, style: LinkStyle) = apply {
        styles[type] = style
        applyLinks()
    }

    /** リンクタップ時のコールバックを設定する。 */
    fun onLinkClick(block: (Link) -> Unit) = apply {
        onLinkClick = block
    }

    /** URL を表示用テキストへ変換する処理を設定する(短縮表示など)。 */
    fun transformUrl(block: (String) -> String) = apply {
        urlTransform = block
        applyLinks()
    }

    override fun setText(text: CharSequence?, type: BufferType) {
        // AppCompatTextView の親コンストラクタからも呼ばれる。その時点では
        // プロパティ初期化前(linkTypes が未代入)なので、素通しして NPE を避ける。
        @Suppress("SENSELESS_COMPARISON")
        val notReady = linkTypes == null
        rawSource = text ?: ""
        if (notReady || rawSource.isEmpty() || linkTypes.isEmpty()) {
            super.setText(text, type)
            return
        }
        applyLinks()
    }

    private fun applyLinks() {
        if (rawSource.isEmpty() || linkTypes.isEmpty()) return
        val matches = LinkDetector.detect(rawSource, linkTypes)
        val spanned = SpanFactory.build(
            source = rawSource,
            matches = matches,
            styleOf = { styles[it] ?: LinkStyle(currentTextColor) },
            transformUrl = { urlTransform?.invoke(it) ?: it },
            onClick = { link ->
                // onLinkClick 未設定なら種別ごとの既定アクション(ブラウザ/電話/X など)を起動
                val handler = onLinkClick
                if (handler != null) handler(link) else LinkActions.open(context, link)
            },
        )
        super.setText(spanned, BufferType.SPANNABLE)
    }

    private companion object {
        val DEFAULT_LINK_COLOR = Color.parseColor("#1A73E8")
        val DEFAULT_TAG_COLOR = Color.parseColor("#7C4DFF")
        val DEFAULT_PRESSED_COLOR = Color.LTGRAY
    }
}
