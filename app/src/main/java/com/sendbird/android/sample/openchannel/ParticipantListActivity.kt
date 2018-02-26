package com.sendbird.android.sample.openchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.sendbird.android.OpenChannel
import com.sendbird.android.SendBird
import com.sendbird.android.User
import com.sendbird.android.UserListQuery
import com.sendbird.android.sample.R
import com.sendbird.android.sample.groupchannel.UserListAdapter
import kotlinx.android.synthetic.main.activity_participant_list.*
import java.util.*

/**
 * Displays a list of the participants of a specified Open Channel.
 */

class ParticipantListActivity : AppCompatActivity() {

    private var mListAdapter: UserListAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var mChannel: OpenChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_participant_list)

        val mChannelUrl = intent.getStringExtra(OpenChatFragment.EXTRA_CHANNEL_URL)
        mListAdapter = UserListAdapter(this, mChannelUrl, false)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_participant_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)

        setUpRecyclerView()

        getChannelFromUrl(mChannelUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setUpRecyclerView() {
        mLayoutManager = LinearLayoutManager(this)
        recycler_participant_list.layoutManager = mLayoutManager
        recycler_participant_list.adapter = mListAdapter
        recycler_participant_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun getChannelFromUrl(url: String) {
        OpenChannel.getChannel(url, OpenChannel.OpenChannelGetHandler { openChannel, e ->
            if (e != null) {
                // Error!
                return@OpenChannelGetHandler
            }

            mChannel = openChannel

            getUserList()
        })
    }

    private fun getUserList() {
        val userListQuery = mChannel!!.createParticipantListQuery()
        userListQuery.next(UserListQuery.UserListQueryResultHandler { list, e ->
            if (e != null) {
                // Error!
                return@UserListQueryResultHandler
            }

            setUserList(list)
        })
    }

    private fun setUserList(userList: List<User>) {
        val sortedUserList = ArrayList<User>()
        for (me in userList) {
            if (me.userId == SendBird.getCurrentUser().userId) {
                sortedUserList.add(me)
                break
            }
        }
        for (other in userList) {
            if (other.userId == SendBird.getCurrentUser().userId) {
                continue
            }
            sortedUserList.add(other)
        }

        mListAdapter!!.setUserList(sortedUserList)
    }
}
