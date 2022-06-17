package com.sendbird.chat.sample.openchannel.copymessage.ui.openchannel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.channel.query.OpenChannelListQuery
import com.sendbird.android.params.OpenChannelListQueryParams
import com.sendbird.chat.module.ui.base.BaseFragment
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.openchannel.copymessage.databinding.FragmentOpenChannelListBinding

class OpenChannelListFragment :
    BaseFragment<FragmentOpenChannelListBinding>(FragmentOpenChannelListBinding::inflate) {
    private lateinit var adapter: OpenChannelListAdapter
    private var openChannelListQuery: OpenChannelListQuery? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        initRecyclerView()
        createOpenChannelListQuery()
    }

    private fun init() {
        binding.circularImageviewAddChannel.setOnClickListener {
            val intent = Intent(context, OpenChannelCreateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        adapter = OpenChannelListAdapter(this::onChannelClicked)

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

    private fun onChannelClicked(openChannel: OpenChannel) {
        val messageId = this.activity?.intent?.getLongExtra("message_id", -1L)
        if (messageId != null && messageId != -1L) {
            val intentToLaunch = Intent().apply {
                putExtra("message_id", messageId)
                putExtra("channel_url", openChannel.url)
            }
            this.activity?.setResult(Activity.RESULT_OK, intentToLaunch)
            this.activity?.finish()
            return
        }
        val intentToLaunch =
            Intent(requireContext(), OpenChannelChatActivity::class.java).apply {
                putExtra(Constants.INTENT_KEY_CHANNEL_URL, openChannel.url)
                putExtra(Constants.INTENT_KEY_CHANNEL_TITLE, openChannel.name)
            }
        startActivity(intentToLaunch)
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