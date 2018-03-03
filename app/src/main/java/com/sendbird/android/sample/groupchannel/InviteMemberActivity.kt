package com.sendbird.android.sample.groupchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import com.sendbird.android.User
import com.sendbird.android.UserListQuery
import com.sendbird.android.sample.R
import kotlinx.android.synthetic.main.activity_invite_member.*
import java.util.*

/**
 * Displays a selectable list of users within the app. Selected users will be invited into the
 * current channel.
 */

class InviteMemberActivity : AppCompatActivity() {

    private var mListAdapter: SelectableUserListAdapter? = null

    private var mUserListQuery: UserListQuery? = null
    private var mChannelUrl: String? = null

    private var mSelectedUserIds: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_invite_member)

        mSelectedUserIds = ArrayList()

        mListAdapter = SelectableUserListAdapter(this, false, true)
        mListAdapter!!.setItemCheckedChangeListener(object : SelectableUserListAdapter.OnItemCheckedChangeListener {
            override fun OnItemChecked(user: User, checked: Boolean) {
                if (checked) {
                    mSelectedUserIds!!.add(user.userId)
                } else {
                    mSelectedUserIds!!.remove(user.userId)
                }

                // If no users are selected, disable the invite button.
                if (mSelectedUserIds!!.size > 0) {
                    button_invite_member.isEnabled = true
                } else {
                    button_invite_member.isEnabled = false
                }
            }
        })

        setSupportActionBar(toolbar_invite_member)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)
        }

        mChannelUrl = intent.getStringExtra(GroupChatFragment.EXTRA_CHANNEL_URL)

        button_invite_member.setOnClickListener {
            if (mSelectedUserIds!!.size > 0) {
                inviteSelectedMembersWithUserIds()
            }
        }
        button_invite_member.isEnabled = false

        setUpRecyclerView()

        loadInitialUserList(15)
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
        val mLayoutManager = LinearLayoutManager(this)
        recycler_invite_member.layoutManager = mLayoutManager
        recycler_invite_member.adapter = mListAdapter
        recycler_invite_member.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        recycler_invite_member.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (mLayoutManager.findLastVisibleItemPosition() == mListAdapter!!.itemCount - 1) {
                    loadNextUserList(10)
                }
            }
        })
    }

    private fun inviteSelectedMembersWithUserIds() {

        // Get channel instance from URL first.
        GroupChannel.getChannel(mChannelUrl, GroupChannel.GroupChannelGetHandler { groupChannel, e ->
            if (e != null) {
                // Error!
                return@GroupChannelGetHandler
            }

            // Then invite the selected members to the channel.
            groupChannel.inviteWithUserIds(mSelectedUserIds, GroupChannel.GroupChannelInviteHandler { e ->
                if (e != null) {
                    // Error!
                    return@GroupChannelInviteHandler
                }

                finish()
            })
        })
    }

    /**
     * Replaces current user list with new list.
     * Should be used only on initial load.
     */
    private fun loadInitialUserList(size: Int) {
        mUserListQuery = SendBird.createUserListQuery()

        mUserListQuery!!.setLimit(size)
        mUserListQuery!!.next(UserListQuery.UserListQueryResultHandler { list, e ->
            if (e != null) {
                // Error!
                return@UserListQueryResultHandler
            }

            mListAdapter!!.setUserList(list)
        })
    }

    /**
     * Loads users and adds them to current user list.
     *
     * A PreviousMessageListQuery must have been already initialized through [.loadInitialUserList]
     */
    private fun loadNextUserList(size: Int) {
        mUserListQuery!!.setLimit(size)

        mUserListQuery!!.next(UserListQuery.UserListQueryResultHandler { list, e ->
            if (e != null) {
                // Error!
                return@UserListQueryResultHandler
            }

            for (user in list) {
                mListAdapter!!.addLast(user)
            }
        })
    }

}
