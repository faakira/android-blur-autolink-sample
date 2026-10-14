# autolinklib

本文中の URL・メール・電話番号・@メンション・#ハッシュタグを自動でリンク化し、
タップを受け取れる `TextView`。パッケージ `com.firmapp.autolink`。

## 必要環境
`minSdk 31` / `compileSdk 34`

## 使い方
```kotlin
autoLinkTextView.linkTypes = setOf(LinkType.Web, LinkType.Mention, LinkType.Hashtag)
autoLinkTextView.onLinkClick { link -> /* link.type / link.rawText */ }
autoLinkTextView.transformUrl { url -> url.removePrefix("https://") }
autoLinkTextView.text = "……"
```

- 種別: `sealed interface LinkType`（`Web` / `Email` / `Phone` / `Mention` / `Hashtag` / `Custom`）
- ハッシュタグ・メンションは `\p{L}` により日本語などマルチバイト文字に対応
- 見た目は `setLinkStyle(type, LinkStyle(...))` で種別ごとに設定

XML 属性: `linkColor` / `tagColor` / `pressedLinkColor` / `linkUnderline`

## ライセンス
[MIT License](../LICENSE) © firm-app
