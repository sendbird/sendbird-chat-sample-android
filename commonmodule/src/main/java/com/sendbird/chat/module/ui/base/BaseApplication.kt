package com.sendbird.chat.module.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import com.sendbird.android.handlers.InitResultHandler
import com.sendbird.android.log.Logger
import com.sendbird.chat.module.utils.SharedPreferenceUtils
import com.sendbird.chat.module.utils.changeValue

const val SENDBIRD_APP_ID = "B745E68D-A949-4CE5-9ADE-0013ABE31685"

class BaseApplication : Application() {

    companion object {
        private val initMutableLiveData: MutableLiveData<Boolean> =
            MutableLiveData<Boolean>().apply { changeValue(false) }
    }

    override fun onCreate() {
        super.onCreate()
        SendBird.init(
            SENDBIRD_APP_ID,
            applicationContext,
            false,
            object : InitResultHandler {
                override fun onInitFailed(e: SendBirdException) {
                    initMutableLiveData.changeValue(true)
                    // If useLocalCaching is true and init fails, the SDK will turn off the useLocalCaching flag so that you can still proceed with your app
                }

                override fun onMigrationStarted() {
                    // This won't be called if useLocalCaching is set to false.
                }

                override fun onInitSucceed() {
                    Logger.d("onInitSucceed")
                    initMutableLiveData.changeValue(true)
                }
            })

        SharedPreferenceUtils.init(applicationContext)
    }

    fun getInitListData(): LiveData<Boolean> = initMutableLiveData
}