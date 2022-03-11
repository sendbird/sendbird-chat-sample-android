package com.sendbird.chat.module.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.BaseMessage

const val MESSAGE_REFRESH_RANGE = 3

class ChatRecyclerDataObserver(
    private val recyclerView: RecyclerView,
    private val adapter: ListAdapter<BaseMessage, RecyclerView.ViewHolder>
) : RecyclerView.AdapterDataObserver() {
    private var scrollToBottom: Boolean = false
    private var isUpdate: Boolean = false

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        if ((recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == adapter.itemCount - 2) {
            notifyUpdate()
            isUpdate = true
        } else {
            if (scrollToBottom) {
                notifyUpdate()
            }
        }
        if ((recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() == 0) {
            recyclerView.adapter?.notifyItemChanged(0)
        }
        scrollToBottom = false
        super.onItemRangeInserted(positionStart, itemCount)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        if (isUpdate) {
            isUpdate = false
            notifyUpdate()
        } else {
            super.onItemRangeChanged(positionStart, itemCount)
        }
    }

    private fun notifyUpdate() {
        recyclerView.adapter?.notifyItemRangeChanged(
            if (adapter.itemCount - MESSAGE_REFRESH_RANGE < 0) 0 else adapter.itemCount - MESSAGE_REFRESH_RANGE,
            MESSAGE_REFRESH_RANGE
        )
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun scrollToBottom(boolean: Boolean) {
        scrollToBottom = boolean
    }
}