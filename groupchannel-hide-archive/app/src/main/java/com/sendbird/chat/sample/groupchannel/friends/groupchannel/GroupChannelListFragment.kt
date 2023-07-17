package com.sendbird.chat.sample.groupchannel.friends.groupchannel

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
import com.sendbird.android.channel.HiddenState
import com.sendbird.android.channel.query.GroupChannelListQueryOrder
import com.sendbird.android.channel.query.HiddenChannelFilter
import com.sendbird.android.channel.query.MyMemberStateFilter
import com.sendbird.android.collection.GroupChannelCollection
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.handler.GroupChannelCollectionHandler
import com.sendbird.android.params.GroupChannelCollectionCreateParams
import com.sendbird.android.params.GroupChannelListQueryParams
import com.sendbird.chat.sample.groupchannel.friends.R
import com.sendbird.chat.sample.groupchannel.friends.util.Constants
import com.sendbird.chat.sample.groupchannel.friends.user.SelectUserActivity
import com.sendbird.chat.sample.groupchannel.friends.base.BaseFragment
import com.sendbird.chat.sample.groupchannel.friends.databinding.FragmentGroupChannelListBinding
import com.sendbird.chat.sample.groupchannel.friends.util.showToast

class GroupChannelListFragment :
    BaseFragment<FragmentGroupChannelListBinding>(FragmentGroupChannelListBinding::inflate) {
    private lateinit var adapter: GroupChannelListAdapter
    private var groupChannelCollection: GroupChannelCollection? = null
    private val linearLayoutManager =
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    private var showingMode: String = "NORMAL"

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (showingMode == "NORMAL") {
            inflater.inflate(R.menu.normal_channels, menu)
        } else if (showingMode == "ARCHIVED") {
            inflater.inflate(R.menu.archived_channels, menu)
        } else {
            inflater.inflate(R.menu.hidden_channels, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.archived -> {
                showingMode = "ARCHIVED"
                createCollection()
                requireActivity().invalidateOptionsMenu()
                true
            }
            R.id.normal -> {
                showingMode = "NORMAL"
                createCollection()
                requireActivity().invalidateOptionsMenu()
                true
            }
            R.id.hidden -> {
                showingMode = "HIDDEN"
                createCollection()
                requireActivity().invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerView() {
        adapter = GroupChannelListAdapter({ groupChannel ->
            val intent = Intent(context, GroupChannelChatActivity::class.java)
            intent.putExtra(Constants.INTENT_KEY_CHANNEL_URL, groupChannel.url)
            intent.putExtra(Constants.INTENT_KEY_CHANNEL_TITLE, groupChannel.name)
            startActivity(intent)
        }) { view, groupChannel ->
            view.setOnCreateContextMenuListener { contextMenu, _, _ ->
                if (groupChannel.hiddenState == HiddenState.HIDDEN_PREVENT_AUTO_UNHIDE) {
                    val unArchive = contextMenu.add(Menu.NONE, 0, 0, getString(R.string.unarchive))
                    unArchive.setOnMenuItemClickListener {
                        unHideChannel(channel = groupChannel)
                        return@setOnMenuItemClickListener true
                    }
                } else {
                    val hideMenu = contextMenu.add(Menu.NONE, 0, 0, getString(R.string.hide))
                    hideMenu.setOnMenuItemClickListener {
                        hideOrArchiveChannel(channel = groupChannel, hideChannel = true)
                        return@setOnMenuItemClickListener true
                    }
                    val archiveChannel =
                        contextMenu.add(Menu.NONE, 1, 1, getString(R.string.archive))
                    archiveChannel.setOnMenuItemClickListener {
                        hideOrArchiveChannel(channel = groupChannel, hideChannel = false)
                        return@setOnMenuItemClickListener true
                    }
                }

            }
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

    private fun hideOrArchiveChannel(channel: GroupChannel, hideChannel: Boolean) {
        channel.hide(hidePreviousMessages = false, allowAutoUnhide = hideChannel) {
            if (it != null) {
                it.printStackTrace()
                showToast("Failed to archive the channel")
                return@hide
            }
        }
    }

    private fun unHideChannel(channel: GroupChannel) {
        channel.unhide {
            if (it != null) {
                it.printStackTrace()
                showToast("Operation failed")
            }
        }
    }

    private fun createCollection() {
        var filter = HiddenChannelFilter.UNHIDDEN
        if(showingMode == "ARCHIVED") {
            filter = HiddenChannelFilter.HIDDEN_PREVENT_AUTO_UNHIDE
        } else if(showingMode == "HIDDEN") {
            filter = HiddenChannelFilter.HIDDEN_ALLOW_AUTO_UNHIDE
        }

        val listQuery = GroupChannel.createMyGroupChannelListQuery(
            GroupChannelListQueryParams(
                order = GroupChannelListQueryOrder.LATEST_LAST_MESSAGE,
                myMemberStateFilter = MyMemberStateFilter.ALL,
                includeEmpty = true,
                hiddenChannelFilter = filter,
            )
        )
        val params = GroupChannelCollectionCreateParams(listQuery)
        adapter.clearChannels()
        groupChannelCollection = SendbirdChat.createGroupChannelCollection(params).apply {
            groupChannelCollectionHandler = (object : GroupChannelCollectionHandler {
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