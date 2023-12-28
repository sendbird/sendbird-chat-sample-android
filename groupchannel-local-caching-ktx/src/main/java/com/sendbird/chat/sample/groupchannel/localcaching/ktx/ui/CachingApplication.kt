package com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui

import com.sendbird.android.LogLevel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.ktx.extension.init
import com.sendbird.android.params.InitParams
import com.sendbird.chat.module.ui.base.BaseApplication
import com.sendbird.chat.module.ui.base.SENDBIRD_APP_ID
import com.sendbird.chat.module.utils.changeValue
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CachingApplication : BaseApplication() {
    override fun sendbirdChatInit() {
        val initParams = InitParams(SENDBIRD_APP_ID, applicationContext, true)
        initParams.logLevel = LogLevel.ERROR
        MainScope().launch {
            runCatching {
                SendbirdChat.init(initParams)
            }.onFailure {
                initMutableLiveData.changeValue(true)
            }.onSuccess {
                initMutableLiveData.changeValue(true)
            }
        }
    }
}
