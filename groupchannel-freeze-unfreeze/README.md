# Freeze or unfreeze a group channel

This code sample with UI components demonstrates how to freeze or unfreeze a group channel on Sendbird Chat SDK for Android. Freezing a group channel is part of moderation. When a channel is frozen, only the [operators](https://sendbird.com/docs/chat/v4/android/user/overview-user#2-user-types-3-operator) can send messages to the channel and users aren't allowed to chat.

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

You can freeze or unfreeze a group channel by calling the `freeze()` and `unfreeze()` methods of the `GroupChannel` class. 

To freeze a channel, first, you need a `Operator` role in the channel. When a group channel is frozen, only the channel operators can send a message while other users can't. To unfreeze the channel, you also need an operator in the channel.

``` kotlin
// GroupChannelChatActivity.kt
private fun freezeChannel() {
    currentGroupChannel?.freeze {
        if (it != null) {
            showToast("Freeze failed: ${it.message}")
            return@freeze
        }
        showToast("Channel frozen")
        setChannelTitle()
        invalidateOptionsMenu()
    }
}

private fun unfreezeChannel() {
    currentGroupChannel?.unfreeze {
        if (it != null) {
            showToast("Unfreeze failed: ${it.message}")
            return@unfreeze
        }
        showToast("Channel unfrozen")
        setChannelTitle()
        invalidateOptionsMenu()
    }
}
```

## How to run

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.

``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.freeze/com.sendbird.chat.sample.groupchannel.freeze.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```
