package com.sendbird.chat.module.ui.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendBird
import com.sendbird.chat.module.utils.SharedPreferenceUtils
import com.sendbird.chat.module.utils.showToast

class BaseSplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseApplication().getInitListData().observe(this) {
            if (it) {
                setTimer()
            }
        }
    }

    private fun setTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserId()
        }, 1000)
    }

    private fun checkUserId() {
        val userId = SharedPreferenceUtils.getUserId()
        if (userId.isNullOrBlank()) {
            val intent = Intent(this, BaseSignUpActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            SendBird.connect(userId) { user, e ->
                if (e != null) {
                    showToast("$e")
                    finish()
                } else {
                    val intent = Intent()
                    intent.setClassName(
                        packageName,
                        "${packageName.replace("/", "")}.ui.main.MainActivity"
                    )
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}