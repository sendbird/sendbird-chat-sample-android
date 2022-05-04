package com.sendbird.chat.sample.openchannel.categorizemessages.ui.main

import androidx.fragment.app.Fragment
import com.sendbird.chat.module.ui.base.BaseMainActivity
import com.sendbird.chat.sample.openchannel.categorizemessages.ui.openchannel.OpenChannelListFragment

class MainActivity : BaseMainActivity() {
    override fun getFragmentItems(): List<Fragment> = listOf(OpenChannelListFragment())
}