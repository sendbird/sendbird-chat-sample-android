package com.sendbird.chat.sample.groupchannel.localcaching.ktx.ui

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.sendbird.android.SendbirdChat
import com.sendbird.android.ktx.extension.connect
import com.sendbird.chat.module.ui.base.BaseSignUpActivity
import com.sendbird.chat.module.ui.base.BaseSplashActivity
import com.sendbird.chat.module.utils.SharedPreferenceUtils
import com.sendbird.chat.module.utils.showToast
import kotlinx.coroutines.launch

class SplashActivity: BaseSplashActivity() {
    override fun checkUserId() {
        val userId = SharedPreferenceUtils.userId
        if (userId.isNullOrBlank()) {
            val intent = Intent(this, BaseSignUpActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            lifecycleScope.launch {
                runCatching {
                    SendbirdChat.connect(userId)
                }.onSuccess {
                    val intent = Intent("$packageName.MAIN")
                    startActivity(intent)
                    finish()
                }.onFailure {
                    showToast("$it")
                    finish()
                    return@launch
                }
            }
        }
    }
}
