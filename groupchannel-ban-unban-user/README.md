# Ban or unban users

This sample app demonstrates how to ban or unban user in a group channel on Sendbird Chat SDK for Android.

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

## Ban And unban user

`ChatMemberListActivity` is an activity where you can view a list of channel members and manage them using Sendbird Chat SDK. An operator or admin in a group channel can moderate users in a group channel by banning or unbanning them as required. 

In the context menu created by the `createMenuForMember()` function, you can find options to either ban or unban a user. When you select "Ban" from the context menu, the `banUser()` function is called. This function uses the `GroupChannel.banUser()` method from the SDK to ban the selected user from the channel. If the operation is successful, it refreshes the list to show the current state of banned users.

If you select "Unban" from the context menu, the `unbanUser()` function is called. This function uses the `GroupChannel.unbanUser()` method from the SDK to lift the ban from the selected user. If the operation is successful, it refreshes the list to show the current state of active users.

```kotlin
private fun initRecyclerView() {
  // Create an adapter when a user clicks on a channel member.
  // Then a menu shows items to ban or unban the member.
  adapter = ChatMemberListAdapter { member, view, _ ->
    createMenuForMember(member, view)
  }
  ...
}

// Create a menu with a long-tab on a channel member.
// When a user clicks on one of the menu items,
// they can ban or unban the member by calling GroupChannel.banUser() or GroupChannel.unbanUser().
private fun createMenuForMember(member: User, view: View) {
    view.setOnCreateContextMenuListener { contextMenu, _, _ ->
        if (areBannedUsersDisplayed) {
            val menu = contextMenu.add(Menu.NONE, 0, 0, "UnBan")
            menu.setOnMenuItemClickListener {
                unbanUser(member)
                return@setOnMenuItemClickListener true
            }
        } else {
            val menu = contextMenu.add(Menu.NONE, 0, 0, "Ban")
            menu.setOnMenuItemClickListener {
                banUser(member)
                return@setOnMenuItemClickListener true
            }
        }
    }
}

// Ban the user for a set period of time.
private fun banUser(member: User, periodToBan: Int = -1) {
    val groupChannel = currentChannel ?: return
    groupChannel.banUser(member, "ban reason", periodToBan) handler@{
        if (it != null) {
            showToast("Cannot ban user: ${it.message}")
            return@handler
        }
        retrieveAndDisplayBannedUsers()
        showToast("User banned")
    }
}

// Unban the user.
private fun unbanUser(member: User) {
    val groupChannel = currentChannel ?: return
    // Ban the user for an indifinite period of time.
    groupChannel.unbanUser(member) handler@{
        if (it != null) {
            showToast("Cannot unban user: ${it.message}")
            return@handler
        }
        retrieveAndDisplayActiveUsers()
        showToast("User unbanned")
    }
}
```

## How to run

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.


``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.ban/com.sendbird.chat.sample.groupchannel.ban.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
