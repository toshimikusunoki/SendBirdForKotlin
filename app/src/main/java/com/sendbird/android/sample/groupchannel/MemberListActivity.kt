package com.sendbird.android.sample.groupchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.sendbird.android.GroupChannel
import com.sendbird.android.Member
import com.sendbird.android.SendBird
import com.sendbird.android.sample.R
import com.sendbird.android.sample.main.ConnectionManager
import kotlinx.android.synthetic.main.activity_member_list.*
import java.util.*


class MemberListActivity : AppCompatActivity() {

    private var mListAdapter: UserListAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var mChannelUrl: String? = null
    private var mChannel: GroupChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_member_list)

        mChannelUrl = intent.getStringExtra(GroupChatFragment.EXTRA_CHANNEL_URL)
        mListAdapter = UserListAdapter(this, mChannelUrl!!, true)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_member_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)

        setUpRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionManager.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                getChannelFromUrl(mChannelUrl)
            }
        })
    }

    override fun onPause() {
        super.onPause()

        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
    }

    private fun setUpRecyclerView() {
        mLayoutManager = LinearLayoutManager(this)
        recycler_member_list.layoutManager = mLayoutManager
        recycler_member_list.adapter = mListAdapter
        recycler_member_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getChannelFromUrl(url: String?) {
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

            setMemberList(mChannel!!.members)
        })
    }

    private fun setMemberList(memberList: List<Member>) {
        val sortedUserList = ArrayList<Member>()
        for (me in memberList) {
            if (me.userId == SendBird.getCurrentUser().userId) {
                sortedUserList.add(me)
                break
            }
        }
        for (other in memberList) {
            if (other.userId == SendBird.getCurrentUser().userId) {
                continue
            }
            sortedUserList.add(other)
        }

        mListAdapter!!.setUserList(sortedUserList)
    }

    companion object {

        private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MEMBER_LIST"
        internal val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
        internal val EXTRA_USER_ID = "EXTRA_USER_ID"
        internal val EXTRA_USER_PROFILE_URL = "EXTRA_USER_PROFILE_URL"
        internal val EXTRA_USER_NICKNAME = "EXTRA_USER_NICKNAME"
        internal val EXTRA_USER_BLOCKED_BY_ME = "EXTRA_USER_BLOCKED_BY_ME"
    }
}
