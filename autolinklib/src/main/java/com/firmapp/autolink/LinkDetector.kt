package com.firmapp.autolink

/**
 * テキストから各 [LinkType] のマッチを洗い出し、範囲の重なりを解決する検出エンジン。
 */
internal object LinkDetector {

    private const val MIN_PHONE_DIGITS = 10
    private const val MAX_PHONE_DIGITS = 15

    /** 検出された 1 件のマッチ(元テキスト上の位置を保持) */
    data class Match(
        val type: LinkType,
        val start: Int,
        val end: Int,
        val raw: String,
    )

    /**
     * [text] から [types] に含まれる種別を検出し、開始位置順に並べて返す。
     * 範囲が重なる場合は、先に現れる(=開始が早く、同点なら長い)方を優先する。
     */
    fun detect(text: CharSequence, types: Set<LinkType>): List<Match> {
        val matches = ArrayList<Match>()
        for (type in types) {
            val matcher = type.pattern.matcher(text)
            while (matcher.find()) {
                val raw = matcher.group()
                if (type is LinkType.Phone && !isPlausiblePhone(raw)) continue
                matches += Match(type, matcher.start(), matcher.end(), raw)
            }
        }
        return resolveOverlaps(matches)
    }

    private fun isPlausiblePhone(raw: String): Boolean {
        val digits = raw.count(Char::isDigit)
        return digits in MIN_PHONE_DIGITS..MAX_PHONE_DIGITS
    }

    private fun resolveOverlaps(matches: List<Match>): List<Match> {
        val sorted = matches.sortedWith(compareBy({ it.start }, { -(it.end - it.start) }))
        val result = ArrayList<Match>(sorted.size)
        var consumedUntil = -1
        for (match in sorted) {
            if (match.start >= consumedUntil) {
                result += match
                consumedUntil = match.end
            }
        }
        return result
    }
}
