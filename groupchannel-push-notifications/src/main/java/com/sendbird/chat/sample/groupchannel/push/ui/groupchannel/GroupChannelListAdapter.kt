package com.sendbird.chat.sample.groupchannel.push.ui.groupchannel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQuery
import com.sendbird.android.message.BaseMessage
import com.sendbird.chat.module.utils.TextUtils
import com.sendbird.chat.module.utils.toChatTime
import com.sendbird.chat.sample.groupchannel.push.databinding.ListItemChannelBinding

class GroupChannelListAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<GroupChannelListAdapter.GroupChannelListViewHolder>() {
    fun interface OnItemClickListener {
        fun onItemClick(groupChannel: GroupChannel)
    }

    private val groupChannelList = mutableListOf<GroupChannel>()
    private val cachedGroupChannelInfoList = mutableListOf<GroupChannelInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GroupChannelListViewHolder(
        ListItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: GroupChannelListViewHolder, position: Int) {
        holder.bind(groupChannelList[position])
    }

    fun addChannels(channels: List<GroupChannel>) {
        val channelUrls = channels.map { it.url }
        val newChannelList = mutableListOf<GroupChannel>().apply {
            addAll(groupChannelList.filter { it.url !in channelUrls })
            addAll(channels)
        }
        notifyItemChanged(newChannelList)
    }

    fun updateChannels(channels: List<GroupChannel>) {
        val channelUrls = channels.map { it.url }
        val newChannelList = mutableListOf<GroupChannel>().apply {
            addAll(groupChannelList.filter { it.url !in channelUrls })
            addAll(channels)
        }
        val groupChannelComparator = Comparator<GroupChannel> { groupChannelA, groupChannelB ->
            GroupChannel.compareTo(
                groupChannelA,
                groupChannelB,
                GroupChannelListQuery.Order.LATEST_LAST_MESSAGE,
                GroupChannelListQuery.Order.LATEST_LAST_MESSAGE.channelSortOrder
            )
        }

        newChannelList.sortWith(groupChannelComparator)
        notifyItemChanged(newChannelList)
    }

    fun deleteChannels(channelUrls: List<String>) {
        val newChannelList = mutableListOf<GroupChannel>().apply {
            addAll(groupChannelList.filter { it.url !in channelUrls })
        }
        notifyItemChanged(newChannelList)
    }

    private fun notifyItemChanged(newChannelList: List<GroupChannel>) {
        val diffCallback = GroupChannelDiffCallback(cachedGroupChannelInfoList, newChannelList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cachedGroupChannelInfoList.clear()
        cachedGroupChannelInfoList.addAll(GroupChannelInfo.toGroupChannelInfoList(newChannelList))
        groupChannelList.clear()
        groupChannelList.addAll(newChannelList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class GroupChannelListViewHolder(private val binding: ListItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(groupChannelList[adapterPosition])
            }
        }

        fun bind(groupChannel: GroupChannel) {
            val lastMessage = groupChannel.lastMessage
            binding.chatChannelListItemView.setText(
                if (groupChannel.name.isBlank() || groupChannel.name == TextUtils.CHANNEL_DEFAULT_NAME)
                    TextUtils.getGroupChannelTitle(groupChannel)
                else
                    groupChannel.name,
                lastMessage?.message ?: TextUtils.NEW_CHANNEL_DEFAULT_TEXT
            )
            binding.textviewTime.text =
                (lastMessage?.createdAt ?: groupChannel.createdAt).toChatTime()
        }
    }

    override fun getItemCount() = groupChannelList.size


    private class GroupChannelDiffCallback(
        private val oldItems: List<GroupChannelInfo>,
        private val newItems: List<GroupChannel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int =
            oldItems.size

        override fun getNewListSize(): Int =
            newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]
            val oldLastMessage = oldItem.lastMessage ?: return false
            val newLastMessage = newItem.lastMessage ?: return false

            return (oldItem.name == newItem.name && oldLastMessage.message == newLastMessage.message)

        }
    }

    data class GroupChannelInfo(
        val url: String,
        val lastMessage: BaseMessage?,
        val name: String,
    ) {
        constructor(groupChannel: GroupChannel) : this(
            groupChannel.url,
            groupChannel.lastMessage,
            groupChannel.name,
        )

        companion object {
            fun toGroupChannelInfoList(channelList: List<GroupChannel>): List<GroupChannelInfo> {
                val results = mutableListOf<GroupChannelInfo>()
                for (channel in channelList) {
                    results.add(GroupChannelInfo(channel))
                }
                return results
            }
        }

    }
}
