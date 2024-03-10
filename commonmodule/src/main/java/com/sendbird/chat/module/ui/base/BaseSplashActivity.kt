package com.sendbird.chat.module.ui.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.chat.module.utils.SharedPreferenceUtils
import com.sendbird.chat.module.utils.showToast

open class BaseSplashActivity : AppCompatActivity() {

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

    open fun checkUserId() {
        val userId = SharedPreferenceUtils.userId
        if (userId.isNullOrBlank()) {
            val intent = Intent(this, BaseSignUpActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            SendbirdChat.connect(userId) { user, e ->
                if (user != null) {
                    val intent = Intent("$packageName.MAIN")
                    startActivity(intent)
                } else {
                    if (e != null) {
                        showToast("$e")
                    }
                }
                finish()
            }
        }
    }
}
