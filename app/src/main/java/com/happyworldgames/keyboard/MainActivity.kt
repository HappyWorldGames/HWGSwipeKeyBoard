package com.happyworldgames.keyboard

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.happyworldgames.keyboard.databinding.MainBinding

class MainActivity : AppCompatActivity() {

    private val mainBinding by lazy { MainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        mainBinding.drawOverButton.setOnClickListener {
            if (Build.VERSION.SDK_INT > 23 /*&& !Settings.canDrawOverlays(this)*/) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                ActivityCompat.startActivityForResult(this, intent, 1, null)
            }
        }
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
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}