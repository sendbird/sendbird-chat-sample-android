# Add or remove operators in a group channel

This sample with UI components demonstrates how to register or remove operators in a group channel on Sendbird Chat SDK for Android. Operators are users who can moderate channels by muting or banning users as well as freezing channels. To learn more, see our [documentation on operators](https://sendbird.com/docs/chat/v4/android/channel/managing-operators/register-and-remove-operators).

![final_output](https://github.com/sendbird/sendbird-chat-sample-android/assets/104121286/6c7271fe-6c22-4ae3-8ddc-97fdf347a997)

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

When a user clicks the member list button in `GroupChannelChatActivity.kt`, the `changeMemberOperatorStatus()` method and [`ChatMemberListActivity.kt`](https://github.com/sendbird/sendbird-chat-sample-android/blob/02514ee6e46e598cd81f285b75174761b6b1c5d7/groupchannel-add-remove-operators/app/src/main/java/com/sendbird/chat/sample/groupchannel/addremoveoperators/user/ChatMemberListActivity.kt#L35) are called.

The `changeMemberOperatorStatus()` method creates an adapter and change a user's role into an operator. If the current user wasn't an operator, their status changes to an operator. If the user was already an operator, their status remains the same. After a user's role is changed, the `refreshMembers()` method is called to refresh the member list.

As for the kit, `initRecyclerView()` is the first method to be called and the method sets the adapter to `RecyclerView`.
When the `ChatMemberListActivity.kt` is called, the `initRecyclerView()` method is called first.
And the `initRecyclerView()` method sets the adapter to the RecyclerView.
The adapter is created with the `changeMemberOperatorStatus()` method.

The `changeMemberOperatorStatus()` method is called when the user clicks the member list item.
If the current user is the operator, the operator role cannot be changed.
If the current user is not the operator, the operator role can be changed.
And after the operator role is changed, the `refreshMembers()` method is called to refresh the member list.

[ChatMemberListActivity.kt](./app/src/main/java/com/sendbird/chat/sample/groupchannel/addremoveoperators/user/ChatMemberListActivity.kt#L56-L65)
``` kotlin
private fun initRecyclerView() {
    // Create an adapter about change the operator status of the member
    // So It is possible to change the operator status of the member by clicking the item.
    adapter = ChatMemberListAdapter { member, position -> changeMemberOperatorStatus(member, position) }
    ...
}

// Change the operator status of the member
private fun changeMemberOperatorStatus(member: Member, position: Int) {
    // If the current user is the operator, the operator role cannot be changed.
    if (member.role == Role.OPERATOR) {
        // Call the removeOperators() method to remove the operator role from the member.
        removeOperatorRole(member, position)
        return
    }
    // If the current user is not the operator, the operator role can be changed.
    addOperatorRole(member, position)
}

// Add operator role to the member
private fun addOperatorRole(member: Member, position: Int) {
    // Call the addOperators() method to add the operator role to the member.
    currentChannel?.addOperators(listOf(member.userId)) { exception ->
        if (exception != null) {
            showToast("${exception.message}")
            return@addOperators
        }
        showToast("User made operator")
        // Refresh the member if the member is not Operator refresh one more time.
        refreshMember(member, position, true)
    }
}

// Remove operator role from the member
private fun removeOperatorRole(member: Member, position: Int) {
    // Call the removeOperators() method to remove the operator role from the member.
    currentChannel?.removeOperators(listOf(member.userId)) { exception ->
        if (exception != null) {
            showToast("${exception.message}")
            return@removeOperators
        }
        showToast("User back to basic")
        // Refresh the member
        refreshMember(member, position,false)
    }
}

// Refresh the member
private fun refreshMember(member: Member, position: Int, isOperator: Boolean) {
    currentChannel?.createMemberListQuery(MemberListQueryParams())?.next { list, e ->
        if (e != null) {
            showToast("${e.message}")
            return@next
        }
        if (list != null) {
            // check if the member is Operator in the list about same userId from member.
            val operator = list.find { it.userId == member.userId }?.role == Role.OPERATOR
            // and Check operator is same with isOperator
            if (operator == isOperator) {
                // If the member is Operator and isOperator is true or
                // If the member is not Operator and isOperator is false
                // Refresh the member
                adapter.notifyItemChanged(position)
                return@next
            }else{
                // refresh one more time
                refreshMember(member, position, isOperator)
            }
        }
    }
}
```

## How to run

Copy and paste the following code into Terminal or run it on an emulator to see what the sample looks like.

``` bash
./gradlew :app:installDebug
adb shell am start -n "com.sendbird.chat.sample.groupchannel.addremoveoperators/com.sendbird.chat.sample.groupchannel.addremoveoperators.base.SplashActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER --splashscreen-show-icon
```
