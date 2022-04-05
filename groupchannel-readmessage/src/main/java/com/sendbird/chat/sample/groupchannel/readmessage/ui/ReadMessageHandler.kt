package com.sendbird.chat.sample.groupchannel.readmessage.ui

import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.handler.GroupChannelHandler
import com.sendbird.android.message.BaseMessage

class ReadMessageHandler(
    private val onChannelReadStatusChanged: (GroupChannel) -> Unit
) : GroupChannelHandler() {
    override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
    }

    override fun onReadStatusUpdated(channel: GroupChannel) {
        super.onReadStatusUpdated(channel)
        onChannelReadStatusChanged.invoke(channel)
    }
}