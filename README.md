# Sendbird Chat sample for Android

This repository provides feature-level Chat samples with Kotlin

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
├── openchannel-basic
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
...

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

## How to add new feature sample

1. Copy openchannel-basic or groupchannel-basic
2. Paste on the same layer
3. Change the name of project
4. Include copied module to setting.gradle
5. Rename package
6. Change the applicationId in the build.gradle of the created project.

+ When the main activity needs to be updated to another activity, the <intent-filter> of
  MainActivity must be moved as well the corresponding activity.