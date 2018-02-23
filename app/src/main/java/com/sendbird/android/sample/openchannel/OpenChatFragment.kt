package com.sendbird.android.sample.openchannel

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sendbird.android.sample.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OpenChatFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [OpenChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OpenChatFragment : Fragment() {

    private var mChannelUrl: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mChannelUrl = arguments!!.getString(EXTRA_OPEN_CHANNEL_URL)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_open_chat, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val EXTRA_OPEN_CHANNEL_URL = "OPEN_CHANNEL_URL"

        /**
         *
         */
        fun newInstance(channelUrl: String): OpenChatFragment {
            val fragment = OpenChatFragment()
            val args = Bundle()
            args.putString(EXTRA_OPEN_CHANNEL_URL, channelUrl)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
