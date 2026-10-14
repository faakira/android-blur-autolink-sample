package com.firmapp.autolinkblursample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firmapp.autolinkblursample.databinding.ActivityMainBinding

/** デモの入り口。各ライブラリのデモ画面へ遷移する。 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBlur.setOnClickListener {
            startActivity(Intent(this, BlurDemoActivity::class.java))
        }
        binding.btnBlurCompose.setOnClickListener {
            startActivity(Intent(this, ComposeBlurDemoActivity::class.java))
        }
        binding.btnAutoLink.setOnClickListener {
            startActivity(Intent(this, AutoLinkDemoActivity::class.java))
        }
        binding.btnAutoLinkCompose.setOnClickListener {
            startActivity(Intent(this, ComposeAutoLinkDemoActivity::class.java))
        }
    }
}
