package com.sendbird.android.sample.groupchannel

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.SendBird
import com.sendbird.android.User
import com.sendbird.android.sample.R
import com.sendbird.android.sample.main.BlockedMembersListActivity
import com.sendbird.android.sample.utils.ImageUtils
import kotlinx.android.synthetic.main.list_item_selectable_user.view.*
import java.util.*

/**
 * Populates a RecyclerView with a list of users, each with a checkbox.
 */

class SelectableUserListAdapter(private val mContext: Context, private val mIsBlockedList: Boolean, private var mShowCheckBox: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mUsers: MutableList<User>? = null

    private var mSelectableUserHolder: SelectableUserHolder? = null

    // For the adapter to track which users have been selected
    private var mCheckedChangeListener: OnItemCheckedChangeListener? = null

    interface OnItemCheckedChangeListener {
        fun OnItemChecked(user: User, checked: Boolean)
    }

    init {
        mUsers = ArrayList()
        mSelectedUserIds = ArrayList()
    }

    fun setItemCheckedChangeListener(listener: OnItemCheckedChangeListener) {
        mCheckedChangeListener = listener
    }

    fun setUserList(users: MutableList<User>) {
        mUsers = users
        notifyDataSetChanged()
    }

    fun setShowCheckBox(showCheckBox: Boolean) {
        mShowCheckBox = showCheckBox
        if (mSelectableUserHolder != null) {
            mSelectableUserHolder!!.setShowCheckBox(showCheckBox)
        }
        notifyDataSetChanged()
    }

    fun unblock() {
        for (userId in mSelectedUserIds) {
            SendBird.unblockUserWithUserId(userId, SendBird.UserUnblockHandler { e ->
                if (e != null) {
                    return@UserUnblockHandler
                }

                var user: User
                for (index in mUsers!!.indices) {
                    user = mUsers!![index]
                    if (userId == user.userId) {
                        mUsers!!.removeAt(index)
                        break
                    }
                }

                (mContext as BlockedMembersListActivity).blockedMemberCount(mUsers!!.size)

                notifyDataSetChanged()
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_selectable_user, parent, false)
        mSelectableUserHolder = SelectableUserHolder(view, mIsBlockedList, mShowCheckBox)
        return mSelectableUserHolder as SelectableUserHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SelectableUserHolder).bind(
                mContext,
                mUsers!![position],
                isSelected(mUsers!![position]),
                mCheckedChangeListener!!)
    }

    override fun getItemCount(): Int {
        return mUsers!!.size
    }

    fun isSelected(user: User): Boolean {
        return mSelectedUserIds.contains(user.userId)
    }

    fun addLast(user: User) {
        mUsers!!.add(user)
        notifyDataSetChanged()
    }

    private inner class SelectableUserHolder(itemView: View, private val mIsBlockedList: Boolean, private var mShowCheckBox: Boolean) : RecyclerView.ViewHolder(itemView) {

        init {
            this.setIsRecyclable(false)
        }

        fun setShowCheckBox(showCheckBox: Boolean) {
            mShowCheckBox = showCheckBox
        }

        fun bind(context: Context, user: User, isSelected: Boolean, listener: OnItemCheckedChangeListener) {
            itemView.text_selectable_user_list_nickname.text = user.nickname
            ImageUtils.displayRoundImageFromUrl(context, user.profileUrl, itemView.image_selectable_user_list_profile)

            if (mIsBlockedList) {
                itemView.image_user_list_blocked.visibility = View.VISIBLE
            } else {
                itemView.image_user_list_blocked.visibility = View.GONE
            }

            if (mShowCheckBox) {
                itemView.checkbox_selectable_user_list.visibility = View.VISIBLE
            } else {
                itemView.checkbox_selectable_user_list.visibility = View.GONE
            }

            itemView.checkbox_selectable_user_list.isChecked = isSelected

            if (mShowCheckBox) {
                itemView.setOnClickListener {
                    if (mShowCheckBox) {
                        itemView.checkbox_selectable_user_list.isChecked = !itemView.checkbox_selectable_user_list.isChecked
                    }
                }
            }

            itemView.checkbox_selectable_user_list.setOnCheckedChangeListener { buttonView, isChecked ->
                listener.OnItemChecked(user, isChecked)

                if (isChecked) {
                    mSelectedUserIds.add(user.userId)
                } else {
                    mSelectedUserIds.remove(user.userId)
                }
            }
        }
    }

    companion object {
        private lateinit var mSelectedUserIds: MutableList<String>
    }
}
