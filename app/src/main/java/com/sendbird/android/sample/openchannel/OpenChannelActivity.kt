package com.sendbird.android.sample.openchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sendbird.android.sample.R
import kotlinx.android.synthetic.main.activity_open_channel.*

class OpenChannelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_channel)
        setSupportActionBar(toolbar_open_channel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)
    }
}
