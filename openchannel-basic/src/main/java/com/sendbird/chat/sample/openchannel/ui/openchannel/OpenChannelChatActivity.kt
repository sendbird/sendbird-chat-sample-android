package com.sendbird.chat.sample.openchannel.ui.openchannel

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import com.sendbird.chat.module.ui.ChatInputView
import com.sendbird.chat.module.utils.*
import com.sendbird.chat.module.utils.Constants.CHANNEL_HANDLER_ID
import com.sendbird.chat.module.utils.Constants.CONNECTION_HANDLER_ID
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL
import com.sendbird.chat.module.utils.Constants.PERMISSION_REQUEST_CODE
import com.sendbird.chat.sample.openchannel.R
import com.sendbird.chat.sample.openchannel.databinding.ActivityOpenChannelChatBinding


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
            SendBird.setAutoBackgroundDetection(true)
            if (data.resultCode == RESULT_OK) {
                data.data?.data?.let {
                    sendFileMessage(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenChannelChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        channelUrl = intent.getStringExtra(INTENT_KEY_CHANNEL_URL) ?: ""
        channelTitle = intent.getStringExtra(INTENT_KEY_CHANNEL_TITLE) ?: ""

        init()
        addHandler()
    }

    private fun init() {
        binding.toolbar.title = channelTitle
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initRecyclerView()
        enterChannel(channelUrl)

        binding.chatInputView.setOnSendMessageClickListener(object :
            ChatInputView.OnSendMessageClickListener {
            override fun onUserMessageSend() {
                val message = binding.chatInputView.getText()
                sendMessage(message)
            }

            override fun onFileMessageSend() {
                SendBird.setAutoBackgroundDetection(false)
                FileUtils.selectFile(
                    Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                    startForResult,
                    this@OpenChannelChatActivity
                )
            }
        })
    }

    private fun initRecyclerView() {
        adapter =
            OpenChannelChatAdapter({ baseMessage, view, position ->
                view.setOnCreateContextMenuListener { contextMenu, _, _ ->
                    if (SendBird.getCurrentUser() != null && baseMessage.sender.userId == SendBird.getCurrentUser().userId) {
                        val deleteMenu =
                            contextMenu.add(Menu.NONE, 0, 0, getString(R.string.delete))
                        deleteMenu.setOnMenuItemClickListener {
                            currentOpenChannel?.deleteMessage(baseMessage) {
                                if (it != null) {
                                    showToast("${it.message}")
                                }
                            }
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
                                    {
                                        if (it.isBlank()) {
                                            showToast(R.string.enter_message_msg)
                                            return@showInputDialog
                                        }
                                        val params = UserMessageParams()
                                            .setMessage(it)
                                        currentOpenChannel?.updateUserMessage(
                                            baseMessage.messageId,
                                            params
                                        ) { baseMessage, e ->
                                            if (e != null) {
                                                showToast("${e.message}")
                                                return@updateUserMessage
                                            }
                                            adapter.updateItem(
                                                baseMessage
                                            )
                                        }
                                    },
                                )
                                return@setOnMenuItemClickListener false
                            }
                        }
                    }
                }
            }, {
                showListDialog(
                    listOf(getString(R.string.retry), getString(R.string.delete))
                ) { _, position ->
                    when (position) {
                        0 -> resendMessage(it)
                        1 -> adapter.deletePendingItem(it)
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

    private fun addHandler() {
        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, object : SendBird.ConnectionHandler {
            override fun onReconnectStarted() {
            }

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

            override fun onReconnectFailed() {
            }
        })

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel?, baseMessage: BaseMessage?) {
                val channel = currentOpenChannel ?: return
                if (baseChannel != null && baseMessage != null) {
                    if (baseChannel.url == channel.url) {
                        adapter.addItem(baseMessage)
                    }
                }
            }

            override fun onMessageDeleted(baseChannel: BaseChannel?, msgId: Long) {
                val channel = currentOpenChannel ?: return
                if (baseChannel != null) {
                    if (baseChannel.url == channel.url) {
                        adapter.deleteItem(msgId)
                    }
                }
                super.onMessageDeleted(baseChannel, msgId)
            }

            override fun onMessageUpdated(baseChannel: BaseChannel?, baseMessage: BaseMessage?) {
                val channel = currentOpenChannel ?: return
                if (baseChannel != null && baseMessage != null) {
                    if (baseChannel.url == channel.url) {
                        adapter.updateItem(baseMessage)
                    }
                }
                super.onMessageUpdated(baseChannel, baseMessage)
            }

            override fun onChannelChanged(channel: BaseChannel?) {
                super.onChannelChanged(channel)
                updateChannel(channel as OpenChannel)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                showAlertDialog(
                    getString(R.string.delete_channel),
                    getString(R.string.channel_delete_msg),
                    getString(R.string.delete),
                    getString(R.string.cancel),
                    {
                        currentOpenChannel?.delete {
                            if (it != null) {
                                showToast("$it")
                            }
                            finish()
                        }
                    },
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
                    {
                        if (it.isBlank()) {
                            showToast(R.string.enter_message_msg)
                            return@showInputDialog
                        }
                        val params = OpenChannelParams()
                            .setName(it)
                        channel.updateChannel(
                            params
                        ) { _, e ->
                            if (e != null) {
                                showToast("${e.message}")
                            }
                        }
                    },
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

    private fun enterChannel(channelUrl: String) {
        if (channelUrl.isBlank()) {
            showToast(getString(R.string.channel_url_error))
            finish()
            return
        }
        OpenChannel.getChannel(
            channelUrl
        ) { openChannel, e ->
            if (e != null || openChannel == null) {
                showToast(getString(R.string.error) + e.message)
            } else {
                openChannel.enter { e2 ->
                    if (e2 != null) {
                        showToast(getString(R.string.error) + e2.message)
                    }
                    currentOpenChannel = openChannel
                    loadMessagesPreviousMessages(Long.MAX_VALUE)
                }
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
            setReverse(false)
        }
        channel.getMessagesByTimestamp(
            timeStamp, params
        ) { messages, e ->
            if (e != null) {
                showToast("${e.message}")
            } else {
                if (!messages.isNullOrEmpty()) {
                    hasPrevious = messages.size >= params.previousResultSize
                    adapter.addPreviousItems(messages)
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
            setReverse(false)
        }
        channel.getMessagesByTimestamp(
            timeStamp,
            params
        ) { messages, e ->
            if (e != null) {
                showToast("${e.message}")
            }
            if (!messages.isNullOrEmpty()) {
                adapter.addNextItems(messages)
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

    private fun sendMessage(message: String) {
        if (message.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        val channel = currentOpenChannel ?: return
        val params: UserMessageParams = UserMessageParams().setMessage(message.trim())
        binding.chatInputView.clearText()
        val pendingMessage = channel.sendUserMessage(params) { userMessage, e ->
            if (e != null) {
                //failed
                adapter.updatePendingItem(userMessage)
                return@sendUserMessage
            }
            //succeeded
            adapter.updateSucceedItem(userMessage)
            recyclerObserver.scrollToBottom(true)
        }
        //pending
        adapter.addPendingItem(pendingMessage)
    }

    private fun sendFileMessage(imgUri: Uri) {
        val channel = currentOpenChannel ?: return
        val thumbnailSizes = listOf(
            FileMessage.ThumbnailSize(100, 100),
            FileMessage.ThumbnailSize(200, 200)
        )
        val fileInfo = FileUtils.getFileInfo(imgUri, applicationContext)
        if (fileInfo != null) {
            val params = FileMessageParams()
                .setFile(fileInfo.file)
                .setFileName(fileInfo.name)
                .setFileSize(fileInfo.size)
                .setThumbnailSizes(thumbnailSizes)
                .setMimeType(fileInfo.mime)
            val pendingMessage = channel.sendFileMessage(
                params
            ) sendFileMessageLabel@{ fileMessage, e ->
                if (e != null) {
                    //failed
                    adapter.updatePendingItem(fileMessage)
                    return@sendFileMessageLabel
                }
                //succeeded
                adapter.updateSucceedItem(fileMessage)
                recyclerObserver.scrollToBottom(true)
            }
            //pending
            adapter.addPendingItem(pendingMessage)
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
                val params = baseMessage.messageParams
                if (params != null) {
                    channel.resendMessage(
                        baseMessage,
                        params.file,
                        null as BaseChannel.ResendFileMessageHandler?
                    )
                }
            }
        }
    }

    private fun getMessageChangeLogsSinceTimestamp(timeStamp: Long) {
        val channel = currentOpenChannel ?: return
        val params = MessageChangeLogsParams()
        params.replyTypeFilter = ReplyTypeFilter.NONE
        channel.getMessageChangeLogsSinceTimestamp(
            timeStamp,
            params
        ) getMessageChangeLogsSinceTimestampLabel@{ updatedMessages, deletedMessageIds, hasMore, token, e ->
            if (e != null) {
                showToast("$e")
                return@getMessageChangeLogsSinceTimestampLabel
            }
            adapter.updateItems(updatedMessages)
            adapter.deleteItems(deletedMessageIds)
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
        params.replyTypeFilter = ReplyTypeFilter.NONE
        channel.getMessageChangeLogsSinceToken(
            changelogToken,
            params
        ) getMessageChangeLogsSinceTokenLabel@{ updatedMessages, deletedMessageIds, hasMore, token, e ->
            if (e != null) {
                showToast("$e")
                return@getMessageChangeLogsSinceTokenLabel
            }
            adapter.updateItems(updatedMessages)
            adapter.deleteItems(deletedMessageIds)
            changelogToken = token
            if (hasMore) {
                getMessageChangeLogsSinceToken()
            }
        }
    }

    private fun updateChannel(openChannel: OpenChannel) {
        currentOpenChannel = openChannel
        if (currentOpenChannel == null) {
            showToast(R.string.channel_error)
            return
        }
        binding.toolbar.title = openChannel.name
    }

    override fun onDestroy() {
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID)
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        currentOpenChannel?.exit {}
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        showToast(getString(R.string.permission_granted))
                        SendBird.setAutoBackgroundDetection(false)
                        FileUtils.selectFile(
                            Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                            startForResult,
                            this
                        )
                    } else {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                PERMISSION_REQUEST_CODE
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