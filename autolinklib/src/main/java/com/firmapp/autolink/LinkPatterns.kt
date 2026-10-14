package com.firmapp.autolink

import android.util.Patterns
import java.util.regex.Pattern

/**
 * リンク検出に使う正規表現の定義。
 *
 * メンション・ハッシュタグは Unicode 文字プロパティ `\p{L}`(=あらゆる言語の文字)を
 * 使うことで、日本語・韓国語などマルチバイト文字にも素直に対応している。
 */
internal object LinkPatterns {

    /** 直前が行頭または空白であることを表すゼロ幅の先読み(可変長回避のため分割) */
    private const val BOUNDARY = "(?:^|(?<=\\s))"

    /** http(s):// または www. で始まる URL */
    val WEB_URL: Pattern = Pattern.compile(
        "(?:https?://|www\\.)[\\w\\-]+(?:\\.[\\w\\-]+)+(?:[/?#][^\\s]*)?",
        Pattern.CASE_INSENSITIVE,
    )

    /** メールアドレス(Android 標準定義を流用) */
    val EMAIL: Pattern = Patterns.EMAIL_ADDRESS

    /** 電話番号(Android 標準定義を流用。桁数は検出側で絞り込む) */
    val PHONE: Pattern = Patterns.PHONE

    /** @ユーザー名 */
    val MENTION: Pattern = Pattern.compile("$BOUNDARY@[\\p{L}\\p{N}_]+")

    /** #ハッシュタグ(日本語可) */
    val HASHTAG: Pattern = Pattern.compile("$BOUNDARY#[\\p{L}\\p{N}_]+")
}
