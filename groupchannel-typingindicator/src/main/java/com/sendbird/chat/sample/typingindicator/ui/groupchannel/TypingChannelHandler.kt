package com.sendbird.chat.sample.typingindicator.ui.groupchannel

import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.handler.GroupChannelHandler
import com.sendbird.android.message.BaseMessage

abstract class TypingChannelHandler : GroupChannelHandler() {

    override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
        //no-op
    }

    override fun onTypingStatusUpdated(channel: GroupChannel) {
        typingStatusUpdated(channel)
    }

    abstract fun typingStatusUpdated(channel: GroupChannel)
}