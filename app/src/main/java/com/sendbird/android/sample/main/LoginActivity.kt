package com.sendbird.android.sample.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.sendbird.android.SendBird
import com.sendbird.android.sample.R
import com.sendbird.android.sample.utils.PreferenceUtils
import com.sendbird.android.sample.utils.PushUtils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edittext_login_user_id.setText(PreferenceUtils.getUserId(this))
        edittext_login_user_nickname.setText(PreferenceUtils.getNickname(this))

        button_login_connect.setOnClickListener( {
            var userId = edittext_login_user_id.text.toString()
            // Remove all spaces from userID
            userId = userId.replace("\\s".toRegex(), "")
            val userNickname = edittext_login_user_nickname.text.toString()

            PreferenceUtils.setUserId(this@LoginActivity, userId)
            PreferenceUtils.setNickname(this@LoginActivity, userNickname)

            connectToSendBird(userId, userNickname)
        })
    }


    /**
     * Attempts to connect a user to SendBird.
     * @param userId    The unique ID of the user.
     * @param userNickname  The user's nickname, which will be displayed in chats.
     */
    private fun connectToSendBird(userId: String, userNickname: String) {
        // Show the loading indicator
        progress_bar_login.show()
        button_login_connect.isEnabled = false

        ConnectionManager.login(userId, SendBird.ConnectHandler { user, e ->
            // Callback received; hide the progress bar.
            progress_bar_login.hide()

            if (e != null) {
                // Error!
                Toast.makeText(
                        this@LoginActivity, "" + e.code + ": " + e.message,
                        Toast.LENGTH_SHORT)
                        .show()

                // Show login failure snackbar
                showSnackbar("Login to SendBird failed")
                button_login_connect.isEnabled = true
                PreferenceUtils.setConnected(this@LoginActivity, false)
                return@ConnectHandler
            }

            PreferenceUtils.setNickname(this@LoginActivity, user.nickname)
            PreferenceUtils.setProfileUrl(this@LoginActivity, user.profileUrl)
            PreferenceUtils.setConnected(this@LoginActivity, true)

            // Update the user's nickname
            updateCurrentUserInfo(userNickname)
            updateCurrentUserPushToken()

            // Proceed to MainActivity
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        })
    }

    /**
     * Update the user's push token.
     */
    private fun updateCurrentUserPushToken() {
        PushUtils.registerPushTokenForCurrentUser(this@LoginActivity, null)
    }

    /**
     * Updates the user's nickname.
     * @param userNickname  The new nickname of the user.
     */
    private fun updateCurrentUserInfo(userNickname: String) {
        SendBird.updateCurrentUserInfo(userNickname, null, SendBird.UserInfoUpdateHandler { e ->
            if (e != null) {
                // Error!
                Toast.makeText(
                        this@LoginActivity, "" + e.code + ":" + e.message,
                        Toast.LENGTH_SHORT)
                        .show()

                // Show update failed snackbar
                showSnackbar("Update user nickname failed")

                return@UserInfoUpdateHandler
            }

            PreferenceUtils.setNickname(this@LoginActivity, userNickname)
        })
    }

    // Displays a Snackbar from the bottom of the screen
    private fun showSnackbar(text: String) {
        val snackbar = Snackbar.make(layout_login, text, Snackbar.LENGTH_SHORT)
        snackbar.show()
    }
}
