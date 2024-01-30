package com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.sendbird.android.message.UserMessage
import com.sendbird.android.params.FileMessageCreateParams
import com.sendbird.android.params.GroupChannelUpdateParams
import com.sendbird.android.params.MessageCollectionCreateParams
import com.sendbird.android.params.MessageListParams
import com.sendbird.android.params.UserMessageCreateParams
import com.sendbird.android.params.UserMessageUpdateParams
import com.sendbird.chat.module.utils.SharedPreferenceUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

enum class MessageEvent {
    INIT_FROM_CACHE,
    INIT_FROM_API,
    LOADED_FROM_NEXT,
    LOADED_FROM_PREV,
    UPDATED,
    ADDED,
    DELETED,
    PENDING_ADDED,
    PENDING_UPDATED,
    PENDING_DELETED,
    HUGEGAP_DETECTED,
}

enum class ChannelEvent {
    UPDATED,
    DELETED,
}

data class CollectionEvent(val event: MessageEvent, val messages: List<BaseMessage>, val pendingMessages: List<BaseMessage>)

class ChannelViewModel : ViewModel(), MessageCollectionHandler {
    private var messageCollection: MessageCollection? = null
    private var channelTSHashMap = ConcurrentHashMap<String, Long>()
    private val _channelEvent = MutableLiveData<Pair<ChannelEvent, GroupChannel>>()
    private val _exceptionalMessage = MutableLiveData<String>()
    private val _messageEvent = MutableLiveData<CollectionEvent>()
    private val apiCoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        _exceptionalMessage.postValue(throwable.message ?: "")
    }

    private var isCollectionInitialized = false
    lateinit var channel: GroupChannel
    val channelEvent: LiveData<Pair<ChannelEvent, GroupChannel>>
        get() = _channelEvent

    val exceptionalMessage: LiveData<String>
        get() = _exceptionalMessage

    val messageEvent: LiveData<CollectionEvent>
        get() = _messageEvent

    val startingPoint: Long
        get() = messageCollection?.startingPoint ?: Long.MAX_VALUE

    override fun onCleared() {
        super.onCleared()
        messageCollection?.dispose()
    }

    init {
        channelTSHashMap = SharedPreferenceUtils.channelTSMap
    }

    @Synchronized
    fun updateLastVisibleMessageTs(lastMessage: BaseMessage) {
        if (channel.url.isEmpty()) return
        viewModelScope.launch(Dispatchers.Default) {
            channelTSHashMap[channel.url] = lastMessage.createdAt
            SharedPreferenceUtils.channelTSMap = channelTSHashMap
        }
    }

    fun initialize(channelUrl: String) = flow {
        channel = GroupChannel.getChannel(channelUrl)
        messageCollection = createAndInitCollection(channelTSHashMap[channelUrl] ?: Long.MAX_VALUE)
        emit(channel)
    }

    fun sendUserMessage(params: UserMessageCreateParams) {
        channel.sendUserMessage(params)
            .catch {_exceptionalMessage.postValue(it.message)}
            .launchIn(viewModelScope)
        if (messageCollection?.hasNext == true) {
            messageCollection = createAndInitCollection(Long.MAX_VALUE)
        }
    }

    fun sendFileMessage(params: FileMessageCreateParams) {
        channel.sendFileMessage(params)
            .catch {_exceptionalMessage.postValue(it.message)}
            .launchIn(viewModelScope)
        if (messageCollection?.hasNext == true) {
            createAndInitCollection(Long.MAX_VALUE)
        }
    }

    fun resendMessage(baseMessage: BaseMessage) {
        when (baseMessage) {
            is UserMessage -> {
                channel.resendMessage(baseMessage)
                    .catch {_exceptionalMessage.postValue(it.message)}
                    .launchIn(viewModelScope)
            }

            is FileMessage -> {
                val params = baseMessage.messageCreateParams
                if (params != null) {
                    channel.resendMessage(baseMessage, params.file)
                        .catch {_exceptionalMessage.postValue(it.message)}
                        .launchIn(viewModelScope)
                }
            }
        }
    }

    @Synchronized
    fun createAndInitCollection(timeStamp: Long): MessageCollection {
        messageCollection?.dispose()
        isCollectionInitialized = false

        val messageListParams = MessageListParams().apply {
            reverse = false
            previousResultSize = 20
            nextResultSize = 20
        }
        val messageCollectionCreateParams =
            MessageCollectionCreateParams(channel, messageListParams)
                .apply {
                    startingPoint = timeStamp
                    messageCollectionHandler = this@ChannelViewModel
                }

        return SendbirdChat.createMessageCollection(messageCollectionCreateParams).apply {
            initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API)
                .onEach {
                    when (it) {
                        is MessageCollectionInitResult.CachedResult -> {
                            notifyMessageEvent(MessageEvent.INIT_FROM_CACHE, it.result)
                        }
                        is MessageCollectionInitResult.ApiResult -> {
                            notifyMessageEvent(MessageEvent.INIT_FROM_API, it.result)
                            markAsRead()
                        }
                    }
                }.catch {
                    _exceptionalMessage.postValue(it.message)
                }.onCompletion {
                    isCollectionInitialized = it == null
                }.launchIn(viewModelScope)
        }
    }

    fun loadPrevious() {
        if (!isCollectionInitialized) return
        messageCollection?.let {
            if (it.hasPrevious) {
                viewModelScope.launch(apiCoroutineContext) {
                    val messages = it.loadPrevious()
                    notifyMessageEvent(MessageEvent.LOADED_FROM_PREV, messages)
                }
            }
        }
    }

    fun loadNext() {
        if (!isCollectionInitialized) return
        messageCollection?.let {
            if (it.hasNext) {
                viewModelScope.launch(apiCoroutineContext) {
                    val messages = it.loadNext()
                    notifyMessageEvent(MessageEvent.LOADED_FROM_NEXT, messages)
                    markAsRead()
                }
            }
        }
    }

    fun deleteMessage(baseMessage: BaseMessage) {
        viewModelScope.launch(apiCoroutineContext) {
            channel.deleteMessage(baseMessage.messageId)
        }
    }

    fun updateMessage(msg: String, baseMessage: BaseMessage) {
        viewModelScope.launch(apiCoroutineContext) {
            channel.updateUserMessage(baseMessage.messageId, UserMessageUpdateParams(msg))
        }
    }

    fun deleteChannel() {
        viewModelScope.launch(apiCoroutineContext) {
            channel.delete()
        }
    }

    fun leaveChannel() {
        viewModelScope.launch(apiCoroutineContext) {
            channel.leave()
        }
    }

    fun updateChannel(params: GroupChannelUpdateParams) {
        viewModelScope.launch(apiCoroutineContext) {
            channel.updateChannel(params)
        }
    }

    fun inviteUsers(userIds: List<String>) {
        viewModelScope.launch(apiCoroutineContext) {
            channel.invite(userIds)
        }
    }

    @Synchronized
    private fun markAsRead() {
        viewModelScope.launch(apiCoroutineContext) {
            channel.markAsRead()
        }
    }

    private fun notifyMessageEvent(event: MessageEvent, messages: List<BaseMessage>) {
        _messageEvent.postValue(
            CollectionEvent(
                event,
                messages,
                messageCollection?.pendingMessages ?: emptyList()
            )
        )
    }

    private fun notifyChannelEvent(event: ChannelEvent, channel: GroupChannel) {
        _channelEvent.postValue(event to channel)
    }

    override fun onMessagesAdded(
        context: MessageContext,
        channel: GroupChannel,
        messages: List<BaseMessage>
    ) {
        when (context.messagesSendingStatus) {
            SendingStatus.SUCCEEDED -> {
                notifyMessageEvent(MessageEvent.ADDED, messages)
                markAsRead()
            }
            SendingStatus.PENDING -> notifyMessageEvent(MessageEvent.PENDING_ADDED, messages)
            else -> {}
        }
    }

    override fun onMessagesUpdated(
        context: MessageContext,
        channel: GroupChannel,
        messages: List<BaseMessage>
    ) {
        when (context.messagesSendingStatus) {
            SendingStatus.SUCCEEDED -> notifyMessageEvent(MessageEvent.UPDATED, messages)
            SendingStatus.PENDING, SendingStatus.FAILED -> notifyMessageEvent(MessageEvent.PENDING_UPDATED, messages)
            SendingStatus.CANCELED -> notifyMessageEvent(MessageEvent.PENDING_DELETED, messages)// The cancelled messages in the sample will be deleted
            else -> {}
        }
    }

    override fun onMessagesDeleted(
        context: MessageContext,
        channel: GroupChannel,
        messages: List<BaseMessage>
    ) {
        when (context.messagesSendingStatus) {
            SendingStatus.SUCCEEDED -> notifyMessageEvent(MessageEvent.DELETED, messages)
            SendingStatus.FAILED -> notifyMessageEvent(MessageEvent.PENDING_DELETED, messages)
            else -> {}
        }
    }

    override fun onChannelUpdated(context: GroupChannelContext, channel: GroupChannel) {
        notifyChannelEvent(ChannelEvent.UPDATED, channel)
    }

    override fun onChannelDeleted(context: GroupChannelContext, channelUrl: String) {
        notifyChannelEvent(ChannelEvent.DELETED, channel)
    }

    override fun onHugeGapDetected() {
        notifyMessageEvent(MessageEvent.HUGEGAP_DETECTED, emptyList())
    }
}
