package com.sendbird.chat.sample.openchannel.categorizemessages.ui.pinned

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.params.PreviousMessageListQueryParams
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.openchannel.categorizemessages.R
import com.sendbird.chat.sample.openchannel.categorizemessages.databinding.ActivityPinnedMessagesBinding
import com.sendbird.chat.sample.openchannel.categorizemessages.ui.openchannel.OpenChannelChatActivity.Companion.PinnedMessage

class OpenChannelPinnedMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinnedMessagesBinding
    private lateinit var adapter: OpenChannelPinnedMessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelUrl = intent.getStringExtra(ChannelUrl) ?: run {
            finish()
            return
        }
        binding = ActivityPinnedMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerview()
        getChannel(channelUrl)
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.pinned_messages)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerview() {
        adapter = OpenChannelPinnedMessagesAdapter()
        binding.listItem.apply {
            itemAnimator = null
            this.adapter = this@OpenChannelPinnedMessagesActivity.adapter
        }
    }

    private fun getChannel(channelUrl: String) {
        if (channelUrl.isBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }
        OpenChannel.getChannel(channelUrl) getChannelLabel@{ channel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@getChannelLabel
            }
            if (channel != null) {
                getPinnedMessages(channel)
            }
        }
    }

    private fun getPinnedMessages(channel: OpenChannel) {
        val params = PreviousMessageListQueryParams(
            customTypesFilter = listOf(PinnedMessage)
        )
        val pinnedMessagesQuery = channel.createPreviousMessageListQuery(params)
        pinnedMessagesQuery.load { messages, e ->
            if (e != null) {
                showToast("${e.message}")
                return@load
            }
            if (messages.isNullOrEmpty()) {
                showToast("No pinned messages")
                return@load
            }
            adapter.submitList(messages)
        }
    }

    companion object {
        private const val ChannelUrl = "channel_url"
        fun newIntent(context: Context, channelUrl: String): Intent =
            Intent(context, OpenChannelPinnedMessagesActivity::class.java).apply {
                putExtra(ChannelUrl, channelUrl)
            }
    }
}