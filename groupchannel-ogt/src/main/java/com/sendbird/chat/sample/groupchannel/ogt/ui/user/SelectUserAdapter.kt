package com.sendbird.chat.sample.groupchannel.ogt.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.android.user.User
import com.sendbird.chat.sample.groupchannel.ogt.R
import com.sendbird.chat.sample.groupchannel.ogt.databinding.ListItemSelectUserBinding

class SelectUserAdapter(
    private val listener: OnItemClickListener?,
    private val selectMode: Boolean,
    selectedUserList: ArrayList<String>?,
    baseUserIdList: ArrayList<String>?,
) : ListAdapter<User, SelectUserAdapter.SelectUserListViewHolder>(diffCallback) {
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

    val selectUserIdSet: MutableSet<String> = mutableSetOf()
    private val baseUserIdSet: MutableSet<String> = mutableSetOf()

    init {
        selectedUserList?.let { selectUserIdSet.addAll(it) }
        baseUserIdList?.let { baseUserIdSet.addAll(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectUserListViewHolder(
        ListItemSelectUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: SelectUserListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun addUsers(users: List<User>) {
        val baseUserList = mutableListOf<User>().apply {
            addAll(currentList)
            addAll(users)
        }
        submitList(baseUserList)
    }

    fun toggleUser(userId: String) {
        if (isUserChecked(userId)) {
            selectUserIdSet.remove(userId)
        } else {
            selectUserIdSet.add(userId)
        }
    }

    private fun isUserChecked(userId: String): Boolean {
        return selectUserIdSet.contains(userId)
    }

    private fun isBaseUser(userId: String): Boolean {
        return baseUserIdSet.contains(userId)
    }

    inner class SelectUserListViewHolder(private val binding: ListItemSelectUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            if (selectMode) {
                itemView.setOnClickListener {
                    listener?.onItemClick(
                        getItem(adapterPosition),
                        adapterPosition
                    )
                    toggleUser(getItem(adapterPosition).userId)
                    notifyItemChanged(adapterPosition)

                }
            }
        }

        fun bind(user: User) {
            binding.imageviewProfile.clipToOutline = true
            if (user.profileUrl.isEmpty()) {
                binding.imageviewProfile.load(R.drawable.ic_baseline_person_24) {
                    crossfade(true)
                }
            } else {
                binding.imageviewProfile.load(user.profileUrl) {
                    crossfade(true)
                    memoryCacheKey(user.plainProfileImageUrl)
                }
            }
            binding.textviewName.text = user.nickname.ifBlank { user.userId }
            if (selectMode) {
                binding.checkboxUserSelect.visibility = View.VISIBLE
                binding.checkboxUserSelect.isChecked = isUserChecked(user.userId)
                if (isBaseUser(user.userId)) {
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
}