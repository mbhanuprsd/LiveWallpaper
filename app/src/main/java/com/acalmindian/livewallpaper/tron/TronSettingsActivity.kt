package com.acalmindian.livewallpaper.tron

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.acalmindian.livewallpaper.R
import androidx.appcompat.app.AppCompatActivity

class TronSettingsActivity : AppCompatActivity() {

    private lateinit var lineColorButton: Button
    private lateinit var lineColorDisplay: TextView
    private lateinit var glowRadiusInput: EditText
    private lateinit var lineSpeedInput: EditText
    private lateinit var maxLinesInput: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    private var currentLineColorHex: String = ""

    companion object {
        // Use a distinct SharedPreferences name for Tron settings
        const val PREFS_NAME_TRON = "tron_wallpaper_prefs"

        const val KEY_LINE_COLOR_TRON = "line_color_tron"
        const val DEFAULT_LINE_COLOR_TRON = "#00FFFF" // Cyan

        const val KEY_GLOW_RADIUS_TRON = "glow_radius_tron"
        const val DEFAULT_GLOW_RADIUS_TRON = 10f

        const val KEY_LINE_SPEED_TRON = "line_speed_tron"
        const val DEFAULT_LINE_SPEED_TRON = 10f

        const val KEY_MAX_LINES_TRON = "max_lines_tron"
        const val DEFAULT_MAX_LINES_TRON = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tron_settings) // Link to the new layout file

        // Initialize UI elements
        lineColorButton = findViewById(R.id.line_color_button)
        lineColorDisplay = findViewById(R.id.line_color_display)
        glowRadiusInput = findViewById(R.id.glow_radius_input)
        lineSpeedInput = findViewById(R.id.line_speed_input)
        maxLinesInput = findViewById(R.id.max_lines_input)
        saveButton = findViewById(R.id.save_button)

        // Initialize SharedPreferences with the Tron-specific name
        sharedPreferences = getSharedPreferences(PREFS_NAME_TRON, MODE_PRIVATE)

        // Load and display current settings
        loadSettingsAndDisplay()

        // Set listener for color button
        lineColorButton.setOnClickListener {
            showColorPickerDialog()
        }

        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettingsAndDisplay() {
        currentLineColorHex = sharedPreferences.getString(KEY_LINE_COLOR_TRON, DEFAULT_LINE_COLOR_TRON) ?: DEFAULT_LINE_COLOR_TRON
        glowRadiusInput.setText(sharedPreferences.getFloat(KEY_GLOW_RADIUS_TRON, DEFAULT_GLOW_RADIUS_TRON).toString())
        lineSpeedInput.setText(sharedPreferences.getFloat(KEY_LINE_SPEED_TRON, DEFAULT_LINE_SPEED_TRON).toString())
        maxLinesInput.setText(sharedPreferences.getInt(KEY_MAX_LINES_TRON, DEFAULT_MAX_LINES_TRON).toString())

        updateColorDisplay()
    }

    private fun updateColorDisplay() {
        try {
            lineColorDisplay.setBackgroundColor(Color.parseColor(currentLineColorHex))
            lineColorDisplay.text = currentLineColorHex
            lineColorDisplay.setTextColor(getContrastColor(Color.parseColor(currentLineColorHex)))
        } catch (e: IllegalArgumentException) {
            lineColorDisplay.setBackgroundColor(Color.parseColor(DEFAULT_LINE_COLOR_TRON))
            lineColorDisplay.text = DEFAULT_LINE_COLOR_TRON
            lineColorDisplay.setTextColor(getContrastColor(Color.parseColor(DEFAULT_LINE_COLOR_TRON)))
        }
    }

    private fun getContrastColor(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val y = (0.299 * r + 0.587 * g + 0.114 * b) / 255
        return if (y > 0.5) Color.BLACK else Color.WHITE
    }

