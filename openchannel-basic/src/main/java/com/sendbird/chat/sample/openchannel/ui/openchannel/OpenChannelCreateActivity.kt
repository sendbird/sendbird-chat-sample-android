package com.sendbird.chat.sample.openchannel.ui.openchannel

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.OpenChannel
import com.sendbird.android.OpenChannelParams
import com.sendbird.android.SendBird
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.openchannel.R
import com.sendbird.chat.sample.openchannel.databinding.ActivityOpenChannelCreateBinding


class OpenChannelCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpenChannelCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenChannelCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.open_channel_create)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.buttonCreate.setOnClickListener {
            val channelName = binding.tagEdittextChannelName.getText()
            if (channelName.isBlank()) {
                showToast(R.string.channel_name_enter_msg)
                return@setOnClickListener
            }
            val params = OpenChannelParams().apply {
                setName(channelName)
                if (SendBird.getCurrentUser() != null) {
                    setOperators(listOf(SendBird.getCurrentUser()))
                }
            }
            OpenChannel.createChannel(params) { openChannel, e ->
                if (e != null || openChannel == null) {
                    showToast("${e.message}")
                    finish()
                    return@createChannel
                }
                val intent = Intent(
                    this@OpenChannelCreateActivity,
                    OpenChannelChatActivity::class.java
                )
                intent.putExtra(INTENT_KEY_CHANNEL_URL, openChannel.url)
                intent.putExtra(INTENT_KEY_CHANNEL_TITLE, openChannel.name)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}