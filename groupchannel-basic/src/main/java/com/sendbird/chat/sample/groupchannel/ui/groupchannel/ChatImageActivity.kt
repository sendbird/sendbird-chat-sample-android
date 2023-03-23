package com.sendbird.chat.sample.groupchannel.ui.groupchannel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import coil.load
import com.sendbird.chat.module.R

class ChatImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_image)
        val url = intent.extras?.getString("image_url")
        findViewById<ImageView>(R.id.image_view).load(url)
    }
}
