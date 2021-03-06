package com.sendbird.android.sample.openchannel

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.OpenChannel
import com.sendbird.android.OpenChannelListQuery
import com.sendbird.android.sample.R
import com.sendbird.android.sample.main.ConnectionManager
import kotlinx.android.synthetic.main.fragment_open_channel_list.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OpenChannelListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [OpenChannelListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OpenChannelListFragment : Fragment() {


    private var mChannelListQuery: OpenChannelListQuery? = null
    private var mChannelListAdapter: OpenChannelListAdapter? = null

    private val INTENT_REQUEST_NEW_OPEN_CHANNEL = 402

    private val CHANNEL_LIST_LIMIT = 15
    private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHANNEL_LIST"

    companion object {

        val EXTRA_OPEN_CHANNEL_URL = "OPEN_CHANNEL_URL"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OpenChannelListFragment.
         */
        fun newInstance(): OpenChannelListFragment {
            return  OpenChannelListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_open_channel_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        retainInstance = true
        setHasOptionsMenu(true)
        (activity as OpenChannelActivity).setActionBarTitle(resources.getString(R.string.all_open_channels))

        mChannelListAdapter = OpenChannelListAdapter(context!!)

        // Swipe down to refresh channel list.
        swipe_layout_open_channel_list.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            swipe_layout_open_channel_list.isRefreshing = true
            refresh()
        })

        fab_open_channel_list.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, CreateOpenChannelActivity::class.java)
            startActivityForResult(intent, INTENT_REQUEST_NEW_OPEN_CHANNEL)
        })

        setUpRecyclerView()
        setUpChannelListAdapter()
    }

    override fun onResume() {
        super.onResume()

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionManager.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                refresh()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == INTENT_REQUEST_NEW_OPEN_CHANNEL) {
            if (resultCode == RESULT_OK) {
                refresh()
            }
        }
    }

    internal fun setUpRecyclerView() {
        val mLayoutManager = LinearLayoutManager(context)
        recycler_open_channel_list.layoutManager = mLayoutManager
        recycler_open_channel_list.adapter = mChannelListAdapter
        recycler_open_channel_list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        // If user scrolls to bottom of the list, loads more channels.
        recycler_open_channel_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter!!.getItemCount() - 1) {
                    loadNextChannelList()
                }
            }
        })
    }

    // Set touch listeners to RecyclerView items
    private fun setUpChannelListAdapter() {
        mChannelListAdapter?.setOnItemClickListener(object : OpenChannelListAdapter.OnItemClickListener {
            override fun onItemClick(channel: OpenChannel) {
                val fragment = OpenChatFragment.newInstance(channel.url)
                fragmentManager!!.beginTransaction()
                        .replace(R.id.container_open_channel, fragment)
                        .addToBackStack(null)
                        .commit()
            }
        })

        mChannelListAdapter?.setOnItemLongClickListener(object : OpenChannelListAdapter.OnItemLongClickListener {
            override fun onItemLongPress(channel: OpenChannel) {}
        })
    }

    private fun refresh() {
        refreshChannelList(CHANNEL_LIST_LIMIT)
    }

    /**
     * Creates a new query to get the list of the user's Open Channels,
     * then replaces the existing dataset.
     *
     * @param numChannels   The number of channels to load.
     */
    internal fun refreshChannelList(numChannels: Int) {
        mChannelListQuery = OpenChannel.createOpenChannelListQuery()
        mChannelListQuery?.setLimit(numChannels)
        mChannelListQuery?.next(OpenChannelListQuery.OpenChannelListQueryResultHandler { list, e ->
            if (e != null) {
                e.printStackTrace()
                return@OpenChannelListQueryResultHandler
            }

            mChannelListAdapter?.setOpenChannelList(list)

            if (swipe_layout_open_channel_list.isRefreshing) {
                swipe_layout_open_channel_list.isRefreshing = false
            }
        })
    }

    /**
     * Loads the next channels from the current query instance.
     */
    internal fun loadNextChannelList() {
        mChannelListQuery?.next(OpenChannelListQuery.OpenChannelListQueryResultHandler { list, e ->
            if (e != null) {
                e.printStackTrace()
                return@OpenChannelListQueryResultHandler
            }

            for (channel in list) {
                mChannelListAdapter?.addLast(channel)
            }
        })
    }
}
