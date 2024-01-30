package com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui.groupchannel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.ThumbnailSize
import com.sendbird.android.message.UserMessage
import com.sendbird.android.params.FileMessageCreateParams
import com.sendbird.android.params.GroupChannelUpdateParams
import com.sendbird.android.params.UserMessageCreateParams
import com.sendbird.chat.module.ui.ChatInputView
import com.sendbird.chat.module.utils.ChatRecyclerDataObserver
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.FileUtils
import com.sendbird.chat.module.utils.TextUtils
import com.sendbird.chat.module.utils.copy
import com.sendbird.chat.module.utils.showAlertDialog
import com.sendbird.chat.module.utils.showInputDialog
import com.sendbird.chat.module.utils.showListDialog
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.localcaching.ktx.R
import com.sendbird.chat.sample.groupchannel.localcaching.ktx.databinding.ActivityGroupChannelChatBinding
import com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui.user.ChatMemberListActivity
import com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui.user.SelectUserActivity
import com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui.vm.ChannelEvent
import com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui.vm.ChannelViewModel
import com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui.vm.MessageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GroupChannelChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChannelChatBinding
    private lateinit var adapter: GroupChannelChatAdapter
    private lateinit var recyclerObserver: ChatRecyclerDataObserver
    private val viewModel: ChannelViewModel by viewModels()

    private val startForResultFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            SendbirdChat.autoBackgroundDetection = true
            if (data.resultCode == RESULT_OK) {
                val uri = data.data?.data
                sendFileMessage(uri)
            }
        }
    private val startForResultInvite =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            if (data.resultCode == RESULT_OK) {
                val selectIds = data.data?.getStringArrayListExtra(Constants.INTENT_KEY_SELECT_USER)
                inviteUser(selectIds)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChannelChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPage()
    }

    private fun initPage() {
        val intent = intent
        val channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL) ?: ""
        val channelTitle = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_TITLE) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.chatInputView.setOnSendMessageClickListener(object :
            ChatInputView.OnSendMessageClickListener {
            override fun onUserMessageSend() {
                val message = binding.chatInputView.getText()
                sendMessage(message)
            }

            override fun onFileMessageSend() {
                SendbirdChat.autoBackgroundDetection = false
                FileUtils.selectFile(
                    Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                    startForResultFile,
                    this@GroupChannelChatActivity
                )
            }
        })
        initRecyclerView()
        bindEvents()
        viewModel.initialize(channelUrl)
            .onEach {
                val currentChannel = viewModel.channel
                if (channelTitle == TextUtils.CHANNEL_DEFAULT_NAME) {
                    binding.toolbar.title = TextUtils.getGroupChannelTitle(currentChannel)
                } else {
                    binding.toolbar.title = channelTitle
                }
            }.catch {
                showToast(it.message ?: getString(R.string.channel_error))
                finish()
            }.launchIn(lifecycleScope)
    }

    private fun bindEvents() {
        viewModel.exceptionalMessage.observe(this) {
            showToast(it)
        }
        viewModel.messageEvent.observe(this) {
            val messages = it.messages
            val pendingMessages = it.pendingMessages
            when (it.event) {
                MessageEvent.INIT_FROM_CACHE -> {
                    adapter.addMessages(messages)
                    adapter.addPendingMessages(pendingMessages)
                }
                MessageEvent.INIT_FROM_API -> {
                    adapter.changeMessages(messages, false)
                    adapter.addPendingMessages(pendingMessages)
                }
                MessageEvent.LOADED_FROM_PREV -> adapter.addPreviousMessages(messages)
                MessageEvent.LOADED_FROM_NEXT -> adapter.addNextMessages(messages)
                MessageEvent.UPDATED -> adapter.updateSucceedMessages(messages)
                MessageEvent.ADDED -> adapter.addMessages(messages)
                MessageEvent.PENDING_ADDED -> adapter.addPendingMessages(messages)
                MessageEvent.DELETED -> adapter.deleteMessages(messages)
                MessageEvent.PENDING_UPDATED -> adapter.updatePendingMessages(messages)
                MessageEvent.PENDING_DELETED -> adapter.deletePendingMessages(messages)
                MessageEvent.HUGEGAP_DETECTED -> {
                    val position: Int =
                        (binding.recyclerviewChat.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val sp = if (position >= 0) {
                        adapter.currentList[position].createdAt
                    } else {
                        viewModel.startingPoint
                    }

                    viewModel.createAndInitCollection(sp)
                }
            }
        }
        viewModel.channelEvent.observe(this) {
            val channel = it.second
            when (it.first) {
                ChannelEvent.UPDATED -> updateChannelView(channel)
                ChannelEvent.DELETED -> {
                    showToast(R.string.channel_deleted_event_msg)
                    finish()
                }
            }
        }
    }

    private fun initRecyclerView() {
        adapter = GroupChannelChatAdapter({ baseMessage, view ->
            view.setOnCreateContextMenuListener { contextMenu, _, _ ->
                if (SendbirdChat.currentUser != null && baseMessage.sender?.userId == SendbirdChat.currentUser!!.userId) {
                    val deleteMenu =
                        contextMenu.add(Menu.NONE, 0, 0, getString(R.string.delete))
                    deleteMenu.setOnMenuItemClickListener {
                        viewModel.deleteMessage(baseMessage)
                        return@setOnMenuItemClickListener true
                    }
                    if (baseMessage is UserMessage) {
                        val updateMenu =
                            contextMenu.add(Menu.NONE, 1, 1, getString(R.string.update))
                        updateMenu.setOnMenuItemClickListener {
                            showInputDialog(
                                getString(R.string.update),
                                null,
                                baseMessage.message,
                                getString(R.string.update),
                                getString(R.string.cancel),
                                { viewModel.updateMessage(it, baseMessage) },
                            )
                            return@setOnMenuItemClickListener true
                        }
                    }
                }
                if (baseMessage is UserMessage) {
                    val copyMenu = contextMenu.add(Menu.NONE, 2, 2, getString(R.string.copy))
                    copyMenu.setOnMenuItemClickListener {
                        copy(baseMessage.message)
                        return@setOnMenuItemClickListener true
                    }
                }
            }
        }, {
            showListDialog(
                listOf(getString(R.string.retry), getString(R.string.delete))
            ) { _, position ->
                when (position) {
                    0 -> viewModel.resendMessage(it)
                    1 -> adapter.deletePendingMessages(mutableListOf(it))
                }
            }
        })
        binding.recyclerviewChat.itemAnimator = null
        binding.recyclerviewChat.adapter = adapter
        recyclerObserver = ChatRecyclerDataObserver(binding.recyclerviewChat, adapter)
        adapter.registerAdapterDataObserver(recyclerObserver)

        binding.recyclerviewChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    viewModel.loadPrevious()
                } else if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadNext()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                showAlertDialog(
                    getString(R.string.delete_channel),
                    getString(R.string.channel_delete_msg),
                    getString(R.string.delete),
                    getString(R.string.cancel),
                    { viewModel.deleteChannel() },
                )
                true
            }

            R.id.leave -> {
                showAlertDialog(
                    getString(R.string.leave_channel),
                    getString(R.string.channel_leave_msg),
                    getString(R.string.leave),
                    getString(R.string.cancel),
                    { viewModel.leaveChannel() },
                )
                true
            }

            R.id.member_list -> {
                val intent = Intent(this, ChatMemberListActivity::class.java)
                intent.putExtra(Constants.INTENT_KEY_CHANNEL_URL, viewModel.channel.url)
                startActivity(intent)
                true
            }

            R.id.invite -> {
                selectInviteUser()
                true
            }

            R.id.update_channel_name -> {
                val channel = viewModel.channel
                showInputDialog(
                    getString(R.string.update),
                    null,
                    channel.name,
                    getString(R.string.update),
                    getString(R.string.cancel),
                    { updateChannelView(it, channel) },
                )
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun selectInviteUser() {
        val channel = viewModel.channel
        val memberIds = ArrayList(channel.members.map { it.userId })
        val intent = Intent(this, SelectUserActivity::class.java)
        intent.putExtra(Constants.INTENT_KEY_BASE_USER, memberIds)
        startForResultInvite.launch(intent)
    }

    private fun inviteUser(selectIds: List<String>?) {
        if (!selectIds.isNullOrEmpty()) {
            viewModel.inviteUsers(selectIds)
        }
    }

    private fun updateChannelView(name: String, channel: GroupChannel) {
        if (name.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        if (channel.name != name) {
            viewModel.updateChannel(GroupChannelUpdateParams().apply { this.name = name })
        }
    }

    private fun sendMessage(message: String) {
        if (message.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        binding.chatInputView.clearText()
        recyclerObserver.scrollToBottom(true)
        viewModel.sendUserMessage(UserMessageCreateParams().apply { this.message = message.trim() })
    }

    private fun sendFileMessage(imgUri: Uri?) {
        if (imgUri == null) {
            showToast(R.string.file_transfer_error)
            return
        }

        lifecycleScope.launch(Dispatchers.Default) {
            FileUtils.getFileInfo(imgUri, applicationContext)?.let { fileInfo ->
                recyclerObserver.scrollToBottom(true)
                viewModel.sendFileMessage(
                    FileMessageCreateParams().apply {
                        file = fileInfo.file
                        fileName = fileInfo.name
                        fileSize = fileInfo.size
                        thumbnailSizes = listOf(
                            ThumbnailSize(100, 100),
                            ThumbnailSize(200, 200)
                        )
                        mimeType = fileInfo.mime
                    }
                )
            } ?: run {
                showToast(R.string.file_transfer_error)
            }
        }
    }

    private fun updateChannelView(groupChannel: GroupChannel) {
        binding.toolbar.title =
            if (groupChannel.name.isBlank() || groupChannel.name == TextUtils.CHANNEL_DEFAULT_NAME)
                TextUtils.getGroupChannelTitle(groupChannel)
            else groupChannel.name
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onPause() {
        val lastMessage = adapter.currentList.lastOrNull()
        if (lastMessage != null) {
            viewModel.updateLastVisibleMessageTs(lastMessage)
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        SendbirdChat.autoBackgroundDetection = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(getString(R.string.permission_granted))
                    SendbirdChat.autoBackgroundDetection = false
                    FileUtils.selectFile(
                        Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                        startForResultFile,
                        this
                    )
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            Constants.PERMISSION_REQUEST_CODE
                        )
                    } else {
                        showToast(getString(R.string.permission_denied))
                    }
                }
            }
        }
    }
}
