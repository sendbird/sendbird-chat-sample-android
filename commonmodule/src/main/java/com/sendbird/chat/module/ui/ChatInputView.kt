package com.sendbird.chat.module.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.chat.module.R
import com.sendbird.chat.module.databinding.ViewChatInputBinding

class ChatInputView : FrameLayout {
    private var listener: OnSendMessageClickListener? = null

    interface OnSendMessageClickListener {
        fun onUserMessageSend()
        fun onFileMessageSend()
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
        getAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView()
        getAttrs(attrs, defStyle)
    }

    private lateinit var binding: ViewChatInputBinding

    private fun initView() {
        binding = ViewChatInputBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatInputView)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ChatInputView, defStyle, 0)
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        binding.edittextMsg.hint = typedArray.getString(R.styleable.ChatInputView_hint)
        binding.textviewSend.text = typedArray.getString(R.styleable.ChatInputView_button_text)
        binding.textviewSend.setOnClickListener { listener?.onUserMessageSend() }
        binding.imageviewSendFile.setOnClickListener { listener?.onFileMessageSend() }
        typedArray.recycle()
    }

    fun getText(): String {
        return binding.edittextMsg.text.toString()
    }

    fun clearText() {
        setText("")
    }

    fun setText(text: String) {
        binding.edittextMsg.setText(text)
    }

    fun setOnSendMessageClickListener(listener: OnSendMessageClickListener?) {
        this.listener = listener
    }

}