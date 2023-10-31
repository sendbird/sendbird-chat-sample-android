package com.sendbird.chat.sample.groupchannel.friends.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.chat.sample.groupchannel.friends.util.Constants
import com.sendbird.chat.sample.groupchannel.friends.util.FileUtils
import com.sendbird.chat.sample.groupchannel.friends.util.SharedPreferenceUtils
import com.sendbird.chat.sample.groupchannel.friends.util.hideProgress
import com.sendbird.chat.sample.groupchannel.friends.util.showProgress
import com.sendbird.chat.sample.groupchannel.friends.util.showToast
import com.sendbird.chat.sample.groupchannel.friends.R
import com.sendbird.chat.sample.groupchannel.friends.databinding.ActivityUserInfoBinding

class UserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding
    private var userProfileUrl: String? = null
    private var userNickname: String? = null
    private var profileUri: Uri? = null

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            SendbirdChat.autoBackgroundDetection = true
            if (data.resultCode == RESULT_OK) {
                val uri = data.data?.data
                profileUri = uri
                binding.circularImageviewProfile.setImageUri(uri)
            }
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
            FileUtils.selectFile(Constants.DATA_TYPE_ONLY_IMAGE, startForResult, this)
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
        val intent = Intent(this, SignUpActivity::class.java)
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(getString(R.string.permission_granted))
                    SendbirdChat.autoBackgroundDetection = false
                    FileUtils.selectFile(Constants.DATA_TYPE_ONLY_IMAGE, startForResult, this)
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            Constants.PERMISSION_REQUEST_CODE
                        )
                    } else {
                        showToast(getString(R.string.permission_denied))
                    }
                }
            }
        }
    }

}