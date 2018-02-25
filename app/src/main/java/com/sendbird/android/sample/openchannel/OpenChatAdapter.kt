package com.sendbird.android.sample.openchannel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.sendbird.android.*
import com.sendbird.android.sample.R
import com.sendbird.android.sample.utils.DateUtils
import com.sendbird.android.sample.utils.FileUtils
import com.sendbird.android.sample.utils.ImageUtils
import kotlinx.android.synthetic.main.list_item_open_chat_file.view.*
import java.util.*

/**
 * Created by toshimikusunoki on 2018/02/23.
 */
internal class OpenChatAdapter(private val mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mMessageList: MutableList<BaseMessage>? = null
    private var mItemClickListener: OnItemClickListener? = null
    private var mItemLongClickListener: OnItemLongClickListener? = null

    /**
     * An interface to implement item click callbacks in the activity or fragment that
     * uses this adapter.
     */
    internal interface OnItemClickListener {
        fun onUserMessageItemClick(message: UserMessage)

        fun onFileMessageItemClick(message: FileMessage)

        fun onAdminMessageItemClick(message: AdminMessage)
    }

    internal interface OnItemLongClickListener {
        fun onBaseMessageLongClick(message: BaseMessage, position: Int)
    }


    init {
        mMessageList = ArrayList()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        mItemLongClickListener = listener
    }

    fun setMessageList(messages: MutableList<BaseMessage>) {
        mMessageList = messages
        notifyDataSetChanged()
    }

    fun addFirst(message: BaseMessage) {
        mMessageList!!.add(0, message)
        notifyDataSetChanged()
    }

    fun addLast(message: BaseMessage) {
        mMessageList!!.add(message)
        notifyDataSetChanged()
    }

    fun delete(msgId: Long) {
        for (msg in mMessageList!!) {
            if (msg.messageId == msgId) {
                mMessageList!!.remove(msg)
                notifyDataSetChanged()
                break
            }
        }
    }

    fun update(message: BaseMessage) {
        var baseMessage: BaseMessage
        for (index in mMessageList!!.indices) {
            baseMessage = mMessageList!![index]
            if (message.messageId == baseMessage.messageId) {
                mMessageList!!.removeAt(index)
                mMessageList!!.add(index, message)
                notifyDataSetChanged()
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_open_chat_user, parent, false)
            return UserMessageHolder(view)

        } else if (viewType == VIEW_TYPE_ADMIN_MESSAGE) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_open_chat_admin, parent, false)
            return AdminMessageHolder(view)

        } else if (viewType == VIEW_TYPE_FILE_MESSAGE) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_open_chat_file, parent, false)
            return FileMessageHolder(view)
        }

        // Theoretically shouldn't happen.
        return null
    }

    override fun getItemViewType(position: Int): Int {
        if (mMessageList!![position] is UserMessage) {
            return VIEW_TYPE_USER_MESSAGE
        } else if (mMessageList!![position] is AdminMessage) {
            return VIEW_TYPE_ADMIN_MESSAGE
        } else if (mMessageList!![position] is FileMessage) {
            return VIEW_TYPE_FILE_MESSAGE
        }

        // Unhandled message type.
        return -1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = mMessageList!![position]

        var isNewDay = false

        // If there is at least one item preceding the current one, check the previous message.
        if (position < mMessageList!!.size - 1) {
            val prevMessage = mMessageList!![position + 1]

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (!DateUtils.hasSameDate(message.createdAt, prevMessage.createdAt)) {
                isNewDay = true
            }

        } else if (position == mMessageList!!.size - 1) {
            isNewDay = true
        }

        when (holder.itemViewType) {
            VIEW_TYPE_USER_MESSAGE -> (holder as UserMessageHolder).bind(mContext, message as UserMessage, isNewDay,
                    mItemClickListener, mItemLongClickListener, position)
            VIEW_TYPE_ADMIN_MESSAGE -> (holder as AdminMessageHolder).bind(message as AdminMessage, isNewDay,
                    mItemClickListener)
            VIEW_TYPE_FILE_MESSAGE -> (holder as FileMessageHolder).bind(mContext, message as FileMessage, isNewDay,
                    mItemClickListener, mItemLongClickListener, position)
            else -> {
            }
        }
    }

    override fun getItemCount(): Int {
        return mMessageList!!.size
    }

    private inner class UserMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView
        internal var editedText: TextView
        internal var timeText: TextView
        internal var dateText: TextView
        internal var profileImage: ImageView

        init {
            messageText = itemView.findViewById(R.id.text_open_chat_message) as TextView
            editedText = itemView.findViewById(R.id.text_open_chat_edited) as TextView
            timeText = itemView.findViewById(R.id.text_open_chat_time) as TextView
            profileImage = itemView.findViewById(R.id.image_open_chat_profile) as ImageView
            dateText = itemView.findViewById(R.id.text_open_chat_date) as TextView
        }

        // Binds message details to ViewHolder item
        internal fun bind(context: Context, message: UserMessage, isNewDay: Boolean,
                          clickListener: OnItemClickListener?,
                          longClickListener: OnItemLongClickListener?, postion: Int) {

            val sender = message.sender

            // If current user sent the message, display name in different color
            if (sender.userId == SendBird.getCurrentUser().userId) {
                itemView.text_open_chat_nickname.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameMe))
            } else {
                itemView.text_open_chat_nickname.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameOther))
            }

            // Show the date if the message was sent on a different date than the previous one.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.setText(DateUtils.formatDate(message.createdAt))
            } else {
                dateText.visibility = View.GONE
            }

            itemView.text_open_chat_nickname.text = message.sender.nickname
            messageText.text = message.message
            timeText.setText(DateUtils.formatTime(message.createdAt))

            if (message.updatedAt > 0) {
                editedText.visibility = View.VISIBLE
            } else {
                editedText.visibility = View.GONE
            }

            // Get profile image and display it
            ImageUtils.displayRoundImageFromUrl(context, message.sender.profileUrl, profileImage)

            if (clickListener != null) {
                itemView.setOnClickListener { clickListener.onUserMessageItemClick(message) }
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener {
                    longClickListener.onBaseMessageLongClick(message, postion)
                    true
                }
            }
        }
    }

    private inner class AdminMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView
        internal var dateText: TextView

        init {

            messageText = itemView.findViewById(R.id.text_open_chat_message) as TextView
            dateText = itemView.findViewById(R.id.text_open_chat_date) as TextView
        }

        internal fun bind(message: AdminMessage, isNewDay: Boolean, listener: OnItemClickListener?) {
            messageText.text = message.message

            // Show the date if the message was sent on a different date than the previous one.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.setText(DateUtils.formatDate(message.createdAt))
            } else {
                dateText.visibility = View.GONE
            }

            itemView.setOnClickListener { listener!!.onAdminMessageItemClick(message) }
        }
    }

    private inner class FileMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Binds message details to ViewHolder item
        internal fun bind(context: Context, message: FileMessage, isNewDay: Boolean,
                          clickListener: OnItemClickListener?,
                          longClickListener: OnItemLongClickListener?, position: Int) {
            val sender = message.sender

            // If current user sent the message, display name in different color
            if (sender.userId == SendBird.getCurrentUser().userId) {
                itemView.text_open_chat_nickname.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameMe))
            } else {
                itemView.text_open_chat_nickname.setTextColor(ContextCompat.getColor(context, R.color.openChatNicknameOther))
            }

            // Show the date if the message was sent on a different date than the previous one.
            if (isNewDay) {
                itemView.text_open_chat_date.visibility = View.VISIBLE
                itemView.text_open_chat_date.setText(DateUtils.formatDate(message.createdAt))
            } else {
                itemView.text_open_chat_date.visibility = View.GONE
            }

            // Get profile image and display it
            ImageUtils.displayRoundImageFromUrl(context, message.sender.profileUrl, itemView.image_open_chat_profile)

            itemView.text_open_chat_file_name.text = message.name
            itemView.text_open_chat_file_size.setText(FileUtils.toReadableFileSize(message.size))
            itemView.text_open_chat_nickname.text = message.sender.nickname

            // If image, display thumbnail
            if (message.type.toLowerCase().startsWith("image")) {
                // Get thumbnails from FileMessage
                val thumbnails = message.thumbnails as ArrayList<FileMessage.Thumbnail>

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size > 0) {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(context, message.url, itemView.image_open_chat_file_thumbnail, thumbnails[0].url, itemView.image_open_chat_file_thumbnail.drawable)
                    } else {
                        ImageUtils.displayImageFromUrl(context, thumbnails[0].url, itemView.image_open_chat_file_thumbnail, itemView.image_open_chat_file_thumbnail.drawable)
                    }
                } else {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(context, message.url, itemView.image_open_chat_file_thumbnail, null as String?, itemView.image_open_chat_file_thumbnail.drawable)
                    } else {
                        ImageUtils.displayImageFromUrl(context, message.url, itemView.image_open_chat_file_thumbnail, itemView.image_open_chat_file_thumbnail.drawable)
                    }
                }

            } else if (message.type.toLowerCase().startsWith("video")) {
                // Get thumbnails from FileMessage
                val thumbnails = message.thumbnails as ArrayList<FileMessage.Thumbnail>

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size > 0) {
                    ImageUtils.displayImageFromUrlWithPlaceHolder(
                            context, thumbnails[0].url, itemView.image_open_chat_file_thumbnail, R.drawable.ic_file_message)
                } else {
                    itemView.image_open_chat_file_thumbnail.setImageDrawable(context.resources.getDrawable(R.drawable.ic_play))
                }

            } else {
                itemView.image_open_chat_file_thumbnail.setImageDrawable(context.resources.getDrawable(R.drawable.ic_file_message))
            }

            if (clickListener != null) {
                itemView.setOnClickListener { clickListener.onFileMessageItemClick(message) }
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener {
                    longClickListener.onBaseMessageLongClick(message, position)
                    true
                }
            }

        }
    }

    companion object {

        private val VIEW_TYPE_USER_MESSAGE = 10
        private val VIEW_TYPE_FILE_MESSAGE = 20
        private val VIEW_TYPE_ADMIN_MESSAGE = 30
    }


}
