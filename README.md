# Sendbird Chat sample for Android

## Overview

This repository provides feature-level Chat samples with Kotlin to demonstrate how to use the [Sendbird Chat SDK](https://sendbird.com/docs/chat).

## Requirements

Android Studio 2.0+

## Running the app

Open this project in Android Studio and select 'run configurations' to view a dropdown of feature-level Chat samples to choose from.

Once a sample is chosen, create a device and run the app on an Android device or Android emulator.

### Language

+ Kotlin

### Version

+ Android SDK
    + compileSdk: 32
    + minSdk: 26
    + targetSdk: 32
+ Gradle Version
    + gradle: 7.2
    + android gradle plugin: 4.2.0

## Project structure

```
.
├── commonmodule
│   ├── ui
│   │   ├── CustomViews
│   │   └── base
│   │        ├── BaseApplication
│   │        ├── BaseActivity
│   │        └── BaseFragment
│   └── utils
│       ├── Utils
│       └── Extension
├── groupchannel-basic
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-typingindicator
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-readnessage
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-unreadmessages
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-onlinemembers
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-membersandoperators
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-banned-and-muted-users
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-groupchannel-add-remove-operators
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-mention-members
│   ├── groupchannel
│   ├── main
│   └── user
├── groupchannel-report-user-message-channel
│   ├── groupchannel
│   ├── main
│   └── user
├── openchannel-basic
│   ├── openchannel
│   └── main
├── openchannel-user-online
│   ├── openchannel
│   └── main
├── openchannel-update-message
│   ├── openchannel
│   └── main
├── openchannel-delete-message
│   ├── openchannel
│   └── main
├── openchannel-copy-message
│   ├── openchannel
│   └── main
├── openchannel-mention-user
│   ├── openchannel
│   └── main
├── openchannel-report-message-user-channel
│   ├── openchannel
│   └── main
└── openchannel-feature-a
...

```

### [CommonModule](https://github.com/sendbird/sendbird-chat-sample-android/tree/main/commonmodule)

- [ui](https://github.com/sendbird/sendbird-chat-sample-android/tree/main/commonmodule/src/main/java/com/sendbird/chat/module/ui)
    - Created for frequently used views.
    - Created activities commonly used such as Sign-Up, Splash, and UserInfo.
- [utils](https://github.com/sendbird/examples-chat-ios/tree/main/CommonModule/CommonModule/View)
    - Commonly used functions in Basic, Feature App

### BasicSample

- groupchannel-basic
- openchannel-basic

### FeatureSample

- groupchannel-typingindicator
- groupchannel-unreadmessages
- groupchannel-readmessage
- groupchannel-onlinemembers
- groupchannel-membersandoperators
- groupchannel-banned-and-muted-users
- groupchannel-user-online
- groupchannel-mention-members
- openchannel-user-online
- openchannel-delete-message
- openchannel-update-message
- openchannel-copy-message
...