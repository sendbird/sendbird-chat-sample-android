package com.sendbird.chat.sample.groupchannel.polls.ui.groupchannel.polls

import android.os.Bundle
import android.os.PersistableBundle
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.PollRetrievalParams
import com.sendbird.android.poll.Poll
import com.sendbird.android.poll.PollOption
import com.sendbird.android.poll.PollStatus
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityPollDetailBinding
import com.sendbird.chat.sample.groupchannel.databinding.ActivityPollVoteBinding
import com.sendbird.chat.sample.groupchannel.databinding.ItemPollOptionVoteAddBinding
import com.sendbird.chat.sample.groupchannel.databinding.ItemPollOptionVoteBinding
import com.sendbird.chat.sample.groupchannel.databinding.ItemPollVoteCountBinding
import java.util.*

class PollVoteActivity : AppCompatActivity() {

    private var channelUrl: String = ""
    private var pollId: Long = 0L
    private lateinit var binding: ActivityPollVoteBinding

    private val dateFormat by lazy { DateFormat.getMediumDateFormat(this) }
    private val timeFormat by lazy { DateFormat.getTimeFormat(this) }

    private var currentGroupChannel: GroupChannel? = null
    private var currentPoll: Poll? = null
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPollVoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val intent = intent
        channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL) ?: ""
        pollId = intent.getLongExtra(Constants.INTENT_KEY_POLL_ID, 0L)

        GroupChannel.getChannel(channelUrl) { channel, e ->
            if (e != null) {
                Toast.makeText(this, "Get channel error : $e", Toast.LENGTH_SHORT).show()
                return@getChannel
            }

            if (channel != null) {
                currentGroupChannel = channel
                Poll.get(
                    PollRetrievalParams(
                        pollId,
                        channel.channelType,
                        channel.url
                    )
                ) { poll, pollError ->
                    currentPoll = poll

                    if (pollError != null) {
                        Toast.makeText(this, "Get poll error : $pollError", Toast.LENGTH_SHORT).show()
                        return@get
                    }

                    if(poll != null) {
                        showPoll(channel, poll)
                    }
                }
            }
        }
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

    private fun showPoll(channel: GroupChannel, poll: Poll) {
        binding.tvPollTitle.text = poll.title

        val date = Date(poll.createdAt * 1000L)
        binding.tvPollMultipleCreatedAt.text = if(poll.allowMultipleVotes) {
            "Multi select | ${dateFormat.format(date)} ${timeFormat.format(date)}"
        } else {
            "${dateFormat.format(date)} ${timeFormat.format(date)}"
        }

        binding.layoutPollOptions.removeAllViewsInLayout()
        poll.options.forEach {
            binding.layoutPollOptions.addView(createOption(poll, it, channel.memberCount))
        }

        binding.btnAddOption.isVisible = poll.allowUserSuggestion
        binding.btnAddOption.setOnClickListener {
            binding.layoutPollOptionsAdding.addView(createOptionAdding(poll))
        }
    }

    private fun createOption(poll: Poll, option: PollOption, memberCount: Int): View {
        val optionLayout = ItemPollOptionVoteBinding.inflate(layoutInflater)
        optionLayout.tvOption.text = option.text
        optionLayout.checkBox.isChecked = option.id in poll.votedPollOptionIds
        optionLayout.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val newIds = if(isChecked) {
                poll.votedPollOptionIds + option.id
            } else {
                poll.votedPollOptionIds - option.id
            }
            currentGroupChannel?.votePoll(poll.id, newIds) { voteEvent, e ->
                if (e != null) {
                    Toast.makeText(this, "error $e", Toast.LENGTH_SHORT).show()
                }

                if(voteEvent != null) {
                    poll.applyPollVoteEvent(voteEvent)
                    showPoll(currentGroupChannel!!, poll)
                }
            }
        }
        return optionLayout.root
    }

    private fun createOptionAdding(poll: Poll): View {
        val optionLayout = ItemPollOptionVoteAddBinding.inflate(layoutInflater)

        optionLayout.done.setOnClickListener {
            currentGroupChannel?.addPollOption(poll.id, optionLayout.edOption.text.toString()) { newPoll, e ->
                if (e != null) {
                    Toast.makeText(this, "error $e", Toast.LENGTH_SHORT).show()
                }

                if(newPoll != null) {
                    binding.layoutPollOptionsAdding.removeView(optionLayout.root)
                    showPoll(currentGroupChannel!!, newPoll)
                }
            }
        }
        optionLayout.cancel.setOnClickListener {
            binding.layoutPollOptionsAdding.removeView(optionLayout.root)
        }
        return optionLayout.root
    }
}
