package com.sendbird.chat.sample.openchannel.categorizemessages.ui.openchannel

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.handler.ConnectionHandler
import com.sendbird.android.handler.OpenChannelHandler
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.ThumbnailSize
import com.sendbird.android.message.UserMessage
import com.sendbird.android.params.*
import com.sendbird.chat.module.ui.ChatInputView
import com.sendbird.chat.module.utils.*
import com.sendbird.chat.sample.openchannel.categorizemessages.R
import com.sendbird.chat.sample.openchannel.categorizemessages.databinding.ActivityOpenChannelChatBinding

class OpenChannelChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpenChannelChatBinding
    private lateinit var adapter: OpenChannelChatAdapter
    private lateinit var recyclerObserver: ChatRecyclerDataObserver
    private var currentOpenChannel: OpenChannel? = null
    private var channelUrl: String = ""
    private var channelTitle: String = ""
    private var hasPrevious: Boolean = true
    private var isMessageLoading: Boolean = false
    private var changelogToken: String? = null

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            SendbirdChat.autoBackgroundDetection = true
            if (data.resultCode == RESULT_OK) {
                val uri = data.data?.data
                sendFileMessage(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenChannelChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL) ?: ""
        channelTitle = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_TITLE) ?: ""

        init()
        initRecyclerView()
        enterChannel(channelUrl)
        addHandler()
    }

    private fun init() {
        binding.toolbar.title = channelTitle
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
                    startForResult,
                    this@OpenChannelChatActivity
                )
            }
        })
    }

    private fun initRecyclerView() {
        adapter = OpenChannelChatAdapter({ baseMessage, view, _ ->
            view.setOnCreateContextMenuListener { contextMenu, _, _ ->
                val currentUser = SendbirdChat.currentUser
                if (currentUser != null && baseMessage.sender?.userId == currentUser.userId) {
                    val deleteMenu =
                        contextMenu.add(Menu.NONE, 0, 0, getString(R.string.delete))
                    deleteMenu.setOnMenuItemClickListener {
                        deleteMessage(baseMessage)
                        return@setOnMenuItemClickListener false
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
                                { updateMessage(it, baseMessage) },
                            )
                            return@setOnMenuItemClickListener false
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
                    0 -> resendMessage(it)
                    1 -> adapter.deletePendingMessage(it)
                }
            }
        })
        binding.recyclerviewChat.adapter = adapter
        binding.recyclerviewChat.itemAnimator = null
        recyclerObserver = ChatRecyclerDataObserver(binding.recyclerviewChat, adapter)
        adapter.registerAdapterDataObserver(recyclerObserver)
        binding.recyclerviewChat.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    if (hasPrevious && !isMessageLoading && adapter.currentList.isNotEmpty()) {
                        loadMessagesPreviousMessages(adapter.currentList.first().createdAt)
                    }
                }
            }
        })
    }

    private fun enterChannel(channelUrl: String) {
        if (channelUrl.isBlank()) {
            showToast(getString(R.string.channel_url_error))
            finish()
            return
        }
        OpenChannel.getChannel(channelUrl) { openChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@getChannel
            }
            openChannel?.enter { e2 ->
                if (e2 != null) {
                    showToast("${e2.message}")
                    return@enter
                }
                currentOpenChannel = openChannel
                loadMessagesPreviousMessages(Long.MAX_VALUE)
            }
        }
    }

    private fun addHandler() {
        SendbirdChat.addConnectionHandler(
            Constants.CONNECTION_HANDLER_ID,
            object : ConnectionHandler {
                override fun onReconnectStarted() {}

                override fun onReconnectSucceeded() {
                    if (changelogToken != null) {
                        getMessageChangeLogsSinceToken()
                    } else {
                        val lastMessage = adapter.currentList.lastOrNull()
                        if (lastMessage != null) {
                            getMessageChangeLogsSinceTimestamp(lastMessage.createdAt)
                        }
                    }
                    loadToLatestMessages(adapter.currentList.lastOrNull()?.createdAt ?: 0)
                }

                override fun onConnected(userId: String) {}

                override fun onDisconnected(userId: String) {}

                override fun onReconnectFailed() {}
            })

        SendbirdChat.addChannelHandler(Constants.CHANNEL_HANDLER_ID, object : OpenChannelHandler() {

            override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
                if (channel.url == currentOpenChannel?.url) {
                    adapter.addMessage(message)
                }
            }

            override fun onMessageDeleted(channel: BaseChannel, msgId: Long) {
                if (channel.url == currentOpenChannel?.url) {
                    adapter.deleteMessages(listOf(msgId))
                }
            }

            override fun onMessageUpdated(channel: BaseChannel, message: BaseMessage) {
                if (channel.url == currentOpenChannel?.url) {
                    adapter.updateMessages(listOf(message))
                }
            }

            override fun onChannelDeleted(
                channelUrl: String,
                channelType: ChannelType
            ) {
                showToast(R.string.channel_deleted_event_msg)
                finish()
            }

            override fun onChannelChanged(channel: BaseChannel) {
                updateChannel(channel as OpenChannel)
            }

        })
    }

    private fun deleteMessage(baseMessage: BaseMessage) {
        currentOpenChannel?.deleteMessage(baseMessage) {
            if (it != null) {
                showToast("${it.message}")
            }
        }
    }

    private fun updateMessage(msg: String, baseMessage: BaseMessage) {
        if (msg.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        val params = UserMessageUpdateParams()
            .apply {
                message = msg
            }
        currentOpenChannel?.updateUserMessage(
            baseMessage.messageId,
            params
        ) { message, e ->
            if (e != null) {
                showToast("${e.message}")
                return@updateUserMessage
            }
            if (message != null) {
                adapter.updateMessages(
                    listOf(message)
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                showAlertDialog(
                    getString(R.string.delete_channel),
                    getString(R.string.channel_delete_msg),
                    getString(R.string.delete),
                    getString(R.string.cancel),
                    { deleteChannel() },
                )
                true
            }

            R.id.update_channel_name -> {
                val channel = currentOpenChannel ?: return true
                showInputDialog(
                    getString(R.string.update),
                    null,
                    channel.name,
                    getString(R.string.update),
                    getString(R.string.cancel),
                    { updateChannel(it, channel) },
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

    private fun deleteChannel() {
        currentOpenChannel?.delete {
            if (it != null) {
                showToast("$it")
            }
            finish()
        }
    }

    private fun updateChannel(name: String, channel: OpenChannel) {
        if (name.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        val params = OpenChannelUpdateParams()
            .apply { this.name = name }
        channel.updateChannel(params) { _, e ->
            if (e != null) {
                showToast("${e.message}")
            }
        }
    }

    private fun loadMessagesPreviousMessages(
        timeStamp: Long,
    ) {
        val channel = currentOpenChannel ?: return
        isMessageLoading = true
        val params = MessageListParams().apply {
            previousResultSize = 20
            nextResultSize = 0
            reverse = false
        }
        channel.getMessagesByTimestamp(timeStamp, params) { messages, e ->
            if (e != null) {
                showToast("${e.message}")
            }
            if (messages != null) {
                if (messages.isNotEmpty()) {
                    hasPrevious = messages.size >= params.previousResultSize
                    adapter.addPreviousMessages(messages)
                } else {
                    hasPrevious = false
                }
            }
            isMessageLoading = false
        }
    }

    private fun loadToLatestMessages(timeStamp: Long) {
        val channel = currentOpenChannel ?: return
        isMessageLoading = true
        val params = MessageListParams().apply {
            nextResultSize = 100
            reverse = false
        }
        channel.getMessagesByTimestamp(timeStamp, params) { messages, e ->
            if (e != null) {
                showToast("${e.message}")
            }
            if (!messages.isNullOrEmpty()) {
                adapter.addNextMessages(messages)
                if (messages.size >= params.nextResultSize) {
                    loadToLatestMessages(messages.last().createdAt)
                } else {
                    isMessageLoading = false
                }
            } else {
                isMessageLoading = false
            }
        }
    }

    private fun sendMessage(msg: String) {
        if (msg.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        val channel = currentOpenChannel ?: return
        val params = UserMessageCreateParams()
            .apply {
                message = msg.trim()
            }
        if (msg.lowercase().contains("alert")) {
            params.customType = "alert"
        }
        binding.chatInputView.clearText()
        val pendingMessage = channel.sendUserMessage(params) { message, e ->
            if (e != null) {
                //failed
                showToast("${e.message}")
                adapter.updatePendingMessage(message)
                return@sendUserMessage
            }
            //succeeded
            adapter.updateSucceedMessage(message)
        }
        //pending
        adapter.addPendingMessage(pendingMessage)
        recyclerObserver.scrollToBottom(true)
    }

    private fun sendFileMessage(imgUri: Uri?) {
        if (imgUri == null) {
            showToast(R.string.file_transfer_error)
            return
        }
        val channel = currentOpenChannel ?: return
        val thumbnailSizes = listOf(
            ThumbnailSize(100, 100),
            ThumbnailSize(200, 200)
        )
        val fileInfo = FileUtils.getFileInfo(imgUri, applicationContext)
        if (fileInfo != null) {
            val params = FileMessageCreateParams().apply {
                file = fileInfo.file
                fileName = fileInfo.name
                fileSize = fileInfo.size
                this.thumbnailSizes = thumbnailSizes
                mimeType = fileInfo.mime
            }
            val pendingMessage = channel.sendFileMessage(
                params
            ) sendFileMessageLabel@{ fileMessage, e ->
                if (e != null) {
                    //failed
                    showToast("${e.message}")
                    adapter.updatePendingMessage(fileMessage)
                    return@sendFileMessageLabel
                }
                //succeeded
                adapter.updateSucceedMessage(fileMessage)
            }
            //pending
            adapter.addPendingMessage(pendingMessage)
            recyclerObserver.scrollToBottom(true)
        } else {
            showToast(R.string.file_transfer_error)
        }
    }

    private fun resendMessage(baseMessage: BaseMessage) {
        val channel = currentOpenChannel ?: return
        when (baseMessage) {
            is UserMessage -> {
                channel.resendMessage(baseMessage, null)
            }
            is FileMessage -> {
                val params = baseMessage.messageCreateParams
                if (params != null) {
                    channel.resendMessage(
                        baseMessage,
                        params.file
                    ) { _, _ -> }
                }
            }
        }
    }

    private fun getMessageChangeLogsSinceTimestamp(timeStamp: Long) {
        val channel = currentOpenChannel ?: return
        val params = MessageChangeLogsParams()
        channel.getMessageChangeLogsSinceTimestamp(
            timeStamp,
            params
        ) getMessageChangeLogsSinceTimestampLabel@{ updatedMessages, deletedMessageIds, hasMore, token, e ->
            if (e != null) {
                showToast("$e")
                return@getMessageChangeLogsSinceTimestampLabel
            }
            adapter.updateMessages(updatedMessages)
            adapter.deleteMessages(deletedMessageIds)
            changelogToken = token
            if (hasMore) {
                getMessageChangeLogsSinceToken()
            }

        }
    }

    private fun getMessageChangeLogsSinceToken() {
        if (changelogToken == null) return
        val channel = currentOpenChannel
        if (channel == null) {
            showToast(R.string.channel_error)
            return
        }
        val params = MessageChangeLogsParams()
        channel.getMessageChangeLogsSinceToken(
            changelogToken,
            params
        ) getMessageChangeLogsSinceTokenLabel@{ updatedMessages, deletedMessageIds, hasMore, token, e ->
            if (e != null) {
                showToast("$e")
                return@getMessageChangeLogsSinceTokenLabel
            }
            adapter.updateMessages(updatedMessages)
            adapter.deleteMessages(deletedMessageIds)
            changelogToken = token
            if (hasMore) {
                getMessageChangeLogsSinceToken()
            }
        }
    }

    private fun updateChannel(openChannel: OpenChannel) {
        currentOpenChannel = openChannel
        binding.toolbar.title = openChannel.name
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        SendbirdChat.removeConnectionHandler(Constants.CONNECTION_HANDLER_ID)
        SendbirdChat.removeChannelHandler(Constants.CHANNEL_HANDLER_ID)
        SendbirdChat.autoBackgroundDetection = true
        currentOpenChannel?.exit {}
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        showToast(getString(R.string.permission_granted))
                        SendbirdChat.autoBackgroundDetection = false
                        FileUtils.selectFile(
                            Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                            startForResult,
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
}