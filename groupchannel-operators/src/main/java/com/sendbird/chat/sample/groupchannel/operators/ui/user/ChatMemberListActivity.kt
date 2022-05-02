package com.sendbird.chat.sample.groupchannel.operators.ui.user

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.user.User
import com.sendbird.android.user.query.OperatorListQuery
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.operators.R
import com.sendbird.chat.sample.groupchannel.operators.databinding.ActivityChatMemberListBinding

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
                getOperators()
            }
        }
    }

    private fun getOperators() {
        val channel = currentChannel ?: return
        val query = channel.createOperatorListQuery()
        val operators = mutableListOf<User>()
        getOperators(query, operators) {
            adapter.submitList(operators)
        }
    }

    private fun getOperators(query: OperatorListQuery, operators: MutableList<User>, onQueryFinished: () -> Unit) {
        query.getOperators internal@{
            if (it.isEmpty()) {
                onQueryFinished.invoke()
                return@internal
            }
            operators.addAll(it)
            getOperators(query, operators, onQueryFinished)
        }
    }

    private fun OperatorListQuery.getOperators(onOperatorsReceived: (List<User>) -> Unit) {
        if (hasNext) {
            next { result, exception ->
                if (exception != null) {
                    exception.printStackTrace()
                    onOperatorsReceived(emptyList())
                    return@next
                }
                if (result == null) {
                    onOperatorsReceived(emptyList())
                    return@next
                }
                onOperatorsReceived(result)
            }
            return
        }
        onOperatorsReceived(emptyList())
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