package com.sendbird.chat.module.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.chat.module.R
import com.sendbird.chat.module.databinding.ActivityUserInfoBinding
import com.sendbird.chat.module.utils.*

class BaseUserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding
    private var userProfileUrl: String? = null
    private var userNickname: String? = null
    private var profileUri: Uri? = null

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            SendbirdChat.autoBackgroundDetection = true
            profileUri = uri
            binding.circularImageviewProfile.setImageUri(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        initUserProfile()
    }

    private fun init() {
        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.circularImageviewEditProfile.setOnClickListener {
            SendbirdChat.autoBackgroundDetection = false
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.purpleButtonSave.setOnClickListener {
            val nickname = binding.tagEdittextNickname.getText()
            saveUserInfo(profileUri, nickname)
        }
        binding.textviewSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun initUserProfile() {
        val user = SendbirdChat.currentUser
        if (user == null) {
            showToast(R.string.user_id_info_error)
            finish()
            return
        }
        if (user.profileUrl.isNotBlank()) {
            userProfileUrl = user.profileUrl
            userNickname = user.nickname
            binding.circularImageviewProfile.setImageUrl(user.profileUrl)
        }
        if (user.nickname.isNotBlank()) {
            binding.tagEdittextNickname.setText(user.nickname)
        }
    }

    private fun saveUserInfo(profileUri: Uri?, nickname: String) {
        if (nickname.isBlank()) {
            showToast(R.string.enter_nickname_msg)
            return
        }
        if (profileUri != null) {
            val progressId = showProgress()
            val fileInfo = FileUtils.getFileInfo(profileUri, applicationContext)
            if (fileInfo == null) {
                showToast(R.string.file_transfer_error)
                return
            }
            val params = UserUpdateParams().apply {
                this.nickname = nickname
                this.profileImageFile = fileInfo.file
            }

            SendbirdChat.updateCurrentUserInfo(params) {
                if (it != null) {
                    showToast("${it.message}")
                }
                hideProgress(progressId)
                finish()
            }
        } else {
            if (userNickname != nickname) {
                val params = UserUpdateParams().apply {
                    this.nickname = nickname
                }
                SendbirdChat.updateCurrentUserInfo(params) {
                    if (it != null) {
                        showToast("${it.message}")
                    }
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    protected fun signOut() {
        SendbirdChat.disconnect {}
        val intent = Intent(this, BaseSignUpActivity::class.java)
        startActivity(intent)
        SharedPreferenceUtils.clear()
        finishAffinity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}