package com.acalmindian.livewallpaper.matrix

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper // Import Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.graphics.toColorInt

class MatrixWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MatrixWallpaperEngine()
    }

    inner class MatrixWallpaperEngine : Engine(), SharedPreferences.OnSharedPreferenceChangeListener {
        private val renderer = MatrixRainRenderer() // Instantiate your renderer
        private var isVisible = false
        // Update: Use Handler with Looper.getMainLooper() to avoid deprecation
        private val handler = Handler(Looper.getMainLooper())

        // Reference to SharedPreferences
        private lateinit var sharedPreferences: SharedPreferences

        // Runnable to draw frames
        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            // Initialize SharedPreferences and register listener
            sharedPreferences = getSharedPreferences(MatrixSettingsActivity.PREFS_NAME, MODE_PRIVATE)
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)

            // No need to call loadAndApplySettings here, as surface dimensions aren't guaranteed yet.
            // onSurfaceCreated or onSurfaceChanged will handle initial setup with dimensions.
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            // Initial setup for the renderer. onSurfaceChanged will typically follow this
            // with the definitive dimensions, but we can provide an initial value.
            loadAndApplySettings(holder.surfaceFrame.width(), holder.surfaceFrame.height())
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            // This is the most reliable place to get the correct width and height.
            loadAndApplySettings(width, height)
            drawFrame() // Draw immediately after size change
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            isVisible = false
            handler.removeCallbacks(drawRunner) // Stop drawing when surface is destroyed
            // Unregister SharedPreferences listener
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                handler.post(drawRunner) // Start drawing when visible
            } else {
                handler.removeCallbacks(drawRunner) // Stop drawing when not visible
            }
        }

        // Called when SharedPreferences change
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            // Reload all settings and re-initialize renderer with current surface dimensions
            loadAndApplySettings(surfaceHolder.surfaceFrame.width(), surfaceHolder.surfaceFrame.height())
            // Request a redraw to apply changes immediately
            if (isVisible) {
                drawFrame()
            }
        }

        // Helper function to load all settings and apply them to the renderer
        // Now requires width and height as mandatory parameters.
        private fun loadAndApplySettings(width: Int, height: Int) {
            // Ensure width and height are valid before initializing the renderer
            if (width <= 0 || height <= 0) {
                // Log an error or handle this case if necessary, but typically onSurfaceChanged
                // will provide valid dimensions.
                return
            }

            val chars = sharedPreferences.getString(MatrixSettingsActivity.KEY_MATRIX_CHARS, MatrixSettingsActivity.DEFAULT_MATRIX_CHARS) ?: MatrixSettingsActivity.DEFAULT_MATRIX_CHARS
            val fadeSpeed = sharedPreferences.getInt(MatrixSettingsActivity.KEY_FADE_SPEED, MatrixSettingsActivity.DEFAULT_FADE_SPEED)
            val textSize = sharedPreferences.getFloat(MatrixSettingsActivity.KEY_TEXT_SIZE, MatrixSettingsActivity.DEFAULT_TEXT_SIZE)
            val brightColorHex = sharedPreferences.getString(MatrixSettingsActivity.KEY_BRIGHT_COLOR, MatrixSettingsActivity.DEFAULT_BRIGHT_COLOR) ?: MatrixSettingsActivity.DEFAULT_BRIGHT_COLOR
            val darkColorHex = sharedPreferences.getString(MatrixSettingsActivity.KEY_DARK_COLOR, MatrixSettingsActivity.DEFAULT_DARK_COLOR) ?: MatrixSettingsActivity.DEFAULT_DARK_COLOR

            val brightColor = try {
                brightColorHex.toColorInt() } catch (e: IllegalArgumentException) {
                MatrixSettingsActivity.DEFAULT_BRIGHT_COLOR.toColorInt() }
            val darkColor = try {
                darkColorHex.toColorInt() } catch (e: IllegalArgumentException) {
                MatrixSettingsActivity.DEFAULT_DARK_COLOR.toColorInt() }

            renderer.setCharacters(chars)
            renderer.initialize(width, height, fadeSpeed, textSize, brightColor, darkColor)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderer.draw(canvas) // Call your renderer's draw method
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            // Schedule the next frame if visible
            handler.removeCallbacks(drawRunner)
            if (isVisible) {
                handler.postDelayed(drawRunner, 30) // Adjust delay for desired frame rate (e.g., 30ms for ~33 FPS)
            }
        }
    }
}
