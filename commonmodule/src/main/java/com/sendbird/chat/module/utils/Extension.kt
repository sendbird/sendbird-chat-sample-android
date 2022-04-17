package com.sendbird.chat.module.utils

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
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
