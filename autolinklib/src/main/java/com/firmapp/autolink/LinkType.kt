package com.firmapp.autolink

import java.util.regex.Pattern

/**
 * 検出対象のリンク種別。
 *
 * 組み込みの種別は [object] で表現し、任意の正規表現は [Custom] で指定する。
 */
sealed interface LinkType {

    /** この種別の検出に使う正規表現 */
    val pattern: Pattern

    /** http(s):// / www. で始まる URL */
    object Web : LinkType {
        override val pattern: Pattern get() = LinkPatterns.WEB_URL
    }

    /** メールアドレス */
    object Email : LinkType {
        override val pattern: Pattern get() = LinkPatterns.EMAIL
    }

    /** 電話番号 */
    object Phone : LinkType {
        override val pattern: Pattern get() = LinkPatterns.PHONE
    }

    /** @メンション */
    object Mention : LinkType {
        override val pattern: Pattern get() = LinkPatterns.MENTION
    }

    /** #ハッシュタグ */
    object Hashtag : LinkType {
        override val pattern: Pattern get() = LinkPatterns.HASHTAG
    }

    /** 任意の正規表現で検出するカスタム種別 */
    data class Custom(val regex: String) : LinkType {
        override val pattern: Pattern = Pattern.compile(regex)
    }
}
