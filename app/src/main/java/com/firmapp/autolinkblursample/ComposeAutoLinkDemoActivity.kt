package com.firmapp.autolinkblursample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firmapp.autolink.LinkType
import com.firmapp.autolink.compose.AutoLinkText
import com.firmapp.autolink.compose.AutoLinkTextField

/** AutoLinkText の Compose 版デモ。autolinklib の AutoLinkText を使う。 */
class ComposeAutoLinkDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AutoLinkScreen() }
    }
}

@Composable
private fun AutoLinkScreen() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                Text(
                    "AutoLinkText (Compose)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "URL・メール・電話・@メンション・#ハッシュタグを自動でリンク化します。タップすると対応アプリが開きます。",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                val allTypes = setOf(
                    LinkType.Web,
                    LinkType.Email,
                    LinkType.Phone,
                    LinkType.Mention,
                    LinkType.Hashtag,
                )
                val bodyStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                AutoLinkText(
                    text = stringResource(R.string.autolink_sample_text),
                    linkTypes = allTypes,
                    style = bodyStyle,
                )

                Spacer(Modifier.height(40.dp))
                Text(
                    "AutoLinkTextField (Compose)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "入力中にリアルタイムで色付けし、Enter(完了)で同じ欄をタップ可能なリンク表示に切り替えます。",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                var input by remember { mutableStateOf(TextFieldValue()) }
                val underlineColor = MaterialTheme.colorScheme.outline
                AutoLinkTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val stroke = 1.dp.toPx()
                            drawLine(
                                color = underlineColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = stroke,
                            )
                        }
                        .padding(bottom = 8.dp),
                    linkTypes = allTypes,
                    textStyle = bodyStyle,
                    placeholder = {
                        Text(
                            stringResource(R.string.autolink_edit_hint),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                    },
                )
            }
        }
    }
}
