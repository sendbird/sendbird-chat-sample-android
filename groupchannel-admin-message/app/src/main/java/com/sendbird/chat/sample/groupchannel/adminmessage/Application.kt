package com.sendbird.chat.sample.groupchannel.adminmessage

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sendbird.android.LogLevel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.sendbird.chat.sample.groupchannel.adminmessage.util.SharedPreferenceUtils
import com.sendbird.chat.sample.groupchannel.adminmessage.util.changeValue

const val SENDBIRD_APP_ID = "43A36A3F-6248-4CDE-84F8-DC91436DECF8" // US-1 Demo

open class BaseApplication : Application() {
    protected val initMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { changeValue(false) }
    val initLiveData: LiveData<Boolean>
        get() = initMutableLiveData

    override fun onCreate() {
        super.onCreate()
        sendbirdChatInit()
        SharedPreferenceUtils.init(applicationContext)
    }

    open fun sendbirdChatInit(){
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
