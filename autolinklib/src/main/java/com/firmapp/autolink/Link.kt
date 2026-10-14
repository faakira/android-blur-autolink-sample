package com.firmapp.autolink

/**
 * 検出され、クリック可能になった 1 件のリンク。
 *
 * @property type     リンク種別
 * @property text     画面に表示されているテキスト(URL 変換後の値)
 * @property rawText  元の(変換前の)テキスト
 * @property start    表示テキスト中の開始インデックス
 * @property end      表示テキスト中の終了インデックス
 */
data class Link(
    val type: LinkType,
    val text: String,
    val rawText: String,
    val start: Int,
    val end: Int,
)
