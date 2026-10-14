package com.firmapp.autolink.compose

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.firmapp.autolink.Link
import com.firmapp.autolink.LinkActions
import com.firmapp.autolink.LinkDetector
import com.firmapp.autolink.LinkStyle
import com.firmapp.autolink.LinkType

internal const val LINK_TAG = "autolink"

/** 既定の種別別スタイル([AutoLinkTextView] の初期値と同じ)。 */
fun defaultAutoLinkStyles(): Map<LinkType, LinkStyle> {
    val url = LinkStyle(0xFF1A73E8.toInt(), underline = true)
    val tagColor = 0xFF7C4DFF.toInt()
    return mapOf(
        LinkType.Web to url,
        LinkType.Email to url,
        LinkType.Phone to url,
        LinkType.Mention to LinkStyle(tagColor, bold = true),
        LinkType.Hashtag to LinkStyle(tagColor),
    )
}

/**
 * 本文中の URL・メール・電話・@メンション・#ハッシュタグを自動でリンク化して表示する、
 * [com.firmapp.autolink.AutoLinkTextView] の Compose 版。
 *
 * 検出は View 版と共通の `LinkDetector` を使用。リンクのタップは [onLinkClick] 未指定時、
 * 種別ごとの既定アクション([LinkActions]: ブラウザ/電話/メール/X)を起動する。
 *
 * @param style 本文のテキストスタイル(色など。テーマに合わせて渡すこと)
 */
@Composable
fun AutoLinkText(
    text: CharSequence,
    modifier: Modifier = Modifier,
    linkTypes: Set<LinkType> = DEFAULT_TYPES,
    linkStyles: Map<LinkType, LinkStyle> = defaultAutoLinkStyles(),
    style: TextStyle = TextStyle.Default,
    onLinkClick: ((Link) -> Unit)? = null,
) {
    val context = LocalContext.current
    val built = remember(text, linkTypes, linkStyles) {
        buildAutoLinkString(text, linkTypes, linkStyles)
    }
    ClickableText(
        text = built.annotated,
        modifier = modifier,
        style = style,
    ) { offset ->
        built.annotated.getStringAnnotations(LINK_TAG, offset, offset).firstOrNull()?.let { ann ->
            val link = built.links[ann.item.toInt()]
            if (onLinkClick != null) onLinkClick(link) else LinkActions.open(context, link)
        }
    }
}

/** [AutoLinkText] 用に組み立てた表示文字列と、注釈に対応する [Link] 一覧。 */
internal class AutoLinkString(val annotated: AnnotatedString, val links: List<Link>)

internal fun buildAutoLinkString(
    source: CharSequence,
    types: Set<LinkType>,
    styles: Map<LinkType, LinkStyle>,
): AutoLinkString {
    val text = source.toString()
    val matches = LinkDetector.detect(text, types)
    val links = ArrayList<Link>()
    val annotated = buildAnnotatedString {
        append(text)
        for (match in matches) {
            val style = styles[match.type] ?: continue
            addStyle(style.toSpanStyle(), match.start, match.end)
            addStringAnnotation(LINK_TAG, links.size.toString(), match.start, match.end)
            links += Link(match.type, match.raw, match.raw, match.start, match.end)
        }
    }
    return AutoLinkString(annotated, links)
}

internal fun LinkStyle.toSpanStyle() = SpanStyle(
    color = Color(color),
    fontWeight = if (bold) FontWeight.Bold else null,
    textDecoration = if (underline) TextDecoration.Underline else null,
)

internal val DEFAULT_TYPES = setOf(
    LinkType.Web,
    LinkType.Email,
    LinkType.Phone,
    LinkType.Mention,
    LinkType.Hashtag,
)
