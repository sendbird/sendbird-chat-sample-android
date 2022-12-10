package com.sendbird.chat.sample.groupchannel.scheduled.message.ui.scheduledmessages

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.UserMessage
import com.sendbird.android.params.ScheduledFileMessageUpdateParams
import com.sendbird.android.params.ScheduledMessageListQueryParams
import com.sendbird.android.params.ScheduledUserMessageUpdateParams
import com.sendbird.android.scheduled.ScheduledStatus
import com.sendbird.android.scheduled.query.ScheduledMessageListQuery
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.TextUtils
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityScheduledMessagesBinding
import com.sendbird.chat.sample.groupchannel.scheduled.message.ui.openDateTimeSelector

class ScheduledMessagesActivity : AppCompatActivity() {

    private var channelUrl: String = ""
    private var channelTitle: String = ""
    private var currentGroupChannel: GroupChannel? = null

    private lateinit var binding: ActivityScheduledMessagesBinding

    private val scheduledMessagesAdapter = ScheduledMessagesAdapter(
        sendNow = this::sendNow,
        cancel = this::cancelMessage,
        reschedule = this::rescheduleMessage
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduledMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL) ?: ""
        channelTitle = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_TITLE) ?: ""
        init()
        getChannel(channelUrl)
        getScheduledMessages()
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

    private fun init() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.recyclerviewChat.adapter = scheduledMessagesAdapter
    }

    private fun getChannel(channelUrl: String?) {
        if (channelUrl.isNullOrBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }
        GroupChannel.getChannel(
            channelUrl
        ) getChannelLabel@{ groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@getChannelLabel
            }
            if (groupChannel != null) {
                currentGroupChannel = groupChannel
                setChannelTitle()
            }
        }
    }

    private fun setChannelTitle() {
        val currentChannel = currentGroupChannel
        if (channelTitle == TextUtils.CHANNEL_DEFAULT_NAME && currentChannel != null) {
            binding.toolbar.title =
                "Scheduled messages " + TextUtils.getGroupChannelTitle(currentChannel)
        } else {
            binding.toolbar.title = "Scheduled messages$channelTitle"
        }
    }

    private fun getScheduledMessages() {
        val params = ScheduledMessageListQueryParams(
            channelUrl = channelUrl,
            scheduledStatus = listOf(ScheduledStatus.PENDING)
        )
        val query = SendbirdChat.createScheduledMessageListQuery(params)

        val listToAdd = mutableListOf<BaseMessage>()
        getMessages(query, listToAdd) { messages ->
            scheduledMessagesAdapter.messages = messages
        }
    }

    private fun getMessages(
        query: ScheduledMessageListQuery,
        listToAdd: MutableList<BaseMessage>,
        onMessageLoadingFinished: (List<BaseMessage>) -> Unit
    ) {
        if (query.hasNext) {
            query.next { messages, error ->
                if (error != null) {
                    Log.e("ScheduledMessages", error.message ?: "error getting messages")
                    onMessageLoadingFinished(listToAdd)
                    return@next
                }
                listToAdd.addAll(messages?.takeIf { it.isNotEmpty() } ?: emptyList())
                getMessages(query, listToAdd, onMessageLoadingFinished)
            }
        } else {
            onMessageLoadingFinished(listToAdd)
        }
    }

    private fun sendNow(message: BaseMessage) {
        val channel = currentGroupChannel ?: return
        val scheduleMessageID = message.scheduledInfo?.scheduledMessageId ?: return
        channel.sendScheduledMessageNow(scheduleMessageID) {
            if (it != null) {
                Log.e("ScheduledMessages", it.message ?: "Error sending now message")
                return@sendScheduledMessageNow
            }
            getScheduledMessages()
        }
    }

    private fun cancelMessage(message: BaseMessage) {
        val channel = currentGroupChannel ?: return
        val scheduleMessageID = message.scheduledInfo?.scheduledMessageId ?: return
        channel.cancelScheduledMessage(scheduleMessageID) {
            if (it != null) {
                Log.e("ScheduledMessages", it.message ?: "Error canceling message")
                return@cancelScheduledMessage
            }
            getScheduledMessages()
        }
    }

    private fun rescheduleMessage(message: BaseMessage) {
        val channel = currentGroupChannel ?: return
        val scheduleMessageID = message.scheduledInfo?.scheduledMessageId ?: return
        openDateTimeSelector { selectedTime ->
            when (message) {
                is UserMessage -> {
                    val params = ScheduledUserMessageUpdateParams().apply {
                        this.scheduledAt = selectedTime
                    }
                    channel.updateScheduledUserMessage(
                        scheduleMessageID,
                        params
                    ) { _, exception ->
                        if (exception != null) {
                            Log.e(
                                "ScheduledMessages",
                                exception.message ?: "Error updating message"
                            )
                            return@updateScheduledUserMessage
                        }
                        getScheduledMessages()
                    }
                }
                is FileMessage -> {
                    val params = ScheduledFileMessageUpdateParams().apply {
                        this.scheduledAt = selectedTime
                    }
                    channel.updateScheduledFileMessage(
                        scheduleMessageID,
                        params
                    ) { _, exception ->
                        if (exception != null) {
                            Log.e(
                                "ScheduledMessages",
                                exception.message ?: "Error updating message"
                            )
                            return@updateScheduledFileMessage
                        }
                        getScheduledMessages()
                    }
                }
            }
        }
    }
}
