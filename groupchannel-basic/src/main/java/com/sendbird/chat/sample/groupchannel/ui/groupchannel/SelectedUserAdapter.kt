package com.sendbird.chat.sample.groupchannel.ui.groupchannel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.android.User
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ListItemSelectedUserBinding

class SelectedUserAdapter :
    ListAdapter<User, SelectedUserAdapter.SelectedUserListViewHolder>(diffCallback) {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.nickname == oldItem.nickname
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectedUserListViewHolder(
        ListItemSelectedUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: SelectedUserListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SelectedUserListViewHolder(private val binding: ListItemSelectedUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.imageviewProfile.clipToOutline = true
            if (user.profileUrl.isNullOrBlank()) {
                binding.imageviewProfile.load(R.drawable.ic_baseline_person_24) {
                    crossfade(true)
                }
            } else {
                binding.imageviewProfile.load(user.profileUrl) {
                    crossfade(true)
                    memoryCacheKey(user.plainProfileImageUrl)
                }
            }
            binding.textviewName.text =
                if (user.nickname.isNullOrBlank()) user.userId else user.nickname
        }
    }

}