package com.sendbird.android.sample.groupchannel

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.sample.R
import kotlinx.android.synthetic.main.fragment_select_distinct.*


/**
 * A fragment displaying an option to set the channel as Distinct.
 */
class SelectDistinctFragment : Fragment() {

    internal interface DistinctSelectedListener {
        fun onDistinctSelected(distinct: Boolean)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_distinct, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as CreateGroupChannelActivity).setState(CreateGroupChannelActivity.STATE_SELECT_DISTINCT)
        val mListener = activity as CreateGroupChannelActivity

        checkbox_select_distinct.isChecked = true
        checkbox_select_distinct.setOnCheckedChangeListener { buttonView, isChecked -> mListener.onDistinctSelected(isChecked) }
    }

    companion object {

        internal fun newInstance(): SelectDistinctFragment {
            return SelectDistinctFragment()
        }
    }
}
