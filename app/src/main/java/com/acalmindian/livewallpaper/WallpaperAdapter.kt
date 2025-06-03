package com.acalmindian.livewallpaper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class WallpaperAdapter(
    private val context: Context,
    private val wallpapers: List<WallpaperOption>,
    private val onApplyClick: (WallpaperOption) -> Unit
) : BaseAdapter() {

    override fun getCount() = wallpapers.size
    override fun getItem(position: Int) = wallpapers[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_wallpaper, parent, false)

        val thumbnail: ImageView = view.findViewById(R.id.imageThumbnail)

        val option = wallpapers[position]

        thumbnail.setImageResource(option.thumbnailResId)

        // Set wallpaper on thumbnail tap
        thumbnail.setOnClickListener {
            onApplyClick(option)
        }

        return view
    }
}

