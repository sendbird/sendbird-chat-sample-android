package com.sendbird.chat.sample.groupchannel.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.android.User
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ListItemSelectUserBinding

class SelectUserAdapter(
    private val listener: OnItemClickListener?,
    private val selectMode: Boolean,
    selectedUserIds: MutableSet<String>?,
    private val baseUserIdSet: MutableSet<String>?,
) :
    ListAdapter<User, SelectUserAdapter.SelectUserListViewHolder>(diffCallback) {
    fun interface OnItemClickListener {
        fun onItemClick(user: User, position: Int)
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

    private var selectUserIdSet: MutableSet<String> = mutableSetOf()

    init {
        selectedUserIds?.let { selectUserIdSet.addAll(it.toMutableSet()) }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectUserListViewHolder(
        ListItemSelectUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: SelectUserListViewHolder, position: Int) {
        holder.bind(getItem(position))
        if (selectMode) {
            holder.itemView.setOnClickListener {
                listener?.onItemClick(
                    getItem(position),
                    position
                )
            }
        }
    }

    fun addItems(users: List<User>) {
        val baseUserList = mutableListOf<User>().apply {
            addAll(currentList)
            addAll(users)
        }
        submitList(baseUserList)
    }

    inner class SelectUserListViewHolder(private val binding: ListItemSelectUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.imageviewProfile.clipToOutline = true
            if (user.profileUrl.isNullOrEmpty()) {
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


            if (selectMode) {
                binding.checkboxUserSelect.visibility = View.VISIBLE
                binding.checkboxUserSelect.isChecked = isUserChecked(user.userId)
                if (isBaseUserChecked(user.userId)) {
                    binding.checkboxUserSelect.isChecked = true
                    binding.checkboxUserSelect.isEnabled = false
                } else {
                    binding.checkboxUserSelect.isEnabled = true
                }
            } else {
                binding.checkboxUserSelect.visibility = View.GONE
            }
        }
    }

    fun userSelect(userId: String) {
        if (isUserChecked(userId)) {
            selectUserIdSet.remove(userId)
        } else {
            selectUserIdSet.add(userId)
        }
    }

    private fun isUserChecked(userId: String): Boolean {
        return selectUserIdSet.contains(userId)
    }

    private fun isBaseUserChecked(userId: String): Boolean {
        return baseUserIdSet?.contains(userId) ?: false
    }
}