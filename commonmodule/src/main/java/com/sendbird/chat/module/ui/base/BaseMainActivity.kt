package com.sendbird.chat.module.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sendbird.chat.module.R
import com.sendbird.chat.module.databinding.ActivityMainBinding
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.getAppName
import com.sendbird.chat.module.utils.showLongToast

abstract class BaseMainActivity : AppCompatActivity() {
    abstract fun getFragmentItems(): List<Fragment>
    private lateinit var binding: ActivityMainBinding
    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.getBooleanExtra(Constants.INTENT_KEY_NICKNAME_REQUIRE, false)) {
            nicknameRequired()
        }
        init()
        initFragment()
    }

    protected open fun init() {
        binding.toolbar.title = getAppName()
        setSupportActionBar(binding.toolbar)
    }

    protected open fun initFragment() {
        if (getFragmentItems().isNotEmpty()) {
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, getFragmentItems()[0], null)
                .setReorderingAllowed(true)
                .commit()
        }
    }

    protected open fun nicknameRequired() {
        showLongToast(R.string.enter_nickname_msg)
        val intent = Intent(this, BaseUserInfoActivity::class.java)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.user -> {
                val intent = Intent(this, BaseUserInfoActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
}