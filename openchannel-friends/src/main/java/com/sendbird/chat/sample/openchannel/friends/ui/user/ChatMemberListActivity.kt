package com.sendbird.chat.sample.openchannel.friends.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.params.FriendListQueryParams
import com.sendbird.android.user.User
import com.sendbird.android.user.query.FriendListQuery
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.openchannel.friends.R
import com.sendbird.chat.sample.openchannel.friends.databinding.ActivityChatMemberListBinding

class ChatMemberListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatMemberListBinding
    private lateinit var adapter: ChatMemberListAdapter
    private val startForResultInvite =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            if (data.resultCode == RESULT_OK) {
                val selectIds = data.data?.getStringArrayListExtra(Constants.INTENT_KEY_SELECT_USER)
                addUserFriends(selectIds ?: emptyList())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMemberListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView()
        getFriends()
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.friends)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.buttonAddFriends.apply {
            setOnClickListener { openSelectUserActivity() }
        }
    }

    private fun openSelectUserActivity() {
        val memberIds = ArrayList(adapter.currentList.map { it.userId })
        val intent = Intent(this, SelectUserActivity::class.java)
        intent.putExtra(Constants.INTENT_KEY_BASE_USER, memberIds)
        startForResultInvite.launch(intent)
    }

    private fun addUserFriends(users: List<String>) {
        SendbirdChat.addFriends(users) { usersAdded, e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to add friends")
                return@addFriends
            }
            getFriends()
        }

    }

    private fun initRecyclerView() {
        adapter = ChatMemberListAdapter { user, _ -> deleteFriend(user) }
        binding.recyclerviewMember.adapter = adapter
        binding.recyclerviewMember.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
    }

    private fun deleteFriend(friend: User) {
        SendbirdChat.deleteFriend(friend.userId) handler@{ e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to delete friend")
                return@handler
            }
            getFriends()
        }
    }

    private fun getFriends() {
        val query = SendbirdChat.createFriendListQuery(FriendListQueryParams())
        val users = mutableListOf<User>()
        fetchAllFriends(query, users) {
            adapter.submitList(users)
        }
    }

    private fun fetchAllFriends(query: FriendListQuery, users: MutableList<User>, onLoadFinished: () -> Unit) {
        getFriends(query) internal@{ usersFetched ->
            users.addAll(usersFetched)
            if (usersFetched.isEmpty()) {
                onLoadFinished()
                return@internal
            }
            fetchAllFriends(query, users, onLoadFinished)
        }
    }

    private fun getFriends(query: FriendListQuery, onLoadFinished: (List<User>) -> Unit) {
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
            R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}