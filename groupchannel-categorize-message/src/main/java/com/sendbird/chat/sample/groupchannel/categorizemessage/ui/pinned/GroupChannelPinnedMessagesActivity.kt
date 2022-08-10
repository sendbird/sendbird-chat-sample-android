package com.sendbird.chat.sample.groupchannel.categorizemessage.ui.pinned

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.PreviousMessageListQueryParams
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.categorizemessage.R
import com.sendbird.chat.sample.groupchannel.categorizemessage.databinding.ActivityPinnedMessageBinding
import com.sendbird.chat.sample.groupchannel.categorizemessage.ui.groupchannel.GroupChannelChatActivity

class GroupChannelPinnedMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinnedMessageBinding
    private lateinit var adapter: GroupChannelPinnedMessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelUrl = intent.getStringExtra(ChannelUrl) ?: run {
            finish()
            return
        }
        binding = ActivityPinnedMessageBinding.inflate(layoutInflater)
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
        adapter = GroupChannelPinnedMessagesAdapter()
        binding.listItem.apply {
            itemAnimator = null
            this.adapter = this@GroupChannelPinnedMessagesActivity.adapter
        }
    }

    private fun getChannel(channelUrl: String) {
        if (channelUrl.isBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }
        GroupChannel.getChannel(channelUrl) getChannelLabel@{ groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@getChannelLabel
            }
            if (groupChannel != null) {
                getPinnedMessages(groupChannel)
            }
        }
    }

    private fun getPinnedMessages(channel: GroupChannel) {
        val params = PreviousMessageListQueryParams(
            customTypesFilter = listOf(
                GroupChannelChatActivity.PINNED
            )
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
            Intent(context, GroupChannelPinnedMessagesActivity::class.java).apply {
                putExtra(ChannelUrl, channelUrl)
            }
    }
}