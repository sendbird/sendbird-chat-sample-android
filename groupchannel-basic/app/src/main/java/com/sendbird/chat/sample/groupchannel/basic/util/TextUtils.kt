package com.sendbird.chat.sample.groupchannel.basic.util

import com.sendbird.android.channel.GroupChannel

object TextUtils {
    const val CHANNEL_DEFAULT_NAME = "Group Channel"
    const val NEW_CHANNEL_DEFAULT_TEXT = "New Channel"

    fun getGroupChannelTitle(channel: GroupChannel): String {
        return when {
            channel.members.size < 2 -> "No Members"
            
            else -> channel.members.joinToString(limit = 10, separator = ", ") { it.nickname }
        }
    }
}