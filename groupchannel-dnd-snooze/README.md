# DND And Snooze in a group channel

This code sample with UI components demonstrates how to dnd or snooze in a group channel on Sendbird Chat SDK for Android.

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
DND(Don not disturb) and Snooze are features that can be used to block notifications from a specific channel or group of channels for a certain period of time.
This sample demonstrates how to use these features in a group channel.

DNDFragment.kt
``` kotlin
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
    // Set DND by setting start and end time
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
    // Cancel DND by setting start and end time to 0
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

SnoozeFragment.kt
``` kotlin
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
``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.dndsnooze/com.sendbird.chat.sample.groupchannel.dndsnooze.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