    private fun showColorPickerDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tron_color_picker, null) // Use new dialog layout
        val hexInput = dialogView.findViewById<EditText>(R.id.hex_color_input)
        val previewColorDisplay = dialogView.findViewById<TextView>(R.id.preview_color_display)

        hexInput.setText(currentLineColorHex)
        updatePreviewColor(currentLineColorHex, previewColorDisplay)

        hexInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreviewColor(s.toString(), previewColorDisplay)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        AlertDialog.Builder(this)
            .setTitle("Select Line Color")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val newColorHex = hexInput.text.toString().trim()
                if (isValidHexColor(newColorHex)) {
                    currentLineColorHex = newColorHex
                    updateColorDisplay()
                } else {
                    Toast.makeText(this, "Invalid Hex Color Code. Please use #RRGGBB format.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun updatePreviewColor(hex: String, previewTextView: TextView) {
        try {
            val color = Color.parseColor(hex)
            previewTextView.setBackgroundColor(color)
            previewTextView.text = hex
            previewTextView.setTextColor(getContrastColor(color))
        } catch (e: IllegalArgumentException) {
            previewTextView.setBackgroundColor(Color.GRAY)
            previewTextView.text = "Invalid"
            previewTextView.setTextColor(Color.WHITE)
        }
    }

    private fun saveSettings() {
        val editor = sharedPreferences.edit()
        var settingsChanged = false

        if (isValidHexColor(currentLineColorHex)) {
            editor.putString(KEY_LINE_COLOR_TRON, currentLineColorHex)
            settingsChanged = true
        } else {
            Toast.makeText(this, "Invalid Line Color Hex. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putString(KEY_LINE_COLOR_TRON, DEFAULT_LINE_COLOR_TRON)
            currentLineColorHex = DEFAULT_LINE_COLOR_TRON
            updateColorDisplay()
        }

        val glowRadiusStr = glowRadiusInput.text.toString()
        try {
            val glowRadius = glowRadiusStr.toFloat()
            if (glowRadius >= 0) {
                editor.putFloat(KEY_GLOW_RADIUS_TRON, glowRadius)
                settingsChanged = true
            } else {
                Toast.makeText(this, "Glow Radius cannot be negative. Reverting to default.", Toast.LENGTH_SHORT).show()
                editor.putFloat(KEY_GLOW_RADIUS_TRON, DEFAULT_GLOW_RADIUS_TRON)
                glowRadiusInput.setText(DEFAULT_GLOW_RADIUS_TRON.toString())
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Glow Radius. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putFloat(KEY_GLOW_RADIUS_TRON, DEFAULT_GLOW_RADIUS_TRON)
            glowRadiusInput.setText(DEFAULT_GLOW_RADIUS_TRON.toString())
        }

        val lineSpeedStr = lineSpeedInput.text.toString()
        try {
            val lineSpeed = lineSpeedStr.toFloat()
            if (lineSpeed > 0) {
                editor.putFloat(KEY_LINE_SPEED_TRON, lineSpeed)
                settingsChanged = true
            } else {
                Toast.makeText(this, "Line Speed must be a positive number. Reverting to default.", Toast.LENGTH_SHORT).show()
                editor.putFloat(KEY_LINE_SPEED_TRON, DEFAULT_LINE_SPEED_TRON)
                lineSpeedInput.setText(DEFAULT_LINE_SPEED_TRON.toString())
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Line Speed. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putFloat(KEY_LINE_SPEED_TRON, DEFAULT_LINE_SPEED_TRON)
            lineSpeedInput.setText(DEFAULT_LINE_SPEED_TRON.toString())
        }

        val maxLinesStr = maxLinesInput.text.toString()
        try {
            val maxLines = maxLinesStr.toInt()
            if (maxLines > 0) {
                editor.putInt(KEY_MAX_LINES_TRON, maxLines)
                settingsChanged = true
            } else {
                Toast.makeText(this, "Max Lines must be a positive number. Reverting to default.", Toast.LENGTH_SHORT).show()
                editor.putInt(KEY_MAX_LINES_TRON, DEFAULT_MAX_LINES_TRON)
                maxLinesInput.setText(DEFAULT_MAX_LINES_TRON.toString())
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Max Lines. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putInt(KEY_MAX_LINES_TRON, DEFAULT_MAX_LINES_TRON)
            maxLinesInput.setText(DEFAULT_MAX_LINES_TRON.toString())
        }

        editor.apply()

        if (settingsChanged) {
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "No valid settings were changed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidHexColor(colorString: String): Boolean {
        val hexPattern = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$")
        return hexPattern.matches(colorString)
    }
}
