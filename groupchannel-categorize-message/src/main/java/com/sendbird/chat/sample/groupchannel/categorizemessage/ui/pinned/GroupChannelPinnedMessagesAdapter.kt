package com.sendbird.chat.sample.groupchannel.categorizemessage.ui.pinned

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.chat.module.utils.toTime
import com.sendbird.chat.sample.groupchannel.categorizemessage.databinding.ListItemChatPinnedImageBinding
import com.sendbird.chat.sample.groupchannel.categorizemessage.databinding.ListItemChatPinnedMessageBinding

class GroupChannelPinnedMessagesAdapter :
    ListAdapter<BaseMessage, RecyclerView.ViewHolder>(diffCallback) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is FileMessage) {
            VIEW_TYPE_IMAGE
        } else {
            VIEW_TYPE_MESSAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_IMAGE) {
            ImageViewHolder(
                ListItemChatPinnedImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            MessageViewHolder(
                ListItemChatPinnedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> holder.bind(getItem(position) as FileMessage)
            is MessageViewHolder -> holder.bind(getItem(position))
        }
    }

    inner class ImageViewHolder(private val binding: ListItemChatPinnedImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: FileMessage) {
            binding.chatBubbleImage.setImageUrl(message.url, message.plainUrl)
            binding.dateTagView.setMillisecond(message.createdAt)
            binding.textviewTime.text = message.createdAt.toTime()
            binding.dateTagView.setMillisecond(message.createdAt)
        }

    }

    inner class MessageViewHolder(private val binding: ListItemChatPinnedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: BaseMessage) {
            binding.chatBubble.setText(message.message)
            binding.textviewNickname.text = message.sender?.nickname ?: message.sender?.userId
            binding.dateTagView.setMillisecond(message.createdAt)
            binding.textviewTime.text = message.createdAt.toTime()
        }
    }

    companion object {

        const val VIEW_TYPE_IMAGE = 0
        const val VIEW_TYPE_MESSAGE = 1

        val diffCallback = object : DiffUtil.ItemCallback<BaseMessage>() {
            override fun areItemsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
                return if (oldItem.messageId > 0 && newItem.messageId > 0) {
                    oldItem.messageId == newItem.messageId
                } else {
                    oldItem.requestId == newItem.requestId
                }
            }

            override fun areContentsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
                return oldItem.message == newItem.message
                        && oldItem.sender?.nickname == newItem.sender?.nickname
                        && oldItem.sendingStatus == newItem.sendingStatus
                        && oldItem.updatedAt == newItem.updatedAt
            }
        }
    }

}