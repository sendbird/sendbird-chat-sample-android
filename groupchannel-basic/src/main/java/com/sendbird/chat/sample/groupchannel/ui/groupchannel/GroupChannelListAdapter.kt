package com.sendbird.chat.sample.groupchannel.ui.groupchannel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.GroupChannel
import com.sendbird.chat.module.utils.TextUtils
import com.sendbird.chat.module.utils.TextUtils.CHANNEL_DEFAULT_NAME
import com.sendbird.chat.module.utils.toChatTime
import com.sendbird.chat.sample.groupchannel.databinding.ListItemChannelBinding

class GroupChannelListAdapter(private val listener: OnItemClickListener) :
    ListAdapter<GroupChannel, GroupChannelListAdapter.GroupChannelListViewHolder>(diffCallback) {

    fun interface OnItemClickListener {
        fun onItemClick(groupChannel: GroupChannel)
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<GroupChannel>() {
            override fun areItemsTheSame(oldItem: GroupChannel, newItem: GroupChannel): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: GroupChannel, newItem: GroupChannel): Boolean {
                return if (oldItem.lastMessage == null && newItem.lastMessage == null) {
                    true
                } else if (oldItem.lastMessage == null && newItem.lastMessage != null) {
                    false
                } else {
                    oldItem.lastMessage.message == newItem.lastMessage.message
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GroupChannelListViewHolder(
        ListItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: GroupChannelListViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            listener.onItemClick(getItem(holder.adapterPosition))
        }
    }

    class GroupChannelListViewHolder(private val binding: ListItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(groupChannel: GroupChannel) {
            binding.chatChannelListItemView.setText(
                if (groupChannel.name.isNullOrBlank() || groupChannel.name == CHANNEL_DEFAULT_NAME) TextUtils.getGroupChannelTitle(groupChannel) else groupChannel.name,
                if (groupChannel.lastMessage != null) groupChannel.lastMessage.message else ""
            )
            if (groupChannel.lastMessage != null) {
                binding.textviewTime.text = groupChannel.lastMessage.createdAt.toChatTime()
            }
        }
    }

    fun loadChannels(channels: List<GroupChannel>) {
        val channelUrls = channels.map { it.url }
        val originList = mutableListOf<GroupChannel>().apply {
            addAll(currentList)
            removeAll { it.url in channelUrls }
            addAll(channels)
        }
        submitList(originList)
    }

    fun updateChannels(channels: List<GroupChannel>) {
        val channelUrls = channels.map { it.url }
        val originList = mutableListOf<GroupChannel>().apply {
            addAll(currentList)
            removeAll { it.url in channelUrls }
            addAll(0, channels)
        }
        submitList(originList)
        notifyItemChanged(0)
    }

    fun deleteChannels(channelUrls: List<String>) {
        val originList = mutableListOf<GroupChannel>().apply {
            addAll(currentList)
            removeAll { it.url in channelUrls }
        }
        submitList(originList.toMutableList())
    }
}