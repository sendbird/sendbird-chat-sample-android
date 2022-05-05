package com.sendbird.chat.sample.groupchannel.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.android.user.User
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ListItemMemberBinding

class ChatMemberListAdapter(
    private val listener: OnItemClickListener?
) : ListAdapter<User, ChatMemberListAdapter.ChatMembersListViewHolder>(diffCallback) {
    fun interface OnItemClickListener {
        fun onItemClick(member: User, position: Int)
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.nickname == oldItem.nickname && oldItem.plainProfileImageUrl == newItem.plainProfileImageUrl
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatMembersListViewHolder(
        ListItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ChatMembersListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatMembersListViewHolder(private val binding: ListItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener?.onItemClick(
                    getItem(adapterPosition),
                    adapterPosition
                )
            }
        }

        fun bind(member: User) {
            binding.imageviewProfile.clipToOutline = true
            if (member.profileUrl.isEmpty()) {
                binding.imageviewProfile.load(R.drawable.ic_baseline_person_24) {
                    crossfade(true)
                }
            } else {
                binding.imageviewProfile.load(member.profileUrl) {
                    crossfade(true)
                    memoryCacheKey(member.plainProfileImageUrl)
                }
            }
            binding.textviewName.text = member.nickname.ifBlank { member.userId }
            if (member.connectionStatus == User.ConnectionStatus.ONLINE) {
                binding.textviewName.append(" (Online)")
            }
        }
    }
}