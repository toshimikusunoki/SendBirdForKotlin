package com.sendbird.android.sample.openchannel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.sendbird.android.*
import com.sendbird.android.sample.R
import com.sendbird.android.sample.main.ConnectionManager
import com.sendbird.android.sample.utils.FileUtils
import com.sendbird.android.sample.utils.MediaPlayerActivity
import com.sendbird.android.sample.utils.PhotoViewerActivity
import kotlinx.android.synthetic.main.fragment_open_chat.*
import java.io.File
import java.util.*


class OpenChatFragment : Fragment() {

    private var mIMM: InputMethodManager? = null

    private var mChatAdapter: OpenChatAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null

    private var mChannel: OpenChannel? = null
    private var mChannelUrl: String? = null
    private var mPrevMessageListQuery: PreviousMessageListQuery? = null

    private var mCurrentState = STATE_NORMAL
    private var mEditingMessage: BaseMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIMM = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_open_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        retainInstance = true
        setHasOptionsMenu(true)

        setUpChatAdapter()
        setUpRecyclerView()

        edittext_chat_message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                button_open_channel_chat_send.isEnabled = if (s.length > 0) true else false
            }
        })

        button_open_channel_chat_send.isEnabled = false
        button_open_channel_chat_send.setOnClickListener {
            if (mCurrentState == STATE_EDIT) {
                val userInput = edittext_chat_message.text.toString()
                if (userInput.length > 0) {
                    if (mEditingMessage != null) {
                        editMessage(mEditingMessage, edittext_chat_message.text.toString())
                    }
                }
                setState(STATE_NORMAL, null, -1)
            } else {
                val userInput = edittext_chat_message.text.toString()
                if (userInput.length > 0) {
                    sendUserMessage(userInput)
                    edittext_chat_message.setText("")
                }
            }
        }

        button_open_channel_chat_upload.setOnClickListener { requestImage() }
        // Gets channel from URL user requested
        mChannelUrl = arguments!!.getString(OpenChannelListFragment.EXTRA_OPEN_CHANNEL_URL)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_WRITE_EXTERNAL_STORAGE ->

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission granted.
                    Snackbar.make(layout_open_chat_root, "Storage permissions granted. You can now upload or download files.",
                            Snackbar.LENGTH_LONG)
                            .show()
                } else {
                    // Permission denied.
                    Snackbar.make(layout_open_chat_root, "Permissions denied.",
                            Snackbar.LENGTH_SHORT)
                            .show()
                }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Set this as true to restart auto-background detection.
        // This means that you will be automatically disconnected from SendBird when your
        // app enters the background.
        SendBird.setAutoBackgroundDetection(true)

        if (requestCode == INTENT_REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.d(LOG_TAG, "data is null!")
                return
            }
            showUploadConfirmDialog(data.data)
        }
    }

    override fun onResume() {
        super.onResume()

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionManager.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                if (reconnect) {
                    refresh()
                } else {
                    refreshFirst()
                }
            }
        })

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                // Add new message to view
                if (baseChannel.url == mChannelUrl) {
                    mChatAdapter!!.addFirst(baseMessage)
                }
            }

            override fun onMessageDeleted(baseChannel: BaseChannel?, msgId: Long) {
                super.onMessageDeleted(baseChannel, msgId)
                if (baseChannel!!.url == mChannelUrl) {
                    mChatAdapter!!.delete(msgId)
                }
            }

            override fun onMessageUpdated(channel: BaseChannel?, message: BaseMessage?) {
                super.onMessageUpdated(channel, message)
                if (channel!!.url == mChannelUrl) {
                    mChatAdapter!!.update(message!!)
                }
            }
        })
    }


    override fun onPause() {
        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        super.onPause()
    }

    override fun onDestroyView() {
        mChannel?.exit(OpenChannel.OpenChannelExitHandler { e ->
            if (e != null) {
                // Error!
                e.printStackTrace()
                return@OpenChannelExitHandler
            }
        })

        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_open_chat, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if (id == R.id.action_open_chat_view_participants) {
            val intent = Intent(activity, ParticipantListActivity::class.java)
            intent.putExtra(EXTRA_CHANNEL_URL, mChannel!!.url)
            startActivity(intent)

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setUpChatAdapter() {
        mChatAdapter = OpenChatAdapter(activity!!)
        mChatAdapter!!.setOnItemClickListener(object : OpenChatAdapter.OnItemClickListener {
            override fun onUserMessageItemClick(message: UserMessage) {}

            override fun onFileMessageItemClick(message: FileMessage) {
                onFileMessageClicked(message)
            }

            override fun onAdminMessageItemClick(message: AdminMessage) {}
        })

        mChatAdapter!!.setOnItemLongClickListener(object : OpenChatAdapter.OnItemLongClickListener {
            override fun onBaseMessageLongClick(message: BaseMessage, position: Int) {
                showMessageOptionsDialog(message, position)
            }
        })
    }

    private fun showMessageOptionsDialog(message: BaseMessage, position: Int) {
        val options = arrayOf("Edit message", "Delete message")

        val builder = AlertDialog.Builder(activity!!)
        builder.setItems(options) { dialog, which ->
            if (which == 0) {
                setState(STATE_EDIT, message, position)
            } else if (which == 1) {
                deleteMessage(message)
            }
        }
        builder.create().show()
    }

    private fun setState(state: Int, editingMessage: BaseMessage?, position: Int) {
        when (state) {
            STATE_NORMAL -> {
                mCurrentState = STATE_NORMAL
                mEditingMessage = null

                button_open_channel_chat_upload.visibility = View.VISIBLE
                button_open_channel_chat_send.text = "SEND"
                edittext_chat_message.setText("")
            }

            STATE_EDIT -> {
                mCurrentState = STATE_EDIT
                mEditingMessage = editingMessage

                button_open_channel_chat_upload.visibility = View.GONE
                button_open_channel_chat_send.text = "SAVE"
                var messageString: String? = (editingMessage as UserMessage).message
                if (messageString == null) {
                    messageString = ""
                }
                edittext_chat_message.setText(messageString)
                if (messageString.length > 0) {
                    edittext_chat_message.setSelection(0, messageString.length)
                }

                edittext_chat_message.requestFocus()
                edittext_chat_message.postDelayed({
                    mIMM!!.showSoftInput(edittext_chat_message, 0)

                    recycler_open_channel_chat.postDelayed({ recycler_open_channel_chat.scrollToPosition(position) }, 500)
                }, 100)
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (context as OpenChannelActivity).setOnBackPressedListener(object : OpenChannelActivity.onBackPressedListener {
            override fun onBack(): Boolean {
                if (mCurrentState == STATE_EDIT) {
                    setState(STATE_NORMAL, null, -1)
                    return true
                }

                mIMM!!.hideSoftInputFromWindow(edittext_chat_message.windowToken, 0)
                return false
            }
        })
    }

    private fun setUpRecyclerView() {
        mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager!!.reverseLayout = true
        recycler_open_channel_chat.layoutManager = mLayoutManager
        recycler_open_channel_chat.adapter = mChatAdapter

        // Load more messages when user reaches the top of the current message list.
        recycler_open_channel_chat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {

                if (mLayoutManager!!.findLastVisibleItemPosition() == mChatAdapter!!.itemCount - 1) {
                    loadNextMessageList(CHANNEL_LIST_LIMIT)
                }
                Log.v(LOG_TAG, "onScrollStateChanged")
            }
        })
    }

    private fun onFileMessageClicked(message: FileMessage) {
        val type = message.type.toLowerCase()
        if (type.startsWith("image")) {
            val i = Intent(activity, PhotoViewerActivity::class.java)
            i.putExtra("url", message.url)
            i.putExtra("type", message.type)
            startActivity(i)
        } else if (type.startsWith("video")) {
            val intent = Intent(activity, MediaPlayerActivity::class.java)
            intent.putExtra("url", message.url)
            startActivity(intent)
        } else {
            showDownloadConfirmDialog(message)
        }
    }

    private fun showDownloadConfirmDialog(message: FileMessage) {

        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If storage permissions are not granted, request permissions at run-time,
            // as per < API 23 guidelines.
            requestStoragePermissions()
        } else {
            AlertDialog.Builder(activity!!)
                    .setMessage("Download file?")
                    .setPositiveButton(R.string.download) { dialog, which ->
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            FileUtils.downloadFile(activity!!, message.url, message.name)
                        }
                    }
                    .setNegativeButton(R.string.cancel, null).show()
        }

    }

    private fun showUploadConfirmDialog(uri: Uri?) {
        AlertDialog.Builder(activity!!)
                .setMessage("Upload file?")
                .setPositiveButton(R.string.upload) { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {

                        // Specify two dimensions of thumbnails to generate
                        val thumbnailSizes = ArrayList<FileMessage.ThumbnailSize>()
                        thumbnailSizes.add(FileMessage.ThumbnailSize(240, 240))
                        thumbnailSizes.add(FileMessage.ThumbnailSize(320, 320))

                        sendImageWithThumbnail(uri, thumbnailSizes)
                    }
                }
                .setNegativeButton(R.string.cancel, null).show()
    }

    private fun requestImage() {
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If storage permissions are not granted, request permissions at run-time,
            // as per < API 23 guidelines.
            requestStoragePermissions()
        } else {
            val intent = Intent()
            // Show only images, no videos or anything else
            intent.type = "image/* video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Media"), INTENT_REQUEST_CHOOSE_IMAGE)

            // Set this as false to maintain connection
            // even when an external Activity is started.
            SendBird.setAutoBackgroundDetection(false)
        }
    }

    private fun requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(layout_open_chat_root, "Storage access permissions are required to upload/download files.",
                    Snackbar.LENGTH_LONG)
                    .setAction("Okay") {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                PERMISSION_WRITE_EXTERNAL_STORAGE)
                    }
                    .show()
        } else {
            // Permission has not been granted yet. Request it directly.
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun refreshFirst() {
        enterChannel(mChannelUrl)
    }

    /**
     * Enters an Open Channel.
     *
     *
     * A user must successfully enter a channel before being able to load or send messages
     * within the channel.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    private fun enterChannel(channelUrl: String?) {
        OpenChannel.getChannel(channelUrl, OpenChannel.OpenChannelGetHandler { openChannel, e ->
            if (e != null) {
                // Error!
                e.printStackTrace()
                return@OpenChannelGetHandler
            }

            // Enter the channel
            openChannel.enter(OpenChannel.OpenChannelEnterHandler { e ->
                if (e != null) {
                    // Error!
                    e.printStackTrace()
                    return@OpenChannelEnterHandler
                }

                mChannel = openChannel

                if (activity != null) {
                    // Set action bar title to name of channel
                    (activity as OpenChannelActivity).setActionBarTitle(mChannel!!.name)
                }

                refresh()
            })
        })
    }

    private fun sendUserMessage(text: String) {
        mChannel!!.sendUserMessage(text, BaseChannel.SendUserMessageHandler { userMessage, e ->
            if (e != null) {
                // Error!
                Log.e(LOG_TAG, e.toString())
                Toast.makeText(
                        activity,
                        "Send failed with error " + e.code + ": " + e.message, Toast.LENGTH_SHORT)
                        .show()
                return@SendUserMessageHandler
            }

            // Display sent message to RecyclerView
            mChatAdapter!!.addFirst(userMessage)
        })
    }

    /**
     * Sends a File Message containing an image file.
     * Also requests thumbnails to be generated in specified sizes.
     *
     * @param uri The URI of the image, which in this case is received through an Intent request.
     */
    private fun sendImageWithThumbnail(uri: Uri?, thumbnailSizes: List<FileMessage.ThumbnailSize>) {
        val info = FileUtils.getFileInfo(activity!!, uri!!)
        val path = info!!["path"] as String
        val file = File(path)
        val name = file.name
        val mime = info["mime"] as String
        val size = info["size"] as Int

        if (path == "") {
            Toast.makeText(activity, "File must be located in local storage.", Toast.LENGTH_LONG).show()
        } else {
            // Send image with thumbnails in the specified dimensions
            mChannel!!.sendFileMessage(file, name, mime, size, "", null, thumbnailSizes, BaseChannel.SendFileMessageHandler { fileMessage, e ->
                if (e != null) {
                    Toast.makeText(activity, "" + e.code + ":" + e.message, Toast.LENGTH_SHORT).show()
                    return@SendFileMessageHandler
                }

                mChatAdapter!!.addFirst(fileMessage)
            })
        }
    }

    private fun refresh() {
        loadInitialMessageList(CHANNEL_LIST_LIMIT)
    }

    /**
     * Replaces current message list with new list.
     * Should be used only on initial load.
     */
    private fun loadInitialMessageList(numMessages: Int) {

        mPrevMessageListQuery = mChannel!!.createPreviousMessageListQuery()
        mPrevMessageListQuery!!.load(numMessages, true, PreviousMessageListQuery.MessageListQueryResult { list, e ->
            if (e != null) {
                // Error!
                e.printStackTrace()
                return@MessageListQueryResult
            }

            mChatAdapter!!.setMessageList(list)
        })

    }

    /**
     * Loads messages and adds them to current message list.
     *
     *
     * A PreviousMessageListQuery must have been already initialized through [.loadInitialMessageList]
     */
    @Throws(NullPointerException::class)
    private fun loadNextMessageList(numMessages: Int) {

        if (mChannel == null) {
            throw NullPointerException("Current channel instance is null.")
        }

        if (mPrevMessageListQuery == null) {
            throw NullPointerException("Current query instance is null.")
        }

        mPrevMessageListQuery!!.load(numMessages, true, PreviousMessageListQuery.MessageListQueryResult { list, e ->
            if (e != null) {
                // Error!
                e.printStackTrace()
                return@MessageListQueryResult
            }

            for (message in list) {
                mChatAdapter!!.addLast(message)
            }
        })
    }

    private fun editMessage(message: BaseMessage?, editedMessage: String) {
        mChannel!!.updateUserMessage(message!!.messageId, editedMessage, null, null, BaseChannel.UpdateUserMessageHandler { userMessage, e ->
            if (e != null) {
                // Error!
                Toast.makeText(activity, "Error " + e.code + ": " + e.message, Toast.LENGTH_SHORT).show()
                return@UpdateUserMessageHandler
            }

            refresh()
        })
    }

    /**
     * Deletes a message within the channel.
     * Note that users can only delete messages sent by oneself.
     *
     * @param message The message to delete.
     */
    private fun deleteMessage(message: BaseMessage) {
        mChannel!!.deleteMessage(message, BaseChannel.DeleteMessageHandler { e ->
            if (e != null) {
                // Error!
                Toast.makeText(activity, "Error " + e.code + ": " + e.message, Toast.LENGTH_SHORT).show()
                return@DeleteMessageHandler
            }

            refresh()
        })
    }

    companion object {

        private val LOG_TAG = OpenChatFragment::class.java.simpleName

        private val CHANNEL_LIST_LIMIT = 30
        private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT"
        private val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT"

        private val STATE_NORMAL = 0
        private val STATE_EDIT = 1

        private val INTENT_REQUEST_CHOOSE_IMAGE = 300
        private val PERMISSION_WRITE_EXTERNAL_STORAGE = 13

        internal val EXTRA_CHANNEL_URL = "CHANNEL_URL"

        /**
         * To create an instance of this fragment, a Channel URL should be passed.
         */
        fun newInstance(channelUrl: String): OpenChatFragment {
            val fragment = OpenChatFragment()

            val args = Bundle()
            args.putString(OpenChannelListFragment.EXTRA_OPEN_CHANNEL_URL, channelUrl)
            fragment.arguments = args

            return fragment
        }
    }
}
