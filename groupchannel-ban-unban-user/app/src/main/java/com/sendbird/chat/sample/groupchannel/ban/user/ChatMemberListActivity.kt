package com.sendbird.chat.sample.groupchannel.ban.user

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.MemberListQueryParams
import com.sendbird.android.user.Member
import com.sendbird.android.user.RestrictedUser
import com.sendbird.android.user.User
import com.sendbird.android.user.query.BannedUserListQuery
import com.sendbird.android.user.query.MemberListQuery
import com.sendbird.chat.sample.groupchannel.ban.R
import com.sendbird.chat.sample.groupchannel.ban.databinding.ActivityChatMemberListBinding
import com.sendbird.chat.sample.groupchannel.ban.util.Constants
import com.sendbird.chat.sample.groupchannel.ban.util.showToast

class ChatMemberListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatMemberListBinding
    private lateinit var adapter: ChatMemberListAdapter
    private var currentChannel: GroupChannel? = null
    private var channelUrl: String? = null
    private var areBannedUsersDisplayed = false

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
        // Create adapter when the user click on a member, we display a menu to ban or unban the user
        adapter = ChatMemberListAdapter { member, view, _ ->
            createMenuForMember(member, view)
        }
        binding.recyclerviewMember.adapter = adapter
        binding.recyclerviewMember.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
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
            }
        }
    }

    // Create menu when long click on a member
    // When the user click on the menu item, we ban or unban the user by calling GroupChannel.banUser() or GroupChannel.unbanUser()
    private fun createMenuForMember(member: User, view: View) {
        view.setOnCreateContextMenuListener { contextMenu, _, _ ->
            if (areBannedUsersDisplayed) {
                val menu = contextMenu.add(Menu.NONE, 0, 0, "UnBan")
                menu.setOnMenuItemClickListener {
                    unbanUser(member)
                    return@setOnMenuItemClickListener true
                }
            } else {
                val menu = contextMenu.add(Menu.NONE, 0, 0, "Ban")
                menu.setOnMenuItemClickListener {
                    banUser(member)
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    // Ban the user for a period of time
    private fun banUser(member: User, periodToBan: Int = -1) {
        val groupChannel = currentChannel ?: return
        groupChannel.banUser(member, "ban reason", periodToBan) handler@{
            if (it != null) {
                showToast("Cannot ban user: ${it.message}")
                return@handler
            }
            retrieveAndDisplayBannedUsers()
            showToast("User banned")
        }
    }

    // Unban the user
    private fun unbanUser(member: User) {
        val groupChannel = currentChannel ?: return
        //we ban the user for an indefinitely period of time
        groupChannel.unbanUser(member) handler@{
            if (it != null) {
                showToast("Cannot unban user: ${it.message}")
                return@handler
            }
            retrieveAndDisplayActiveUsers()
            showToast("User unbanned")
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun retrieveAndDisplayActiveUsers() {
        binding.toolbar.title = getString(R.string.members_list) + " (Active)"
        val groupChannel = currentChannel ?: return
        areBannedUsersDisplayed = false

        val query = groupChannel.createMemberListQuery(MemberListQueryParams())
        val members = mutableListOf<Member>()
        groupChannel.getMembers(query, members) {
            adapter.submitList(members as List<User>?)
        }
    }

    private fun retrieveAndDisplayBannedUsers() {
        binding.toolbar.title = getString(R.string.members_list) + " (Banned)"
        val groupChannel = currentChannel ?: return
        areBannedUsersDisplayed = true

        val query = groupChannel.createBannedUserListQuery()
        val bannedUsers = mutableListOf<RestrictedUser>()
        groupChannel.getUsers(query, bannedUsers) {
            adapter.submitList(bannedUsers as List<User>?)
        }
    }

    private fun GroupChannel.getMembers(
        query: MemberListQuery,
        allUsers: MutableList<Member>,
        onQueryFinished: () -> Unit
    ) {
        query.getMembers internal@{ users ->
            if (users.isEmpty()) {
                onQueryFinished.invoke()
                return@internal
            }
            allUsers.addAll(users)
            getMembers(query, allUsers, onQueryFinished)
        }
    }

    private fun MemberListQuery.getMembers(onUsersReceived: (List<Member>) -> Unit) {
        if (hasNext) {
            next { result, exception ->
                if (exception != null) {
                    exception.printStackTrace()
                    onUsersReceived(emptyList())
                    return@next
                }
                if (result == null) {
                    onUsersReceived(emptyList())
                    return@next
                }
                onUsersReceived(result)
            }
            return
        }
        onUsersReceived(emptyList())
    }

    private fun GroupChannel.getUsers(
        query: BannedUserListQuery,
        allUsers: MutableList<RestrictedUser>,
        onQueryFinished: () -> Unit
    ) {
        query.getUsers internal@{ users ->
            if (users.isEmpty()) {
                onQueryFinished.invoke()
                return@internal
            }
            allUsers.addAll(users)
            getUsers(query, allUsers, onQueryFinished)
        }
    }

    private fun BannedUserListQuery.getUsers(onUsersReceived: (List<RestrictedUser>) -> Unit) {
        if (hasNext) {
            next { result, exception ->
                if (exception != null) {
                    exception.printStackTrace()
                    onUsersReceived(emptyList())
                    return@next
                }
                if (result == null) {
                    onUsersReceived(emptyList())
                    return@next
                }
                onUsersReceived(result)
            }
            return
        }
        onUsersReceived(emptyList())
    }
}