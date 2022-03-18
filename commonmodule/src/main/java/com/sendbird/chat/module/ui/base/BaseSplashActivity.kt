package com.sendbird.chat.module.ui.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.chat.module.utils.SharedPreferenceUtils
import com.sendbird.chat.module.utils.showToast

class BaseSplashActivity : AppCompatActivity() {

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
            val intent = Intent(this, BaseSignUpActivity::class.java)
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