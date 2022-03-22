# SendBird Chat sample for Android

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
