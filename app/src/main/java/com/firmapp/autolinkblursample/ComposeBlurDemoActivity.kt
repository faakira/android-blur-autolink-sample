package com.firmapp.autolinkblursample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firmapp.blur.compose.backdropBlur
import com.firmapp.blur.compose.backdropSource
import com.firmapp.blur.compose.rememberBackdropBlurState

/** BlurView の Compose 版デモ。blurlib の Modifier.backdropBlur を使う。 */
class ComposeBlurDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BlurDemoScreen() }
    }
}

@Composable
private fun BlurDemoScreen() {
    val state = rememberBackdropBlurState()
    var radius by remember { mutableFloatStateOf(8f) }
    var circle by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF00B4A0), Color(0xFF5B5BD6)),
    )
    val white = Color.White

    Box(Modifier.fillMaxSize()) {

        // ▼ ぼかし対象のスクロール背景(テキスト・画像・図形)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .backdropSource(state),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(start = 24.dp, end = 24.dp, top = 88.dp, bottom = 220.dp),
            ) {
                Text("BLUR", color = white, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                Text(
                    "スクロールしても、背後のテキスト・画像・図形がそのままリアルタイムにぼけて映り込みます。",
                    color = white, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp),
                )
                GradientPhoto(SUNSET, Modifier.padding(top = 24.dp))
                Text(
                    "The quick brown fox jumps over the lazy dog. 吾輩は猫である。名前はまだ無い。",
                    color = white, fontSize = 15.sp, modifier = Modifier.padding(top = 12.dp),
                )
                Box(
                    Modifier
                        .padding(top = 32.dp)
                        .size(160.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .background(Color(0x66FFFFFF)),
                )
                GradientPhoto(OCEAN, Modifier.padding(top = 32.dp))
                Text(
                    "あいうえお かきくけこ さしすせそ 0123456789 ABCDEFG abcdefg #hashtag @mention",
                    color = white, fontSize = 15.sp, modifier = Modifier.padding(top = 12.dp),
                )
                Image(
                    painter = painterResource(R.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .size(120.dp)
                        .align(Alignment.End),
                )
                GradientPhoto(FOREST, Modifier.padding(top = 32.dp))
                Text(
                    "最後まで読み込んでも、カードの下を通るすべての要素がぼかされ続けます。",
                    color = white, fontSize = 16.sp, modifier = Modifier.padding(top = 24.dp),
                )
            }
        }

        // ▼ frosted ヘッダー
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(64.dp)
                .backdropBlur(state, radius = radius.dp, overlayColor = Color(0x66FFFFFF)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                "BlurView Demo (Compose)",
                color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp),
            )
        }

        // ▼ 中央カード
        Box(
            Modifier
                .align(Alignment.Center)
                .size(300.dp, 180.dp)
                .backdropBlur(
                    state,
                    radius = radius.dp,
                    overlayColor = Color(0x33FFFFFF),
                    shape = if (circle) CircleShape else RoundedCornerShape(24.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("BlurView", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "背景をリアルタイムにぼかし、その上に子ビューを重ねて表示します。",
                    color = Color.Black, fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp),
                )
            }
        }

        // ▼ frosted 操作パネル
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .backdropBlur(state, radius = radius.dp, overlayColor = Color(0x66FFFFFF))
                .padding(16.dp),
        ) {
            Text("ぼかしの強さ", color = Color.Black, fontWeight = FontWeight.Bold)
            Slider(value = radius, onValueChange = { radius = it }, valueRange = 1f..25f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = circle, onCheckedChange = { circle = it })
                Text("円形にクリップ", color = Color.Black, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

private val SUNSET = listOf(Color(0xFFFFB75E), Color(0xFFED8F03), Color(0xFFEA5455))
private val OCEAN = listOf(Color(0xFF2193B0), Color(0xFF4A54C9), Color(0xFF6DD5ED))
private val FOREST = listOf(Color(0xFF11998E), Color(0xFF38A169), Color(0xFFA8E063))

@Composable
private fun GradientPhoto(colors: List<Color>, modifier: Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors)),
    )
}
