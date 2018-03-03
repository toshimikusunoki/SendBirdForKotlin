package com.sendbird.android.sample.utils

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sendbird.android.sample.R

class PhotoViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)

        val url = intent.getStringExtra("url")
        val type = intent.getStringExtra("type")

        val imageView = findViewById<View>(R.id.main_image_view) as ImageView
        val progressBar = findViewById<View>(R.id.progress_bar) as ProgressBar


        progressBar.visibility = View.VISIBLE

        if (type != null && type.toLowerCase().contains("gif")) {
            ImageUtils.displayGifImageFromUrl(this, url, imageView, null, object : RequestListener<Any?> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Any?>?, isFirstResource: Boolean): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: Any?, model: Any?, target: Target<Any?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }
            })
        } else {
            ImageUtils.displayImageFromUrl(this, url, imageView, null, object : RequestListener<Any?> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Any?>?, isFirstResource: Boolean): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: Any?, model: Any?, target: Target<Any?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }
            })
        }
    }
}
