package com.sendbird.chat.sample.groupchannel.useronline

import com.sendbird.android.SendbirdChat
import com.sendbird.android.handler.ConnectionHandler
import com.sendbird.chat.module.ui.base.BaseApplication
import com.sendbird.chat.module.utils.showToast

class GroupChannelApplication : BaseApplication() {

    override fun sendbirdChatInit() {
        super.sendbirdChatInit()
        addConnectionListener()
    }

    private fun addConnectionListener() {
        SendbirdChat.addConnectionHandler(ConnectionHandlerId, object : ConnectionHandler {
            override fun onConnected(userId: String) {
                showToast("Connected")
            }

            override fun onDisconnected(userId: String) {
                showToast("Disconnected")
            }

            override fun onReconnectFailed() {
                showToast("Connection failed")
            }

            override fun onReconnectStarted() {
                showToast("try to reconnect")
            }

            override fun onReconnectSucceeded() {
                showToast("reconnected")
            }
        })
    }

    companion object {
        private const val ConnectionHandlerId = "ConnectionHandlerId"
    }
}