package com.sendbird.chat.module.utils

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

    fun getUserId() = sharedPreferences.getString(SP_KEY_USER_ID, "")

    fun setUserId(userId: String) {
        sharedPreferences.edit().apply {
            putString(SP_KEY_USER_ID, userId).apply()
        }
    }

    fun setChannelTSMap(hashMap: Map<String, Long>) {
        val jsonObject = JSONObject(hashMap as Map<*, *>)
        val jsonString = jsonObject.toString()
        sharedPreferences.edit().apply {
            remove(SP_KEY_CHANNEL_TIMESTAMP)
            putString(SP_KEY_CHANNEL_TIMESTAMP, jsonString)
            apply()
        }
    }

    fun getChannelTSMap(): ConcurrentHashMap<String, Long> {
        val outputMap = ConcurrentHashMap<String, Long>()
        try {
            val jsonString =
                sharedPreferences.getString(SP_KEY_CHANNEL_TIMESTAMP, JSONObject().toString())
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

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}