package com.firmapp.autolinkblursample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firmapp.autolinkblursample.databinding.ActivityBlurDemoBinding

/** BlurView のデモ。背景コンテナをぼかし、半径と形状を操作できる。 */
class BlurDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBlurDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 中央カードをぼかし元に指定
        binding.blurView.apply {
            blurBehind(binding.backgroundContent)
            blurRadiusDp = binding.radiusSlider.value
            cornerRadiusDp = 24f
        }

        // ヘッダー・操作パネルも同じ背景をぼかす
        binding.headerBlur.blurBehind(binding.backgroundContent)
        binding.panelBlur.blurBehind(binding.backgroundContent)

        // ぼかし半径をスライダーで変更(全 BlurView に反映)
        binding.radiusSlider.addOnChangeListener { _, value, _ ->
            binding.blurView.blurRadiusDp = value
            binding.headerBlur.blurRadiusDp = value
            binding.panelBlur.blurRadiusDp = value
        }

        // 円形クリップの ON/OFF
        binding.circleSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.blurView.circle = isChecked
        }
    }
}
