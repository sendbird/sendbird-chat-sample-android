package com.sendbird.chat.sample.groupchannel.ban.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

const val SP_KEY_USER_ID = "user_id"
const val SP_KEY_CHANNEL_TIMESTAMP = "channel_time_stamp"

object SharedPreferenceUtils {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    var userId: String?
        get() = sharedPreferences.getString(SP_KEY_USER_ID, "")
        set(value) = sharedPreferences.edit { it.putString(SP_KEY_USER_ID, value) }

    var channelTSMap: ConcurrentHashMap<String, Long>
        get() {
            val outputMap = ConcurrentHashMap<String, Long>()
            val jsonString =
                sharedPreferences.getString(SP_KEY_CHANNEL_TIMESTAMP, JSONObject().toString())
                    ?: return outputMap
            try {
                val jsonObject = JSONObject(jsonString)
                val keysItr = jsonObject.keys()
                while (keysItr.hasNext()) {
                    val key = keysItr.next()
                    val value = jsonObject[key] as Long
                    outputMap[key] = value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return outputMap
        }
        set(hashMap) = sharedPreferences.edit {
            val jsonObject = JSONObject(hashMap as Map<*, *>)
            val jsonString = jsonObject.toString()
            it.remove(SP_KEY_CHANNEL_TIMESTAMP)
            it.putString(SP_KEY_CHANNEL_TIMESTAMP, jsonString)
        }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

}