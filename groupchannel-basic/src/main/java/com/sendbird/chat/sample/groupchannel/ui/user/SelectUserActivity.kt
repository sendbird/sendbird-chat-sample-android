package com.sendbird.chat.sample.groupchannel.ui.user

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.ApplicationUserListQuery
import com.sendbird.android.SendBird
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_BASE_USER
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_SELECT_USER
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivitySelectUserBinding


class SelectUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectUserBinding
    private lateinit var adapter: SelectUserAdapter
    private val selectUserIdSet: MutableSet<String> = mutableSetOf()
    private val baseUserIdSet: MutableSet<String> = mutableSetOf()
    private var userListQuery: ApplicationUserListQuery? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.getStringArrayListExtra(INTENT_KEY_SELECT_USER)?.let { selectUserIdSet.addAll(it) }
        intent.getStringArrayListExtra(INTENT_KEY_BASE_USER)?.let { baseUserIdSet.addAll(it) }

        init()
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.select_user)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = SelectUserAdapter({ user, position ->
            adapter.userSelect(user.userId)
            setSelectUser(user.userId)
            adapter.notifyItemChanged(position)
        }, true, selectUserIdSet, baseUserIdSet)
        binding.recyclerviewParticipant.adapter = adapter
        binding.recyclerviewParticipant.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
        binding.recyclerviewParticipant.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    nextUserList()
                }
            }
        })

        userListQuery = SendBird.createApplicationUserListQuery()
        nextUserList()
    }

    private fun nextUserList() {
        val listQuery = userListQuery ?: return
        if (listQuery.hasNext()) {
            listQuery.next { userList, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@next
                }
                if (userList.isNotEmpty()) {
                    adapter.addItems(userList)
                }
            }
        }
    }

    private fun setSelectUser(userId: String) {
        if (selectUserIdSet.contains(userId)) {
            selectUserIdSet.remove(userId)
        } else {
            selectUserIdSet.add(userId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select_participant_menu, menu)
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
        arrayList.addAll(selectUserIdSet)
        intent.putExtra(INTENT_KEY_SELECT_USER, arrayList)
        setResult(RESULT_OK, intent)
        finish()
    }
}