package com.sendbird.chat.sample.groupchannel.friends.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.chat.sample.groupchannel.friends.util.Constants
import com.sendbird.chat.sample.groupchannel.friends.util.SharedPreferenceUtils
import com.sendbird.chat.sample.groupchannel.friends.util.showToast
import com.sendbird.chat.sample.groupchannel.friends.R
import com.sendbird.chat.sample.groupchannel.friends.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSignin.setOnClickListener {
            val userId = binding.tagEdittextUserId.getText()
            signIn(userId)
        }
    }

    private fun signIn(userId: String) {
        if (userId.isBlank()) {
            showToast(R.string.user_id_empty_msg)
            return
        }
        SendbirdChat.connect(userId) { user, e ->
            if (e != null) {
                showToast("$e")
                return@connect
            }
            if (user != null) {
                SharedPreferenceUtils.userId = user.userId
                val intent = Intent("$packageName.MAIN")
                if (user.nickname.isBlank()) {
                    intent.putExtra(Constants.INTENT_KEY_NICKNAME_REQUIRE, true)
                } else {
                    intent.putExtra(Constants.INTENT_KEY_NICKNAME_REQUIRE, false)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}