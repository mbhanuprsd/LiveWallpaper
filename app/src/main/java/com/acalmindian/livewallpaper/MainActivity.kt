package com.acalmindian.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.acalmindian.livewallpaper.matrix.MatrixSettingsActivity
import com.acalmindian.livewallpaper.matrix.MatrixWallpaperService
import com.acalmindian.livewallpaper.tron.TronSettingsActivity
import com.acalmindian.livewallpaper.tron.TronWallpaperService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val grid: GridView = findViewById(R.id.wallpaperGrid)

        val wallpapers = listOf(
            WallpaperOption(
                name = "Matrix Rain",
                thumbnailResId = R.drawable.ic_matrix,
                serviceComponent = ComponentName(this, MatrixWallpaperService::class.java),
                settingsActivityClass = MatrixSettingsActivity::class.java
            ),
            WallpaperOption(
                name = "Tron",
                thumbnailResId = R.drawable.ic_tron,
                serviceComponent = ComponentName(this, TronWallpaperService::class.java),
                settingsActivityClass = TronSettingsActivity::class.java
            )
        )

        val adapter = WallpaperAdapter(
            this,
            wallpapers,
            onApplyClick = { option ->
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, option.serviceComponent)
                startActivity(intent)
            }
        )

        grid.adapter = adapter


        grid.setOnItemClickListener { _, _, position, _ ->
            val option = wallpapers[position]
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, option.serviceComponent)
            startActivity(intent)
        }
    }
}
