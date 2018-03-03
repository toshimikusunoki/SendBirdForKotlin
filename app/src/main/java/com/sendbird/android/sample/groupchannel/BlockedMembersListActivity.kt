package com.sendbird.android.sample.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import com.sendbird.android.SendBird
import com.sendbird.android.User
import com.sendbird.android.UserListQuery
import com.sendbird.android.sample.R
import com.sendbird.android.sample.groupchannel.SelectableUserListAdapter
import kotlinx.android.synthetic.main.activity_blocked_members_list.*
import java.util.*

class BlockedMembersListActivity : AppCompatActivity() {

    private var mLayoutManager: LinearLayoutManager? = null
    private var mListAdapter: SelectableUserListAdapter? = null
    private var mUserListQuery: UserListQuery? = null

    private var mSelectedIds: MutableList<String>? = null
    private var mCurrentState: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_blocked_members_list)

        mSelectedIds = ArrayList()

        button_edit.setOnClickListener { setState(STATE_EDIT) }
        button_edit.isEnabled = false

        button_unblock.setOnClickListener {
            mListAdapter!!.unblock()
            setState(STATE_NORMAL)
        }
        button_unblock.isEnabled = false

        setSupportActionBar(toolbar_blocked_members_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)

        mListAdapter = SelectableUserListAdapter(this, true, false)
        mListAdapter!!.setItemCheckedChangeListener(object : SelectableUserListAdapter.OnItemCheckedChangeListener {
            override fun OnItemChecked(user: User, checked: Boolean) {
                if (checked) {
                    userSelected(true, user.userId)
                } else {
                    userSelected(false, user.userId)
                }
            }
        })

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

    internal fun setState(state: Int) {
        if (state == STATE_EDIT) {
            mCurrentState = STATE_EDIT
            button_unblock.visibility = View.VISIBLE
            button_edit.visibility = View.GONE
            mListAdapter!!.setShowCheckBox(true)
        } else if (state == STATE_NORMAL) {
            mCurrentState = STATE_NORMAL
            button_unblock.visibility = View.GONE
            button_edit.visibility = View.VISIBLE
            mListAdapter!!.setShowCheckBox(false)
        }
    }

    fun userSelected(selected: Boolean, userId: String) {
        if (selected) {
            mSelectedIds!!.add(userId)
        } else {
            mSelectedIds!!.remove(userId)
        }

        if (mSelectedIds!!.size > 0) {
            button_unblock.isEnabled = true
        } else {
            button_unblock.isEnabled = false
        }
    }

    private fun setUpRecyclerView() {
        mLayoutManager = LinearLayoutManager(this)
        recycler_select_user.layoutManager = mLayoutManager
        recycler_select_user.adapter = mListAdapter
        recycler_select_user.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_select_user.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (mLayoutManager!!.findLastVisibleItemPosition() == mListAdapter!!.itemCount - 1) {
                    loadNextUserList(10)
                }
            }
        })
    }

    private fun loadInitialUserList(size: Int) {
        mUserListQuery = SendBird.createBlockedUserListQuery()

        mUserListQuery!!.setLimit(size)
        mUserListQuery!!.next(UserListQuery.UserListQueryResultHandler { list, e ->
            if (e != null) {
                // Error!
                return@UserListQueryResultHandler
            }

            mListAdapter!!.setUserList(list)
            button_edit.isEnabled = list.size > 0
        })
    }

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

    override fun onBackPressed() {
        if (mCurrentState == STATE_EDIT) {
            setState(STATE_NORMAL)
        } else {
            super.onBackPressed()
        }
    }

    fun blockedMemberCount(size: Int) {
        button_edit.isEnabled = size > 0
    }

    companion object {

        private val STATE_NORMAL = 0
        private val STATE_EDIT = 1
    }
}
