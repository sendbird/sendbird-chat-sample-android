package com.sendbird.chat.sample.typingindicator.ui.main

import androidx.fragment.app.Fragment
import com.sendbird.chat.module.ui.base.BaseMainActivity
import com.sendbird.chat.sample.typingindicator.ui.groupchannel.GroupChannelListFragment

class MainActivity : BaseMainActivity() {
    override fun getFragmentItems(): List<Fragment> = listOf(GroupChannelListFragment())
}
