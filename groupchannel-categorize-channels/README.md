# Categorize group channels

This sample app demonstrates how to categorize group channels in Sendbird Chat SDK.

## Requirements

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

You can create a channel with a custom type. The custom type is a string value that you can use to categorize channels.

SelectUserActivity.kt
``` kotlin
private fun createChannel() {
  ...
    val params = GroupChannelCreateParams()
        .apply {
            userIds = adapter.selectUserIdSet.toList()
            // input is the value of the customType field in the input field.
            customType = input
        }
    GroupChannel.createChannel(params) createChannelLabel@{ groupChannel, e ->
        if (e != null) {
            showToast("${e.message}")
            return@createChannelLabel
        }
        if (groupChannel != null) {
            val intent = Intent(
                this@SelectUserActivity,
                GroupChannelChatActivity::class.java
            )
            intent.putExtra(Constants.INTENT_KEY_CHANNEL_URL, groupChannel.url)
            intent.putExtra(Constants.INTENT_KEY_CHANNEL_TITLE, groupChannel.name)
            startActivity(intent)
            finish()
        }
    }
  ...
}
```

You can use the custom type to filter channels when you retrieve a list of channels.

GroupChannelListFragment.kt
``` kotlin
private fun createCollection() {
    // Create a GroupChannelListQuery with a GroupChannelListQueryParams.
    // You can set custom types filter to retrieve only the channels of the custom types.
    val listQuery = GroupChannel.createMyGroupChannelListQuery(
        GroupChannelListQueryParams(
            order = GroupChannelListQueryOrder.LATEST_LAST_MESSAGE,
            customTypesFilter = customTypeFilterList
        )
    )

    // Create a GroupChannelCollection with a GroupChannelCollectionCreateParams.
    val params = GroupChannelCollectionCreateParams(listQuery)
    // Set a GroupChannelCollectionHandler to receive events related to the GroupChannelCollection.
    groupChannelCollection = SendbirdChat.createGroupChannelCollection(params).apply {
        groupChannelCollectionHandler = (object : GroupChannelCollectionHandler {
            override fun onChannelsAdded(
                context: GroupChannelContext,
                channels: List<GroupChannel>
            ) {
                adapter.updateChannels(channels)
            }

            override fun onChannelsDeleted(
                context: GroupChannelContext,
                deletedChannelUrls: List<String>
            ) {
                adapter.deleteChannels(deletedChannelUrls)
            }

            override fun onChannelsUpdated(
                context: GroupChannelContext,
                channels: List<GroupChannel>
            ) {
                adapter.updateChannels(channels)
            }
        })
    }
    loadMore(true)
}
```

## How to run
``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.categorizechannels/com.sendbird.chat.sample.groupchannel.categorizechannels.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
