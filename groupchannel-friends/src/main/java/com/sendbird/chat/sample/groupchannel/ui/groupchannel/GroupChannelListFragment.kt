package com.sendbird.chat.sample.groupchannel.ui.groupchannel

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQuery
import com.sendbird.android.collection.GroupChannelCollection
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.handler.GroupChannelCollectionHandler
import com.sendbird.android.params.GroupChannelCollectionCreateParams
import com.sendbird.chat.module.ui.base.BaseFragment
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.FragmentGroupChannelListBinding
import com.sendbird.chat.sample.groupchannel.ui.user.ChatMemberListActivity
import com.sendbird.chat.sample.groupchannel.ui.user.ChatMemberListActivity.Companion.ShowFriendsKey
import com.sendbird.chat.sample.groupchannel.ui.user.SelectUserActivity

class GroupChannelListFragment :
    BaseFragment<FragmentGroupChannelListBinding>(FragmentGroupChannelListBinding::inflate) {
    private lateinit var adapter: GroupChannelListAdapter
    private var groupChannelCollection: GroupChannelCollection? = null
    private val linearLayoutManager =
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        init()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        createCollection()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
        intent.putExtra(ShowFriendsKey, true)
        startActivity(intent)
    }

    private fun init() {
        binding.circularImageviewAddChannel.setOnClickListener {
            val intent = Intent(context, SelectUserActivity::class.java)
            val currentUser = SendbirdChat.currentUser
            if (currentUser != null) {
                intent.putExtra(Constants.INTENT_KEY_BASE_USER, arrayListOf(currentUser.userId))
            }
            intent.putExtra(Constants.INTENT_KEY_SELECT_USER_MODE_CREATE, true)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        adapter = GroupChannelListAdapter { groupChannel ->
            val intent = Intent(context, GroupChannelChatActivity::class.java)
            intent.putExtra(Constants.INTENT_KEY_CHANNEL_URL, groupChannel.url)
            intent.putExtra(Constants.INTENT_KEY_CHANNEL_TITLE, groupChannel.name)
            startActivity(intent)
        }
        binding.recyclerviewChannel.layoutManager = linearLayoutManager
        binding.recyclerviewChannel.itemAnimator = null
        binding.recyclerviewChannel.adapter = adapter
        binding.recyclerviewChannel.addItemDecoration(
            DividerItemDecoration(context, RecyclerView.VERTICAL)
        )
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                recyclerViewMoveTop()
                adapter.notifyItemChanged(fromPosition)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerViewMoveTop()
            }
        })
        binding.recyclerviewChannel.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    loadMore()
                }
            }
        })
    }

    private fun createCollection() {
        val listQuery = GroupChannel.createMyGroupChannelListQuery().apply {
            memberStateFilter = GroupChannelListQuery.MemberStateFilter.ALL
            order = GroupChannelListQuery.Order.LATEST_LAST_MESSAGE
        }
        val params = GroupChannelCollectionCreateParams(listQuery)
        groupChannelCollection = SendbirdChat.createGroupChannelCollection(params).apply {
            setGroupChannelCollectionHandler(object : GroupChannelCollectionHandler {
                override fun onChannelsAdded(
                    context: GroupChannelContext,
                    channels: List<GroupChannel>
                ) {
                    adapter.updateChannels(channels)
                }

                override fun onChannelsDeleted(
                    context: GroupChannelContext,
                    deletedChannelUrls: List<String>
                ) {
                    adapter.deleteChannels(deletedChannelUrls)
                }

                override fun onChannelsUpdated(
                    context: GroupChannelContext,
                    channels: List<GroupChannel>
                ) {
                    adapter.updateChannels(channels)
                }
            })
        }
        loadMore(true)
    }

    private fun loadMore(isRefreshing: Boolean = false) {
        val collection = groupChannelCollection ?: return
        if (collection.hasMore) {
            collection.loadMore loadMoreLabel@{ channelList, e ->
                if (e != null || channelList == null) {
                    showToast("${e?.message}")
                    return@loadMoreLabel
                }
                if (channelList.isNotEmpty()) {
                    if (isRefreshing) {
                        adapter.addChannels(emptyList())
                    }
                    adapter.addChannels(channelList)
                }

            }
        }
    }

    private fun recyclerViewMoveTop() {
        if (bindingState) {
            val firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
            if (firstVisiblePosition == 0) {
                binding.recyclerviewChannel.scrollToPosition(0)
            }
        }
    }
}