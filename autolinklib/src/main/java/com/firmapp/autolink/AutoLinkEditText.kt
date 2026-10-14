package com.firmapp.autolink

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.ArrowKeyMovementMethod
import android.text.method.KeyListener
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.withStyledAttributes

/**
 * 入力中の本文から URL・メール・電話番号・@メンション・#ハッシュタグをリアルタイムに検出して
 * 色付け(ハイライト)する [AppCompatEditText]。
 *
 * [showLinks] を true にすると、同じ欄のままリンクを**タップ可能**な表示に切り替わり、
 * リンクをタップすると種別ごとのアプリ([LinkActions])が起動する。リンク以外をタップすると
 * 自動で編集モードへ戻る。確定(Enter)で表示に切り替える、といった使い方を想定。
 *
 * 使い方:
 * ```
 * edit.linkTypes = setOf(LinkType.Web, LinkType.Email, LinkType.Hashtag)
 * edit.setOnEditorActionListener { v, id, _ ->
 *     if (id == EditorInfo.IME_ACTION_DONE) { edit.showLinks = true; true } else false
 * }
 * ```
 */
class AutoLinkEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyleAttr) {

    /** 検出対象の種別。変更すると現在の入力を再ハイライトする。 */
    var linkTypes: Set<LinkType> = setOf(LinkType.Web, LinkType.Email, LinkType.Mention, LinkType.Hashtag)
        set(value) {
            field = value
            refresh()
        }

    /**
     * true でリンクをタップ可能な表示(読み取り専用)に、false で通常の編集に切り替える。
     * 表示モードでリンク以外をタップすると自動で編集モードへ戻る。
     */
    var showLinks: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) {
                savedKeyListener = keyListener
                keyListener = null // 読み取り専用化(IME も出なくなる)
                isCursorVisible = false
                movementMethod = PressAwareLinkMovementMethod()
            } else {
                keyListener = savedKeyListener
                isCursorVisible = true
                movementMethod = ArrowKeyMovementMethod.getInstance()
            }
            refresh()
        }

    private val styles = linkedMapOf<LinkType, LinkStyle>()
    private var savedKeyListener: KeyListener? = null

    /** 再入防止(ハイライト適用中のテキスト変更通知を無視する) */
    private var highlighting = false

    init {
        var linkColor = DEFAULT_LINK_COLOR
        var tagColor = DEFAULT_TAG_COLOR
        var underline = true

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.AutoLinkTextView) {
                linkColor = getColor(R.styleable.AutoLinkTextView_linkColor, linkColor)
                tagColor = getColor(R.styleable.AutoLinkTextView_tagColor, tagColor)
                underline = getBoolean(R.styleable.AutoLinkTextView_linkUnderline, underline)
            }
        }

        val urlStyle = LinkStyle(linkColor, underline = underline)
        styles[LinkType.Web] = urlStyle
        styles[LinkType.Email] = urlStyle
        styles[LinkType.Phone] = urlStyle
        styles[LinkType.Mention] = LinkStyle(tagColor, bold = true)
        styles[LinkType.Hashtag] = LinkStyle(tagColor)

        highlightColor = Color.TRANSPARENT

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (highlighting) return
                refresh()
            }
        })
    }

    /** 指定種別の見た目を上書きする。 */
    fun setLinkStyle(type: LinkType, style: LinkStyle) = apply {
        styles[type] = style
        refresh()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 表示モードでリンク以外をタップしたら編集モードへ戻す
        if (showLinks &&
            event.actionMasked == MotionEvent.ACTION_UP &&
            linkSpanUnder(event) == null
        ) {
            showLinks = false
            setSelection(text?.length ?: 0)
            requestFocus()
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun linkSpanUnder(event: MotionEvent): LinkSpan? {
        val layout = layout ?: return null
        val buffer = text as? Spannable ?: return null
        val x = event.x.toInt() - totalPaddingLeft + scrollX
        val y = event.y.toInt() - totalPaddingTop + scrollY
        val line = layout.getLineForVertical(y)
        if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)) return null
        val offset = layout.getOffsetForHorizontal(line, x.toFloat())
        return buffer.getSpans(offset, offset, LinkSpan::class.java).firstOrNull()
    }

    /** 現在の入力を再検出し、[showLinks] に応じて色スパン/クリック可能スパンを貼り直す。 */
    private fun refresh() {
        val editable = text ?: return
        if (highlighting) return
        highlighting = true
        try {
            // 以前に付けたものだけ除去(IME の変換装飾などは残す)
            editable.getSpans(0, editable.length, HighlightSpan::class.java)
                .forEach { editable.removeSpan(it) }
            editable.getSpans(0, editable.length, LinkSpan::class.java)
                .forEach { editable.removeSpan(it) }

            if (linkTypes.isNotEmpty() && editable.isNotEmpty()) {
                for (match in LinkDetector.detect(editable, linkTypes)) {
                    val style = styles[match.type] ?: continue
                    val span: Any = if (showLinks) {
                        val link = Link(match.type, match.raw, match.raw, match.start, match.end)
                        LinkSpan(link, style) { LinkActions.open(context, it) }
                    } else {
                        HighlightSpan(style)
                    }
                    editable.setSpan(span, match.start, match.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        } finally {
            highlighting = false
        }
    }

    /** 編集中の色付け用スパン(クリック不可)。自前のものだけ識別・除去するマーカも兼ねる。 */
    private class HighlightSpan(private val style: LinkStyle) : CharacterStyle() {
        override fun updateDrawState(tp: TextPaint) {
            tp.color = style.color
            tp.isUnderlineText = style.underline
            if (style.bold) {
                tp.typeface = Typeface.create(tp.typeface, Typeface.BOLD)
            }
        }
    }

    private companion object {
        val DEFAULT_LINK_COLOR = Color.parseColor("#1A73E8")
        val DEFAULT_TAG_COLOR = Color.parseColor("#7C4DFF")
    }
}
