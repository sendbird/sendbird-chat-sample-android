# Sendbird Chat SDK samples for Android

This repository contains code samples in Kotlin, showcasing the key functionalities provided by Sendbird Chat SDK for Android. Each sample has a dedicated readme file briefing how the feature works on the code level. To learn more, see our [documentation for Android](https://sendbird.com/docs/chat/v4/android/overview).

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

## Sendbird Application ID

To streamline the implementation process, a sample Application ID has been provided for codes in this repository. However, you need a unique Sendbird Application ID to properly initialize the Chat SDK and enable its features in your production application. Sendbird Application ID can be found in the Overview page on [Sendbird Dashboard](https://dashbaord.sendbird.com). To learn more about how and when to use the Application ID, see our documentation on [initialization](https://sendbird.com/docs/chat/v4/android/getting-started/send-first-message#2-get-started-3-step-3-initialize-the-chat-sdk).

## Code samples

Refer to the following list of code samples and their readme files.

- [Group Channel Add Remove Operators](./groupchannel-add-remove-operators/README.md)
- ...
- ...

## Security tip

// 현재 리드미에 잇는 내용 중 보안 관련 사항을 가져왔습니다. 이 링크(https://github.com/sd-katherinekim/sendbird-chat-sample-android/blob/main/README.md#considerations-in-real-world-app) 내용도 필요할지 알려주세요. & 현재 저희 대시보드에 Disabled옵션이 없는데요, Deny login 일까요?

When a new Sendbird application is created in [Sendbird Dashboard](https://dashbaord.sendbird.com), the default security settings are set permissive to simplify running samples and implementing your first code.

When launching a production application, make sure to review the security settings beforehand in **Settings > Application > Security** on the dashbaord and set **Access token permission** to **Read Only** or **Deny login** so that unauthenticated users can't login as someone else. Also review the **Access Control** lists. Most apps will want to disable **"Allow retrieving user list"** as that could expose sensitivie information such as usage numbers.
