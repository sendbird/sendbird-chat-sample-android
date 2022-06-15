package com.sendbird.chat.sample.groupchannel.membersandoperators.ui.user

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.MemberListQueryParams
import com.sendbird.android.user.Member
import com.sendbird.android.user.query.MemberListQuery
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityChatMemberListBinding

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
        adapter = ChatMemberListAdapter { _, _ -> }
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
                getMembersAndOperators()
            }
        }
    }

    private fun getMembersAndOperators() {
        val channel = currentChannel ?: return
        val listQuery = channel.createMemberListQuery(
            MemberListQueryParams(order = MemberListQuery.Order.OPERATOR_THEN_MEMBER_ALPHABETICAL)
        )
        while (listQuery.hasNext) {
            listQuery.next(::onMembersAndOperatorsReceived)
        }
    }

    private fun onMembersAndOperatorsReceived(members: List<Member>?, exception: Exception?) {
        if (exception != null) {
            exception.printStackTrace()
            return
        }
        val combinedList: List<Member> = adapter.currentList + (members ?: emptyList())
        adapter.submitList(combinedList)
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