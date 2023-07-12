package com.sendbird.chat.sample.groupchannel.dndsnooze.time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.sendbird.android.SendbirdChat
import com.sendbird.chat.sample.groupchannel.dndsnooze.databinding.FragmentSnoozeBinding
import com.sendbird.chat.sample.groupchannel.dndsnooze.util.showToast
import java.util.Calendar
import java.util.Date

class SnoozeFragment : DialogFragment() {

    private lateinit var binding: FragmentSnoozeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSnoozeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.until.setIs24HourView(true)
        binding.cancel.setOnClickListener { dismiss() }
        binding.updateSnooze.setOnClickListener { setSnooze() }
        binding.deleteSnooze.setOnClickListener { cancelSnooze() }
        checkForSnooze()
    }

    private fun checkForSnooze() {
        SendbirdChat.getSnoozePeriod { isSnoozeOn, startTs, endTs, e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to get snooze")
                return@getSnoozePeriod
            }
            if (isSnoozeOn) {
                loadSnooze(endTs)
            } else {
                setNoSnooze()
            }
        }
    }

    private fun loadSnooze(endTs: Long) {
        val dateTime = Date(endTs)
        val calendar = Calendar.getInstance().apply {
            time = dateTime
        }
        with(binding) {
            until.apply {
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
            }
            updateSnooze.isVisible = true
            deleteSnooze.isVisible = true
        }
    }

    private fun setNoSnooze() {
        binding.updateSnooze.text = "Set"
        binding.deleteSnooze.isVisible = false
    }

    private fun setSnooze() {
        val startTime = System.currentTimeMillis()
        val endTime = binding.until.let {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, it.hour)
                set(Calendar.MINUTE, it.minute)
            }
            calendar.time.time
        }
        // Set snooze by snoozeOn = true with start and end time
        SendbirdChat.setSnoozePeriod(true, startTime, endTime) { e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to do operation")
                return@setSnoozePeriod
            }
            showToast("DND setup")
            dismiss()
        }

    }

    private fun cancelSnooze() {
        // Cancel snooze by snoozeOn = false with current time and end time
        SendbirdChat.setSnoozePeriod(false, System.currentTimeMillis(), System.currentTimeMillis() + 100) { e ->
            if (e != null) {
                e.printStackTrace()
                showToast("Failed to do operation")
                return@setSnoozePeriod
            }
            showToast("Snooze canceled")
            dismiss()
        }
    }

}