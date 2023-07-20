# Cancel an in-progress file upload in a group channel

This code sample with UI components demonstrates how to cancel an in-progress file upload in a group channel on Sendbird Chat SDK for Android.

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

You can cancel an in-progress file upload in a group channel by calling the cancelFileMessageUpload method of the GroupChannel class.

GroupChannelChatActivity.kt
``` kotlin
private fun cancelSendingFile() {
    val fileMessage = fileMessage ?: return
    val channel = currentGroupChannel ?: return
    val isCanceled = channel.cancelFileMessageUpload(fileMessage.requestId)
    if (!isCanceled) {
        showToast("File already sent")
    }
    this.fileMessage = null
}
```

## How to run

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.

``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.file/com.sendbird.chat.sample.groupchannel.file.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
