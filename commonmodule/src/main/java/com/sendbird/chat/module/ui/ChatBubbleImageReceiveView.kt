package com.sendbird.chat.module.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.sendbird.chat.module.R
import com.sendbird.chat.module.databinding.ViewChatBubbleImageReceiveBinding

class ChatBubbleImageReceiveView : FrameLayout {

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

    private lateinit var binding: ViewChatBubbleImageReceiveBinding

    private fun initView() {
        binding =
            ViewChatBubbleImageReceiveBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ChatBubbleImageReceiveView)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.ChatBubbleImageReceiveView,
            defStyle,
            0
        )
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        val srcId = typedArray.getResourceId(R.styleable.ChatBubbleImageReceiveView_android_src, 0)
        if (srcId != 0) {
            binding.imageview.setImageResource(srcId)
        }
        typedArray.recycle()
    }

    fun setImageUrl(imageUrl: String, cacheKey: String) {
        binding.imageview.load(imageUrl) {
            crossfade(true)
            scale(Scale.FILL)
            memoryCacheKey(cacheKey)
            transformations(RoundedCornersTransformation(25f))
        }
    }

}