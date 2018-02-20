package com.sendbird.android.sample.openchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.sendbird.android.sample.R
import kotlinx.android.synthetic.main.activity_open_channel.*

class OpenChannelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_channel)
        setSupportActionBar(toolbar_open_channel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)

        if (savedInstanceState == null) {
            val fragment = OpenChannelListFragment.newInstance()
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container_open_channel, fragment)
                    .commit()
        }
    }

    interface onBackPressedListener {
        fun onBack(): Boolean
    }

    private var mOnBackPressedListener: onBackPressedListener? = null

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
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    internal fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }
}
