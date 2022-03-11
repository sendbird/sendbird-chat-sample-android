package com.sendbird.chat.sample.groupchannel.ui.groupchannel

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_SELECT_USER
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityGroupChannelCreateBinding
import com.sendbird.chat.sample.groupchannel.ui.user.SelectUserActivity


class GroupChannelCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChannelCreateBinding
    private lateinit var adapter: SelectedUserAdapter

    private var userListQuery: ApplicationUserListQuery? = null
    private val selectUserList: ArrayList<User> = arrayListOf()
    private val selectUserIdList: ArrayList<String> = arrayListOf()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            if (data.resultCode == RESULT_OK) {
                val selectIds = data.data?.getStringArrayListExtra(INTENT_KEY_SELECT_USER)
                if (selectIds != null) {
                    setSelectUserList(selectIds)
                } else {
                    setSelectUserList(arrayListOf())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChannelCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.group_channel_create)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.buttonCreate.setOnClickListener {
            channelCreate(
                binding.radioGroupType.checkedRadioButtonId == R.id.radiobutton_public
            )
        }

        binding.imageviewAdd.setOnClickListener {
            val intent = Intent(this, SelectUserActivity::class.java)
            intent.putExtra(INTENT_KEY_SELECT_USER, selectUserIdList)
            if (SendBird.getCurrentUser() != null) {
                intent.putExtra(
                    Constants.INTENT_KEY_BASE_USER,
                    arrayListOf(SendBird.getCurrentUser().userId)
                )
            }
            startForResult.launch(intent)
        }
        initRecyclerView()

    }

    private fun initRecyclerView() {
        if (SendBird.getCurrentUser() != null) {
            selectUserList.add(SendBird.getCurrentUser())
        }
        if (SendBird.getCurrentUser() != null) {
            selectUserIdList.add(SendBird.getCurrentUser().userId)
        }
        adapter = SelectedUserAdapter()
        binding.recyclerviewSelectParticipant.adapter = adapter
        binding.recyclerviewSelectParticipant.isFocusable = false

        binding.recyclerviewSelectParticipant.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollHorizontally(1)) {
                    nextUserList()
                }
            }
        })
        adapter.submitList(selectUserList.toMutableList())
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

    private fun channelCreate(isPublic: Boolean) {
        when {
            selectUserIdList.isEmpty() -> {
                showToast(R.string.select_user_msg)
            }
            else -> {
                val params = GroupChannelParams()
                    .setPublic(isPublic)
                    .addUserIds(selectUserIdList)
                GroupChannel.createChannel(params) { groupChannel, e ->
                    if (e != null || groupChannel == null) {
                        showToast("${e.message}")
                        finish()
                        return@createChannel
                    }
                    val intent = Intent(
                        this@GroupChannelCreateActivity,
                        GroupChannelChatActivity::class.java
                    )
                    intent.putExtra(INTENT_KEY_CHANNEL_URL, groupChannel.url)
                    intent.putExtra(INTENT_KEY_CHANNEL_TITLE, groupChannel.name)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setSelectUserList(selectIdList: ArrayList<String>) {
        selectUserIdList.clear()
        selectUserList.clear()
        if (SendBird.getCurrentUser() != null) {
            selectUserIdList.add(SendBird.getCurrentUser().userId)
        }
        selectUserIdList.addAll(selectIdList)

        if (selectUserIdList.isNotEmpty()) {
            userListQuery = SendBird.createApplicationUserListQuery().apply {
                setUserIdsFilter(selectUserIdList)
            }
            adapter.submitList(emptyList())
            nextUserList()
        } else {
            adapter.submitList(selectUserList)
        }
    }

    private fun nextUserList() {
        val listQuery = userListQuery ?: return
        if (listQuery.hasNext()) {
            listQuery.next { userList, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@next
                }
                if (userList.isNotEmpty()) {
                    selectUserList.addAll(userList)
                    adapter.submitList(selectUserList.toMutableList())
                }
            }
        }
    }

}