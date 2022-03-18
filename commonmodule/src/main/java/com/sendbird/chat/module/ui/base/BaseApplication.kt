package com.sendbird.chat.module.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sendbird.android.LogLevel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.chat.module.utils.SharedPreferenceUtils
import com.sendbird.chat.module.utils.changeValue

const val SENDBIRD_APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23" // US-1 Demo

class BaseApplication : Application() {
    private val initMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { changeValue(false) }
    val initLiveData: LiveData<Boolean>
        get() = initMutableLiveData

    override fun onCreate() {
        super.onCreate()
        SendbirdChat.init(
            SENDBIRD_APP_ID,
            applicationContext,
            false,
            object : InitResultHandler {
                override fun onInitFailed(e: SendbirdException) {
                    initMutableLiveData.changeValue(true)
                    // If useLocalCaching is true and init fails, the SDK will turn off the useLocalCaching flag so that you can still proceed with your app
                }

                override fun onMigrationStarted() {
                    // This won't be called if useLocalCaching is set to false.
                }

                override fun onInitSucceed() {
                    SendbirdChat.setLoggerLevel(LogLevel.ERROR)
                    initMutableLiveData.changeValue(true)
                }
            })
        SharedPreferenceUtils.init(applicationContext)
    }

}