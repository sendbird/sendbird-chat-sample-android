package com.sendbird.chat.sample.openchannel.ui.time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.sendbird.android.SendbirdChat
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.openchannel.databinding.FragmentDndBinding
import java.util.*

class DndFragment : DialogFragment() {

    private lateinit var binding: FragmentDndBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDndBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.startTime.setIs24HourView(true)
        binding.endTime.setIs24HourView(true)
        binding.cancel.setOnClickListener { dismiss() }
        binding.updateDnd.setOnClickListener { setDND() }
        binding.deleteDnd.setOnClickListener { cancelDND() }
        checkForDnd()
    }

    private fun checkForDnd() {
        SendbirdChat.getDoNotDisturb { isDndOn, startHour, startMin, endHour, endMin, timezone, e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to get dnd")
                return@getDoNotDisturb
            }
            if (isDndOn) {
                loadDnd(startHour, startMin, endHour, endMin)
            } else {
                setNoDnd()
            }
        }
    }

    private fun loadDnd(startHour: Int, startMin: Int, endHour: Int, endMin: Int) {
        with(binding) {
            startTime.apply {
                hour = startHour
                minute = startMin
            }
            endTime.apply {
                hour = endHour
                minute = endMin
            }
            updateDnd.isVisible = true
            deleteDnd.isVisible = true
        }
    }

    private fun setNoDnd() {
        binding.updateDnd.text = "Set"
        binding.deleteDnd.isVisible = false
    }

    private fun setDND() {
        SendbirdChat.setDoNotDisturb(true, binding.startTime.hour, binding.startTime.minute, binding.endTime.hour, binding.endTime.minute, TimeZone.getDefault().id) { e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to do operation")
                return@setDoNotDisturb
            }
            showToast("DND setup")
            dismiss()
        }
    }

    private fun cancelDND() {
        SendbirdChat.setDoNotDisturb(false, 0, 0, 0, 0, TimeZone.getDefault().id) { e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to do operation")
                return@setDoNotDisturb
            }
            showToast("DND canceled")
            dismiss()
        }
    }

}