package com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui

import com.sendbird.android.LogLevel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.ktx.InitResult
import com.sendbird.android.ktx.extension.init
import com.sendbird.android.params.InitParams
import com.sendbird.chat.module.ui.base.BaseApplication
import com.sendbird.chat.module.ui.base.SENDBIRD_APP_ID
import com.sendbird.chat.module.utils.changeValue
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CachingApplication : BaseApplication() {
    override fun sendbirdChatInit() {
        val initParams = InitParams(SENDBIRD_APP_ID, applicationContext, true)
        initParams.logLevel = LogLevel.ERROR
        SendbirdChat.init(initParams)
            .onEach {
                when (it) {
                    is InitResult.Migrating -> {
                        initMutableLiveData.changeValue(false)
                    }
                    is InitResult.Success -> {
                        initMutableLiveData.changeValue(true)
                    }
                }
            }.catch {
                initMutableLiveData.changeValue(true)
            }.launchIn(MainScope())
    }
}
