package com.sendbird.chat.sample.groupchannel.scheduled.message.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import java.util.*

private const val MinimumTimeAmount = 6 * 60 * 1_000L

fun Context.openDateTimeSelector(onTimeSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, 6)

    val datePicker = DatePickerDialog(
        this,
        { _, y, m, d -> openTimeCalendar(y, m, d, onTimeSelected) },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePicker.datePicker.minDate = calendar.timeInMillis
    calendar.add(Calendar.DATE, 30)
    datePicker.datePicker.maxDate = calendar.timeInMillis
    datePicker.show()
}

private fun Context.openTimeCalendar(
    year: Int,
    month: Int,
    day: Int,
    onTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, 6)

    val timePicker = TimePickerDialog(
        this,
        { _, hour, minute ->
            onDateTimeSelected(
                year,
                month,
                day,
                hour,
                minute,
                onTimeSelected
            )
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )
    timePicker.show()
}

private fun Context.onDateTimeSelected(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    onTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val selectedTime = calendar.timeInMillis
    if (selectedTime < System.currentTimeMillis() + MinimumTimeAmount) {
        Toast.makeText(
            this,
            "The message must be scheduled at least 5 minutes in the future",
            Toast.LENGTH_LONG
        ).show()
        return
    }
    onTimeSelected(selectedTime)
}