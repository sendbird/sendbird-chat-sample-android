package com.sendbird.chat.module.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.chat.module.R
import com.sendbird.chat.module.databinding.ViewChatBubbleReceiveBinding

class ChatBubbleReceiveView : FrameLayout {

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

    private lateinit var binding: ViewChatBubbleReceiveBinding

    private fun initView() {
        binding = ViewChatBubbleReceiveBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatBubbleSendView)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ChatBubbleSendView, defStyle, 0)
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        binding.textviewMsg.text = typedArray.getString(R.styleable.ChatBubbleSendView_text)
        typedArray.recycle()
    }

    fun setText(text: String) {
        binding.textviewMsg.text = text
    }

}