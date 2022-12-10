package com.sendbird.chat.sample.groupchannel.type.ui.groupchannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.sendbird.chat.sample.groupchannel.type.databinding.FragmentChannelTypeBinding

class ChannelTypeDialog : DialogFragment() {

    private lateinit var binding: FragmentChannelTypeBinding
    var onChannelTypeSelected: ((ChannelType) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChannelTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.publicType.setOnClickListener { onChannelTypeSelected?.invoke(ChannelType.PUBLIC) }
        binding.privateType.setOnClickListener { onChannelTypeSelected?.invoke(ChannelType.PRIVATE) }
        binding.superType.setOnClickListener { onChannelTypeSelected?.invoke(ChannelType.SUPER) }
    }

    override fun onDetach() {
        super.onDetach()
        onChannelTypeSelected = null
    }
}

enum class ChannelType {
    PRIVATE, PUBLIC, SUPER
}