package com.acalmindian.livewallpaper

import android.content.ComponentName

data class WallpaperOption(
    val name: String,
    val thumbnailResId: Int,
    val serviceComponent: ComponentName,
    val settingsActivityClass: Class<*>? = null // nullable, optional
)
