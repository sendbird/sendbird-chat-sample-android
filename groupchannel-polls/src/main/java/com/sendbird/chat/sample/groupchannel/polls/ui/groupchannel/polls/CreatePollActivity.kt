package com.sendbird.chat.sample.groupchannel.polls.ui.groupchannel.polls

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.PollCreateParams
import com.sendbird.android.params.UserMessageCreateParams
import com.sendbird.android.poll.Poll
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.TextUtils
import com.sendbird.chat.module.utils.showToast
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityCreatePollBinding
import java.util.*

class CreatePollActivity : AppCompatActivity() {

    private var channelUrl: String = ""
    private var channelTitle: String = ""
    private var currentGroupChannel: GroupChannel? = null
    private lateinit var binding: ActivityCreatePollBinding

    private var closeAt: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePollBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL) ?: ""
        channelTitle = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_TITLE) ?: ""

        binding.llOptions.addView(createOption())
        binding.llOptions.addView(createOption())
        binding.btnAddOption.setOnClickListener { binding.llOptions.addView(createOption()) }
        binding.btnCreatePoll.setOnClickListener { createPoll() }
        binding.btnCloseAt.setOnClickListener {
            if (closeAt != null) {
                closeAt = null
                binding.btnCloseAt.text = "Close at"
                binding.etCloseAt.text = ""
                return@setOnClickListener
            }
            openDateTimeSelector {
                closeAt = it
                binding.etCloseAt.text = Date(it).toString()
                binding.btnCloseAt.text = "Clear"
            }
        }

        getChannel(channelUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getChannel(channelUrl: String?) {
        if (channelUrl.isNullOrBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }
        GroupChannel.getChannel(
            channelUrl
        ) getChannelLabel@{ groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@getChannelLabel
            }
            if (groupChannel != null) {
                currentGroupChannel = groupChannel
                setChannelTitle()
            }
        }
    }

    private fun setChannelTitle() {
        val currentChannel = currentGroupChannel
        val title = if (channelTitle == TextUtils.CHANNEL_DEFAULT_NAME && currentChannel != null) {
            TextUtils.getGroupChannelTitle(currentChannel)
        } else {
            channelTitle
        }

        binding.toolbar.title = "Create Poll in $title"
    }

    private fun createPoll() {
        val channel = currentGroupChannel ?: return
        val title = binding.edPollTitle.text?.toString()?.takeIf { it.isNotBlank() } ?: return
        val options = binding
            .llOptions
            .children
            .map { (it as EditText).text.toString() }
            .toList()
            .takeIf { it.isNotEmpty() }
            ?: return
        val allowMultipleVotes = binding.cbMultipleVotes.isChecked
        val allowUserSuggestion = binding.cbUserSugestions.isChecked
        val params = PollCreateParams(
            title = title,
            optionTexts = options,
            allowUserSuggestion = allowUserSuggestion,
            allowMultipleVotes = allowMultipleVotes,
        ).apply {
            val closeTime = this@CreatePollActivity.closeAt ?: return@apply
            closeAt = closeTime
        }
        Poll.create(params) { poll, exception ->
            if (exception != null) {
                Log.e("CreatePoll", exception.message ?: "error creating poll")
                return@create
            }

            poll ?: return@create
            val userMessageParams = UserMessageCreateParams(title).apply {
                pollId = poll.id
            }
            channel.sendUserMessage(userMessageParams) { _, ex ->
                if (ex != null) {
                    Log.e("CreatePoll", ex.message ?: "error creating poll")
                    return@sendUserMessage
                }
                finish()
            }
        }
    }

    private fun createOption(): EditText {
        return EditText(this).apply {
            hint = "Option ${binding.llOptions.childCount + 1}"
        }
    }
}