package com.sendbird.chat.sample.groupchannel.pushtranslations.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.sendbird.android.SendbirdChat
import com.sendbird.chat.module.ui.base.BaseMainActivity
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.pushtranslations.ui.groupchannel.GroupChannelListFragment

class MainActivity : BaseMainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferredLanguage = listOf("en", "de", "es", "fr")
        SendbirdChat.updateCurrentUserInfo(preferredLanguage) { e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to update the languages")
            }
        }

    }

    override fun getFragmentItems(): List<Fragment> = listOf(GroupChannelListFragment())
}
