package com.sendbird.chat.module.ui.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendBird
import com.sendbird.android.User
import com.sendbird.chat.module.R
import com.sendbird.chat.module.databinding.ActivityUserInfoBinding
import com.sendbird.chat.module.utils.*
import com.sendbird.chat.module.utils.Constants.DATA_TYPE_ONLY_IMAGE

class BaseUserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding
    private var currentUser: User? = null
    private var profileUrl: String? = null
    private var profileUri: Uri? = null

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            SendBird.setAutoBackgroundDetection(true)
            if (data.resultCode == RESULT_OK) {
                data.data?.data?.let {
                    profileUri = it
                    binding.circularImageviewProfile.setImageUri(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = SendBird.getCurrentUser()
        init()
    }

    private fun init() {
        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val user = currentUser
        if (user == null) {
            showToast(R.string.user_id_info_error)
            finish()
            return
        }
        initUserProfile(user)
        initOnClick()
    }

    private fun initUserProfile(user: User) {
        if (!user.profileUrl.isNullOrBlank()) {
            profileUrl = user.profileUrl
            binding.circularImageviewProfile.setImageUrl(user.profileUrl)
        }
        if (!user.nickname.isNullOrBlank()) {
            binding.tagEdittextNickname.setText(user.nickname)
        }
    }

    private fun initOnClick() {
        binding.circularImageviewEditProfile.setOnClickListener {
            SendBird.setAutoBackgroundDetection(false)
            FileUtils.selectFile(DATA_TYPE_ONLY_IMAGE, startForResult, this)
        }
        binding.purpleButtonSave.setOnClickListener {
            val nickname = binding.tagEdittextNickname.getText()
            if (nickname.isBlank()) {
                showToast(R.string.enter_nickname_msg)
                return@setOnClickListener
            }
            saveUserInfo(profileUri, nickname)
        }
        binding.textviewSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun saveUserInfo(profileUri: Uri?, nickname: String) {
        if (profileUri != null) {
            val progressId = showProgress()
            val fileInfo = FileUtils.getFileInfo(profileUri, applicationContext)
            if (fileInfo == null) {
                showToast(R.string.file_transfer_error)
                return
            }

            SendBird.updateCurrentUserInfoWithProfileImage(nickname, fileInfo.file) {
                if (it != null) {
                    showToast("${it.message}")
                }
                hideProgress(progressId)
                finish()
            }
        } else {
            SendBird.updateCurrentUserInfo(nickname, profileUrl) {
                if (it != null) {
                    showToast("${it.message}")
                }
                finish()
            }
        }
    }

    private fun signOut() {
        SendBird.disconnect {}
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(getString(R.string.permission_granted))
                    SendBird.setAutoBackgroundDetection(false)
                    FileUtils.selectFile(DATA_TYPE_ONLY_IMAGE, startForResult, this)
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