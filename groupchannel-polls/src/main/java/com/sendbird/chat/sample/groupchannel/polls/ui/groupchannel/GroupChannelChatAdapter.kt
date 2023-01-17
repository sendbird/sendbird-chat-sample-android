package com.sendbird.chat.sample.groupchannel.polls.ui.groupchannel

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import coil.transform.CircleCropTransformation
import com.sendbird.android.SendbirdChat
import com.sendbird.android.message.*
import com.sendbird.android.poll.Poll
import com.sendbird.android.poll.PollOption
import com.sendbird.android.poll.PollStatus
import com.sendbird.chat.module.utils.ListUtils
import com.sendbird.chat.module.utils.equalDate
import com.sendbird.chat.module.utils.equalTime
import com.sendbird.chat.module.utils.toTime
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.*
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KFunction1

class GroupChannelChatAdapter(
    context: Context,
    private val longClickListener: OnItemLongClickListener,
    private val failedItemClickListener: OnFailedItemClickListener,
    private val onViewResultClicked: (Poll) -> Unit,
    private val onVoteNowClicked: (Poll) -> Unit,
    private val deletePollOption: (Poll, PollOption) -> Unit,
    private val closePoll: (Poll) -> Unit,
) : ListAdapter<BaseMessage, RecyclerView.ViewHolder>(diffCallback) {

    var memberCount = 1

    private val dateFormat by lazy { DateFormat.getMediumDateFormat(context) }
    private val timeFormat by lazy { DateFormat.getTimeFormat(context) }

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
                        && (oldItem as? UserMessage)?.poll?.updatedAt == (newItem as? UserMessage)?.poll?.updatedAt
                        && (oldItem as? UserMessage)?.poll?.options == (newItem as? UserMessage)?.poll?.options
                        && (oldItem as? UserMessage)?.poll?.votedPollOptionIds == (newItem as? UserMessage)?.poll?.votedPollOptionIds
            }
        }
        const val VIEW_TYPE_SEND = 0
        const val VIEW_TYPE_RECEIVE = 1
        const val VIEW_TYPE_SEND_IMAGE = 2
        const val VIEW_TYPE_RECEIVE_IMAGE = 3
        const val VIEW_TYPE_ADMIN = 4
        const val VIEW_TYPE_POOL = 5
    }

    private val baseMessageList = mutableListOf<BaseMessage>()
    private val pendingMessageList = mutableListOf<BaseMessage>()

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            VIEW_TYPE_SEND -> return GroupChatSendViewHolder(
                ListItemChatSendBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            VIEW_TYPE_RECEIVE -> return GroupChatReceiveViewHolder(
                ListItemChatReceiveBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            VIEW_TYPE_SEND_IMAGE -> return GroupChatImageSendViewHolder(
                ListItemChatImageSendBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            VIEW_TYPE_RECEIVE_IMAGE -> return GroupChatImageReceiveViewHolder(
                ListItemChatImageReceiveBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            VIEW_TYPE_ADMIN -> return GroupChatAdminViewHolder(
                ListItemChatAdminBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            VIEW_TYPE_POOL -> return GroupChatPollViewHolder(
                ListItemChatPollBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            else -> return GroupChatSendViewHolder(
                ListItemChatSendBinding.inflate(
                    layoutInflater,
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

        if (currentList[position].sendingStatus == SendingStatus.SUCCEEDED) {
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
                holder.bind(getItem(position), showDate, showTime)
            }
            is GroupChatReceiveViewHolder -> {
                holder.bind(getItem(position), showName, showDate, showTime)
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
            is GroupChatPollViewHolder -> {
                holder.bind(getItem(position) as UserMessage)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is AdminMessage) {
            VIEW_TYPE_ADMIN
        } else if ((getItem(position) as? UserMessage)?.poll != null) {
            VIEW_TYPE_POOL
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
        pendingMessageList.addAll(resultMessageList.mapNotNull { BaseMessage.clone(it) })
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
                if (getItem(adapterPosition).sendingStatus == SendingStatus.SUCCEEDED) {
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
            showTime: Boolean
        ) {
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {
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
            } else {
                binding.dateTagView.visibility = View.GONE
                binding.textviewTime.visibility = View.GONE
                if (message.sendingStatus == SendingStatus.PENDING) {
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
            showTime: Boolean
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
        }
    }

    inner class GroupChatImageSendViewHolder(private val binding: ListItemChatImageSendBinding) :
        BaseViewHolder(binding) {
        fun bind(
            message: FileMessage,
            showDate: Boolean,
            showTime: Boolean
        ) {
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {
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
                binding.chatBubbleImageSend.setImageFile(message.messageCreateParams?.file)
                binding.dateTagView.visibility = View.GONE
                binding.textviewTime.visibility = View.GONE
                if (message.sendingStatus == SendingStatus.PENDING) {
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

    inner class GroupChatPollViewHolder(private val binding: ListItemChatPollBinding) :
        BaseViewHolder(binding) {

        private val adapter = PollOptionAdapter()
        private val votedAdapter = VotedPollOptionAdapter()

        init {
            binding.rvOptions.adapter = adapter
        }

        fun bind(userMessage: UserMessage) {
            val poll = userMessage.poll ?: return
            binding.btnViewMore.isVisible = poll.status != PollStatus.OPEN
            binding.btnViewMore.setOnClickListener {
                onViewResultClicked(poll)
            }
            binding.btnViewResult.isVisible = poll.status == PollStatus.OPEN
            binding.btnViewResult.setOnClickListener {
                onViewResultClicked(poll)
            }
            val closeEnabled = poll.createdBy == SendbirdChat.currentUser?.userId && poll.status == PollStatus.OPEN
            binding.btnClosePoll.isVisible = closeEnabled
            binding.btnClosePoll.setOnClickListener {
                closePoll(poll)
            }
            val isPollOpened = poll.status == PollStatus.OPEN
            binding.tvPollTitle.text = poll.title
            // binding.btnVoteNow.isVisible = poll.allowUserSuggestion && isPollOpened

            binding.senderProfile.load(userMessage.sender?.profileUrl) {
                transformations(CircleCropTransformation())
            }
            binding.sender.text = userMessage.sender?.nickname

            binding.btnVoteNow.isVisible = isPollOpened
            if (isPollOpened) {
                binding.btnVoteNow.setOnClickListener { onVoteNowClicked(poll) }
            }

            val date = Date(poll.createdAt * 1000L)
            binding.tvPollMultipleCreatedAt.text = if(poll.allowMultipleVotes) {
                "Multi select | Created on ${dateFormat.format(date)} ${timeFormat.format(date)}"
            } else {
                "${dateFormat.format(date)} ${timeFormat.format(date)}"
            }

            val voted = poll.votedPollOptionIds.isNotEmpty()
            if(voted) {
                binding.rvOptions.adapter = votedAdapter
                votedAdapter.poll = poll
            } else {
                binding.rvOptions.adapter = adapter
                adapter.poll = poll
            }
            binding.tvClosesAt.isVisible = false

            binding.btnVoteNow.text = if(poll.votedPollOptionIds.isEmpty()) {
                "Vote now"
            } else {
                "Vote again"
            }
/*
            if (poll.closeAt > 0L) {
                binding.tvClosesAt.apply {
                    isVisible = true
                    text =
                        Date(poll.closeAt * 1_000).let {
                            SimpleDateFormat("dd/MM/yyyy-HH:mm").format(
                                it
                            )
                        }
                }
            }
*/
        }

        inner class PollOptionAdapter : RecyclerView.Adapter<PollOptionAdapter.PollViewHolder>() {

            var poll: Poll? by Delegates.observable(null) { _, _, _ -> notifyDataSetChanged() }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollViewHolder {
                return PollViewHolder(ItemPollOptionBinding.inflate(layoutInflater, parent, false))
            }

            override fun onBindViewHolder(holder: PollViewHolder, position: Int) {
                val poll = poll ?: return
                holder.bindOption(poll = poll, option = poll.options[position])
            }

            override fun getItemCount(): Int = poll?.options?.size ?: 0

            inner class PollViewHolder(private val binding: ItemPollOptionBinding) :
                RecyclerView.ViewHolder(binding.root) {

                fun bindOption(poll: Poll, option: PollOption) {
                    binding.tvOption.text = option.text
                    binding.tvVotersCount.text =
                        option.voteCount.takeIf { it != 0L }?.let { "+$it" } ?: ""
                    if (poll.status == PollStatus.OPEN) {
                        addDeleteMenuToOption(poll, option)
                    }
                }

                private fun addDeleteMenuToOption(poll: Poll, option: PollOption) {
                    if (SendbirdChat.currentUser?.userId == poll.createdBy) {
                        binding.root.setOnLongClickListener {
                            it.setOnCreateContextMenuListener { menu, _, _ ->
                                val deleteMenu =
                                    menu.add(
                                        Menu.NONE,
                                        0,
                                        0,
                                        itemView.resources.getString(R.string.delete)
                                    )
                                deleteMenu.setOnMenuItemClickListener {
                                    deletePollOption(poll, option)
                                    return@setOnMenuItemClickListener true
                                }
                            }
                            false
                        }
                    }
                }
            }
        }
    }

    inner class VotedPollOptionAdapter : RecyclerView.Adapter<VotedPollOptionAdapter.VotedPollOptionViewHolder>() {

        var poll: Poll? by Delegates.observable(null) { _, _, _ -> notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotedPollOptionViewHolder {
            return VotedPollOptionViewHolder(ItemPollOptionProgressBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(holder: VotedPollOptionViewHolder, position: Int) {
            val poll = poll ?: return
            holder.bindOption(poll = poll, option = poll.options[position])
        }

        override fun getItemCount(): Int = poll?.options?.size ?: 0

        inner class VotedPollOptionViewHolder(private val binding: ItemPollOptionProgressBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bindOption(poll: Poll, option: PollOption) {
                binding.tvPollOptionTitle.text = option.text
                binding.tvPollOptionTotalVote.text = "${option.voteCount} votes"

                val percent = (option.voteCount / memberCount.toFloat() * 100).toInt()
                binding.tvPercent.text = "$percent%"
                binding.progressPollOptionVote.progress = percent
                if (poll.status == PollStatus.OPEN) {
                    addDeleteMenuToOption(poll, option)
                }
            }

            private fun addDeleteMenuToOption(poll: Poll, option: PollOption) {
                if (SendbirdChat.currentUser?.userId == poll.createdBy) {
                    binding.root.setOnLongClickListener {
                        it.setOnCreateContextMenuListener { menu, _, _ ->
                            val deleteMenu =
                                menu.add(
                                    Menu.NONE,
                                    0,
                                    0,
                                    itemView.resources.getString(R.string.delete)
                                )
                            deleteMenu.setOnMenuItemClickListener {
                                deletePollOption(poll, option)
                                return@setOnMenuItemClickListener true
                            }
                        }
                        false
                    }
                }
            }
        }
    }

}
