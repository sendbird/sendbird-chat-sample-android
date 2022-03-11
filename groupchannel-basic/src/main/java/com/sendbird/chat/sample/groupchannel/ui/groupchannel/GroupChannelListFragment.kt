package com.sendbird.chat.sample.groupchannel.ui.groupchannel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannelCollection
import com.sendbird.android.GroupChannelListQuery
import com.sendbird.android.handlers.GroupChannelCollectionHandler
import com.sendbird.android.handlers.GroupChannelContext
import com.sendbird.chat.module.ui.base.BaseFragment
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.databinding.FragmentGroupChannelListBinding


class GroupChannelListFragment :
    BaseFragment<FragmentGroupChannelListBinding>(FragmentGroupChannelListBinding::inflate) {

    private lateinit var adapter: GroupChannelListAdapter
    private var groupChannelCollection: GroupChannelCollection? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initRecyclerView()
        binding.circularImageviewAddChannel.setOnClickListener {
            val intent = Intent(context, GroupChannelCreateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        adapter = GroupChannelListAdapter { groupChannel ->
            val intent = Intent(context, GroupChannelChatActivity::class.java)
            intent.putExtra(INTENT_KEY_CHANNEL_URL, groupChannel.url)
            intent.putExtra(INTENT_KEY_CHANNEL_TITLE, groupChannel.name)
            startActivity(intent)
        }

        binding.recyclerviewChannel.adapter = adapter
        binding.recyclerviewChannel.addItemDecoration(
            DividerItemDecoration(
                context,
                RecyclerView.VERTICAL
            )
        )
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                recyclerViewMoveTop()
                adapter.notifyItemChanged(fromPosition)
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recyclerViewMoveTop()
                super.onItemRangeInserted(positionStart, itemCount)
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
        val listQuery = GroupChannel.createMyGroupChannelListQuery().apply {
            isIncludeEmpty = true
            memberStateFilter = GroupChannelListQuery.MemberStateFilter.ALL
            order = GroupChannelListQuery.Order.LATEST_LAST_MESSAGE
        }

        createCollection(listQuery)
    }

    private fun createCollection(listQuery: GroupChannelListQuery) {
        groupChannelCollection = GroupChannelCollection.Builder(listQuery).build().apply {
            setGroupChannelCollectionHandler(object :
                GroupChannelCollectionHandler {
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
        loadMore()
    }

    private fun loadMore() {
        val collection = groupChannelCollection ?: return
        if (collection.hasMore()) {
            collection.loadMore loadMoreLabel@{ channelList, e ->
                if (e != null || channelList == null) {
                    showToast("${e?.message}")
                    return@loadMoreLabel
                }
                adapter.loadChannels(channelList)
            }
        }
    }

    private fun recyclerViewMoveTop() {
        val firstVisiblePosition =
            (binding.recyclerviewChannel.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (firstVisiblePosition == 0) {
            binding.recyclerviewChannel.scrollToPosition(0)
        }
    }
}