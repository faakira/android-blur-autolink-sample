package com.firmapp.autolink.compose

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.firmapp.autolink.LinkActions
import com.firmapp.autolink.LinkDetector
import com.firmapp.autolink.LinkStyle
import com.firmapp.autolink.LinkType

/**
 * 入力中にリンクをリアルタイム色付けし、確定(Done)で同じ欄をタップ可能なリンク表示へ切り替える、
 * [com.firmapp.autolink.AutoLinkEditText] の Compose 版。
 *
 * 編集中は [VisualTransformation] で色付けのみ、確定後は [ClickableText] でリンクを表示して
 * タップで種別ごとのアプリ([LinkActions])を起動する。リンク以外をタップすると編集へ戻る。
 *
 * @param placeholder 空のときに表示する内容(ヒント)
 */
@Composable
fun AutoLinkTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    linkTypes: Set<LinkType> = DEFAULT_TYPES,
    linkStyles: Map<LinkType, LinkStyle> = defaultAutoLinkStyles(),
    textStyle: TextStyle = TextStyle.Default,
    placeholder: @Composable (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf(true) }

    if (editing) {
        val transformation = remember(linkTypes, linkStyles) {
            AutoLinkVisualTransformation(linkTypes, linkStyles)
        }
        val focusRequester = remember { FocusRequester() }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.focusRequester(focusRequester),
            textStyle = textStyle,
            singleLine = true,
            visualTransformation = transformation,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { editing = false }),
            decorationBox = { innerTextField ->
                if (value.text.isEmpty()) placeholder?.invoke()
                innerTextField()
            },
        )
        // 編集モードに入るたびフォーカス(＝キーボード表示)する
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    } else {
        val built = remember(value.text, linkTypes, linkStyles) {
            buildAutoLinkString(value.text, linkTypes, linkStyles)
        }
        ClickableText(
            text = built.annotated,
            modifier = modifier,
            style = textStyle,
        ) { offset ->
            val annotation = built.annotated
                .getStringAnnotations(LINK_TAG, offset, offset).firstOrNull()
            if (annotation != null) {
                LinkActions.open(context, built.links[annotation.item.toInt()])
            } else {
                editing = true // リンク以外をタップ → 編集へ戻る
            }
        }
    }
}

/** 入力中の色付け用。文字数は変えないので [OffsetMapping.Identity]。 */
private class AutoLinkVisualTransformation(
    private val linkTypes: Set<LinkType>,
    private val linkStyles: Map<LinkType, LinkStyle>,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text.text)
            for (match in LinkDetector.detect(text.text, linkTypes)) {
                val style = linkStyles[match.type] ?: continue
                addStyle(style.toSpanStyle(), match.start, match.end)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
