package com.sendbird.chat.sample.openchannel.ui.openchannel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.OpenChannel
import com.sendbird.chat.sample.openchannel.databinding.ListItemChannelBinding

class OpenChannelListAdapter(private val listener: OnItemClickListener) :
    ListAdapter<OpenChannel, OpenChannelListAdapter.OpenChannelListViewHolder>(diffCallback) {
    fun interface OnItemClickListener {
        fun onItemClick(openChannel: OpenChannel)
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<OpenChannel>() {
            override fun areItemsTheSame(oldItem: OpenChannel, newItem: OpenChannel): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: OpenChannel, newItem: OpenChannel): Boolean {
                return oldItem.participantCount == newItem.participantCount && oldItem.name == newItem.name
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OpenChannelListViewHolder(
        ListItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: OpenChannelListViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            listener.onItemClick(getItem(holder.adapterPosition))
        }
    }

    fun addItems(channels: List<OpenChannel>) {
        val baseChannelList = mutableListOf<OpenChannel>().apply {
            addAll(currentList)
            addAll(channels)
        }
        submitList(baseChannelList)
    }

    class OpenChannelListViewHolder(private val binding: ListItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(openChannel: OpenChannel) {
            binding.chatChannelListItemView.setText(
                openChannel.name,
                "Participant: ${openChannel.participantCount}"
            )
        }
    }
}