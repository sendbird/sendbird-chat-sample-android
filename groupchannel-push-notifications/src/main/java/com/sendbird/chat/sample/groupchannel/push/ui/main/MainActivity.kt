package com.sendbird.chat.sample.groupchannel.push.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.messaging.FirebaseMessaging
import com.sendbird.android.SendbirdChat
import com.sendbird.android.push.PushTokenRegistrationStatus
import com.sendbird.chat.module.ui.base.BaseMainActivity
import com.sendbird.chat.sample.groupchannel.push.ui.groupchannel.GroupChannelListFragment

class MainActivity : BaseMainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                task.exception?.printStackTrace()
                return@addOnCompleteListener
            }
            SendbirdChat.registerPushToken(task.result) { status, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@registerPushToken
                }

                if (status == PushTokenRegistrationStatus.PENDING) {
                    // A token registration is pending.
                    // Retry the registration after a connection has been successfully established.
                }
            }
        }
    }

    override fun getFragmentItems(): List<Fragment> = listOf(GroupChannelListFragment())
}
