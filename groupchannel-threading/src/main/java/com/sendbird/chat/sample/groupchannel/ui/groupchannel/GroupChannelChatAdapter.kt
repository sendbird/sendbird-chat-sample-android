package com.sendbird.chat.sample.groupchannel.ui.groupchannel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.sendbird.android.SendbirdChat
import com.sendbird.android.message.AdminMessage
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.UserMessage
import com.sendbird.chat.module.utils.ListUtils
import com.sendbird.chat.module.utils.equalDate
import com.sendbird.chat.module.utils.equalTime
import com.sendbird.chat.module.utils.toTime
import com.sendbird.chat.sample.groupchannel.databinding.*

class GroupChannelChatAdapter(
    private val longClickListener: OnItemLongClickListener,
    private val failedItemClickListener: OnFailedItemClickListener,
    private val getParentMessageText: (Long) -> String,
) : ListAdapter<BaseMessage, RecyclerView.ViewHolder>(diffCallback) {

    fun interface OnItemLongClickListener {
        fun onItemLongClick(baseMessage: BaseMessage, view: View)
    }

    fun interface OnFailedItemClickListener {
        fun onItemClick(baseMessage: BaseMessage)
    }

    companion object {
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
        const val VIEW_TYPE_SEND = 0
        const val VIEW_TYPE_RECEIVE = 1
        const val VIEW_TYPE_SEND_IMAGE = 2
        const val VIEW_TYPE_RECEIVE_IMAGE = 3
        const val VIEW_TYPE_ADMIN = 4
    }

    private val baseMessageList = mutableListOf<BaseMessage>()
    private val pendingMessageList = mutableListOf<BaseMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            VIEW_TYPE_SEND -> return GroupChatSendViewHolder(
                ListItemChatSendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_RECEIVE -> return GroupChatReceiveViewHolder(
                ListItemChatReceiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_SEND_IMAGE -> return GroupChatImageSendViewHolder(
                ListItemChatImageSendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_RECEIVE_IMAGE -> return GroupChatImageReceiveViewHolder(
                ListItemChatImageReceiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_ADMIN -> return GroupChatAdminViewHolder(
                ListItemChatAdminBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> return GroupChatSendViewHolder(
                ListItemChatSendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var showName = false
        var showDate = false
        var showTime = false

        if (currentList[position].sendingStatus == BaseMessage.SendingStatus.SUCCEEDED) {
            showName = true
            showDate = true
            showTime = true
            if (position > 0) {
                showDate =
                    !currentList[position].createdAt.equalDate(currentList[position - 1].createdAt)
                if (currentList[position].sender != null && currentList[position - 1].sender != null) {
                    showName =
                        currentList[position].sender?.userId != currentList[position - 1].sender?.userId
                }
                if (position < currentList.size - 1) {
                    showTime =
                        !(currentList[position].createdAt.equalTime(currentList[position + 1].createdAt))
                    if (!showTime) {
                        if (currentList[position].sender != null && currentList[position + 1].sender != null) {
                            showTime =
                                currentList[position].sender?.userId != currentList[position + 1].sender?.userId
                        }
                    }
                }
            } else {
                if (position < currentList.size - 1) {
                    showTime =
                        !(currentList[position].createdAt.equalTime(currentList[position + 1].createdAt))
                }
            }
        }

        when (holder) {
            is GroupChatSendViewHolder -> {
                val item = getItem(position)
                val parentMessage = getParentMessageText(item.parentMessageId)
                holder.bind(item, showDate, showTime, parentMessage)
            }
            is GroupChatReceiveViewHolder -> {
                val item = getItem(position)
                val parentMessage = (item.parentMessage as? UserMessage)?.message ?: ""
                holder.bind(item, showName, showDate, showTime, parentMessage)
            }
            is GroupChatImageSendViewHolder -> {
                holder.bind(getItem(position) as FileMessage, showDate, showTime)
            }
            is GroupChatImageReceiveViewHolder -> {
                holder.bind(getItem(position) as FileMessage, showName, showDate, showTime)
            }
            is GroupChatAdminViewHolder -> {
                holder.bind(getItem(position), showDate)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is AdminMessage) {
            VIEW_TYPE_ADMIN
        } else {
            val currentUser = SendbirdChat.currentUser
            if (currentUser != null) {
                if (getItem(position).sender?.userId == currentUser.userId) {
                    if (getItem(position) is FileMessage) {
                        VIEW_TYPE_SEND_IMAGE
                    } else {
                        VIEW_TYPE_SEND
                    }
                } else {
                    if (getItem(position) is FileMessage) {
                        VIEW_TYPE_RECEIVE_IMAGE
                    } else {
                        VIEW_TYPE_RECEIVE
                    }
                }
            } else {
                if (getItem(position) is FileMessage) {
                    VIEW_TYPE_RECEIVE_IMAGE
                } else {
                    VIEW_TYPE_RECEIVE
                }
            }
        }
    }

    fun changeMessages(messages: List<BaseMessage>?, isPendingClear: Boolean = true) {
        baseMessageList.clear()
        if (isPendingClear) {
            pendingMessageList.clear()
        }
        if (messages != null) {
            baseMessageList.addAll(messages)
        }
        mergeList()
    }

    fun addNextMessages(messages: List<BaseMessage>?) {
        if (messages != null) {
            baseMessageList.addAll(messages)
            mergeList()
        }
    }

    fun addPreviousMessages(messages: List<BaseMessage>?) {
        if (messages != null) {
            baseMessageList.addAll(0, messages)
            mergeList()
        }
    }

    fun addPendingMessages(messages: List<BaseMessage>) {
        pendingMessageList.addAll(messages)
        mergeList()
    }

    fun updateSucceedMessages(messages: List<BaseMessage>) {
        val requestIdIndexMap =
            pendingMessageList.mapIndexed { index, pendingMessage ->
                pendingMessage.requestId to index
            }.toMap()
        val messageIdIndexMap =
            baseMessageList.mapIndexed { index, baseMessage ->
                baseMessage.messageId to index
            }.toMap()
        val resultMessageList = mutableListOf<BaseMessage>().apply { addAll(pendingMessageList) }
        messages.forEach {
            val requestIndex = requestIdIndexMap[it.requestId]
            if (requestIndex != null) {
                baseMessageList.add(it)
                resultMessageList.remove(pendingMessageList[requestIndex])
            } else {
                val messageIndex = messageIdIndexMap[it.messageId]
                if (messageIndex != null) {
                    baseMessageList[messageIndex] = it
                }
            }
        }
        pendingMessageList.clear()
        pendingMessageList.addAll(resultMessageList)
        mergeList()
    }

    fun updatePendingMessages(messages: List<BaseMessage>) {
        val requestIdIndexMap =
            pendingMessageList.mapIndexed { index, pendingMessage ->
                pendingMessage.requestId to index
            }.toMap()
        messages.forEach {
            val index = requestIdIndexMap[it.requestId]
            if (index != null) {
                pendingMessageList[index] = it
            }
        }
        mergeList()
    }

    fun deletePendingMessages(messages: List<BaseMessage>) {
        val requestIdIndexMap =
            pendingMessageList.mapIndexed { index, pendingMessage ->
                pendingMessage.requestId to index
            }.toMap()
        val resultMessageList = mutableListOf<BaseMessage>().apply { addAll(pendingMessageList) }
        messages.forEach {
            val index = requestIdIndexMap[it.requestId]
            if (index != null) {
                resultMessageList.remove(pendingMessageList[index])
            }
        }
        pendingMessageList.clear()
        pendingMessageList.addAll(resultMessageList)
        mergeList()
    }

    fun deleteMessages(messages: List<BaseMessage>) {
        val messageIdIndexMap =
            baseMessageList.mapIndexed { index, message ->
                message.messageId to index
            }.toMap()
        val resultMessageList = mutableListOf<BaseMessage>().apply { addAll(baseMessageList) }
        messages.forEach {
            val index = messageIdIndexMap[it.messageId]
            if (index != null) {
                resultMessageList.remove(baseMessageList[index])
            }
        }
        baseMessageList.clear()
        baseMessageList.addAll(resultMessageList)
        mergeList()
    }

    fun addMessages(messages: List<BaseMessage>) {
        messages.forEach {
            ListUtils.findAddMessageIndex(baseMessageList, it).apply {
                if (this > -1) {
                    baseMessageList.add(this, it)
                }
            }
        }
        mergeList()
    }

    private fun mergeList() = submitList(baseMessageList + pendingMessageList)

    open inner class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnLongClickListener {
                if (getItem(adapterPosition).sendingStatus == BaseMessage.SendingStatus.SUCCEEDED) {
                    longClickListener.onItemLongClick(getItem(adapterPosition), it)
                }
                return@setOnLongClickListener false
            }
        }
    }

    inner class GroupChatSendViewHolder(private val binding: ListItemChatSendBinding) :
        BaseViewHolder(binding) {
        fun bind(
            message: BaseMessage,
            showDate: Boolean,
            showTime: Boolean,
            parentMessage: String
        ) {
            if (message.sendingStatus == BaseMessage.SendingStatus.SUCCEEDED) {
                binding.progressSend.visibility = View.GONE
                binding.chatErrorButton.visibility = View.GONE
                if (showDate) {
                    binding.dateTagView.setMillisecond(message.createdAt)
                    binding.dateTagView.visibility = View.VISIBLE
                } else {
                    binding.dateTagView.visibility = View.GONE
                }
                if (showTime) {
                    binding.textviewTime.text = message.createdAt.toTime()
                    binding.textviewTime.visibility = View.VISIBLE
                } else {
                    binding.textviewTime.visibility = View.GONE
                }
                binding.parentText.isVisible = parentMessage.isNotBlank()
                if (parentMessage.isNotBlank()) {
                    binding.parentText.text = parentMessage
                }
            } else {
                binding.dateTagView.visibility = View.GONE
                binding.textviewTime.visibility = View.GONE
                if (message.sendingStatus == BaseMessage.SendingStatus.PENDING) {
                    binding.progressSend.visibility = View.VISIBLE
                    binding.chatErrorButton.visibility = View.GONE
                } else {
                    binding.progressSend.visibility = View.GONE
                    binding.chatErrorButton.visibility = View.VISIBLE
                    binding.chatErrorButton.setOnClickListener {
                        failedItemClickListener.onItemClick(message)
                    }
                }
            }
            binding.chatBubbleSend.setText(message.message)
        }
    }

    inner class GroupChatReceiveViewHolder(private val binding: ListItemChatReceiveBinding) :
        BaseViewHolder(binding) {
        fun bind(
            message: BaseMessage,
            showName: Boolean,
            showDate: Boolean,
            showTime: Boolean,
            parentMessage: String
        ) {
            binding.chatBubbleReceive.setText(message.message)
            if (showName) {
                binding.textviewNickname.text = message.sender?.nickname ?: message.sender?.userId
                binding.textviewNickname.visibility = View.VISIBLE
            } else {
                binding.textviewNickname.visibility = View.GONE
            }
            if (showDate) {
                binding.dateTagView.setMillisecond(message.createdAt)
                binding.dateTagView.visibility = View.VISIBLE
            } else {
                binding.dateTagView.visibility = View.GONE
            }
            if (showTime) {
                binding.textviewTime.text = message.createdAt.toTime()
                binding.textviewTime.visibility = View.VISIBLE
            } else {
                binding.textviewTime.visibility = View.GONE
            }
            binding.parentText.isVisible = parentMessage.isNotBlank()
            if (parentMessage.isNotBlank()) {
                binding.parentText.text = parentMessage
            }
        }
    }

    inner class GroupChatImageSendViewHolder(private val binding: ListItemChatImageSendBinding) :
        BaseViewHolder(binding) {
        fun bind(
            message: FileMessage,
            showDate: Boolean,
            showTime: Boolean
        ) {
            if (message.sendingStatus == BaseMessage.SendingStatus.SUCCEEDED) {
                binding.chatBubbleImageSend.setImageUrl(message.url, message.plainUrl)
                binding.progressImageSend.visibility = View.GONE
                binding.chatImageErrorButton.visibility = View.GONE
                if (showDate) {
                    binding.dateTagView.setMillisecond(message.createdAt)
                    binding.dateTagView.visibility = View.VISIBLE
                } else {
                    binding.dateTagView.visibility = View.GONE
                }
                if (showTime) {
                    binding.textviewTime.text = message.createdAt.toTime()
                    binding.textviewTime.visibility = View.VISIBLE
                } else {
                    binding.textviewTime.visibility = View.GONE
                }
            } else {
                binding.chatBubbleImageSend.setImageFile(message.messageParams?.file)
                binding.dateTagView.visibility = View.GONE
                binding.textviewTime.visibility = View.GONE
                if (message.sendingStatus == BaseMessage.SendingStatus.PENDING) {
                    binding.progressImageSend.visibility = View.VISIBLE
                    binding.chatImageErrorButton.visibility = View.GONE
                } else {
                    binding.progressImageSend.visibility = View.GONE
                    binding.chatImageErrorButton.visibility = View.VISIBLE
                    binding.chatImageErrorButton.setOnClickListener {
                        failedItemClickListener.onItemClick(message)
                    }
                }
            }
        }
    }

    inner class GroupChatImageReceiveViewHolder(private val binding: ListItemChatImageReceiveBinding) :
        BaseViewHolder(binding) {
        fun bind(
            message: FileMessage,
            showName: Boolean,
            showDate: Boolean,
            showTime: Boolean
        ) {
            binding.chatBubbleImageReceive.setImageUrl(message.url, message.plainUrl)
            if (showName) {
                binding.textviewNickname.text = message.sender?.nickname ?: message.sender?.userId
                binding.textviewNickname.visibility = View.VISIBLE
            } else {
                binding.textviewNickname.visibility = View.GONE
            }
            if (showDate) {
                binding.dateTagView.setMillisecond(message.createdAt)
                binding.dateTagView.visibility = View.VISIBLE
            } else {
                binding.dateTagView.visibility = View.GONE
            }
            if (showTime) {
                binding.textviewTime.text = message.createdAt.toTime()
                binding.textviewTime.visibility = View.VISIBLE
            } else {
                binding.textviewTime.visibility = View.GONE
            }
        }
    }

    inner class GroupChatAdminViewHolder(private val binding: ListItemChatAdminBinding) :
        BaseViewHolder(binding) {
        fun bind(
            message: BaseMessage,
            showDate: Boolean
        ) {
            binding.chatBubbleAdminView.setText(message.message)
            if (showDate) {
                binding.dateTagView.setMillisecond(message.createdAt)
                binding.dateTagView.visibility = View.VISIBLE
            } else {
                binding.dateTagView.visibility = View.GONE
            }
        }
    }
}