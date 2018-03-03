package com.sendbird.android.sample.groupchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.sendbird.android.sample.R


class GroupChannelActivity : AppCompatActivity() {
    private var mOnBackPressedListener: onBackPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_channel)

        val toolbar = findViewById<View>(R.id.toolbar_group_channel) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)

        if (savedInstanceState == null) {
            // Load list of Group Channels
            val fragment = GroupChannelListFragment.newInstance()

            val manager = supportFragmentManager
            manager.popBackStack()

            manager.beginTransaction()
                    .replace(R.id.container_group_channel, fragment)
                    .commit()
        }

        val channelUrl = intent.getStringExtra("groupChannelUrl")
        if (channelUrl != null) {
            // If started from notification
            val fragment = GroupChatFragment.newInstance(channelUrl)
            val manager = supportFragmentManager
            manager.beginTransaction()
                    .replace(R.id.container_group_channel, fragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    interface onBackPressedListener {
        fun onBack(): Boolean
    }

    fun setOnBackPressedListener(listener: onBackPressedListener) {
        mOnBackPressedListener = listener
    }

    override fun onBackPressed() {
        if (mOnBackPressedListener != null && mOnBackPressedListener!!.onBack()) {
            return
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    internal fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }
}
