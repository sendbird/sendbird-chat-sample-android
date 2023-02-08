package com.sendbird.chat.sample.groupchannel.push.ui

import com.google.firebase.FirebaseApp
import com.sendbird.chat.module.ui.base.BaseApplication

class PushNotificationApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

}
