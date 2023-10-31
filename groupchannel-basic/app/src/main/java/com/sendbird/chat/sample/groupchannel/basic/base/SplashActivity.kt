package com.sendbird.chat.sample.groupchannel.basic.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.chat.sample.groupchannel.basic.BaseApplication
import com.sendbird.chat.sample.groupchannel.basic.util.SharedPreferenceUtils
import com.sendbird.chat.sample.groupchannel.basic.util.showToast

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as BaseApplication).initLiveData.observe(this) {
            if (it) {
                startTimer()
            }
        }
    }

    private fun startTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserId()
        }, 1000)
    }

    private fun checkUserId() {
        val userId = SharedPreferenceUtils.userId
        if (userId.isNullOrBlank()) {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            SendbirdChat.connect(userId) { _, e ->
                if (e != null) {
                    showToast("$e")
                    finish()
                    return@connect
                }
                val intent = Intent("$packageName.MAIN")
                startActivity(intent)
                finish()
            }
        }
    }
}