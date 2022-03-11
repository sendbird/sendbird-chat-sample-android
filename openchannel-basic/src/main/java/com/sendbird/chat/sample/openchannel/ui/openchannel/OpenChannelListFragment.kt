package com.sendbird.chat.sample.openchannel.ui.openchannel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.OpenChannel
import com.sendbird.android.OpenChannelListQuery
import com.sendbird.chat.module.ui.base.BaseFragment
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.openchannel.databinding.FragmentOpenChannelListBinding

class OpenChannelListFragment : BaseFragment<FragmentOpenChannelListBinding>(FragmentOpenChannelListBinding::inflate) {

    private lateinit var adapter: OpenChannelListAdapter
    private var openChannelListQuery: OpenChannelListQuery? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.circularImageviewAddChannel.setOnClickListener {
            val intent = Intent(context, OpenChannelCreateActivity::class.java)
            startActivity(intent)
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = OpenChannelListAdapter { openChannel ->
            val intent = Intent(context, OpenChannelChatActivity::class.java)
            intent.putExtra(INTENT_KEY_CHANNEL_URL, openChannel.url)
            intent.putExtra(INTENT_KEY_CHANNEL_TITLE, openChannel.name)
            startActivity(intent)
        }

        binding.recyclerviewOpenChannel.adapter = adapter
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
                    loadChannelList()
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            createOpenChannelListQuery()
        }

        createOpenChannelListQuery()
    }

    private fun createOpenChannelListQuery() {
        openChannelListQuery = OpenChannel.createOpenChannelListQuery()
        loadChannelList()
    }

    private fun loadChannelList() {
        val listQuery = openChannelListQuery ?: return

        if (listQuery.hasNext()) {
            listQuery.next { openChannels, e ->
                if (e != null || openChannels == null) {
                    showToast("${e.message}")
                    return@next
                }
                if (openChannels.size > 0) {
                    if (binding.swipeRefreshLayout.isRefreshing) {
                        adapter.submitList(openChannels.toMutableList())
                        adapter.notifyDataSetChanged()
                        binding.swipeRefreshLayout.isRefreshing = false
                    } else {
                        adapter.addItems(openChannels.toMutableList())
                    }
                }
            }
        }
    }
}