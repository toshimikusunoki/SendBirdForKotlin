package com.sendbird.android.sample.main

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sendbird.android.SendBird
import com.sendbird.android.sample.R
import com.sendbird.android.sample.utils.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File
import java.util.*


class SettingsActivity : AppCompatActivity() {

    private var mCalendar: Calendar? = null

    private var mNickNameChanged = false

    private var mRequestingCamera = false
    private var mTempPhotoUri: Uri? = null

    private var mCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val mIMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mCalendar = Calendar.getInstance(Locale.getDefault())

        setSupportActionBar(toolbar_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_window_close_white_24_dp)

        linear_layout_blocked_members_list.setOnClickListener {
            val intent = Intent(this@SettingsActivity, BlockedMembersListActivity::class.java)
            startActivity(intent)
        }

        //+ ProfileUrl
        val profileUrl = PreferenceUtils.profileUrl
        if (profileUrl.length > 0) {
            ImageUtils.displayRoundImageFromUrl(this@SettingsActivity, profileUrl, image_view_profile)
        }
        image_view_profile.setOnClickListener { showSetProfileOptionsDialog() }
        //- ProfileUrl

        //+ Nickname
        edit_text_nickname.isEnabled = false
        val nickname = PreferenceUtils.nickname
        if (nickname.length > 0) {
            edit_text_nickname.setText(nickname)
        }
        edit_text_nickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 0 && s.toString() != nickname) {
                    mNickNameChanged = true
                }
            }
        })
        button_save_nickname.setOnClickListener {
            if (edit_text_nickname.isEnabled) {
                if (mNickNameChanged) {
                    mNickNameChanged = false

                    updateCurrentUserInfo(edit_text_nickname.text.toString())
                }

                button_save_nickname.text = "EDIT"
                edit_text_nickname.isEnabled = false
                edit_text_nickname.isFocusable = false
                edit_text_nickname.isFocusableInTouchMode = false
            } else {
                button_save_nickname.text = "SAVE"
                edit_text_nickname.isEnabled = true
                edit_text_nickname.isFocusable = true
                edit_text_nickname.isFocusableInTouchMode = true
                if (edit_text_nickname.text != null && edit_text_nickname.text.length > 0) {
                    edit_text_nickname.setSelection(0, edit_text_nickname.text.length)
                }
                edit_text_nickname.requestFocus()
                edit_text_nickname.postDelayed({ mIMM!!.showSoftInput(edit_text_nickname, 0) }, 100)
            }
        }
        //- Nickname

        //+ Notifications
        val notifications = PreferenceUtils.notifications
        switch_notifications.isChecked = notifications
        switch_notifications_show_previews.isChecked = PreferenceUtils.notificationsShowPreviews
        checkNotifications(notifications)

        val doNotDisturb = PreferenceUtils.notificationsDoNotDisturb
        switch_notifications_do_not_disturb.isChecked = doNotDisturb
        checkDoNotDisturb(doNotDisturb)

        switch_notifications.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                PushUtils.registerPushTokenForCurrentUser(this@SettingsActivity, SendBird.RegisterPushTokenWithStatusHandler { pushTokenRegistrationStatus, e ->
                    if (e != null) {
                        switch_notifications.isChecked = !isChecked
                        checkNotifications(!isChecked)
                        return@RegisterPushTokenWithStatusHandler
                    }

                    PreferenceUtils.notifications = isChecked
                    checkNotifications(isChecked)
                })
            } else {
                PushUtils.unregisterPushTokenForCurrentUser(this@SettingsActivity, SendBird.UnregisterPushTokenHandler { e ->
                    if (e != null) {
                        switch_notifications.isChecked = !isChecked
                        checkNotifications(!isChecked)
                        return@UnregisterPushTokenHandler
                    }

                    PreferenceUtils.notifications = isChecked
                    checkNotifications(isChecked)
                })
            }
        }

        switch_notifications_show_previews.setOnCheckedChangeListener { buttonView, isChecked -> PreferenceUtils.notificationsShowPreviews = isChecked }

        mCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> saveDoNotDisturb(isChecked) }

        switch_notifications_do_not_disturb.setOnCheckedChangeListener(mCheckedChangeListener)

        linear_layout_notifications_do_not_disturb_from.setOnClickListener { setDoNotDisturbTime(true, text_view_notifications_do_not_disturb_to) }

        linear_layout_notifications_do_not_disturb_to.setOnClickListener { setDoNotDisturbTime(false, text_view_notifications_do_not_disturb_to) }

        SendBird.getDoNotDisturb { isDoNotDisturbOn, startHour, startMin, endHour, endMin, timezone, e ->
            mCalendar!!.clear()
            mCalendar!!.set(Calendar.HOUR_OF_DAY, startHour)
            mCalendar!!.set(Calendar.MINUTE, startMin)
            val fromMillis = mCalendar!!.timeInMillis

            PreferenceUtils.notificationsDoNotDisturbFrom = fromMillis.toString()
            text_view_notifications_do_not_disturb_to.text = DateUtils.formatTimeWithMarker(fromMillis)

            mCalendar!!.clear()
            mCalendar!!.set(Calendar.HOUR_OF_DAY, endHour)
            mCalendar!!.set(Calendar.MINUTE, endMin)
            val toMillis = mCalendar!!.timeInMillis

            PreferenceUtils.notificationsDoNotDisturbTo = toMillis.toString()
            text_view_notifications_do_not_disturb_to.text = DateUtils.formatTimeWithMarker(toMillis)

            switch_notifications_do_not_disturb.isChecked = isDoNotDisturbOn
        }
        //- Notifications

        //+ Group Channel Distinct
        checkbox_make_group_channel_distinct.isChecked = PreferenceUtils.groupChannelDistinct
        checkbox_make_group_channel_distinct.setOnCheckedChangeListener { buttonView, isChecked -> PreferenceUtils.groupChannelDistinct = isChecked }
        //- Group Channel Distinct
    }

    private fun saveDoNotDisturb(doNotDisturb: Boolean) {
        val doNotDisturbFrom = PreferenceUtils.notificationsDoNotDisturbFrom
        val doNotDisturbTo = PreferenceUtils.notificationsDoNotDisturbTo
        if (doNotDisturbFrom.length > 0 && doNotDisturbTo.length > 0) {
            val startHour = DateUtils.getHourOfDay(java.lang.Long.valueOf(doNotDisturbFrom)!!)
            val startMin = DateUtils.getMinute(java.lang.Long.valueOf(doNotDisturbFrom)!!)
            val endHour = DateUtils.getHourOfDay(java.lang.Long.valueOf(doNotDisturbTo)!!)
            val endMin = DateUtils.getMinute(java.lang.Long.valueOf(doNotDisturbTo)!!)

            SendBird.setDoNotDisturb(doNotDisturb, startHour, startMin, endHour, endMin, TimeZone.getDefault().id, SendBird.SetDoNotDisturbHandler { e ->
                if (e != null) {
                    switch_notifications_do_not_disturb.setOnCheckedChangeListener(null)
                    switch_notifications_do_not_disturb.isChecked = !doNotDisturb
                    switch_notifications_do_not_disturb.setOnCheckedChangeListener(mCheckedChangeListener)

                    PreferenceUtils.notificationsDoNotDisturb = !doNotDisturb
                    checkDoNotDisturb(!doNotDisturb)
                    return@SetDoNotDisturbHandler
                }

                switch_notifications_do_not_disturb.setOnCheckedChangeListener(null)
                switch_notifications_do_not_disturb.isChecked = doNotDisturb
                switch_notifications_do_not_disturb.setOnCheckedChangeListener(mCheckedChangeListener)

                PreferenceUtils.notificationsDoNotDisturb = doNotDisturb
                checkDoNotDisturb(doNotDisturb)
            })
        }
    }

    private fun setDoNotDisturbTime(from: Boolean, textView: TextView?) {
        var timeMillis = System.currentTimeMillis()
        if (from) {
            val doNotDisturbFrom = PreferenceUtils.notificationsDoNotDisturbFrom
            if (doNotDisturbFrom.length > 0) {
                timeMillis = java.lang.Long.valueOf(doNotDisturbFrom)!!
            }
        } else {
            val doNotDisturbTo = PreferenceUtils.notificationsDoNotDisturbTo
            if (doNotDisturbTo.length > 0) {
                timeMillis = java.lang.Long.valueOf(doNotDisturbTo)!!
            }
        }

        TimePickerDialog(this@SettingsActivity, TimePickerDialog.OnTimeSetListener { timePicker, hour, min ->
            mCalendar!!.clear()
            mCalendar!!.set(Calendar.HOUR_OF_DAY, hour)
            mCalendar!!.set(Calendar.MINUTE, min)
            val millis = mCalendar!!.timeInMillis

            if (from) {
                if (millis.toString() != PreferenceUtils.notificationsDoNotDisturbFrom) {
                    PreferenceUtils.notificationsDoNotDisturbFrom = millis.toString()
                    saveDoNotDisturb(true)
                }
            } else {
                if (millis.toString() != PreferenceUtils.notificationsDoNotDisturbTo) {
                    PreferenceUtils.notificationsDoNotDisturbTo = millis.toString()
                    saveDoNotDisturb(true)
                }
            }
            textView!!.text = DateUtils.formatTimeWithMarker(millis)
        }, DateUtils.getHourOfDay(timeMillis), DateUtils.getMinute(timeMillis), true).show()
    }

    private fun checkNotifications(notifications: Boolean) {
        if (notifications) {
            linear_layout_notifications.visibility = View.VISIBLE
            val doNotDisturb = PreferenceUtils.notificationsDoNotDisturb
            checkDoNotDisturb(doNotDisturb)
        } else {
            linear_layout_notifications.visibility = View.GONE
        }
    }

    private fun checkDoNotDisturb(doNotDisturb: Boolean) {
        if (doNotDisturb) {
            linear_layout_do_not_disturb.visibility = View.VISIBLE
        } else {
            linear_layout_do_not_disturb.visibility = View.GONE
        }

        val doNotDisturbFrom = PreferenceUtils.notificationsDoNotDisturbFrom
        if (doNotDisturbFrom.length > 0) {
            text_view_notifications_do_not_disturb_from.text = DateUtils.formatTimeWithMarker(java.lang.Long.valueOf(doNotDisturbFrom)!!)
        } else {
            text_view_notifications_do_not_disturb_from.text = ""
        }

        val doNotDisturbTo = PreferenceUtils.notificationsDoNotDisturbTo
        if (doNotDisturbTo.length > 0) {
            text_view_notifications_do_not_disturb_to.text = DateUtils.formatTimeWithMarker(java.lang.Long.valueOf(doNotDisturbTo)!!)
        } else {
            text_view_notifications_do_not_disturb_to.text = ""
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSetProfileOptionsDialog() {
        val options = arrayOf("Upload a photo", "Take a photo")

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle("Set profile image")
                .setItems(options) { dialog, which ->
                    if (which == 0) {
                        requestMedia()
                    } else if (which == 1) {
                        requestCamera()
                    }
                }
        builder.create().show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == Activity.RESULT_OK) {
            // If user has successfully chosen the image, show a dialog to confirm upload.
            if (data == null) {
                return
            }

            val uri = data.data
            val info = FileUtils.getFileInfo(this@SettingsActivity, uri!!)
            if (info != null) {
                val path = info["path"] as String
                if (path != null) {
                    val profileImage = File(path)
                    updateCurrentUserProfileImage(profileImage, image_view_profile)
                }
            }
        } else if (requestCode == INTENT_REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            if (!mRequestingCamera) {
                return
            }

            val profileImage = File(mTempPhotoUri!!.path)
            updateCurrentUserProfileImage(profileImage, image_view_profile)
            mRequestingCamera = false
        }

        // Set this as true to restore background connection management.
        SendBird.setAutoBackgroundDetection(true)
    }

    private fun requestMedia() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions(PERMISSION_WRITE_EXTERNAL_STORAGE_UPLOAD)
        } else {
            val intent = Intent()
            // Show only images, no videos or anything else
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Image"), INTENT_REQUEST_CHOOSE_MEDIA)

            // Set this as false to maintain connection
            // even when an external Activity is started.
            SendBird.setAutoBackgroundDetection(false)
        }
    }

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions(PERMISSION_WRITE_EXTERNAL_STORAGE_CAMERA)
        } else {
            mRequestingCamera = true
            mTempPhotoUri = getTempFileUri(false)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri)

            val resInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, mTempPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivityForResult(intent, INTENT_REQUEST_CAMERA)

            SendBird.setAutoBackgroundDetection(false)
        }
    }

    private fun getTempFileUri(doNotUseFileProvider: Boolean): Uri? {
        var uri: Uri? = null
        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val tempFile = File.createTempFile("SendBird_" + System.currentTimeMillis(), ".jpg", path)

            if (Build.VERSION.SDK_INT >= 24 && !doNotUseFileProvider) {
                uri = FileProvider.getUriForFile(this, "com.sendbird.android.sample.provider", tempFile)
            } else {
                uri = Uri.fromFile(tempFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return uri
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        SendBird.setAutoBackgroundDetection(true)

        when (requestCode) {
            PERMISSION_WRITE_EXTERNAL_STORAGE_UPLOAD -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestMedia()
            }

            PERMISSION_WRITE_EXTERNAL_STORAGE_CAMERA -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestCamera()
            }
        }
    }

    private fun requestStoragePermissions(code: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(layout_settings, "Storage access permissions are required to upload/download files.", Snackbar.LENGTH_LONG)
                    .setAction("OK") {
                        // Maintains connection.
                        SendBird.setAutoBackgroundDetection(false)
                        ActivityCompat.requestPermissions(
                                this@SettingsActivity,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                code
                        )
                    }
                    .show()
        } else {
            // Maintains connection.
            SendBird.setAutoBackgroundDetection(false)
            // Permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                    this@SettingsActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    code
            )
        }
    }

    private fun updateCurrentUserProfileImage(profileImage: File, imageView: ImageView?) {
        val nickname = PreferenceUtils.nickname
        SendBird.updateCurrentUserInfoWithProfileImage(nickname, profileImage, SendBird.UserInfoUpdateHandler { e ->
            if (e != null) {
                // Error!
                Toast.makeText(this@SettingsActivity, "" + e.code + ":" + e.message, Toast.LENGTH_SHORT).show()

                // Show update failed snackbar
                showSnackbar("Update user info failed")
                return@UserInfoUpdateHandler
            }

            try {
                PreferenceUtils.profileUrl = SendBird.getCurrentUser().profileUrl
                ImageUtils.displayRoundImageFromUrl(this@SettingsActivity, Uri.fromFile(profileImage).toString(), imageView)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
        })
    }

    private fun updateCurrentUserInfo(userNickname: String) {
        val profileUrl = PreferenceUtils.profileUrl
        SendBird.updateCurrentUserInfo(userNickname, profileUrl, SendBird.UserInfoUpdateHandler { e ->
            if (e != null) {
                // Error!
                Toast.makeText(this@SettingsActivity, "" + e.code + ":" + e.message, Toast.LENGTH_SHORT).show()

                // Show update failed snackbar
                showSnackbar("Update user info failed")
                return@UserInfoUpdateHandler
            }

            PreferenceUtils.nickname = userNickname
        })
    }

    private fun showSnackbar(text: String) {
        val snackbar = Snackbar.make(layout_settings, text, Snackbar.LENGTH_SHORT)
        snackbar.show()
    }

    companion object {

        private val INTENT_REQUEST_CHOOSE_MEDIA = 0xf0
        private val INTENT_REQUEST_CAMERA = 0xf1

        private val PERMISSION_WRITE_EXTERNAL_STORAGE_UPLOAD = 0xf0
        private val PERMISSION_WRITE_EXTERNAL_STORAGE_CAMERA = 0xf1
    }

}
