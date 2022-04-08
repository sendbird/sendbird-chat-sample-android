package com.sendbird.chat.sample.groupchannel.bannedandmutedusers.ui.user

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.user.Member
import com.sendbird.android.user.query.GroupChannelMemberListQuery
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityChatMemberListBinding

class ChatMemberListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatMemberListBinding
    private lateinit var adapter: ChatMemberListAdapter
    private var currentChannel: GroupChannel? = null
    private var channelUrl: String? = null
    private var loadNextUsers: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMemberListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL)
        init()
        initRecyclerView()
        getGroupChannel()
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.members_list)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initRecyclerView() {
        adapter = ChatMemberListAdapter { _, _ -> }
        binding.recyclerviewMember.apply {
            adapter = this@ChatMemberListActivity.adapter
            addItemDecoration(
                DividerItemDecoration(
                    this@ChatMemberListActivity,
                    RecyclerView.VERTICAL
                )
            )
            addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1)) {
                        loadNextUsers?.invoke()
                    }
                }
            })
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
                retrieveAndDisplayActiveUsers()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group_channel_member_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.active_users -> {
                retrieveAndDisplayActiveUsers()
                true
            }
            R.id.banned -> {
                retrieveAndDisplayBannedUsers()
                true
            }
            R.id.muted -> {
                retrieveAndDisplayMutedUsers()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun retrieveAndDisplayActiveUsers() {
        val groupChannel = currentChannel ?: return
        val userQuery = groupChannel.createMemberListQuery()
        adapter.submitList(emptyList())
        loadNextUsers = {
            if (userQuery.hasNext) {
                userQuery.next { users, e ->
                    if (e != null) {
                        showToast("${e.message}")
                        return@next
                    }
                    if (!users.isNullOrEmpty()) {
                        val newList = adapter.currentList + users
                        adapter.submitList(newList)
                    }
                }
            }
        }
        loadNextUsers?.invoke()
    }

    private fun retrieveAndDisplayBannedUsers() {
        if (!isUserOperator()) {
            showToast("You are not an Operator")
            return
        }
        val groupChannel = currentChannel ?: return
        val listQuery = groupChannel.createMemberListQuery()
        listQuery.mutedMemberFilter = GroupChannelMemberListQuery.MutedMemberFilter.MUTED
        adapter.submitList(emptyList())
        loadNextUsers = {
            if (listQuery.hasNext) {
                listQuery.next { result, exception ->
                    if (exception != null) {
                        exception.printStackTrace()
                        return@next
                    }
                    result ?: return@next
                    binding.toolbar.title = getString(R.string.banned_users)
                    adapter.submitList(result)
                }
            }
        }
        loadNextUsers?.invoke()
    }

    private fun retrieveAndDisplayMutedUsers() {
        if (!isUserOperator()) {
            showToast("You are not an Operator")
            return
        }
        val groupChannel = currentChannel ?: return
        val listQuery = groupChannel.createBannedUserListQuery()
        adapter.submitList(emptyList())
        loadNextUsers = {
            if (listQuery.hasNext) {
                listQuery.next { result, exception ->
                    if (exception != null) {
                        exception.printStackTrace()
                        return@next
                    }
                    result ?: return@next
                    binding.toolbar.title = getString(R.string.muted_users)
                    val newList = adapter.currentList + result
                    adapter.submitList(newList)
                }
            }
        }
        loadNextUsers?.invoke()
    }

    private fun isUserOperator(): Boolean {
        val currentChannel = currentChannel ?: return false
        val currentUser = SendbirdChat.currentUser ?: return false
        val member =
            currentChannel.members.firstOrNull { it.userId == currentUser.userId } ?: return false
        return member.role == Member.Role.OPERATOR
    }
}