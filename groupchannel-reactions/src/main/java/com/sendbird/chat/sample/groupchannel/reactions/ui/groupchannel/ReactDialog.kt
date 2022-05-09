package com.sendbird.chat.sample.groupchannel.reactions.ui.groupchannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.sendbird.chat.sample.groupchannel.reactions.databinding.FragmentReactionBinding

class ReactDialog : DialogFragment() {

    private lateinit var binding: FragmentReactionBinding
    var onReactClicked: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding
            .root
            .children
            .filterIsInstance(TextView::class.java)
            .forEach { reactionView ->
                reactionView.setOnClickListener {
                    onReactClicked?.invoke(reactionView.text.toString())
                    dismiss()
                }
            }
    }

    override fun onDetach() {
        super.onDetach()
        onReactClicked = null
    }
}