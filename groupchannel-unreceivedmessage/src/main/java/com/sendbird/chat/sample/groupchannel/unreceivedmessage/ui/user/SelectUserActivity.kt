package com.sendbird.chat.sample.groupchannel.unreceivedmessage.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.ApplicationUserListQueryParams
import com.sendbird.android.params.GroupChannelCreateParams
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivitySelectUserBinding
import com.sendbird.chat.sample.groupchannel.unreceivedmessage.ui.groupchannel.GroupChannelChatActivity


class SelectUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectUserBinding
    private lateinit var adapter: SelectUserAdapter
    private var userListQuery = SendbirdChat.createApplicationUserListQuery(
        ApplicationUserListQueryParams()
    )
    private var isCreateMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        initRecyclerView()
        loadNextUsers()
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.select_user)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        isCreateMode = intent.getBooleanExtra(Constants.INTENT_KEY_SELECT_USER_MODE_CREATE, false)
    }

    private fun initRecyclerView() {
        adapter = SelectUserAdapter(
            { _, _ -> },
            true,
            intent.getStringArrayListExtra(Constants.INTENT_KEY_SELECT_USER),
            intent.getStringArrayListExtra(Constants.INTENT_KEY_BASE_USER)
        )
        binding.recyclerviewUser.adapter = adapter
        binding.recyclerviewUser.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
        binding.recyclerviewUser.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    loadNextUsers()
                }
            }
        })
    }

    private fun loadNextUsers() {
        if (userListQuery.hasNext) {
            userListQuery.next { users, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@next
                }
                if (!users.isNullOrEmpty()) {
                    adapter.addUsers(users)
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.select)
        item.title = if (isCreateMode) getString(R.string.create) else getString(R.string.select)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select_user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.select -> {
                if (isCreateMode) {
                    createChannel()
                } else {
                    selectUser()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createChannel() {
        if (adapter.selectUserIdSet.isEmpty()) {
            showToast(R.string.select_user_msg)
            return
        }
        val params = GroupChannelCreateParams().apply {
            userIds = adapter.selectUserIdSet.toList()
        }
        GroupChannel.createChannel(params) createChannelLabel@{ groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@createChannelLabel
            }
            if (groupChannel != null) {
                val intent = Intent(
                    this@SelectUserActivity,
                    GroupChannelChatActivity::class.java
                )
                intent.putExtra(Constants.INTENT_KEY_CHANNEL_URL, groupChannel.url)
                intent.putExtra(Constants.INTENT_KEY_CHANNEL_TITLE, groupChannel.name)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun selectUser() {
        val intent = intent
        val arrayList = arrayListOf<String>()
        arrayList.addAll(adapter.selectUserIdSet)
        intent.putExtra(Constants.INTENT_KEY_SELECT_USER, arrayList)
        setResult(RESULT_OK, intent)
        finish()
    }
}