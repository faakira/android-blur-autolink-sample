package com.firmapp.autolink

import androidx.annotation.ColorInt

/**
 * リンクの見た目設定。[LinkType] ごとに割り当てる。
 *
 * @property color        通常時の文字色
 * @property pressedColor 押下時の文字色(既定は [color] と同じ)
 * @property underline    下線を引くか
 * @property bold         太字にするか
 */
data class LinkStyle(
    @ColorInt val color: Int,
    @ColorInt val pressedColor: Int = color,
    val underline: Boolean = false,
    val bold: Boolean = false,
)
