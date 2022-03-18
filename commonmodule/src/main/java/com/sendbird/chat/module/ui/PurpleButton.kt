package com.sendbird.chat.module.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.sendbird.chat.module.R
import com.sendbird.chat.module.databinding.ViewPurpleButtonBinding

class PurpleButton : FrameLayout, View.OnClickListener {
    private var listener: OnClickListener? = null

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

    private lateinit var binding: ViewPurpleButtonBinding

    private fun initView() {
        binding = ViewPurpleButtonBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PurpleButton)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PurpleButton, defStyle, 0)
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        binding.button.text = typedArray.getString(R.styleable.PurpleButton_text)
        binding.button.setOnClickListener(this)
        typedArray.recycle()
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        listener?.onClick(v)
    }

}