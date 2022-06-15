package com.sendbird.chat.sample.groupchannel.addremoveoperators.ui.user

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.Role
import com.sendbird.android.params.MemberListQueryParams
import com.sendbird.android.user.Member
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.addremoveoperators.R
import com.sendbird.chat.sample.groupchannel.addremoveoperators.databinding.ActivityChatMemberListBinding

class ChatMemberListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatMemberListBinding
    private lateinit var adapter: ChatMemberListAdapter
    private var currentChannel: GroupChannel? = null
    private var channelUrl: String? = null

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
        adapter = ChatMemberListAdapter { member, _ -> changeMemberOperatorStatus(member) }
        binding.recyclerviewMember.adapter = adapter
        binding.recyclerviewMember.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
    }

    private fun changeMemberOperatorStatus(member: Member) {
        if (member.role == Role.OPERATOR) {
            removeOperatorRole(member)
            return
        }
        addOperatorRole(member)
    }

    private fun addOperatorRole(member: Member) {
        currentChannel?.addOperators(listOf(member.userId)) { exception ->
            if (exception != null) {
                showToast("${exception.message}")
                return@addOperators
            }
            showToast("User made operator")
            refreshMembers()
        }
    }

    private fun removeOperatorRole(member: Member) {
        currentChannel?.removeOperators(listOf(member.userId)) { exception ->
            if (exception != null) {
                showToast("${exception.message}")
                return@removeOperators
            }
            showToast("User back to basic")
            refreshMembers()
        }
    }

    private fun refreshMembers() {
        val query = currentChannel?.createMemberListQuery(MemberListQueryParams()) ?: return
        query.next { members, exception ->
            if (exception == null && !members.isNullOrEmpty()) {
                adapter.submitList(members)
            }
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
            }
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
}