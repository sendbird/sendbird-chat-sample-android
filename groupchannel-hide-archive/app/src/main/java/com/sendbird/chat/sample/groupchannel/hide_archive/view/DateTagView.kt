package com.sendbird.chat.sample.groupchannel.hide_archive.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.chat.sample.groupchannel.hide_archive.util.toDate
import com.sendbird.chat.sample.groupchannel.hide_archive.R
import com.sendbird.chat.sample.groupchannel.hide_archive.databinding.ViewDateTagBinding

class DateTagView : FrameLayout {

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
        getAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initView()
        getAttrs(attrs, defStyle)
    }

    private lateinit var binding: ViewDateTagBinding

    private fun initView() {
        binding = ViewDateTagBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DateTagView)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DateTagView, defStyle, 0)
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        typedArray.recycle()
    }

    fun setMillisecond(millisecond: Long) {
        binding.textviewDateTag.text = millisecond.toDate()
    }
}