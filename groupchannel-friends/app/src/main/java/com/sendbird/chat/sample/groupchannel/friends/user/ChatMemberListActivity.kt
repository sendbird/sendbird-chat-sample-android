package com.sendbird.chat.sample.groupchannel.friends.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.FriendListQueryParams
import com.sendbird.android.user.User
import com.sendbird.android.user.query.FriendListQuery
import com.sendbird.chat.sample.groupchannel.friends.R
import com.sendbird.chat.sample.groupchannel.friends.databinding.ActivityChatMemberListBinding
import com.sendbird.chat.sample.groupchannel.friends.util.Constants
import com.sendbird.chat.sample.groupchannel.friends.util.showToast

class ChatMemberListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatMemberListBinding
    private lateinit var adapter: ChatMemberListAdapter
    private var currentChannel: GroupChannel? = null
    private var channelUrl: String? = null

    private var showFriends: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMemberListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL)
        showFriends = intent.getBooleanExtra(ShowFriendsKey, false)
        init()
        initRecyclerView()
        if (channelUrl != null && !showFriends) {
            getGroupChannel()
        } else {
            getFriendsFromQuery()
        }
    }

    private fun init() {
        binding.toolbar.title =
            if (!showFriends) getString(R.string.members_list) else getString(R.string.friends)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.buttonAddFriends.apply {
            isVisible = showFriends
            setOnClickListener { openSelectUserActivity() }
        }
    }

    private fun openSelectUserActivity() {
        val memberIds = ArrayList(adapter.currentList.map { it.userId })
        val intent = Intent(this, SelectUserActivity::class.java)
        intent.putExtra(Constants.INTENT_KEY_BASE_USER, memberIds)
    }

    private fun initRecyclerView() {
        adapter = ChatMemberListAdapter{ user, isFriend ->
            if (isFriend) {
                deleteFriend(user)
            } else {
                addFriend(user)
            }
        }
        binding.recyclerviewMember.adapter = adapter
        binding.recyclerviewMember.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
    }

    private fun addFriend(user: User) {
        SendbirdChat.addFriends(listOf(user.userId)) { _, e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to add friend")
                return@addFriends
            }
            getFriendsFromQuery()
        }
    }

    private fun deleteFriend(friend: User) {
        SendbirdChat.deleteFriend(friend.userId) handler@{ e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to delete friend")
                return@handler
            }
            getFriendsFromQuery()
        }
    }

    private fun getGroupChannel() {
        val url = channelUrl
        if (url.isNullOrBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }
        GroupChannel.getChannel(url) { groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                finish()
                return@getChannel
            }
            if (groupChannel != null) {
                currentChannel = groupChannel
                adapter.submitList(groupChannel.members)
                getFriendsFromQuery()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getFriendsFromQuery() {
        val query = SendbirdChat.createFriendListQuery(FriendListQueryParams())
        val users = mutableListOf<User>()
        fetchAllFriends(query, users) {
            adapter.setFriends(users)
            if (showFriends) {
                adapter.submitList(users)
            } else {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun fetchAllFriends(
        query: FriendListQuery,
        users: MutableList<User>,
        onLoadFinished: () -> Unit
    ) {
        getFriendsFromQuery(query) internal@{ usersFetched ->
            users.addAll(usersFetched)
            if (usersFetched.isEmpty()) {
                onLoadFinished()
                return@internal
            }
            fetchAllFriends(query, users, onLoadFinished)
        }
    }

    private fun getFriendsFromQuery(query: FriendListQuery, onLoadFinished: (List<User>) -> Unit) {
        if (query.hasNext) {
            query.next { users, e ->
                if (e != null) {
                    e.printStackTrace()
                    onLoadFinished(emptyList())
                    return@next
                }
                onLoadFinished(users ?: emptyList())
            }
        } else {
            onLoadFinished(emptyList())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val ShowFriendsKey = "show_friends"
    }
}