package com.sendbird.android.sample.groupchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.sendbird.android.GroupChannel
import com.sendbird.android.Member
import com.sendbird.android.SendBird
import com.sendbird.android.sample.R
import com.sendbird.android.sample.main.ConnectionManager
import com.sendbird.android.sample.utils.ImageUtils
import kotlinx.android.synthetic.main.activity_member_info.*


class MemberInfoActivity : AppCompatActivity() {

    private var mChannelUrl: String? = null
    private var mUserId: String? = null
    private var mChannel: GroupChannel? = null
    private var mMember: Member? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_member_info)

        mChannelUrl = intent.getStringExtra(MemberListActivity.EXTRA_CHANNEL_URL)
        mUserId = intent.getStringExtra(MemberListActivity.EXTRA_USER_ID)
        val profileUrl = intent.getStringExtra(MemberListActivity.EXTRA_USER_PROFILE_URL)
        val nickname = intent.getStringExtra(MemberListActivity.EXTRA_USER_NICKNAME)
        val blockedByMe = intent.getBooleanExtra(MemberListActivity.EXTRA_USER_BLOCKED_BY_ME, false)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_member_info)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)

        if (mUserId != null && mUserId == SendBird.getCurrentUser().userId) {
            relative_layout_blocked_by_me.visibility = View.GONE
        } else {
            relative_layout_blocked_by_me.visibility = View.VISIBLE
        }

        switch_blocked_by_me.setOnClickListener {
            if (switch_blocked_by_me.isChecked) {
                SendBird.blockUser(mMember, SendBird.UserBlockHandler { user, e ->
                    if (e != null) {
                        switch_blocked_by_me.isChecked = false
                        return@UserBlockHandler
                    }
                })
            } else {
                SendBird.unblockUser(mMember, SendBird.UserUnblockHandler { e ->
                    if (e != null) {
                        switch_blocked_by_me.isChecked = true
                        return@UserUnblockHandler
                    }
                })
            }
        }

        refreshUser(profileUrl, nickname, blockedByMe)
    }

    override fun onResume() {
        super.onResume()

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionManager.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                getUserFromUrl(mChannelUrl)
            }
        })
    }

    override fun onPause() {
        super.onPause()

        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getUserFromUrl(url: String?) {
        GroupChannel.getChannel(url, GroupChannel.GroupChannelGetHandler { groupChannel, e ->
            if (e != null) {
                // Error!
                return@GroupChannelGetHandler
            }

            mChannel = groupChannel

            refreshChannel()
        })
    }

    private fun refreshChannel() {
        mChannel!!.refresh(GroupChannel.GroupChannelRefreshHandler { e ->
            if (e != null) {
                // Error!
                return@GroupChannelRefreshHandler
            }

            for (member in mChannel!!.members) {
                if (member.userId == mUserId) {
                    mMember = member
                    break
                }
            }

            refreshUser(mMember!!.profileUrl, mMember!!.nickname, mMember!!.isBlockedByMe)
        })
    }

    private fun refreshUser(profileUrl: String, nickname: String, isBlockedByMe: Boolean) {
        ImageUtils.displayRoundImageFromUrl(this, profileUrl, image_view_profile)
        text_view_nickname.text = nickname
        switch_blocked_by_me.isChecked = isBlockedByMe
    }

    companion object {

        private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MEMBER_INFO"
    }
}
