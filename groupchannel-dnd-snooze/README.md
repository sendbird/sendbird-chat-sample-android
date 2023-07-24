# DND And Snooze in a group channel

This code sample with UI components demonstrates how to use [Do Not Disturb (DND) or Snooze](https://sendbird.com/docs/chat/sdk/v4/android/push-notifications/managing-push-notifications/configure-push-notification-preferences) functionalities in a group channel on Sendbird Chat SDK for Android. While the DND mode mutes notifications from the SDK for a set period and repeats on a daily basis, snoozing will let you block notificatoins for a set period and does not repeat. 

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

Do Not Disturb (DND) and Snooze are one of the notification settings used to block notifications from a specific channel or a group of channels for a set period of time. 

Set the duration for DND in `DNDFragmant.kt`.

``` kotlin
// DNDFragment.kt
private fun checkForDND() {
    SendbirdChat.getDoNotDisturb { isDndOn, startHour, startMin, endHour, endMin, timezone, e ->
        if (e != null) {
            e.printStackTrace()
            showToast("Failed to get dnd")
            return@getDoNotDisturb
        }
        if (isDndOn) {
            loadDND(startHour, startMin, endHour, endMin)
        } else {
            setNoDND()
        }
    }
}

private fun setDND() {
    // Set `startTime` and `endTime` to enable the DND mode.
    SendbirdChat.setDoNotDisturb(true, binding.startTime.hour, binding.startTime.minute, binding.endTime.hour, binding.endTime.minute, TimeZone.getDefault().id) { e ->
        if (e != null) {
            e.printStackTrace()
            showToast("Failed to do operation")
            return@setDoNotDisturb
        }
        showToast("DND setup")
        dismiss()
    }
}

private fun cancelDND() {
    // Cancel the DND mode by setting `startTime` and `endTime` to 0.
    SendbirdChat.setDoNotDisturb(false, 0, 0, 0, 0, TimeZone.getDefault().id) { e ->
        if (e != null) {
            e.printStackTrace()
            showToast("Failed to do operation")
            return@setDoNotDisturb
        }
        showToast("DND canceled")
        dismiss()
    }
}
```

You can also snooze notifications for a set period of time by setting its `startTs` and `endTs`.

``` kotlin
// SnoozeFragment.kt
private fun checkForSnooze() {
    SendbirdChat.getSnoozePeriod { isSnoozeOn, startTs, endTs, e ->
        if (e != null) {
            e.printStackTrace()
            showToast("Failed to get snooze")
            return@getSnoozePeriod
        }
        if (isSnoozeOn) {
            loadSnooze(endTs)
        } else {
            setNoSnooze()
        }
    }
}

private fun setSnooze() {
    val startTime = System.currentTimeMillis()
    val endTime = binding.until.let {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, it.hour)
            set(Calendar.MINUTE, it.minute)
        }
        calendar.time.time
    }
    SendbirdChat.setSnoozePeriod(true, startTime, endTime) { e ->
        if (e != null) {
            e.printStackTrace()
            showToast("Failed to do operation")
            return@setSnoozePeriod
        }
        showToast("DND setup")
        dismiss()
    }

}

private fun cancelSnooze() {
    SendbirdChat.setSnoozePeriod(false, System.currentTimeMillis(), System.currentTimeMillis() + 100) { e ->
        if (e != null) {
            e.printStackTrace()
            showToast("Failed to do operation")
            return@setSnoozePeriod
        }
        showToast("Snooze canceled")
        dismiss()
    }
}
```

## How to run

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.

``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.dndsnooze/com.sendbird.chat.sample.groupchannel.dndsnooze.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
