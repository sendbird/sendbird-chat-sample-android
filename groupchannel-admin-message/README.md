# Group Channel Add Remove Operators

This sample app demonstrates how to handle Admin Message in a group channel.
In the SDK, Admin messages cannot be sent, but only the part that handles the received messages. You can send it from the Dashboard.

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

## Admin Message
`GroupChannelChatActivity` is an Activity that manages the main screen for chatting. On this screen, it handles and displays messages being exchanged between users. To perform these functions, it utilizes an object called `messageCollection`.

The `messageCollection` object collects new messages sent by users, and helps to sequentially sort and process these messages. These collected and sorted messages are then handled through the `GroupChannelChatAdapter`.

`GroupChannelChatAdapter` operates as a RecyclerView Adapter. It creates views for each message and binds the message data to the corresponding view. This adapter is designed to handle various types of messages. It can handle a wide variety of messages including text messages, image messages, file messages, and admin messages. Among these, admin messages are handled by the `GroupChatAdminViewHolder` through the `onCreateViewHolder` method. By using different ViewHolders depending on the type of message, it can dynamically apply the appropriate layout for each message type.

GroupChannelChatActivity.kt
``` kotlin
private fun initRecyclerView() {
  // When the message is added to the collection, the message is added to the adapter.
  adapter = GroupChannelChatAdapter({ baseMessage, view ->        
  ..
  }, {
  ..
  })
}
```

GroupChannelChatAdapter.kt
``` kotlin
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
  when (viewType) {
    ..
    // When the message is an admin message, return the GroupChatAdminViewHolder.
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
``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.adminmessage/com.sendbird.chat.sample.groupchannel.adminmessage.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
