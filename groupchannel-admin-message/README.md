# Send admin messages in a group channel

This sample with UI components demonstrates how to handle [Admin messages](https://sendbird.com/docs/chat/v3/platform-api/message/message-overview#2-message-types) in a group channel on Sendbird Chat SDK for Android. Admin messages can be sent through [Sendbird Dashboard](https://dashboard.sendbird.com) or a [Platform API request](https://sendbird.com/docs/chat/v3/platform-api/message/messaging-basics/send-a-message#1-send-a-message), not on the SDK. 

## Requirements

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

`GroupChannelChatActivity` is an Activity that manages messages sent by channel members and displays the chat view. To perform these functions, it utilizes an object called `messageCollection`.

The `messageCollection` object collects new messages sent by users, and helps to sequentially sort and process these messages. These collected and sorted messages are then handled through the `GroupChannelChatAdapter`.

``` kotlin
//GroupChannelChatActivity.kt
private fun initRecyclerView() {
  // When the message is added to the collection, the message is added to the adapter.
  adapter = GroupChannelChatAdapter({ baseMessage, view ->        
  ..
  }, {
  ..
  })
}
```

`GroupChannelChatAdapter` operates as a `RecyclerView` adapter. It creates a distinct view for each message type and binds the message data to the corresponding view. This adapter is designed to handle [various types of messages](https://sendbird.com/docs/chat/v3/platform-api/message/message-overview#2-message-types), including text messages, file messages, and admin messages. Among these, admin messages are handled by the `GroupChatAdminViewHolder` through the `onCreateViewHolder` method. By using different `ViewHolders` by message type, it can dynamically apply the appropriate layout for each message type.

``` kotlin
// GroupChannelChatAdapter.kt
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
  when (viewType) {
    ..
    // When the message is an admin message, return GroupChatAdminViewHolder.
    VIEW_TYPE_ADMIN -> return GroupChatAdminViewHolder(
      ListItemChatAdminBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
    ..
  }
}

override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
  ...
  when (holder) {
    ..
    is GroupChatAdminViewHolder -> {
      holder.bind(getItem(position), showDate)
    }
  }  
}

override fun getItemViewType(position: Int): Int {
  return if (getItem(position) is AdminMessage) {
    VIEW_TYPE_ADMIN
  } else {
    ...
  }
}

inner class GroupChatAdminViewHolder(private val binding: ListItemChatAdminBinding) :
  BaseViewHolder(binding) {
  fun bind(
    message: BaseMessage,
    showDate: Boolean
  ) {
    binding.chatBubbleAdminView.setText(message.message)
    if (showDate) {
      binding.dateTagView.setMillisecond(message.createdAt)
      binding.dateTagView.visibility = View.VISIBLE
    } else {
      binding.dateTagView.visibility = View.GONE
    }
  }
}
```

## How to run

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.

``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.adminmessage/com.sendbird.chat.sample.groupchannel.adminmessage.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
