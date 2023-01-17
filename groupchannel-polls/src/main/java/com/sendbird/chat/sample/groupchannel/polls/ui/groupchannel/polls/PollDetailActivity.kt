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
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.PollRetrievalParams
import com.sendbird.android.poll.Poll
import com.sendbird.android.poll.PollOption
import com.sendbird.android.poll.PollStatus
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.sample.groupchannel.R
import com.sendbird.chat.sample.groupchannel.databinding.ActivityPollDetailBinding
import com.sendbird.chat.sample.groupchannel.databinding.ItemPollVoteCountBinding
import java.util.*

class PollDetailActivity : AppCompatActivity() {

    private var channelUrl: String = ""
    private var pollId: Long = 0L
    private lateinit var binding: ActivityPollDetailBinding

    private val dateFormat by lazy { DateFormat.getMediumDateFormat(this) }
    private val timeFormat by lazy { DateFormat.getTimeFormat(this) }

    private var currentGroupChannel: GroupChannel? = null
    private var currentPoll: Poll? = null
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPollDetailBinding.inflate(layoutInflater)
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

                    if (poll != null) {
                        val closeEnabled = poll.createdBy == SendbirdChat.currentUser?.userId && poll.status == PollStatus.OPEN
                        menu?.findItem(R.id.btn_close_poll)?.isEnabled = closeEnabled
                        showPoll(channel, poll)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.poll_detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.btn_close_poll -> {
                closePoll()
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

        val percent = ((poll.voterCount.toFloat() / channel.memberCount.toFloat()) * 100).toInt()
        binding.tvPollVoteCount.text = buildSpannedString {
            bold {
                append("${percent}% (${poll.voterCount} votes)")
            }
            append(" in total")
        }

        binding.layoutPollOptions.removeAllViewsInLayout()
        poll.options.forEach {
            binding.layoutPollOptions.addView(createOption(it, channel.memberCount))
        }
    }

    private fun createOption(option: PollOption, memberCount: Int): View {
        val optionLayout = ItemPollVoteCountBinding.inflate(layoutInflater)
        optionLayout.tvPollOptionTitle.text = option.text
        val percent = ((option.voteCount.toFloat() / memberCount.toFloat()) * 100).toInt()
        optionLayout.tvPollOptionTotalVote.text =
            buildSpannedString {
                bold {
                    append("${percent}% (${option.voteCount} votes)")
                }
            }
            option.voteCount.toString()
        optionLayout.progressPollOptionVote.progress =
            ((option.voteCount.toFloat() / memberCount.toFloat()) * 100).toInt()
        return optionLayout.root
    }

    private fun closePoll() {
        val channel = currentGroupChannel ?: return
        val poll = currentPoll ?: return
        channel.closePoll(poll.id) { closedPoll, exception ->
            if (exception != null) {
                Log.e("PollException", exception.message!!)
            }

            if (closedPoll != null) {
                showPoll(channel, closedPoll)
            }
        }
    }
}
