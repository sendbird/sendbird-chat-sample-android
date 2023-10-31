package com.sendbird.chat.sample.groupchannel.friends.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.android.user.Member
import com.sendbird.android.user.User
import com.sendbird.chat.sample.groupchannel.friends.R
import com.sendbird.chat.sample.groupchannel.friends.databinding.ListItemMemberBinding

class ChatMemberListAdapter(
    private val onUserClicked: (User, Boolean) -> Unit
) : ListAdapter<User, ChatMemberListAdapter.ChatMembersListViewHolder>(diffCallback) {

    private var friends = emptyList<User>()

    fun setFriends(friends: List<User>) {
        this.friends = friends
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.nickname == newItem.nickname && oldItem.plainProfileImageUrl == newItem.plainProfileImageUrl
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
                val member = getItem(adapterPosition)
                val isFriend = friends.find { it.userId == member.userId } != null
                onUserClicked(member, isFriend)
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
            val isFriend = friends.find { it.userId == member.userId } != null
            val nickname = member.nickname.ifBlank { member.userId }
            binding.textviewName.text = if (isFriend) "$nickname (Friend)" else nickname
            if (member.connectionStatus == User.ConnectionStatus.ONLINE) {
                binding.textviewName.append(" (Online)")
            }
        }
    }
}
