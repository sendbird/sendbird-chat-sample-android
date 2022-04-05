package com.sendbird.chat.sample.groupchannel.unreceivedmessage.ui

import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.handler.GroupChannelHandler
import com.sendbird.android.message.BaseMessage

class DeliverMessageHandler(
    private val onMessageDeliveryStatusUpdated: (channel: BaseChannel) -> Unit
) : GroupChannelHandler() {

    override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
    }

    override fun onDeliveryStatusUpdated(channel: GroupChannel) {
        super.onDeliveryStatusUpdated(channel)
        onMessageDeliveryStatusUpdated.invoke(channel)
    }
}