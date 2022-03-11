package com.sendbird.chat.module.utils

import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird

object TextUtils {
    const val CHANNEL_DEFAULT_NAME = "Group Channel"

    fun getGroupChannelTitle(channel: GroupChannel?): String {
        return if (channel == null) {
            "No Members"
        } else {
            val members = channel.members
            if (members.size < 2 || SendBird.getCurrentUser() == null) {
                "No Members"
            } else if (members.size == 2) {
                members.joinToString(separator = ", ") { it.nickname }
            } else {
                members.joinToString(limit = 10, separator = ", ") { it.nickname }
            }
        }
    }
}