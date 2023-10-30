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

You can cancel an in-progress file upload in a group channel by calling the `cancelFileMessageUpload` method of the `GroupChannel` class. This can be done in  `GroupChannelChatActivity.kt`.

[GroupChannelChatActivity.kt](./app/src/main/java/com/sendbird/chat/sample/groupchannel/file/groupchannel/GroupChannelChatActivity.kt#L175-L183)
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

## More info

To learn more about how to send and cancel a file message, see [our documentation](https://sendbird.com/docs/chat/sdk/v4/android/message/sending-a-message/cancel-an-in-progress-file-upload).
