package com.sendbird.android.sample.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sendbird.android.SendBird
import com.sendbird.android.sample.R
import com.sendbird.android.sample.openchannel.OpenChannelActivity
import com.sendbird.android.sample.utils.PreferenceUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)

        linear_layout_open_channels.setOnClickListener( {
            val intent = Intent(this@MainActivity, OpenChannelActivity::class.java)
            startActivity(intent)
        })
        button_disconnect.setOnClickListener { disconnect() }
        val sdkVersion = String.format(resources.getString(R.string.all_app_version),
                BaseApplication.VERSION, SendBird.getSDKVersion())
        text_main_versions.setText(sdkVersion)
    }

    /**
     * Unregisters all push tokens for the current user so that they do not receive any notifications,
     * then disconnects from SendBird.
     */
    private fun disconnect() {
        SendBird.unregisterPushTokenAllForCurrentUser { e ->
            if (e != null) {
                // Error!
                e.printStackTrace()

                // Don't return because we still need to disconnect.
            } else {
                //                    Toast.makeText(MainActivity.this, "All push tokens unregistered.", Toast.LENGTH_SHORT).show();
            }

            ConnectionManager.logout(SendBird.DisconnectHandler {
                PreferenceUtils.setConnected(this@MainActivity, false)
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                finish()
            })
        }
    }
}
