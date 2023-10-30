# Hide a group channel

This code sample with UI components demonstrates how to hide a group channel on Sendbird Chat SDK for Android. The SDK provides an additional set of functionalities such as unhide, auto-unhide, 
and archive. To learn more, see [our documentation](https://sendbird.com/docs/chat/v4/android/channel/managing-channels/hide-or-archive-a-group-channel#1-hide-or-archive-a-group-channel) on the feature.

## Prerequisites

+ Android Studio
  + Android Studio Electric Eel | 2022.1.1
+ Android SDK
    + compileSdk: 32
    + minSdk: 26
    + targetSdk: 32
+ Gradle Version
    + gradle: 7.5
    + android gradle plugin: 7.4.2

## How it works

In `GroupChannelListFragment.kt`, use the `GroupChannel.hide(hidePreviousMessages, allowAutoUnhide)` method to have the inactive group channels hidden or archived from the list. The method takes two parameters: `hidePreviousMessages` and `allowAutoUnhide`. When `hidePreviousMessages` is set to `true`, messages that were sent and received before the channels is hidden won't be displayed in the channel. If `allowAutoUnhide` is set to `true`, you can also set the hidden channel to automatically reappear when a new message arrives in the channel. Once hidden with `allowAutoUnhide` set to `true`, the group channel's [`HiddenState`](https://sendbird.com/docs/chat/v4/android/ref/-sendbird%20-chat/com.sendbird.android.channel/-hidden-state/index.html) will be changed to `HIDDEN_ALLOW_AUTO_UNHIDE`.

You can also use the `HiddenState` value as a filter when creating a channel list view with [`GroupChannelCollection`](https://sendbird.com/docs/chat/v4/android/local-caching/using-group-channel-collection/group-channel-collection). First, create a `groupChannelCollection` instance and determine which group channels to include in the collection using the `HiddenChannelFilter` filter. Then, set the filter to `hiddenChannelFilter` in `GroupChannelListQueryParams` to create a group channel list query. 

[GroupChannelListFragment.kt](./app/src/main/java/com/sendbird/chat/sample/groupchannel/hide_archive/groupchannel/GroupChannelListFragment.kt#L155-L217)
``` kotlin
private fun createCollection() {
    var filter = HiddenChannelFilter.UNHIDDEN
    if(showingMode == "ARCHIVED") {
        filter = HiddenChannelFilter.HIDDEN_PREVENT_AUTO_UNHIDE
    } else if(showingMode == "HIDDEN") {
        filter = HiddenChannelFilter.HIDDEN_ALLOW_AUTO_UNHIDE
    }

    val listQuery = GroupChannel.createMyGroupChannelListQuery(
        GroupChannelListQueryParams(
            order = GroupChannelListQueryOrder.LATEST_LAST_MESSAGE,
            myMemberStateFilter = MyMemberStateFilter.ALL,
            includeEmpty = true,
            hiddenChannelFilter = filter,
        )
    )
    val params = GroupChannelCollectionCreateParams(listQuery)
    adapter.clearChannels()
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

private fun hideOrArchiveChannel(channel: GroupChannel, hideChannel: Boolean) {
    channel.hide(hidePreviousMessages = false, allowAutoUnhide = hideChannel) {
        if (it != null) {
            it.printStackTrace()
            showToast("Failed to archive the channel")
            return@hide
        }
    }
}

private fun unhideChannel(channel: GroupChannel) {
    channel.unhide {
        if (it != null) {
            it.printStackTrace()
            showToast("Operation failed")
        }
    }
}
```

## How to run

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.

``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.friends/com.sendbird.chat.sample.groupchannel.friends.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```
