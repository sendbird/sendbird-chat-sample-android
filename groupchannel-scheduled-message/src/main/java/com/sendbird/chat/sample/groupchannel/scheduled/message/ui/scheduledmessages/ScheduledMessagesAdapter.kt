package com.sendbird.chat.sample.groupchannel.scheduled.message.ui.scheduledmessages

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.UserMessage
import com.sendbird.chat.module.ui.ChatBubbleImageSendView
import com.sendbird.chat.module.ui.ChatBubbleSendView
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ListItemChatImageScheduleBinding
import com.sendbird.chat.sample.groupchannel.databinding.ListItemChatScheduledBinding
import java.util.*
import kotlin.properties.Delegates

class ScheduledMessagesAdapter(
    private val sendNow: (BaseMessage) -> Unit,
    private val cancel: (BaseMessage) -> Unit,
    private val reschedule: (BaseMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var messages: List<BaseMessage> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_MESSAGE) {
            UserMessageViewHolder(
                ListItemChatScheduledBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            FileMessageViewHolder(
                ListItemChatImageScheduleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserMessageViewHolder) {
            holder.bindMessage(
                message = messages[position] as UserMessage,
                sendNow,
                cancel,
                reschedule
            )
        } else if (holder is FileMessageViewHolder) {
            holder.bindMessage(
                message = messages[position] as FileMessage,
                sendNow,
                cancel,
                reschedule
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position] is UserMessage) ITEM_MESSAGE else ITEM_FILE
    }

    override fun getItemCount(): Int = messages.size

    class UserMessageViewHolder(binding: ListItemChatScheduledBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val sendView: ChatBubbleSendView = binding.chatBubbleSend
        private val scheduledTime: TextView = binding.textviewTime
        fun bindMessage(
            message: UserMessage,
            sendNow: (BaseMessage) -> Unit,
            cancel: (BaseMessage) -> Unit,
            reschedule: (BaseMessage) -> Unit
        ) {
            sendView.setText(message.message)
            scheduledTime.text =
                message.scheduledInfo?.scheduledAt?.let { Date(it).toString() } ?: ""

            itemView.setOnClickListener { anchor ->
                PopupMenu(anchor.context, anchor).apply {
                    menuInflater.inflate(R.menu.scheduled_message_menu, menu)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.send_now -> sendNow(message)
                            R.id.cancel -> cancel(message)
                            R.id.reschedule -> reschedule(message)
                        }
                        true
                    }
                    show()
                }
            }
        }
    }

    class FileMessageViewHolder(binding: ListItemChatImageScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val sendView: ChatBubbleImageSendView = binding.chatBubbleImageSend
        private val scheduledTime: TextView = binding.textviewTime

        fun bindMessage(
            message: FileMessage,
            sendNow: (BaseMessage) -> Unit,
            cancel: (BaseMessage) -> Unit,
            reschedule: (BaseMessage) -> Unit
        ) {
            sendView.setImageUrl(message.url, message.plainUrl)
            scheduledTime.text =
                message.scheduledInfo?.scheduledAt?.let { Date(it).toString() } ?: ""

            itemView.setOnClickListener { anchor ->
                PopupMenu(anchor.context, anchor).apply {
                    menuInflater.inflate(R.menu.scheduled_message_menu, menu)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.send_now -> sendNow(message)
                            R.id.cancel -> cancel(message)
                            R.id.reschedule -> reschedule(message)
                        }
                        true
                    }
                    show()
                }
            }
        }
    }

    companion object {
        private const val ITEM_MESSAGE = 0
        private const val ITEM_FILE = 1
    }
}