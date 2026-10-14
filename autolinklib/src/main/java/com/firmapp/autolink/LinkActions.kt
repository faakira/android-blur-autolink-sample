package com.firmapp.autolink

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * リンク種別ごとの既定アクション(アプリ起動)。
 *
 * - [LinkType.Web]     : ブラウザで URL を開く
 * - [LinkType.Email]   : メールアプリを起動(mailto:)
 * - [LinkType.Phone]   : 電話アプリを起動(tel:)
 * - [LinkType.Mention] : X(旧 Twitter)のプロフィールを開く
 * - [LinkType.Hashtag] : X のハッシュタグ検索を開く
 *
 * [AutoLinkTextView] は `onLinkClick` が未設定のとき、これを既定の挙動として呼ぶ。
 */
object LinkActions {

    /**
     * [link] の種別に応じたアプリを起動する。
     * @return 起動できたら true、対応アプリが無い等で起動できなければ false
     */
    fun open(context: Context, link: Link): Boolean {
        val intent = intentFor(link) ?: return false
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    /** [link] の種別に応じた起動 [Intent] を組み立てる。未対応なら null。 */
    fun intentFor(link: Link): Intent? = when (link.type) {
        LinkType.Web -> view(normalizeUrl(link.rawText))
        LinkType.Email -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${link.rawText}"))
        LinkType.Phone -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:${link.rawText}"))
        LinkType.Mention -> view("https://x.com/${link.rawText.removePrefix("@")}")
        LinkType.Hashtag ->
            view("https://x.com/hashtag/${Uri.encode(link.rawText.removePrefix("#"))}")
        is LinkType.Custom -> null
    }

    private fun view(url: String) = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    /** スキームが無い URL(例: www.example.com)に https:// を補う。 */
    private fun normalizeUrl(raw: String): String =
        if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"
}
