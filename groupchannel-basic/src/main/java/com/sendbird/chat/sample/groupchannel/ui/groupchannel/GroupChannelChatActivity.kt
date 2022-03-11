package com.sendbird.chat.sample.groupchannel.ui.groupchannel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import com.sendbird.android.handlers.*
import com.sendbird.chat.module.ui.ChatInputView
import com.sendbird.chat.module.utils.*
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_SELECT_USER
import com.sendbird.chat.module.utils.Constants.PERMISSION_REQUEST_CODE
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityGroupChannelChatBinding
import com.sendbird.chat.sample.groupchannel.ui.user.ChatMemberListActivity
import com.sendbird.chat.sample.groupchannel.ui.user.SelectUserActivity
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

    private val startForResultFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            SendBird.setAutoBackgroundDetection(true)
            if (data.resultCode == RESULT_OK) {
                data.data?.data?.let {
                    sendFileMessage(it)
                }
            }
        }
    private val startForResultInvite =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            if (data.resultCode == RESULT_OK) {
                val selectIds = data.data?.getStringArrayListExtra(INTENT_KEY_SELECT_USER)
                if (selectIds != null && selectIds.isNotEmpty()) {
                    val channel = currentGroupChannel ?: return@registerForActivityResult
                    channel.inviteWithUserIds(selectIds.toList()) {
                        if (it != null) {
                            showToast("${it.message}")
                        }
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChannelChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        channelUrl = intent.getStringExtra(INTENT_KEY_CHANNEL_URL) ?: ""
        channelTitle = intent.getStringExtra(INTENT_KEY_CHANNEL_TITLE) ?: ""
        channelTSHashMap = SharedPreferenceUtils.getChannelTSMap()

        init()
    }

    private fun init() {
        binding.toolbar.title = channelTitle
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        getChannel(channelUrl)
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
                    startForResultFile,
                    this@GroupChannelChatActivity
                )
            }
        })
    }

    private fun getChannel(channelUrl: String?) {
        if (channelUrl.isNullOrBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }
        GroupChannel.getChannel(
            channelUrl
        ) getChannelLabel@{ groupChannel, e ->
            if (e != null || groupChannel == null) {
                showToast("${e?.message}")
                return@getChannelLabel
            }
            currentGroupChannel = groupChannel
            initRecyclerView()
        }
    }

    private fun initRecyclerView() {
        adapter = GroupChannelChatAdapter({ baseMessage, view ->
            view.setOnCreateContextMenuListener { contextMenu, _, _ ->
                if (SendBird.getCurrentUser() != null && baseMessage.sender.userId == SendBird.getCurrentUser().userId) {
                    val deleteMenu =
                        contextMenu.add(Menu.NONE, 0, 0, getString(R.string.delete))
                    deleteMenu.setOnMenuItemClickListener {
                        currentGroupChannel?.deleteMessage(baseMessage) {
                            if (it != null) {
                                showToast("${it.message}")
                            }
                        }
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
                                {
                                    if (it.isBlank()) {
                                        showToast(R.string.enter_message_msg)
                                        return@showInputDialog
                                    }
                                    val params = UserMessageParams().setMessage(it)
                                    currentGroupChannel?.updateUserMessage(
                                        baseMessage.messageId, params
                                    ) { _, e ->
                                        if (e != null) {
                                            showToast("${e.message}")
                                        }
                                    }
                                },
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
        }) {
            showListDialog(
                listOf(getString(R.string.retry), getString(R.string.delete))
            ) { _, position ->
                when (position) {
                    0 -> resendMessage(it)
                    1 -> adapter.deletePendingItems(mutableListOf(it))
                }
            }
        }

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
        createMessageCollection(channelTSHashMap[channelUrl] ?: Long.MAX_VALUE)
    }

    private fun createMessageCollection(timeStamp: Long) {
        val channel = currentGroupChannel
        if (channel == null) {
            showToast(R.string.channel_error)
            finish()
            return
        }
        messageCollection?.dispose()

        val params: MessageListParams = MessageListParams().apply {
            setReverse(false)
            previousResultSize = 10
            nextResultSize = 10
        }
        messageCollection = MessageCollection.Builder(channel, params)
            .setStartingPoint(timeStamp)
            .setMessageCollectionHandler(collectionHandler)
            .build().apply {
                initialize(
                    MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API,
                    object : MessageCollectionInitHandler {
                        override fun onCacheResult(
                            cachedList: MutableList<BaseMessage>?,
                            e: SendBirdException?
                        ) {
                            if (cachedList != null) {
                                adapter.changeItems(cachedList, true)
                                adapter.addPendingItems(this@apply.pendingMessages)
                            }
                        }

                        override fun onApiResult(
                            apiResultList: MutableList<BaseMessage>?,
                            e: SendBirdException?
                        ) {
                            if (apiResultList != null) {
                                adapter.changeItems(apiResultList)
                                markAsRead()
                            }
                        }
                    }
                )
            }
    }

    private fun loadPreviousMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasPrevious()) {
            collection.loadPrevious { messageList, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@loadPrevious
                }
                adapter.addPreviousItems(messageList)
            }
        }
    }

    private fun loadNextMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasNext()) {
            collection.loadNext { messageList, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@loadNext
                }
                adapter.addNextItems(messageList)
                markAsRead()
            }
        }
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
                        currentGroupChannel?.delete {
                            if (it != null) {
                                showToast("${it.message}")
                                return@delete
                            }
                            finish()
                        }
                    },
                )
                true
            }
            R.id.leave -> {
                showAlertDialog(
                    getString(R.string.leave_channel),
                    getString(R.string.channel_leave_msg),
                    getString(R.string.leave),
                    getString(R.string.cancel),
                    {
                        currentGroupChannel?.leave {
                            if (it != null) {
                                showToast("${it.message}")
                            }
                            finish()
                        }
                    },
                )
                true
            }
            R.id.member_list -> {
                val intent = Intent(this, ChatMemberListActivity::class.java)
                intent.putExtra(INTENT_KEY_CHANNEL_URL, channelUrl)
                startActivity(intent)
                true
            }

            R.id.invite -> {
                val channel = currentGroupChannel
                if (channel == null) {
                    showToast(R.string.channel_error)
                    return true
                }
                val memberIds = ArrayList(channel.members.map { it.userId })
                val intent = Intent(this, SelectUserActivity::class.java)
                intent.putExtra(Constants.INTENT_KEY_BASE_USER, memberIds)
                startForResultInvite.launch(intent)
                true
            }

            R.id.update_channel_name -> {
                val channel = currentGroupChannel
                if (channel == null) {
                    showToast(R.string.channel_error)
                    return true
                }
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
                        val params = GroupChannelParams()
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

    private fun sendMessage(message: String) {
        if (message.isBlank()) {
            showToast(R.string.enter_message_msg)
        } else {
            val collection = messageCollection ?: return
            val channel = currentGroupChannel ?: return
            val params: UserMessageParams = UserMessageParams().setMessage(message.trim())
            binding.chatInputView.clearText()
            recyclerObserver.scrollToBottom(true)
            channel.sendUserMessage(params, null)
            if (collection.hasNext()) {
                adapter.changeItems(emptyList(), true)
                createMessageCollection(Long.MAX_VALUE)
            }
        }
    }

    private fun sendFileMessage(imgUri: Uri) {
        val collection = messageCollection ?: return
        val channel = currentGroupChannel ?: return

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
            channel.sendFileMessage(
                params,
            ) { _, _ -> }

            if (collection.hasNext()) {
                adapter.changeItems(emptyList(), true)
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
                channel.resendMessage(baseMessage, null)
            }
            is FileMessage -> {
                val params = baseMessage.messageParams
                if (params != null) {
                    channel.resendMessage(
                        baseMessage,
                        params.file
                    ) { _, _ -> }
                }
            }
        }
    }

    private fun markAsRead() {
        val channel = currentGroupChannel ?: return
        channel.markAsRead { e1 -> e1?.printStackTrace() }
    }

    private fun updateChannel(groupChannel: GroupChannel) {
        currentGroupChannel = groupChannel
        binding.toolbar.title =
            if (groupChannel.name.isNullOrBlank() || groupChannel.name == TextUtils.CHANNEL_DEFAULT_NAME)
                TextUtils.getGroupChannelTitle(groupChannel)
            else groupChannel.name
    }


    override fun onPause() {
        val lastMessage = adapter.currentList.lastOrNull()
        if (lastMessage != null && channelUrl.isNotBlank()) {
            channelTSHashMap[channelUrl] = lastMessage.createdAt
            SharedPreferenceUtils.setChannelTSMap(channelTSHashMap)
        }
        super.onPause()
    }

    override fun onDestroy() {
        messageCollection?.dispose()
        super.onDestroy()
    }

    private val collectionHandler = object : MessageCollectionHandler {
        override fun onMessagesAdded(
            context: MessageContext,
            channel: GroupChannel,
            messages: MutableList<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                BaseMessage.SendingStatus.SUCCEEDED -> {
                    adapter.addItems(messages)
                    markAsRead()
                }

                BaseMessage.SendingStatus.PENDING -> adapter.addPendingItems(messages)

                else -> {
                }
            }
        }

        override fun onMessagesUpdated(
            context: MessageContext,
            channel: GroupChannel,
            messages: MutableList<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                BaseMessage.SendingStatus.SUCCEEDED -> adapter.updateSucceedItems(messages)

                BaseMessage.SendingStatus.PENDING -> adapter.updatePendingItems(messages)

                BaseMessage.SendingStatus.FAILED -> adapter.updatePendingItems(messages)

                BaseMessage.SendingStatus.CANCELED -> adapter.deletePendingItems(messages)// The cancelled messages in the sample will be deleted

                else -> {
                }
            }
        }

        override fun onMessagesDeleted(
            context: MessageContext,
            channel: GroupChannel,
            messages: MutableList<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                BaseMessage.SendingStatus.SUCCEEDED -> adapter.deleteItems(messages)

                BaseMessage.SendingStatus.FAILED -> adapter.deletePendingItems(messages)

                else -> {
                }
            }
        }

        override fun onChannelUpdated(context: GroupChannelContext, channel: GroupChannel) {
            updateChannel(channel)
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
                adapter.changeItems(emptyList(), true)
                createMessageCollection(message.createdAt)
            } else {
                adapter.changeItems(emptyList(), true)
                createMessageCollection(startingPoint)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(getString(R.string.permission_granted))
                    SendBird.setAutoBackgroundDetection(false)
                    FileUtils.selectFile(
                        Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                        startForResultFile,
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