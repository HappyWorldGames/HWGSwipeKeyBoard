package com.happyworldgames.keyboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.happyworldgames.keyboard.databinding.MainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private val mainBinding by lazy { MainBinding.inflate(layoutInflater) }

    private val exportSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { destinationUri ->
            exportSettings(destinationUri)
        }
    }

    private val importSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            importSettings(sourceUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        val controller = WindowInsetsControllerCompat(window, mainBinding.root)
        controller.isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(mainBinding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

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
        mainBinding.exportSettingsButton.setOnClickListener {
            exportSettingsLauncher.launch("hwg_keyboard_settings.json")
        }

        mainBinding.importSettingsButton.setOnClickListener {
            importSettingsLauncher.launch("application/json")
        }
    }

    private fun exportSettings(destinationUri: Uri) {
        try {
            val saveFile = File(filesDir, "saveKeyBoardLayout.json")
            if (!saveFile.exists()) {
                Toast.makeText(this, "Файл настроек не найден", Toast.LENGTH_SHORT).show()
                return
            }

            contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                saveFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(this, "Настройки экспортированы", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка экспорта", e)
            Toast.makeText(this, "Ошибка экспорта", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importSettings(sourceUri: Uri) {
        try {
            val saveFile = File(filesDir, "saveKeyBoardLayout.json")

            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                saveFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(this, "Настройки импортированы. Перезапустите клавиатуру.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка импорта", e)
            Toast.makeText(this, "Ошибка импорта", Toast.LENGTH_SHORT).show()
        }
    }
}