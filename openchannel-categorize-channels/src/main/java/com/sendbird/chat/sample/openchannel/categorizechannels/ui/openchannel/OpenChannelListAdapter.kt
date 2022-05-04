package com.sendbird.chat.sample.openchannel.categorizechannels.ui.openchannel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.OpenChannel
import com.sendbird.chat.sample.openchannel.categorizechannels.databinding.ListItemChannelBinding

class OpenChannelListAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<OpenChannelListAdapter.OpenChannelListViewHolder>() {

    fun interface OnItemClickListener {
        fun onItemClick(openChannel: OpenChannel)
    }

    private val openChannelList = mutableListOf<OpenChannel>()
    private val cachedOpenChannelInfoList = mutableListOf<OpenChannelInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OpenChannelListViewHolder(
        ListItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: OpenChannelListViewHolder, position: Int) {
        holder.bind(openChannelList[holder.adapterPosition])
    }

    override fun getItemCount() = openChannelList.size

    fun addChannels(channels: List<OpenChannel>) {
        openChannelList.addAll(channels)
        cachedOpenChannelInfoList.addAll(OpenChannelInfo.toOpenChannelInfoList(channels))
        notifyItemRangeChanged(cachedOpenChannelInfoList.size - 1, channels.size)
    }

    fun changeChannels(channels: List<OpenChannel>) {
        val diffCallback = OpenChannelDiffCallback(cachedOpenChannelInfoList, channels)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cachedOpenChannelInfoList.clear()
        cachedOpenChannelInfoList.addAll(OpenChannelInfo.toOpenChannelInfoList(channels))
        openChannelList.clear()
        openChannelList.addAll(channels)
        diffResult.dispatchUpdatesTo(this)
    }

    private class OpenChannelDiffCallback(
        private val oldItems: List<OpenChannelInfo>,
        private val newItems: List<OpenChannel>
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

            return oldItem.name == newItem.name && oldItem.participantCount == newItem.participantCount
        }
    }

    inner class OpenChannelListViewHolder(private val binding: ListItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(openChannelList[adapterPosition])
            }
        }

        fun bind(openChannel: OpenChannel) {
            binding.chatChannelListItemView.setText(
                openChannel.name.let { if (openChannel.customType == "MEME") "$it (MEME)" else it },
                "Participant: ${openChannel.participantCount}"
            )
        }
    }

    data class OpenChannelInfo(
        val url: String,
        val participantCount: Int,
        val name: String,
    ) {
        constructor(openChannel: OpenChannel) : this(
            openChannel.url,
            openChannel.participantCount,
            openChannel.name
        )

        companion object {
            fun toOpenChannelInfoList(channelList: List<OpenChannel>): List<OpenChannelInfo> {
                val results = mutableListOf<OpenChannelInfo>()
                for (channel in channelList) {
                    results.add(
                        OpenChannelInfo(
                            channel
                        )
                    )
                }
                return results
            }
        }
    }

}