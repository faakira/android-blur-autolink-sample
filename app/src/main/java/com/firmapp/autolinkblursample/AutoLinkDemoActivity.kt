package com.firmapp.autolinkblursample

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.firmapp.autolink.LinkType
import com.firmapp.autolinkblursample.databinding.ActivityAutolinkDemoBinding

/** AutoLinkTextView のデモ。URL とハッシュタグを自動リンク化し、タップを受け取る。 */
class AutoLinkDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAutolinkDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.autoLinkText.apply {
            // URL・メール・電話・メンション・ハッシュタグを検出対象にする
            linkTypes = setOf(
                LinkType.Web,
                LinkType.Email,
                LinkType.Phone,
                LinkType.Mention,
                LinkType.Hashtag,
            )

            // onLinkClick は未設定。種別ごとの既定アクション
            // (URL→ブラウザ / 電話→電話アプリ / メール→メール / @・#→X)が起動する。

            // テキストをセット(セット時に自動でリンク化される)
            text = getString(R.string.autolink_sample_text)
        }

        val allTypes = setOf(
            LinkType.Web,
            LinkType.Email,
            LinkType.Phone,
            LinkType.Mention,
            LinkType.Hashtag,
        )

        // 入力中リアルタイムハイライトの EditText
        binding.autoLinkEdit.linkTypes = allTypes

        // Enter(完了)で同じ欄をリンク表示(タップで各アプリ起動)に切り替える。
        // 編集に戻すにはリンク以外をタップ(AutoLinkEditText 側で処理)。
        binding.autoLinkEdit.setOnEditorActionListener { editView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.autoLinkEdit.showLinks = true
                editView.clearFocus()
                getSystemService(InputMethodManager::class.java)
                    ?.hideSoftInputFromWindow(editView.windowToken, 0)
                true
            } else {
                false
            }
        }
    }
}
