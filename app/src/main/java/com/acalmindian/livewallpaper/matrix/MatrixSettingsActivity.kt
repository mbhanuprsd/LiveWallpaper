package com.acalmindian.livewallpaper.matrix

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.acalmindian.livewallpaper.R
import androidx.core.graphics.toColorInt

class MatrixSettingsActivity : AppCompatActivity() {

    private lateinit var characterInput: EditText
    private lateinit var fadeSpeedInput: EditText
    private lateinit var textSizeInput: EditText

    private lateinit var brightColorButton: Button
    private lateinit var darkColorButton: Button
    private lateinit var brightColorDisplay: TextView // To show the selected color
    private lateinit var darkColorDisplay: TextView   // To show the selected color

    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    // Current color values (will be updated by user input)
    private var currentBrightColorHex: String = ""
    private var currentDarkColorHex: String = ""

    // Define keys and default values for storing settings in SharedPreferences
    companion object {
        const val PREFS_NAME = "matrix_wallpaper_prefs"
        const val KEY_MATRIX_CHARS = "matrix_characters"
        const val DEFAULT_MATRIX_CHARS = "アァカサタナハマヤラワガザダバパイィキシチニヒミリヰギジヂビピウゥクスツヌフムユルグズヅブプエェケセテネヘメレヱゲゼデベペオォコソトノホモヨロヲゴゾドボポヴッン"

        const val KEY_FADE_SPEED = "fade_speed"
        const val DEFAULT_FADE_SPEED = 8

        const val KEY_TEXT_SIZE = "text_size"
        const val DEFAULT_TEXT_SIZE = 30f // Corresponds to the default in renderer

        const val KEY_BRIGHT_COLOR = "bright_color"
        const val DEFAULT_BRIGHT_COLOR = "#00FF00" // Bright Green

        const val KEY_DARK_COLOR = "dark_color"
        const val DEFAULT_DARK_COLOR = "#006600" // Dark Green
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matrix_settings) // Link to the layout file

        // Initialize UI elements
        characterInput = findViewById(R.id.character_input)
        fadeSpeedInput = findViewById(R.id.fade_speed_input)
        textSizeInput = findViewById(R.id.text_size_input)

        brightColorButton = findViewById(R.id.bright_color_button)
        darkColorButton = findViewById(R.id.dark_color_button)
        brightColorDisplay = findViewById(R.id.bright_color_display)
        darkColorDisplay = findViewById(R.id.dark_color_display)

        saveButton = findViewById(R.id.save_button)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Load and display current settings
        loadSettingsAndDisplay()

        // Set listeners for color buttons
        brightColorButton.setOnClickListener {
            showColorPickerDialog(true) // true for bright color
        }
        darkColorButton.setOnClickListener {
            showColorPickerDialog(false) // false for dark color
        }

        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettingsAndDisplay() {
        characterInput.setText(sharedPreferences.getString(KEY_MATRIX_CHARS, DEFAULT_MATRIX_CHARS))
        fadeSpeedInput.setText(sharedPreferences.getInt(KEY_FADE_SPEED, DEFAULT_FADE_SPEED).toString())
        textSizeInput.setText(sharedPreferences.getFloat(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE).toString())

        currentBrightColorHex = sharedPreferences.getString(KEY_BRIGHT_COLOR, DEFAULT_BRIGHT_COLOR) ?: DEFAULT_BRIGHT_COLOR
        currentDarkColorHex = sharedPreferences.getString(KEY_DARK_COLOR, DEFAULT_DARK_COLOR) ?: DEFAULT_DARK_COLOR

        updateColorDisplays()
    }

    private fun updateColorDisplays() {
        // Update bright color display
        try {
            brightColorDisplay.setBackgroundColor(currentBrightColorHex.toColorInt())
            brightColorDisplay.text = currentBrightColorHex
            brightColorDisplay.setTextColor(getContrastColor(currentBrightColorHex.toColorInt())) // Set text color for contrast
        } catch (e: IllegalArgumentException) {
            brightColorDisplay.setBackgroundColor(DEFAULT_BRIGHT_COLOR.toColorInt())
            brightColorDisplay.text = DEFAULT_BRIGHT_COLOR
            brightColorDisplay.setTextColor(getContrastColor(DEFAULT_BRIGHT_COLOR.toColorInt()))
        }

        // Update dark color display
        try {
            darkColorDisplay.setBackgroundColor(currentDarkColorHex.toColorInt())
            darkColorDisplay.text = currentDarkColorHex
            darkColorDisplay.setTextColor(getContrastColor(currentDarkColorHex.toColorInt())) // Set text color for contrast
        } catch (e: IllegalArgumentException) {
            darkColorDisplay.setBackgroundColor(DEFAULT_DARK_COLOR.toColorInt())
            darkColorDisplay.text = DEFAULT_DARK_COLOR
            darkColorDisplay.setTextColor(getContrastColor(DEFAULT_DARK_COLOR.toColorInt()))
        }
    }

    // Helper to determine if black or white text is better for contrast
    private fun getContrastColor(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val y = (0.299 * r + 0.587 * g + 0.114 * b) / 255 // Luminance calculation
        return if (y > 0.5) Color.BLACK else Color.WHITE
    }

