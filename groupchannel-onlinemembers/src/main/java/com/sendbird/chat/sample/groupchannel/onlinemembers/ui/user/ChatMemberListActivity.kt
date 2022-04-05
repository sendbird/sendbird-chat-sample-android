package com.sendbird.chat.sample.groupchannel.onlinemembers.ui.user

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.user.User
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityChatMemberListBinding

class ChatMemberListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatMemberListBinding
    private lateinit var adapter: ChatMemberListAdapter
    private var currentChannel: GroupChannel? = null
    private var channelUrl: String? = null
    private val query = SendbirdChat.createApplicationUserListQuery()
    private val handler = Handler(Looper.getMainLooper())
    private val queryRunnable: () -> Unit = { query.next(::onNextQuery) }

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
                adapter.submitList(groupChannel.members)
                startListeningForOnlineMembers()
            }
        }
    }

    private fun startListeningForOnlineMembers() {
        addUsersToQuery()
        startPeriodicCheck()
    }

    private fun addUsersToQuery() {
        val channel = currentChannel ?: return
        val membersIds = channel.members.map { it.userId }
        query.setUserIdsFilter(membersIds)
    }

    private fun startPeriodicCheck() {
        handler.postDelayed(queryRunnable, CheckIntervalMillis)
    }

    private fun onNextQuery(users: List<User>?, exception: Exception?) {
        if (exception != null) {
            exception.printStackTrace()
            return
        }
        users ?: return
        adapter.submitList(users.sortedBy { it.nickname })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(queryRunnable)
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
        private const val CheckIntervalMillis = 60 * 1_000L //one minute
    }
}