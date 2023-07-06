# Group Channel Ban And Unban User

This sample app demonstrates how to ban and unban user in a group channel.

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

The `ChatMemberListActivity` is an activity where you can view a list of chat members and manage them using the SendBird SDK.

In the context menu created by the `createMenuForMember()` function, you can find options to either ban or unban a user.

When you select "Ban" from the context menu, the `banUser()` function is called. This function uses the `GroupChannel.banUser()` method from the SendBird SDK to ban the selected user from the chat group. If the operation is successful, it refreshes the list to show the current state of banned users.

If you select "Unban" from the context menu, the `unbanUser()` function is called. This function uses the `GroupChannel.unbanUser()` method from the SendBird SDK to remove the ban on the selected user. If the operation is successful, it refreshes the list to show the current state of active users.

These two functions, powered by SendBird SDK, allow an admin to manage the users in the chat group by banning or unbanning them as required.

```kotlin
private fun initRecyclerView() {
  // Create adapter when the user click on a member, we display a menu to ban or unban the user
  adapter = ChatMemberListAdapter { member, view, _ ->
    createMenuForMember(member, view)
  }
  ...
}

// Create menu when long click on a member
// When the user click on the menu item, we ban or unban the user by calling GroupChannel.banUser() or GroupChannel.unbanUser()
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

// Ban the user for a period of time
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

// Unban the user
private fun unbanUser(member: User) {
    val groupChannel = currentChannel ?: return
    //we ban the user for an indefinitely period of time
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
``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.ban/com.sendbird.chat.sample.groupchannel.ban.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
