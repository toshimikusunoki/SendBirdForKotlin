package com.sendbird.android.sample.openchannel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import com.sendbird.android.OpenChannel
import com.sendbird.android.sample.R
import kotlinx.android.synthetic.main.activity_create_open_channel.*

class CreateOpenChannelActivity : AppCompatActivity() {

    private var mIMM: InputMethodManager? = null
    private var enableCreate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_open_channel)

        mIMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setSupportActionBar(toolbar_create_open_channel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp)

        button_create_open_channel.setOnClickListener( {
            createOpenChannel(edittext_create_open_channel_name.text.toString())
        })
        button_create_open_channel.isEnabled = enableCreate
        edittext_create_open_channel_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 0) {
                    if (enableCreate) {
                        button_create_open_channel.isEnabled = false
                        enableCreate = false
                    }
                } else {
                    if (!enableCreate) {
                        button_create_open_channel.isEnabled = true
                        enableCreate = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createOpenChannel(name: String) {
        OpenChannel.createChannelWithOperatorUserIds(name, null, null, null, OpenChannel.OpenChannelCreateHandler { openChannel, e ->
            if (e != null) {
                // Error!
                return@OpenChannelCreateHandler
            }

            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        })
    }

    override fun onPause() {
        super.onPause()
        mIMM?.hideSoftInputFromWindow(edittext_create_open_channel_name.windowToken, 0)
    }
}
