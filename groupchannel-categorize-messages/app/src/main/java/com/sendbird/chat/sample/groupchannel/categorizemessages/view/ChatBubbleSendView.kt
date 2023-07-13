package com.sendbird.chat.sample.groupchannel.categorizemessages.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.chat.sample.groupchannel.categorizemessages.R
import com.sendbird.chat.sample.groupchannel.categorizemessages.databinding.ViewChatBubbleSendBinding

class ChatBubbleSendView : FrameLayout {

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

    private lateinit var binding: ViewChatBubbleSendBinding

    private fun initView() {
        binding = ViewChatBubbleSendBinding.inflate(LayoutInflater.from(context), this, true)
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