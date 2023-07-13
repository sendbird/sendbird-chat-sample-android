# Categorize messages in a group channel

This sample with UI components demonstrates how to categorize messages in a group channel on Sendbird Chat SDK for Android.

## Prerequisites
+ Android Studio
  + Android Studio Electric Eel | 2022.1.1
+ Android SDK
    + compileSdk: 34
    + minSdk: 26
    + targetSdk: 34
+ Gradle Version
    + gradle: 7.5
    + android gradle plugin: 7.4.2

## How it works

You can set a custom type to a message and filter messages by the custom type.
The custom type is a string value that you can set to a message.
You can set a custom type to a message when you send a message or update a message.
You can filter messages by the custom type when you get messages from a channel.

GroupChannelChatActivity.kt
``` kotlin
// Update message to set custom type
private fun updateMessageCustomType(message: BaseMessage) {
    when (message) {
        is FileMessage -> {
            val params = FileMessageUpdateParams().apply {
                customType = if (message.customType == CATEGORIZE) "" else CATEGORIZE
            }
            currentGroupChannel?.updateFileMessage(
                message.messageId,
                params
            ) { _, e ->
                if (e != null) {
                    showToast("${e.message}")
                }

            }
        }
        else -> {
            val params = UserMessageUpdateParams().apply {
                customType = if (message.customType == CATEGORIZE) "" else CATEGORIZE
            }
            currentGroupChannel?.updateUserMessage(message.messageId, params) { _, e ->
                if (e != null) {
                    showToast("${e.message}")
                }
            }
        }
    }
}
```

GroupChannelCategorizeMessagesActivity.kt
``` kotlin
// Search messages by custom type
private fun getCategorizeMessages(channel: GroupChannel) {
    val params = PreviousMessageListQueryParams(
        customTypesFilter = listOf(
            GroupChannelChatActivity.CATEGORIZE
        )
    )
    val categorizeMessagesQuery = channel.createPreviousMessageListQuery(params)
    categorizeMessagesQuery.load { messages, e ->
        if (e != null) {
            showToast("${e.message}")
            return@load
        }
        if (messages.isNullOrEmpty()) {
            showToast("No categorize messages")
            return@load
        }
        adapter.submitList(messages)
    }
}
```

## How to run
``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.categorizemessages/com.sendbird.chat.sample.groupchannel.categorizemessages.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
