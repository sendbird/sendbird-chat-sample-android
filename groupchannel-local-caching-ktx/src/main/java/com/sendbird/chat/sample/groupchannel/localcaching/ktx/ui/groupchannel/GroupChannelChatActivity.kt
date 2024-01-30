package com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui.groupchannel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.collection.MessageCollection
import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.collection.MessageContext
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.ktx.MessageCollectionInitResult
import com.sendbird.android.ktx.extension.channel.delete
import com.sendbird.android.ktx.extension.channel.deleteMessage
import com.sendbird.android.ktx.extension.channel.getChannel
import com.sendbird.android.ktx.extension.channel.invite
import com.sendbird.android.ktx.extension.channel.leave
import com.sendbird.android.ktx.extension.channel.markAsRead
import com.sendbird.android.ktx.extension.channel.resendMessage
import com.sendbird.android.ktx.extension.channel.sendFileMessage
import com.sendbird.android.ktx.extension.channel.sendUserMessage
import com.sendbird.android.ktx.extension.channel.updateChannel
import com.sendbird.android.ktx.extension.channel.updateUserMessage
import com.sendbird.android.ktx.extension.collection.initialize
import com.sendbird.android.ktx.extension.collection.loadNext
import com.sendbird.android.ktx.extension.collection.loadPrevious
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.android.message.ThumbnailSize
import com.sendbird.android.message.UserMessage
import com.sendbird.android.params.FileMessageCreateParams
import com.sendbird.android.params.GroupChannelUpdateParams
import com.sendbird.android.params.MessageCollectionCreateParams
import com.sendbird.android.params.MessageListParams
import com.sendbird.android.params.UserMessageCreateParams
import com.sendbird.android.params.UserMessageUpdateParams
import com.sendbird.chat.module.ui.ChatInputView
import com.sendbird.chat.module.utils.ChatRecyclerDataObserver
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.FileUtils
import com.sendbird.chat.module.utils.SharedPreferenceUtils
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class GroupChannelChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChannelChatBinding
    private lateinit var adapter: GroupChannelChatAdapter
    private lateinit var recyclerObserver: ChatRecyclerDataObserver
    private var channelUrl: String = ""
    private var channelTitle: String = ""
    private var currentGroupChannel: GroupChannel? = null
    private var messageCollection: MessageCollection? = null
    private var channelTSHashMap = ConcurrentHashMap<String, Long>()
    private var isCollectionInitialized = false
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        showToast(throwable.message ?: "")
    }

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

        val intent = intent
        channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL) ?: ""
        channelTitle = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_TITLE) ?: ""
        channelTSHashMap = SharedPreferenceUtils.channelTSMap

        init()
        initRecyclerView()
        getChannel(channelUrl)
    }

    private fun init() {
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
    }

    private fun initRecyclerView() {
        adapter = GroupChannelChatAdapter({ baseMessage, view ->
            view.setOnCreateContextMenuListener { contextMenu, _, _ ->
                if (SendbirdChat.currentUser != null && baseMessage.sender?.userId == SendbirdChat.currentUser!!.userId) {
                    val deleteMenu =
                        contextMenu.add(Menu.NONE, 0, 0, getString(R.string.delete))
                    deleteMenu.setOnMenuItemClickListener {
                        deleteMessage(baseMessage)
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
                                { updateMessage(it, baseMessage) },
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
                    0 -> resendMessage(it)
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
                    loadPreviousMessageItems()
                } else if (!recyclerView.canScrollVertically(1)) {
                    loadNextMessageItems()
                }
            }
        })
    }

    private fun getChannel(channelUrl: String?) {
        if (channelUrl.isNullOrBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }

        lifecycleScope.launch(exceptionHandler) {
            currentGroupChannel = GroupChannel.getChannel(channelUrl)
            setChannelTitle()
            createMessageCollection(channelTSHashMap[channelUrl] ?: Long.MAX_VALUE)
        }
    }

    private fun setChannelTitle() {
        val currentChannel = currentGroupChannel
        if (channelTitle == TextUtils.CHANNEL_DEFAULT_NAME && currentChannel != null) {
            binding.toolbar.title = TextUtils.getGroupChannelTitle(currentChannel)
        } else {
            binding.toolbar.title = channelTitle
        }
    }

    private fun createMessageCollection(timeStamp: Long) {
        messageCollection?.dispose()
        isCollectionInitialized = false
        val channel = currentGroupChannel
        if (channel == null) {
            showToast(R.string.channel_error)
            finish()
            return
        }

        val messageListParams = MessageListParams().apply {
            reverse = false
            previousResultSize = 20
            nextResultSize = 20
        }
        val messageCollectionCreateParams =
            MessageCollectionCreateParams(channel, messageListParams)
                .apply {
                    startingPoint = timeStamp
                    messageCollectionHandler = collectionHandler
                }

        messageCollection = SendbirdChat.createMessageCollection(messageCollectionCreateParams).apply {
            initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API)
                .onEach {
                    when (it) {
                        is MessageCollectionInitResult.CachedResult -> {
                            adapter.changeMessages(it.result)
                            adapter.addPendingMessages(this@apply.pendingMessages)
                        }
                        is MessageCollectionInitResult.ApiResult -> {
                            adapter.changeMessages(it.result, false)
                            markAsRead()
                        }
                    }
                }.catch {
                    showToast("${it.message}")
                }.onCompletion {
                    isCollectionInitialized = true
                }.launchIn(lifecycleScope)
        }
    }

    private fun loadPreviousMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasPrevious) {
            lifecycleScope.launch(exceptionHandler) {
                collection.loadPrevious().let {
                    adapter.addPreviousMessages(it)
                }
            }
        }
    }

    private fun loadNextMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasNext) {
            lifecycleScope.launch(exceptionHandler) {
                val result = collection.loadNext()
                adapter.addNextMessages(result)
                markAsRead()
            }
        }
    }

    private fun deleteMessage(baseMessage: BaseMessage) {
        lifecycleScope.launch(exceptionHandler) {
            currentGroupChannel?.deleteMessage(baseMessage.messageId)
        }
    }

    private fun updateMessage(msg: String, baseMessage: BaseMessage) {
        if (msg.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }

        lifecycleScope.launch(exceptionHandler) {
            currentGroupChannel?.updateUserMessage(baseMessage.messageId, UserMessageUpdateParams(msg))
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

            R.id.leave -> {
                showAlertDialog(
                    getString(R.string.leave_channel),
                    getString(R.string.channel_leave_msg),
                    getString(R.string.leave),
                    getString(R.string.cancel),
                    { leaveChannel() },
                )
                true
            }

            R.id.member_list -> {
                val intent = Intent(this, ChatMemberListActivity::class.java)
                intent.putExtra(Constants.INTENT_KEY_CHANNEL_URL, channelUrl)
                startActivity(intent)
                true
            }

            R.id.invite -> {
                selectInviteUser()
                true
            }

            R.id.update_channel_name -> {
                val channel = currentGroupChannel ?: return true
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

    private fun deleteChannel() {
        lifecycleScope.launch(exceptionHandler) {
            currentGroupChannel?.delete()
            finish()
        }
    }

    private fun leaveChannel() {
        lifecycleScope.launch(exceptionHandler) {
            currentGroupChannel?.leave()
            finish()
        }
    }

    private fun selectInviteUser() {
        val channel = currentGroupChannel
        if (channel == null) {
            showToast(R.string.channel_error)
            return
        }
        val memberIds = ArrayList(channel.members.map { it.userId })
        val intent = Intent(this, SelectUserActivity::class.java)
        intent.putExtra(Constants.INTENT_KEY_BASE_USER, memberIds)
        startForResultInvite.launch(intent)
    }

    private fun inviteUser(selectIds: List<String>?) {
        if (!selectIds.isNullOrEmpty()) {
            val channel = currentGroupChannel ?: return
            lifecycleScope.launch(exceptionHandler) {
                channel.invite(selectIds.toList())
            }
        }
    }

    private fun updateChannelView(name: String, channel: GroupChannel) {
        if (name.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        if (channel.name != name) {
            lifecycleScope.launch(exceptionHandler) {
                channel.updateChannel(GroupChannelUpdateParams().apply { this.name = name })
            }
        }
    }

    private fun sendMessage(message: String) {
        if (message.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        if (!isCollectionInitialized) {
            showToast(R.string.message_collection_init_msg)
            return
        }
        val collection = messageCollection ?: return
        val channel = currentGroupChannel ?: return
        binding.chatInputView.clearText()
        recyclerObserver.scrollToBottom(true)
        channel.sendUserMessage(UserMessageCreateParams().apply { this.message = message.trim() })
            .catch {}
            .launchIn(lifecycleScope)
        if (collection.hasNext) {
            createMessageCollection(Long.MAX_VALUE)
        }
    }

    private fun sendFileMessage(imgUri: Uri?) {
        if (imgUri == null) {
            showToast(R.string.file_transfer_error)
            return
        }
        if (!isCollectionInitialized) {
            showToast(R.string.message_collection_init_msg)
            return
        }
        val collection = messageCollection ?: return
        val channel = currentGroupChannel ?: return

        val thumbnailSizes = listOf(
            ThumbnailSize(100, 100),
            ThumbnailSize(200, 200)
        )
        val fileInfo = FileUtils.getFileInfo(imgUri, applicationContext)
        if (fileInfo != null) {
            recyclerObserver.scrollToBottom(true)
            channel.sendFileMessage(
                FileMessageCreateParams().apply {
                    file = fileInfo.file
                    fileName = fileInfo.name
                    fileSize = fileInfo.size
                    this.thumbnailSizes = thumbnailSizes
                    mimeType = fileInfo.mime
                }
            ).catch {
            }.launchIn(lifecycleScope)
            if (collection.hasNext) {
                createMessageCollection(Long.MAX_VALUE)
            }
        } else {
            showToast(R.string.file_transfer_error)
        }
    }

    private fun resendMessage(baseMessage: BaseMessage) {
        val channel = currentGroupChannel ?: return
        when (baseMessage) {
            is UserMessage -> {
                channel.resendMessage(baseMessage)
                    .catch {}
                    .launchIn(lifecycleScope)
            }

            is FileMessage -> {
                val params = baseMessage.messageCreateParams
                if (params != null) {
                    channel.resendMessage(baseMessage, params.file)
                        .catch {}
                        .launchIn(lifecycleScope)
                }
            }
        }
    }

    private fun markAsRead() {
        lifecycleScope.launch(exceptionHandler) {
            currentGroupChannel?.markAsRead()
        }
    }

    private fun updateChannelView(groupChannel: GroupChannel) {
        currentGroupChannel = groupChannel
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
        if (lastMessage != null && channelUrl.isNotBlank()) {
            channelTSHashMap[channelUrl] = lastMessage.createdAt
            SharedPreferenceUtils.channelTSMap = channelTSHashMap
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        messageCollection?.dispose()
        SendbirdChat.autoBackgroundDetection = true
    }

    private val collectionHandler = object : MessageCollectionHandler {
        override fun onMessagesAdded(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> {
                    adapter.addMessages(messages)
                    markAsRead()
                }

                SendingStatus.PENDING -> adapter.addPendingMessages(messages)

                else -> {
                }
            }
        }

        override fun onMessagesUpdated(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> adapter.updateSucceedMessages(messages)

                SendingStatus.PENDING -> adapter.updatePendingMessages(messages)

                SendingStatus.FAILED -> adapter.updatePendingMessages(messages)

                SendingStatus.CANCELED -> adapter.deletePendingMessages(messages)// The cancelled messages in the sample will be deleted

                else -> {
                }
            }
        }

        override fun onMessagesDeleted(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> adapter.deleteMessages(messages)

                SendingStatus.FAILED -> adapter.deletePendingMessages(messages)

                else -> {
                }
            }
        }

        override fun onChannelUpdated(context: GroupChannelContext, channel: GroupChannel) {
            updateChannelView(channel)
        }

        override fun onChannelDeleted(context: GroupChannelContext, channelUrl: String) {
            showToast(R.string.channel_deleted_event_msg)
            finish()
        }

        override fun onHugeGapDetected() {
            val collection = messageCollection
            if (collection == null) {
                showToast(R.string.channel_error)
                finish()
                return
            }
            val startingPoint = collection.startingPoint
            collection.dispose()
            val position: Int =
                (binding.recyclerviewChat.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (position >= 0) {
                val message: BaseMessage = adapter.currentList[position]
                createMessageCollection(message.createdAt)
            } else {
                createMessageCollection(startingPoint)
            }
        }

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