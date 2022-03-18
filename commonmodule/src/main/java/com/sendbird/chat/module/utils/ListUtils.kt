package com.sendbird.chat.module.utils

import com.sendbird.android.message.BaseMessage

object ListUtils {

    fun findAddMessageIndex(originMessages: List<BaseMessage>, targetMessage: BaseMessage): Int {
        if (originMessages.isEmpty()) {
            return 0
        }
        if (originMessages.last().createdAt > targetMessage.createdAt) {
            return 0
        }
        if (originMessages.last().createdAt < targetMessage.createdAt) {
            return originMessages.size
        }
        for (i in 0 until originMessages.size - 1) {
            val currentMessage = originMessages[i]
            val nextMessage = originMessages[i + 1]
            if (currentMessage.createdAt < targetMessage.createdAt && targetMessage.createdAt < nextMessage.createdAt) {
                return i + 1
            }
        }
        return originMessages.size
    }

}