# Categorize messages in a group channel

This code sample with UI components demonstrates how to categorize messages in a group channel using `customType` on Sendbird Chat SDK for Android. You can use the `customType` value as a filter when getting messages.

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

You can set a `customType` value in `string` to a message in `GroupChannelChatActivity.kt`. This can be done either when sending or updating a message.

[GroupChannelChatActivity.kt](./app/src/main/java/com/sendbird/chat/sample/groupchannel/categorizemessages/groupchannel/GroupChannelChatActivity.kt#L311-L339)
``` kotlin
// Update a message with a `customType` value.
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

Also, use the `customType` value as a filter when getting messages in a group channel.

[GroupChannelCategorizeMessagesActivity.kt](./app/src/main/java/com/sendbird/chat/sample/groupchannel/categorizemessages/categorize/GroupChannelCategorizeMessagesActivity.kt#L74-L93)
``` kotlin
// Search messages by custom type.
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

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.

``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.categorizemessages/com.sendbird.chat.sample.groupchannel.categorizemessages.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
