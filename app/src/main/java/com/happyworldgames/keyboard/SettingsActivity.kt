package com.happyworldgames.keyboard

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

        binding.exportSettingsButton.setOnClickListener {
            exportSettingsLauncher.launch("hwg_keyboard_settings.json")
        }

        binding.importSettingsButton.setOnClickListener {
            importSettingsLauncher.launch("application/json")
        }
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