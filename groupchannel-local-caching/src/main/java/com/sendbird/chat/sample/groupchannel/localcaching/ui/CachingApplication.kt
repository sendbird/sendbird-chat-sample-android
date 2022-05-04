package com.sendbird.chat.sample.groupchannel.localcaching.ui

import com.sendbird.android.LogLevel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.sendbird.chat.module.ui.base.BaseApplication
import com.sendbird.chat.module.ui.base.SENDBIRD_APP_ID
import com.sendbird.chat.module.utils.changeValue

class CachingApplication : BaseApplication() {

    override fun sendbirdChatInit() {
        val initParams = InitParams(SENDBIRD_APP_ID, applicationContext, true)
        initParams.logLevel = LogLevel.ERROR
        SendbirdChat.init(
            initParams,
            object : InitResultHandler {
                override fun onInitFailed(e: SendbirdException) {
                    initMutableLiveData.changeValue(true)
                    // If useLocalCaching is true and init fails, the SDK will turn off the useLocalCaching flag so that you can still proceed with your app
                }

                override fun onMigrationStarted() {
                    // This won't be called if useLocalCaching is set to false.
                }

                override fun onInitSucceed() {
                    initMutableLiveData.changeValue(true)
                }
            })
    }
}