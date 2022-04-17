package com.sendbird.chat.sample.openchannel.mentionuser.ui.main

import androidx.fragment.app.Fragment
import com.sendbird.chat.module.ui.base.BaseMainActivity
import com.sendbird.chat.sample.openchannel.mentionuser.ui.openchannel.OpenChannelListFragment

class MainActivity : BaseMainActivity() {
    override fun getFragmentItems(): List<Fragment> = listOf(OpenChannelListFragment())
}