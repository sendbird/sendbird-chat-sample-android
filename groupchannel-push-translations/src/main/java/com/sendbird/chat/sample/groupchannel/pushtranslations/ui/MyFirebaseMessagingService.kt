package com.sendbird.chat.sample.groupchannel.pushtranslations.ui

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.android.SendbirdChat
import com.sendbird.android.push.PushTokenRegistrationStatus
import org.json.JSONObject
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SendbirdChat.registerPushToken(token) { status, e ->
            if (e != null) {
                e.printStackTrace()
                return@registerPushToken
            }

            if (status == PushTokenRegistrationStatus.PENDING) {
                // A token registration is pending.
                // Retry the registration after a connection has been successfully established.
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            if (remoteMessage.data.containsKey("sendbird")) {
                val sendbird = remoteMessage.data["sendbird"]?.let { JSONObject(it) } ?: return
                val channel = sendbird.get("channel") as JSONObject
                val channelUrl = channel["channel_url"] as String
                val messageTitle = sendbird.get("push_title") as String
                val messageBody = getTranslatedMessage(sendbird)
                // If you want to customize a notification with the received FCM message,
                // write your method like sendNotification() below.
                sendNotification(applicationContext, messageTitle, messageBody, channelUrl)
            }
        } catch (exception: Exception) {

        }
    }

    private fun getTranslatedMessage(jsonObject: JSONObject): String {
        val language = Locale.getDefault().language
        val translations = jsonObject.getJSONObject("translations")
        return try {
            translations.getString(language)
        } catch (exception: Exception) {
            jsonObject.getString("message")
        }
    }
}