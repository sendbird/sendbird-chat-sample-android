# Categorize group channels

This sample app demonstrates how to categorize group channels in Sendbird Chat SDK using `customType`. You can use the `customType` filter when getting a list of group channels.

![final_output](https://github.com/sendbird/sendbird-chat-sample-android/assets/104121286/bb279bec-e98f-4983-b059-d8f22f0e97a1)

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

In Sendbird Chat SDK, group channels can have `customType`, which can be set in `GroupChannelCreateParams`. The custom type is a string value that you can use to categorize channels.

[SelectUserActivity.kt](./app/src/main/java/com/sendbird/chat/sample/groupchannel/categorizechannels/user/SelectUserActivity.kt#L131-L152)
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

Then, you can use the custom type as a filter in `GroupChannelListQuery` when you retrieve a list of channels. Pass the `listQuery` to `GroupChannelCollectionCreateParams` to apply it to the group channel collection.

[GroupChannelListFragment.kt](./app/src/main/java/com/sendbird/chat/sample/groupchannel/categorizechannels/groupchannel/GroupChannelListFragment.kt#L91-L129)
``` kotlin
private fun createCollection() {
    // Create a GroupChannelListQuery instance with GroupChannelListQueryParams.
    // Set customTypesFilter to retrieve only the channels with the specified custom types.
    val listQuery = GroupChannel.createMyGroupChannelListQuery(
        GroupChannelListQueryParams(
            order = GroupChannelListQueryOrder.LATEST_LAST_MESSAGE,
            customTypesFilter = customTypeFilterList
        )
    )

    // Create a GroupChannelCollection instance with GroupChannelCollectionCreateParams.
    val params = GroupChannelCollectionCreateParams(listQuery)
    // Set GroupChannelCollectionHandler to receive events related to the GroupChannelCollection.
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

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.

``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.categorizechannels/com.sendbird.chat.sample.groupchannel.categorizechannels.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
