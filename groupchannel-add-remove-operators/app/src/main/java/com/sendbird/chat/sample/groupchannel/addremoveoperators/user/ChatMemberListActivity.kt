package com.sendbird.chat.sample.groupchannel.addremoveoperators.user

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.Role
import com.sendbird.android.params.MemberListQueryParams
import com.sendbird.android.user.Member
import com.sendbird.chat.sample.groupchannel.addremoveoperators.util.Constants
import com.sendbird.chat.sample.groupchannel.addremoveoperators.util.showToast
import com.sendbird.chat.sample.groupchannel.addremoveoperators.R
import com.sendbird.chat.sample.groupchannel.addremoveoperators.databinding.ActivityChatMemberListBinding

// This activity called when the user clicks the member list button in the GroupChannelChatActivity.
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
        // Create an adapter about change the operator status of the member
        // So It is possible to change the operator status of the member by clicking the item.
        adapter = ChatMemberListAdapter { member, position -> changeMemberOperatorStatus(member, position) }
        binding.recyclerviewMember.adapter = adapter
        binding.recyclerviewMember.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
    }

    // Change the operator status of the member
    private fun changeMemberOperatorStatus(member: Member, position: Int) {
        // If the current user is the operator, the operator role cannot be changed.
        if (member.role == Role.OPERATOR) {
            // Call the removeOperators() method to remove the operator role from the member.
            removeOperatorRole(member, position)
            return
        }
        // If the current user is not the operator, the operator role can be changed.
        addOperatorRole(member, position)
    }

    // Add operator role to the member
    private fun addOperatorRole(member: Member, position: Int) {
        // Call the addOperators() method to add the operator role to the member.
        currentChannel?.addOperators(listOf(member.userId)) { exception ->
            if (exception != null) {
                showToast("${exception.message}")
                return@addOperators
            }
            showToast("User made operator")
            // Refresh the member if the member is not Operator refresh one more time.
            refreshMember(member, position, true)
        }
    }

    // Remove operator role from the member
    private fun removeOperatorRole(member: Member, position: Int) {
        // Call the removeOperators() method to remove the operator role from the member.
        currentChannel?.removeOperators(listOf(member.userId)) { exception ->
            if (exception != null) {
                showToast("${exception.message}")
                return@removeOperators
            }
            showToast("User back to basic")
            // Refresh the member
            refreshMember(member, position,false)
        }
    }

    // Refresh the member
    private fun refreshMember(member: Member, position: Int, isOperator: Boolean) {
        currentChannel?.createMemberListQuery(MemberListQueryParams())?.next { list, e ->
            if (e != null) {
                showToast("${e.message}")
                return@next
            }
            if (list != null) {
                // check if the member is Operator in the list about same userId from member.
                val operator = list.find { it.userId == member.userId }?.role == Role.OPERATOR
                // and Check operator is same with isOperator
                if (operator == isOperator) {
                    // If the member is Operator and isOperator is true or
                    // If the member is not Operator and isOperator is false
                    // Refresh the member
                    adapter.notifyItemChanged(position)
                    return@next
                }else{
                    // refresh one more time
                    refreshMember(member, position, isOperator)
                }
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