# Freeze and Unfreeze in a group channel

This code sample with UI components demonstrates how to freeze and unfreeze in a group channel on Sendbird Chat SDK for Android.

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
You can freeze and unfreeze a group channel by calling the `freeze()` and `unfreeze()` methods of the `GroupChannel` class.
The `freeze()` method freezes a group channel, and the `unfreeze()` method unfreezes a group channel.
When a group channel is frozen, users cannot send messages in the channel.
To freeze a group channel, you need to have the `Operator` role in the channel.
To unfreeze a group channel, you need to have the `Operator` role in the channel or the `Operator` role in the application.

GroupChannelChatActivity.kt
``` kotlin
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
```

## How to run
``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.freeze/com.sendbird.chat.sample.groupchannel.freeze.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```
