package com.sendbird.chat.sample.groupchannel.scheduled.message.ui.groupchannel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.collection.MessageCollection
import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.collection.MessageContext
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.handler.MessageCollectionInitHandler
import com.sendbird.android.message.*
import com.sendbird.android.params.*
import com.sendbird.chat.module.ui.ChatInputView
import com.sendbird.chat.module.utils.*
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityGroupChannelChatBinding
import com.sendbird.chat.sample.groupchannel.scheduled.message.ui.openDateTimeSelector
import com.sendbird.chat.sample.groupchannel.scheduled.message.ui.scheduledmessages.ScheduledMessagesActivity
import com.sendbird.chat.sample.groupchannel.scheduled.message.ui.user.ChatMemberListActivity
import com.sendbird.chat.sample.groupchannel.scheduled.message.ui.user.SelectUserActivity
import java.util.*
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

    private var scheduledTime: Long = 0L

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            SendbirdChat.autoBackgroundDetection = true
            uri?.let { sendFileMessage(it) }
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
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        })

        binding.chatInputView.findViewById<TextView>(R.id.textview_send).apply {
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                resources.getDrawable(R.drawable.ic_chevron_right, null),
                null
            )
            setOnLongClickListener {
                openScheduleMessageMenu(it)
                true
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
        GroupChannel.getChannel(
            channelUrl
        ) getChannelLabel@{ groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@getChannelLabel
            }
            if (groupChannel != null) {
                currentGroupChannel = groupChannel
                setChannelTitle()
                createMessageCollection(channelTSHashMap[channelUrl] ?: Long.MAX_VALUE)
            }
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
        messageCollection =
            SendbirdChat.createMessageCollection(messageCollectionCreateParams).apply {
                initialize(
                    MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API,
                    object : MessageCollectionInitHandler {
                        override fun onCacheResult(
                            cachedList: List<BaseMessage>?,
                            e: SendbirdException?
                        ) {
                            if (e != null) {
                                showToast("${e.message}")
                            }
                            adapter.changeMessages(cachedList)
                            adapter.addPendingMessages(this@apply.pendingMessages)
                        }

                        override fun onApiResult(
                            apiResultList: List<BaseMessage>?,
                            e: SendbirdException?
                        ) {
                            if (e != null) {
                                showToast("${e.message}")
                            }
                            adapter.changeMessages(apiResultList, false)
                            markAsRead()
                            isCollectionInitialized = true
                        }
                    }
                )
            }
    }

    private fun loadPreviousMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasPrevious) {
            collection.loadPrevious { messages, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@loadPrevious
                }
                adapter.addPreviousMessages(messages)
            }
        }
    }

    private fun loadNextMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasNext) {
            collection.loadNext { messages, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@loadNext
                }
                adapter.addNextMessages(messages)
                markAsRead()
            }
        }
    }

    private fun deleteMessage(baseMessage: BaseMessage) {
        currentGroupChannel?.deleteMessage(baseMessage) {
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
        val params = UserMessageUpdateParams().apply {
            message = msg
        }
        currentGroupChannel?.updateUserMessage(
            baseMessage.messageId, params
        ) { _, e ->
            if (e != null) {
                showToast("${e.message}")
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

            R.id.scheduled_messages -> {
                startActivity(Intent(this, ScheduledMessagesActivity::class.java).apply {
                    putExtra(Constants.INTENT_KEY_CHANNEL_URL, channelUrl)
                    putExtra(Constants.INTENT_KEY_CHANNEL_TITLE, channelTitle)
                })
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
        currentGroupChannel?.delete {
            if (it != null) {
                showToast("${it.message}")
                return@delete
            }
            finish()
        }
    }

    private fun leaveChannel() {
        currentGroupChannel?.leave {
            if (it != null) {
                showToast("${it.message}")
            }
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
        if (selectIds != null && selectIds.isNotEmpty()) {
            val channel = currentGroupChannel ?: return
            channel.invite(selectIds.toList()) {
                if (it != null) {
                    showToast("${it.message}")
                }
            }
        }
    }

    private fun updateChannelView(name: String, channel: GroupChannel) {
        if (name.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        if (channel.name != name) {
            val params = GroupChannelUpdateParams()
                .apply { this.name = name }
            channel.updateChannel(
                params
            ) { _, e ->
                if (e != null) {
                    showToast("${e.message}")
                }
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
        if (scheduledTime == 0L) {
            sendNormalMessage(message)
        } else {
            sendScheduledMessage(message)
        }
        binding.chatInputView.clearText()
        recyclerObserver.scrollToBottom(true)

        if (collection.hasNext) {
            createMessageCollection(Long.MAX_VALUE)
        }
    }

    private fun sendNormalMessage(message: String) {
        val channel = currentGroupChannel ?: return
        val params = UserMessageCreateParams().apply {
            this.message = message.trim()
        }
        channel.sendUserMessage(params, null)
    }

    private fun sendScheduledMessage(message: String) {
        val channel = currentGroupChannel ?: return
        val params = ScheduledUserMessageCreateParams(scheduledTime).apply {
            this.message = message.trim()
        }
        channel.createScheduledUserMessage(params) { _, e ->
            if (e != null) {
                Log.e("ScheduledMessage", e.message ?: "Error sending scheduled message")
            }
        }
        scheduledTime = 0L
        changeSendText()
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
        val thumbnailSizes = listOf(
            ThumbnailSize(100, 100),
            ThumbnailSize(200, 200)
        )
        val fileInfo = FileUtils.getFileInfo(imgUri, applicationContext)
        if (fileInfo != null) {
            if (scheduledTime == 0L) {
                sendFileMessage(fileInfo, thumbnailSizes)
            } else {
                sendScheduledFileMessage(fileInfo, thumbnailSizes)
            }
            recyclerObserver.scrollToBottom(true)

            if (collection.hasNext) {
                createMessageCollection(Long.MAX_VALUE)
            }
        } else {
            showToast(R.string.file_transfer_error)
        }
    }

    private fun sendFileMessage(
        fileInfo: FileUtils.FileResult,
        thumbnailSizes: List<ThumbnailSize>
    ) {
        val channel = currentGroupChannel ?: return
        val params = FileMessageCreateParams().apply {
            file = fileInfo.file
            fileName = fileInfo.name
            fileSize = fileInfo.size
            this.thumbnailSizes = thumbnailSizes
            mimeType = fileInfo.mime
        }
        channel.sendFileMessage(params, null)
    }

    private fun sendScheduledFileMessage(
        fileInfo: FileUtils.FileResult,
        thumbnailSizes: List<ThumbnailSize>
    ) {
        val channel = currentGroupChannel ?: return
        val params = ScheduledFileMessageCreateParams(scheduledTime).apply {
            file = fileInfo.file
            fileName = fileInfo.name
            fileSize = fileInfo.size
            this.thumbnailSizes = thumbnailSizes
            mimeType = fileInfo.mime
        }
        channel.createScheduledFileMessage(params) { _, e ->
            if (e != null) {
                Log.e("ScheduledMessage", e.message ?: "Error sending scheduled message")
            }
        }
        scheduledTime = 0L
        changeSendText()
    }

    private fun resendMessage(baseMessage: BaseMessage) {
        val channel = currentGroupChannel ?: return
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

    private fun markAsRead() {
        currentGroupChannel?.markAsRead { e1 -> e1?.printStackTrace() }
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

    private fun openScheduleMessageMenu(anchor: View) {
        if (scheduledTime == 0L) {
            createSelectScheduledMenu(anchor)
            return
        }
        createClearScheduledMenu(anchor)
    }

    private fun createClearScheduledMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menuInflater.inflate(R.menu.clear_menu, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.clear -> {
                        scheduledTime = 0L
                        changeSendText()
                    }
                }
                true
            }
            show()
        }
    }

    private fun createSelectScheduledMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menuInflater.inflate(R.menu.scheduled_messages_menu, menu)
            MenuCompat.setGroupDividerEnabled(menu, true)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.tomorrow -> scheduleTomorrow()
                    R.id.monday -> scheduleNextMonday()
                    R.id.custom -> openDateTimeSelector { timeSelected ->
                        scheduledTime = timeSelected
                        changeSendText()
                    }
                }
                true
            }
            show()
        }
    }

    private fun scheduleTomorrow() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.add(Calendar.DATE, 1)
        scheduledTime = calendar.timeInMillis
        changeSendText()
    }

    private fun scheduleNextMonday() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, 1)
        }
        scheduledTime = calendar.timeInMillis
        changeSendText()
    }

    private fun changeSendText() {
        val text = if (scheduledTime == 0L) {
            "Send"
        } else {
            "Send scheduled"
        }
        binding.chatInputView.findViewById<TextView>(R.id.textview_send).text = text
    }
}