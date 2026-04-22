package com.happyworldgames.keyboard

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.happyworldgames.keyboard.databinding.ActivitySettingsBinding
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val controller = WindowInsetsControllerCompat(window, binding.root)
        controller.isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        val sharedPreferences = getSharedPreferences("keyboard_settings", MODE_PRIVATE)

        binding.checkboxBackspace.isChecked = sharedPreferences.getBoolean("show_backspace", true)
        binding.checkboxSpace.isChecked = sharedPreferences.getBoolean("show_space", true)
        binding.checkboxLayoutSwitch.isChecked = sharedPreferences.getBoolean("show_layout_switch", true)
        binding.checkboxShift.isChecked = sharedPreferences.getBoolean("show_shift", true)
        binding.checkboxSymbolsOnKeys.isChecked = sharedPreferences.getBoolean("show_symbols_on_keys", true)
        binding.checkboxVibration.isChecked = sharedPreferences.getBoolean("enable_vibration", true)
        binding.checkboxVibrationDifferent.isChecked = sharedPreferences.getBoolean("vibration_different", false)
        binding.seekbarVibrationStrength.progress = sharedPreferences.getInt("vibration_strength", 150)
        binding.seekbarVibrationDuration.progress = sharedPreferences.getInt("vibration_duration", 30)
        binding.seekbarKeyboardSize.progress = sharedPreferences.getInt("keyboard_scale", 100)

        binding.checkboxBackspace.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("show_backspace", isChecked) }
        }
        binding.checkboxSpace.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("show_space", isChecked) }
        }
        binding.checkboxLayoutSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("show_layout_switch", isChecked) }
        }
        binding.checkboxShift.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("show_shift", isChecked) }
        }
        binding.checkboxSymbolsOnKeys.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("show_symbols_on_keys", isChecked) }
        }
        binding.checkboxVibration.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("enable_vibration", isChecked) }
        }
        binding.checkboxVibrationDifferent.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("vibration_different", isChecked) }
        }

        binding.seekbarVibrationStrength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPreferences.edit { putInt("vibration_strength", progress) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekbarVibrationDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPreferences.edit { putInt("vibration_duration", progress) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekbarKeyboardSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Ограничим минимальный размер 50%
                val scale = if (progress < 50) 50 else progress
                sharedPreferences.edit { putInt("keyboard_scale", scale) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.resetSettingsButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.reset_settings_title))
                .setMessage(getString(R.string.reset_settings_confirm))
                .setPositiveButton(getString(R.string.reset_settings_button)) { _, _ ->
                    resetToDefaults()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        binding.exportSettingsButton.setOnClickListener {
            exportSettingsLauncher.launch("hwg_keyboard_settings.json")
        }

        binding.importSettingsButton.setOnClickListener {
            importSettingsLauncher.launch("application/json")
        }
    }

    private fun resetToDefaults() {
        val sharedPreferences = getSharedPreferences("keyboard_settings", MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean("show_backspace", true)
            putBoolean("show_space", true)
            putBoolean("show_layout_switch", true)
            putBoolean("show_shift", true)
            putBoolean("show_symbols_on_keys", true)
            putBoolean("enable_vibration", true)
            putBoolean("vibration_different", false)
            putInt("vibration_strength", 100) // Стандартное значение (из 255)
            putInt("vibration_duration", 20)  // Стандартная короткая вибрация (мс)
            putInt("keyboard_scale", 100)
        }

        // Обновляем UI
        binding.checkboxBackspace.isChecked = true
        binding.checkboxSpace.isChecked = true
        binding.checkboxLayoutSwitch.isChecked = true
        binding.checkboxShift.isChecked = true
        binding.checkboxSymbolsOnKeys.isChecked = true
        binding.checkboxVibration.isChecked = true
        binding.checkboxVibrationDifferent.isChecked = false
        binding.seekbarVibrationStrength.progress = 100
        binding.seekbarVibrationDuration.progress = 20
        binding.seekbarKeyboardSize.progress = 100

        Toast.makeText(this, getString(R.string.settings_reset_toast), Toast.LENGTH_SHORT).show()
    }

    private fun exportSettings(destinationUri: Uri) {
        try {
            val saveFile = File(filesDir, "keyboard_layouts.json")
            if (!saveFile.exists()) {
                Toast.makeText(this, getString(R.string.settings_not_found), Toast.LENGTH_SHORT).show()
                return
            }

            contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                saveFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(this, getString(R.string.settings_exported), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Export error", e)
            Toast.makeText(this, getString(R.string.error_exporting), Toast.LENGTH_SHORT).show()
        }
    }

    private fun importSettings(sourceUri: Uri) {
        try {
            val saveFile = File(filesDir, "keyboard_layouts.json")

            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                saveFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(this, getString(R.string.settings_imported), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Import error", e)
            Toast.makeText(this, getString(R.string.error_importing), Toast.LENGTH_SHORT).show()
        }
    }
}