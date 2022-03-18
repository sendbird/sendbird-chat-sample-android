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
import com.sendbird.chat.module.databinding.ViewChatBubbleImageSendBinding
import java.io.File

class ChatBubbleImageSendView : FrameLayout {

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

    private lateinit var binding: ViewChatBubbleImageSendBinding

    private fun initView() {
        binding = ViewChatBubbleImageSendBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatBubbleImageSendView)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ChatBubbleImageSendView, defStyle, 0)
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        val srcId = typedArray.getResourceId(R.styleable.ChatBubbleImageSendView_android_src, 0)
        if (srcId != 0) {
            binding.imageview.setImageResource(srcId)
        }
        typedArray.recycle()
    }

    fun setImageFile(imageFile: File?) {
        if (imageFile != null) {
            binding.imageview.load(imageFile) {
                crossfade(true)
                scale(Scale.FILL)
                transformations(RoundedCornersTransformation(25f))
            }
        }
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