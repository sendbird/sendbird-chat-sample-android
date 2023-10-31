package com.sendbird.chat.sample.groupchannel.categorizemessages.categorize

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.PreviousMessageListQueryParams
import com.sendbird.chat.sample.groupchannel.categorizemessages.R
import com.sendbird.chat.sample.groupchannel.categorizemessages.databinding.ActivityCategorizeMessageBinding
import com.sendbird.chat.sample.groupchannel.categorizemessages.groupchannel.GroupChannelChatActivity
import com.sendbird.chat.sample.groupchannel.categorizemessages.util.showToast

class GroupChannelCategorizeMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategorizeMessageBinding
    private lateinit var adapter: GroupChannelCategorizeMessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelUrl = intent.getStringExtra(ChannelUrl) ?: run {
            finish()
            return
        }
        binding = ActivityCategorizeMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerview()
        getChannel(channelUrl)
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.categorize_messages)
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
        adapter = GroupChannelCategorizeMessagesAdapter()
        binding.listItem.apply {
            itemAnimator = null
            this.adapter = this@GroupChannelCategorizeMessagesActivity.adapter
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
                getCategorizeMessages(groupChannel)
            }
        }
    }

    // Search messages by custom type
    private fun getCategorizeMessages(channel: GroupChannel) {
        val params = PreviousMessageListQueryParams(
            customTypesFilter = listOf(
                GroupChannelChatActivity.CATEGORIZE
            )
        )
        val categorizeMessagesQuery = channel.createPreviousMessageListQuery(params)
        categorizeMessagesQuery.load { messages, e ->
            if (e != null) {
                showToast("${e.message}")
                return@load
            }
            if (messages.isNullOrEmpty()) {
                showToast("No categorize messages")
                return@load
            }
            adapter.submitList(messages)
        }
    }

    companion object {
        private const val ChannelUrl = "channel_url"
        fun newIntent(context: Context, channelUrl: String): Intent =
            Intent(context, GroupChannelCategorizeMessagesActivity::class.java).apply {
                putExtra(ChannelUrl, channelUrl)
            }
    }
}