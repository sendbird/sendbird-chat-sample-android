package com.sendbird.chat.sample.openchannel.friends.ui.openchannel

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.channel.query.OpenChannelListQuery
import com.sendbird.android.params.OpenChannelListQueryParams
import com.sendbird.chat.module.ui.base.BaseFragment
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.openchannel.friends.R
import com.sendbird.chat.sample.openchannel.friends.databinding.FragmentOpenChannelListBinding
import com.sendbird.chat.sample.openchannel.friends.ui.user.ChatMemberListActivity

class OpenChannelListFragment :
    BaseFragment<FragmentOpenChannelListBinding>(FragmentOpenChannelListBinding::inflate) {
    private lateinit var adapter: OpenChannelListAdapter
    private var openChannelListQuery: OpenChannelListQuery? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        init()
        initRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        createOpenChannelListQuery()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.friends -> {
                openFriendsActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFriendsActivity() {
        val intent = Intent(requireContext(), ChatMemberListActivity::class.java)
        startActivity(intent)
    }

    private fun init() {
        binding.circularImageviewAddChannel.setOnClickListener {
            val intent = Intent(context, OpenChannelCreateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        adapter = OpenChannelListAdapter { openChannel ->
            val intent = Intent(context, OpenChannelChatActivity::class.java)
            intent.putExtra(INTENT_KEY_CHANNEL_URL, openChannel.url)
            intent.putExtra(INTENT_KEY_CHANNEL_TITLE, openChannel.name)
            startActivity(intent)
        }

        binding.recyclerviewOpenChannel.adapter = adapter
        binding.recyclerviewOpenChannel.itemAnimator = null
        binding.recyclerviewOpenChannel.addItemDecoration(
            DividerItemDecoration(
                context,
                RecyclerView.VERTICAL
            )
        )

        binding.recyclerviewOpenChannel.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        loadNextOpenChannels(false)
                    }
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            createOpenChannelListQuery()
        }
    }

    private fun createOpenChannelListQuery() {
        openChannelListQuery = OpenChannel.createOpenChannelListQuery(OpenChannelListQueryParams())
        loadNextOpenChannels(true)
    }

    private fun loadNextOpenChannels(isRefresh: Boolean) {
        val listQuery = openChannelListQuery ?: return
        if (listQuery.hasNext) {
            listQuery.next { openChannels, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@next
                }
                if (!openChannels.isNullOrEmpty()) {
                    if (isRefresh) {
                        adapter.changeChannels(openChannels)
                        binding.swipeRefreshLayout.isRefreshing = false
                    } else {
                        adapter.addChannels(openChannels)
                    }
                }
            }
        }
    }
}