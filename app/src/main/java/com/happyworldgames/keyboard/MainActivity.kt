package com.happyworldgames.keyboard

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.happyworldgames.keyboard.databinding.MainBinding

class MainActivity : AppCompatActivity() {

    private val mainBinding by lazy { MainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        setSupportActionBar(mainBinding.toolbar)

        val controller = WindowInsetsControllerCompat(window, mainBinding.root)
        controller.isAppearanceLightStatusBars = false

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })

        mainBinding.turnOnKeyboardButton.setOnClickListener {
            startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS"))
        }
        mainBinding.selectKeyboardButton.setOnClickListener {
            val imeManager: InputMethodManager = applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imeManager.showInputMethodPicker()
        }
        mainBinding.settingLayoutButton.setOnClickListener {
            startActivity(Intent(this, SettingKeyBoardLayout::class.java))
        }
        mainBinding.learningButton.setOnClickListener {
            startActivity(Intent(this, LearningActivity::class.java))
        }
        mainBinding.privacyPolicyButton.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
        mainBinding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}