    private fun showColorPickerDialog(isBrightColor: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null)
        val hexInput = dialogView.findViewById<EditText>(R.id.hex_color_input)
        val previewColorDisplay = dialogView.findViewById<TextView>(R.id.preview_color_display)

        val initialColor = if (isBrightColor) currentBrightColorHex else currentDarkColorHex
        hexInput.setText(initialColor)
        updatePreviewColor(initialColor, previewColorDisplay)

        // Add a TextWatcher to update preview as user types (optional, but good UX)
        hexInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreviewColor(s.toString(), previewColorDisplay)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })


        AlertDialog.Builder(this)
            .setTitle(if (isBrightColor) "Select Bright Color" else "Select Dark Color")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val newColorHex = hexInput.text.toString().trim()
                if (isValidHexColor(newColorHex)) {
                    if (isBrightColor) {
                        currentBrightColorHex = newColorHex
                    } else {
                        currentDarkColorHex = newColorHex
                    }
                    updateColorDisplays()
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
            val color = hex.toColorInt()
            previewTextView.setBackgroundColor(color)
            previewTextView.text = hex
            previewTextView.setTextColor(getContrastColor(color))
        } catch (e: Exception) {
            previewTextView.setBackgroundColor(Color.GRAY) // Indicate invalid input
            previewTextView.text = getString(R.string.invalid)
            previewTextView.setTextColor(Color.WHITE)
        }
    }


    private fun saveSettings() {
        val editor = sharedPreferences.edit()
        var settingsChanged = false // Flag to track if any setting was successfully updated

        // Save characters
        val newChars = characterInput.text.toString().trim()
        if (newChars.isNotBlank()) {
            editor.putString(KEY_MATRIX_CHARS, newChars)
            settingsChanged = true
        } else {
            Toast.makeText(this, "Characters cannot be empty. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putString(KEY_MATRIX_CHARS, DEFAULT_MATRIX_CHARS)
            characterInput.setText(DEFAULT_MATRIX_CHARS) // Update UI to show default
        }

        // Save fade speed
        val fadeSpeedStr = fadeSpeedInput.text.toString()
        try {
            val fadeSpeed = fadeSpeedStr.toInt()
            if (fadeSpeed > 0) { // Ensure fade speed is positive
                editor.putInt(KEY_FADE_SPEED, fadeSpeed)
                settingsChanged = true
            } else {
                Toast.makeText(this, "Fade Speed must be a positive number. Reverting to default.", Toast.LENGTH_SHORT).show()
                editor.putInt(KEY_FADE_SPEED, DEFAULT_FADE_SPEED)
                fadeSpeedInput.setText(DEFAULT_FADE_SPEED.toString())
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Fade Speed. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putInt(KEY_FADE_SPEED, DEFAULT_FADE_SPEED)
            fadeSpeedInput.setText(DEFAULT_FADE_SPEED.toString())
        }

        // Save text size
        val textSizeStr = textSizeInput.text.toString()
        try {
            val textSize = textSizeStr.toFloat()
            if (textSize > 0) { // Ensure text size is positive
                editor.putFloat(KEY_TEXT_SIZE, textSize)
                settingsChanged = true
            } else {
                Toast.makeText(this, "Character Size must be a positive number. Reverting to default.", Toast.LENGTH_SHORT).show()
                editor.putFloat(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE)
                textSizeInput.setText(DEFAULT_TEXT_SIZE.toString())
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid Character Size. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putFloat(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE)
            textSizeInput.setText(DEFAULT_TEXT_SIZE.toString())
        }

        // Save bright color (already updated in currentBrightColorHex by dialog)
        if (isValidHexColor(currentBrightColorHex)) {
            editor.putString(KEY_BRIGHT_COLOR, currentBrightColorHex)
            settingsChanged = true
        } else {
            Toast.makeText(this, "Invalid Bright Color Hex. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putString(KEY_BRIGHT_COLOR, DEFAULT_BRIGHT_COLOR)
            currentBrightColorHex = DEFAULT_BRIGHT_COLOR // Ensure internal state is consistent
            updateColorDisplays() // Update UI to show default
        }

        // Save dark color (already updated in currentDarkColorHex by dialog)
        if (isValidHexColor(currentDarkColorHex)) {
            editor.putString(KEY_DARK_COLOR, currentDarkColorHex)
            settingsChanged = true
        } else {
            Toast.makeText(this, "Invalid Dark Color Hex. Reverting to default.", Toast.LENGTH_SHORT).show()
            editor.putString(KEY_DARK_COLOR, DEFAULT_DARK_COLOR)
            currentDarkColorHex = DEFAULT_DARK_COLOR // Ensure internal state is consistent
            updateColorDisplays() // Update UI to show default
        }

        editor.apply() // Apply all changes

        if (settingsChanged) {
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
            finish() // Close the settings activity and return to the previous screen
        } else {
            Toast.makeText(this, "No valid settings were changed.", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to validate hex color strings
    private fun isValidHexColor(colorString: String): Boolean {
        // Basic validation: starts with # and has 6 or 8 hex characters
        val hexPattern = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$")
        return hexPattern.matches(colorString)
    }
}
