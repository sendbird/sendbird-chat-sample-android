package com.sendbird.chat.sample.groupchannel.unreadmessages.ui

import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.handler.GroupChannelHandler
import com.sendbird.android.message.BaseMessage

class UnreadMessageCountHandler(
    private val onDeliverStatusUpdated: (GroupChannel) -> Unit
) : GroupChannelHandler() {

    override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
    }

    override fun onReadStatusUpdated(channel: GroupChannel) {
        super.onReadStatusUpdated(channel)
        onDeliverStatusUpdated.invoke(channel)
    }
}