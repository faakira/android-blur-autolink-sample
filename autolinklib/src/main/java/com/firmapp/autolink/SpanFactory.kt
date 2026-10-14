package com.firmapp.autolink

import android.text.SpannableStringBuilder
import android.text.Spanned

/**
 * 検出結果をもとに、クリック可能なスパン付きの文字列を組み立てるファクトリ。
 *
 * URL は [transformUrl] で表示用テキストに変換できる(短縮表示など)。
 * 変換で文字数が変わっても、ここで一括構築するためインデックスのズレは起きない。
 */
internal object SpanFactory {

    fun build(
        source: CharSequence,
        matches: List<LinkDetector.Match>,
        styleOf: (LinkType) -> LinkStyle,
        transformUrl: (String) -> String,
        onClick: (Link) -> Unit,
    ): Spanned {
        val builder = SpannableStringBuilder()
        var cursor = 0

        for (match in matches) {
            // リンクの手前までの通常テキスト
            if (match.start > cursor) {
                builder.append(source, cursor, match.start)
            }

            val display = if (match.type is LinkType.Web) transformUrl(match.raw) else match.raw
            val start = builder.length
            builder.append(display)
            val end = builder.length

            val link = Link(match.type, display, match.raw, start, end)
            builder.setSpan(
                LinkSpan(link, styleOf(match.type), onClick),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            cursor = match.end
        }

        // 末尾の残りテキスト
        if (cursor < source.length) {
            builder.append(source, cursor, source.length)
        }
        return builder
    }
}
