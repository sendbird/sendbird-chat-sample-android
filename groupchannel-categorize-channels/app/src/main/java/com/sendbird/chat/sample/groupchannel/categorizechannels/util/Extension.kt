package com.sendbird.chat.sample.groupchannel.categorizechannels.util

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.content.res.Resources.getSystem
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData


fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.showToast(msgId: Int) {
    Toast.makeText(this, getString(msgId), Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(msgId: Int) {
    Toast.makeText(requireContext(), getString(msgId), Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(msgId: Int) {
    Toast.makeText(this, getString(msgId), Toast.LENGTH_LONG).show()
}

fun Fragment.showAlertDialog(
    title: String,
    message: String,
    posText: String,
    negText: String,
    positiveButtonFunction: (() -> Unit)? = null,
    negativeButtonFunction: (() -> Unit)? = null
) {
    val builder = AlertDialog.Builder(requireContext())
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setPositiveButton(posText) { _, _ ->
        positiveButtonFunction?.invoke()
    }
    builder.setNegativeButton(negText) { _, _ ->
        negativeButtonFunction?.invoke()
    }
    builder.show()
}

fun Context.showAlertDialog(
    title: String,
    message: String,
    posText: String,
    negText: String,
    positiveButtonFunction: (() -> Unit)? = null,
    negativeButtonFunction: (() -> Unit)? = null
) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setPositiveButton(posText) { _, _ ->
        positiveButtonFunction?.invoke()
    }
    builder.setNegativeButton(negText) { _, _ ->
        negativeButtonFunction?.invoke()
    }
    builder.show()
}

fun Context.showInputDialog(
    title: String?,
    message: String?,
    baseText: String,
    posText: String,
    negText: String,
    positiveButtonFunction: ((String) -> Unit)? = null,
    negativeButtonFunction: (() -> Unit)? = null
) {
    val builder = AlertDialog.Builder(this)
    val edittext = EditText(this)
    edittext.inputType = InputType.TYPE_CLASS_TEXT
    edittext.setText(baseText)
    if (title != null) {
        builder.setTitle(title)
    }
    if (message != null) {
        builder.setTitle(title)
    }
    builder.setMessage(message)
    val container = FrameLayout(this)
    val params = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.leftMargin = 20.dp()
    params.rightMargin = 20.dp()
    edittext.layoutParams = params
    container.addView(edittext)

    builder.setView(container)
    builder.setPositiveButton(posText) { _, _ ->
        positiveButtonFunction?.invoke(edittext.text.toString())
    }
    builder.setNegativeButton(negText) { _, _ ->
        negativeButtonFunction?.invoke()
    }
    builder.show()
}

fun Fragment.showInputDialog(
    title: String,
    message: String,
    posText: String,
    negText: String,
    positiveButtonFunction: ((String) -> Unit)? = null,
    negativeButtonFunction: (() -> Unit)? = null
) {
    val builder = AlertDialog.Builder(requireContext())
    val edittext = EditText(requireContext())
    edittext.inputType = InputType.TYPE_CLASS_TEXT
    builder.setTitle(title)
    builder.setMessage(message)
    val container = FrameLayout(requireContext())
    val params = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.leftMargin = 20.dp()
    params.rightMargin = 20.dp()
    edittext.layoutParams = params
    container.addView(edittext)

    builder.setView(container)
    builder.setPositiveButton(posText) { _, _ ->
        positiveButtonFunction?.invoke(edittext.text.toString())
    }
    builder.setNegativeButton(negText) { _, _ ->
        negativeButtonFunction?.invoke()
    }
    builder.show()
}

fun Fragment.showInputListDialog(
    title: String,
    message: String,
    posText: String,
    negText: String,
    positiveButtonFunction: ((List<String>) -> Unit)? = null,
    negativeButtonFunction: (() -> Unit)? = null
) {
    val builder = AlertDialog.Builder(requireContext())
    val editTextList = mutableListOf<EditText>()

    // Linear layout which will contain all EditTexts
    val linearLayout = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        // Add first row
        addView(createNewRow(editTextList, this))
    }

    val container = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        addView(linearLayout)
    }

    builder.setTitle(title)
    builder.setMessage(message)
    builder.setView(container)

    builder.setPositiveButton(posText) { _, _ ->
        val textList = editTextList.map { it.text.toString() }
        positiveButtonFunction?.invoke(textList)
    }
    builder.setNegativeButton(negText) { _, _ ->
        negativeButtonFunction?.invoke()
    }
    builder.show()
}

fun Fragment.createNewRow(editTextList: MutableList<EditText>, parent: LinearLayout): LinearLayout {
    val editText = EditText(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            setMargins(5.dp(), 5.dp(), 5.dp(), 5.dp())
        }
        inputType = InputType.TYPE_CLASS_TEXT
        editTextList.add(this)
    }

    val addButton = ImageButton(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END
        }
        setImageResource(android.R.drawable.ic_input_add) // Set image for button
        setColorFilter(ContextCompat.getColor(context, android.R.color.holo_purple)) // Set color to purple
        setOnClickListener {
            // Remove current add button
            (this.parent as? LinearLayout)?.removeView(this)

            // Add new row to parent
            parent.addView(createNewRow(editTextList, parent))
        } // Add new row on click and remove current button
    }

    return LinearLayout(requireContext()).apply {
        orientation = LinearLayout.HORIZONTAL
        addView(editText)
        addView(addButton)
    }
}

fun Context.showListDialog(
    listItem: List<String>,
    onClickListener: DialogInterface.OnClickListener
) {
    val items: Array<CharSequence> = listItem.toTypedArray()
    val builder = AlertDialog.Builder(this)
    builder.setItems(items, onClickListener)
    builder.show()
}

fun Int.dp(): Int {
    val metrics = getSystem().displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
}

fun Activity.showProgress(): Int {
    val progressBar = ProgressBar(applicationContext)
    progressBar.id = View.generateViewId()
    val progressbarId = progressBar.id
    val params = ConstraintLayout.LayoutParams(
        ConstraintLayout.LayoutParams.WRAP_CONTENT,
        ConstraintLayout.LayoutParams.WRAP_CONTENT
    )
    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
    progressBar.layoutParams = params
    val viewGroup =
        (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
    viewGroup.addView(progressBar)
    return progressbarId
}

fun Activity.hideProgress(progressbarId: Int) {
    if (progressbarId != -1) {
        val progressBar = findViewById<ProgressBar>(progressbarId)
        val viewGroup =
            (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        viewGroup.removeView(progressBar)
    }
}

fun <T>MutableLiveData<T>.changeValue(value: T){
    if (Looper.getMainLooper() == Looper.myLooper()){
        setValue(value)
    } else{
        postValue(value)
    }
}

fun Context.copy(text: String){
    val clipboard: ClipboardManager? = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("label", text)
    clipboard?.setPrimaryClip(clip)
}

fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()
