# Friends in a group channel

This sample with UI components demonstrates how to handle friends in a group channel on Sendbird Chat SDK for Android.

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
You can find how to handle friends in a group channel on Sendbird Chat SDK for Android in this sample.

You can get friends in a group channel by using `GroupChannel.getFriends()` method.
and you can add friends to a group channel by using `GroupChannel.addFriends()` method.
also you can remove friends from a group channel by using `GroupChannel.removeFriends()` method.

ChatMemberListActivity.kt
``` kotlin
val query = SendbirdChat.createFriendListQuery(FriendListQueryParams())
val users = mutableListOf<User>()
fetchAllFriends(query, users) {
    adapter.setFriends(users)
    if (showFriends) {
        adapter.submitList(users)
    } else {
        adapter.notifyDataSetChanged()
    }
}

private fun addFriend(user: User) {
    SendbirdChat.addFriends(listOf(user.userId)) { _, e ->
        if (e != null) {
            e.printStackTrace()
            showToast("Failed to add friend")
            return@addFriends
        }
        getFriendsFromQuery()
    }
}

private fun deleteFriend(friend: User) {
    SendbirdChat.deleteFriend(friend.userId) handler@{ e ->
        if (e != null) {
            e.printStackTrace()
            showToast("Failed to delete friend")
            return@handler
        }
        getFriendsFromQuery()
    }
}
```

## How to run
``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.friends/com.sendbird.chat.sample.groupchannel.friends.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```
