package com.sendbird.chat.sample.openchannel.friends.ui.user

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.openchannel.friends.R
import com.sendbird.chat.sample.openchannel.friends.databinding.ActivitySelectUserBinding


class SelectUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectUserBinding
    private lateinit var adapter: SelectUserAdapter
    private var userListQuery = SendbirdChat.createApplicationUserListQuery()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        initRecyclerView()
        loadNextUsers()
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.select_user)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initRecyclerView() {
        adapter = SelectUserAdapter(
            { _, _ -> },
            true,
            intent.getStringArrayListExtra(Constants.INTENT_KEY_SELECT_USER),
            intent.getStringArrayListExtra(Constants.INTENT_KEY_BASE_USER)
        )
        binding.recyclerviewUser.adapter = adapter
        binding.recyclerviewUser.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
        binding.recyclerviewUser.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    loadNextUsers()
                }
            }
        })
    }

    private fun loadNextUsers() {
        if (userListQuery.hasNext) {
            userListQuery.next { users, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@next
                }
                if (!users.isNullOrEmpty()) {
                    adapter.addUsers(users)
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.select)
        item.title = getString(R.string.select)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select_user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.select -> {
                selectUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun selectUser() {
        val intent = intent
        val arrayList = arrayListOf<String>()
        arrayList.addAll(adapter.selectUserIdSet)
        intent.putExtra(Constants.INTENT_KEY_SELECT_USER, arrayList)
        setResult(RESULT_OK, intent)
        finish()
    }
}