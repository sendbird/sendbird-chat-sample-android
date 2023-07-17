# Hide and Archive in a group channel

This sample with UI components demonstrates how to hide and archive in a group channel on Sendbird Chat SDK for Android.

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

You can get a `hide` and `archive` feature in a group channel by using `GroupChannelListQuery` with `hiddenChannelFilter`.
You can hide and archive a group channel by using `GroupChannel.hide(hidePreviousMessages, allowAutoUnhide)` method.

GroupChannelListFragment.kt
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

private fun unHideChannel(channel: GroupChannel) {
    channel.unhide {
        if (it != null) {
            it.printStackTrace()
            showToast("Operation failed")
        }
    }
}
```

## How to run
``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.friends/com.sendbird.chat.sample.groupchannel.friends.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```
