package com.sendbird.android.sample.groupchannel

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.Member
import com.sendbird.android.SendBird
import com.sendbird.android.User
import com.sendbird.android.sample.R
import com.sendbird.android.sample.utils.ImageUtils
import kotlinx.android.synthetic.main.list_item_user.view.*
import java.util.*

/**
 * A simple adapter that displays a list of Users.
 */
class UserListAdapter(private val mContext: Context, private val mChannelUrl: String, private val mIsGroupChannel: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mUsers: MutableList<User>

    init {
        mUsers = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserHolder).bind(mContext, holder, mUsers[position])
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    fun setUserList(users: List<User>) {
        mUsers.clear()
        mUsers.addAll(users)
        notifyDataSetChanged()
    }

    fun addLast(user: User) {
        mUsers.add(user)
        notifyDataSetChanged()
    }

    private inner class UserHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {

        internal fun bind(context: Context, holder: UserHolder, user: User) {
            view.text_user_list_nickname.text = user.nickname
            ImageUtils.displayRoundImageFromUrl(context, user.profileUrl, view.image_user_list_profile)

            if (mIsGroupChannel) {
                if (SendBird.getCurrentUser().userId == user.userId) {
                    view.relative_layout_blocked_by_me.visibility = View.GONE
                    view.text_view_blocked.visibility = View.GONE
                } else {
                    view.relative_layout_blocked_by_me.visibility = View.VISIBLE

                    holder.view.setOnClickListener {
                        val intent = Intent(context, MemberInfoActivity::class.java)
                        intent.putExtra(MemberListActivity.EXTRA_CHANNEL_URL, mChannelUrl)
                        intent.putExtra(MemberListActivity.EXTRA_USER_ID, user.userId)
                        intent.putExtra(MemberListActivity.EXTRA_USER_PROFILE_URL, user.profileUrl)
                        intent.putExtra(MemberListActivity.EXTRA_USER_NICKNAME, user.nickname)
                        intent.putExtra(MemberListActivity.EXTRA_USER_BLOCKED_BY_ME, (user as Member).isBlockedByMe)
                        context.startActivity(intent)
                    }
                }

                val isBlockedByMe = (user as Member).isBlockedByMe
                if (isBlockedByMe) {
                    view.image_user_list_blocked.visibility = View.VISIBLE
                    view.text_view_blocked.visibility = View.VISIBLE
                } else {
                    view.image_user_list_blocked.visibility = View.GONE
                    view.text_view_blocked.visibility = View.GONE
                }
            } else {
                view.image_user_list_blocked.visibility = View.GONE
                view.relative_layout_blocked_by_me.visibility = View.GONE
            }
        }
    }
}

