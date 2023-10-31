package com.sendbird.chat.sample.groupchannel.categorizechannels.groupchannel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQueryOrder
import com.sendbird.android.collection.GroupChannelCollection
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.handler.GroupChannelCollectionHandler
import com.sendbird.android.params.GroupChannelCollectionCreateParams
import com.sendbird.android.params.GroupChannelListQueryParams
import com.sendbird.chat.sample.groupchannel.categorizechannels.base.BaseFragment
import com.sendbird.chat.sample.groupchannel.categorizechannels.databinding.FragmentGroupChannelListBinding
import com.sendbird.chat.sample.groupchannel.categorizechannels.user.SelectUserActivity
import com.sendbird.chat.sample.groupchannel.categorizechannels.util.Constants
import com.sendbird.chat.sample.groupchannel.categorizechannels.util.showInputListDialog
import com.sendbird.chat.sample.groupchannel.categorizechannels.util.showToast

class GroupChannelListFragment :
    BaseFragment<FragmentGroupChannelListBinding>(FragmentGroupChannelListBinding::inflate) {
    private lateinit var adapter: GroupChannelListAdapter
    private var groupChannelCollection: GroupChannelCollection? = null
    private var customTypeFilterList: MutableList<String> = mutableListOf()
    private val linearLayoutManager =
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        if(customTypeFilterList.isEmpty()) showAddCustomTypeFilter()
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
        // Create a GroupChannelListQuery with a GroupChannelListQueryParams.
        // You can set custom types filter to retrieve only the channels of the custom types.
        val listQuery = GroupChannel.createMyGroupChannelListQuery(
            GroupChannelListQueryParams(
                order = GroupChannelListQueryOrder.LATEST_LAST_MESSAGE,
                customTypesFilter = customTypeFilterList
            )
        )

        // Create a GroupChannelCollection with a GroupChannelCollectionCreateParams.
        val params = GroupChannelCollectionCreateParams(listQuery)
        // Set a GroupChannelCollectionHandler to receive events related to the GroupChannelCollection.
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

    private fun showAddCustomTypeFilter() {
        showInputListDialog(
            title = "Add Custom Type Filter",
            message = "",
            posText = "Add",
            negText = "Cancel",
            positiveButtonFunction = { list ->
                customTypeFilterList.addAll(list)
                createCollection()
            },
            negativeButtonFunction = { return@showInputListDialog }
        )
    }
}