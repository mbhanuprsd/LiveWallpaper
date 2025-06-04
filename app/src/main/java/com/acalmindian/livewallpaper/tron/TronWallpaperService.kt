package com.acalmindian.livewallpaper.tron

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class TronWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return TronWallpaperEngine()
    }

    inner class TronWallpaperEngine : Engine(), SharedPreferences.OnSharedPreferenceChangeListener {
        private val renderer = TronRenderer()
        private var isVisible = false
        private val handler = Handler(Looper.getMainLooper())

        private lateinit var sharedPreferences: SharedPreferences

        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            // Use a distinct SharedPreferences name for Tron settings
            sharedPreferences = getSharedPreferences(TronSettingsActivity.PREFS_NAME_TRON, MODE_PRIVATE)
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            loadAndApplySettings(holder.surfaceFrame.width(), holder.surfaceFrame.height())
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            loadAndApplySettings(width, height)
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            isVisible = false
            handler.removeCallbacks(drawRunner)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            loadAndApplySettings(surfaceHolder.surfaceFrame.width(), surfaceHolder.surfaceFrame.height())
            if (isVisible) {
                drawFrame()
            }
        }

        private fun loadAndApplySettings(width: Int, height: Int) {
            if (width <= 0 || height <= 0) {
                return
            }

            val lineColorHex = sharedPreferences.getString(TronSettingsActivity.KEY_LINE_COLOR_TRON, TronSettingsActivity.DEFAULT_LINE_COLOR_TRON) ?: TronSettingsActivity.DEFAULT_LINE_COLOR_TRON
            val glowRadius = sharedPreferences.getFloat(TronSettingsActivity.KEY_GLOW_RADIUS_TRON, TronSettingsActivity.DEFAULT_GLOW_RADIUS_TRON)
            val lineSpeed = sharedPreferences.getFloat(TronSettingsActivity.KEY_LINE_SPEED_TRON, TronSettingsActivity.DEFAULT_LINE_SPEED_TRON)
            val maxLines = sharedPreferences.getInt(TronSettingsActivity.KEY_MAX_LINES_TRON, TronSettingsActivity.DEFAULT_MAX_LINES_TRON)

            val lineColor = try { Color.parseColor(lineColorHex) } catch (e: IllegalArgumentException) { Color.parseColor(TronSettingsActivity.DEFAULT_LINE_COLOR_TRON) }

            renderer.initialize(width, height, lineColor, glowRadius, lineSpeed, maxLines)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderer.draw(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            handler.removeCallbacks(drawRunner)
            if (isVisible) {
                handler.postDelayed(drawRunner, 30) // Adjust delay for desired frame rate
            }
        }
    }
}
