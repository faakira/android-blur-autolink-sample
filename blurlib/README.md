# blurlib

背後のビューをリアルタイムにぼかす「すりガラス」ビュー。パッケージ `com.firmapp.blur`。

非推奨の RenderScript を使わず、`RenderNode` + `RenderEffect`（API 31+）のみで実装しています。

## 必要環境
`minSdk 31` / `compileSdk 34`

## 使い方
```kotlin
blurView.blurBehind(backgroundView)
blurView.blurRadiusDp = 20f
blurView.cornerRadiusDp = 16f
blurView.circle = false
blurView.overlayColor = 0x55FFFFFF
```

XML 属性: `blurRadius` / `blurOverlayColor` / `blurCornerRadius` / `blurCircle` / `blurDownscale`

> `blurBehind()` に渡すビューは、その `BlurView` を子孫に含めないこと（描画が再帰します）。

## ライセンス
[MIT License](../LICENSE) © firm-app
