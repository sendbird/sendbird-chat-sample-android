package com.sendbird.chat.sample.groupchannel.localcaching.ui.main

import androidx.fragment.app.Fragment
import com.sendbird.chat.module.ui.base.BaseMainActivity
import com.sendbird.chat.sample.groupchannel.localcaching.ui.groupchannel.GroupChannelListFragment

class MainActivity : BaseMainActivity() {
    override fun getFragmentItems(): List<Fragment> = listOf(GroupChannelListFragment())
}
