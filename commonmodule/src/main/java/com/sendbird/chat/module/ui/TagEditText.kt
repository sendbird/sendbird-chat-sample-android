package com.sendbird.chat.module.ui

import android.content.Context
import android.content.res.TypedArray
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.chat.module.R
import com.sendbird.chat.module.databinding.ViewTagEdittextBinding

class TagEditText : FrameLayout {

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

    private lateinit var binding: ViewTagEdittextBinding

    private fun initView() {
        binding = ViewTagEdittextBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagEditText)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagEditText, defStyle, 0)
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        binding.textviewTag.text = typedArray.getString(R.styleable.TagEditText_text)
        binding.edittext.hint = typedArray.getString(R.styleable.TagEditText_hint)
        binding.edittext.inputType = typedArray.getInt(R.styleable.TagEditText_android_inputType, InputType.TYPE_CLASS_TEXT)
        typedArray.recycle()
    }

    fun getText(): String {
        return binding.edittext.text.toString()
    }

    fun setText(text: String) {
        binding.edittext.setText(text)
    }

}