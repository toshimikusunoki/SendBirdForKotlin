package com.sendbird.android.sample.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtils {

    private val PREFERENCE_KEY_USER_ID = "userId"
    private val PREFERENCE_KEY_NICKNAME = "nickname"
    private val PREFERENCE_KEY_PROFILE_URL = "profileUrl"
    private val PREFERENCE_KEY_CONNECTED = "connected"

    private val PREFERENCE_KEY_NOTIFICATIONS = "notifications"
    private val PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS = "notificationsShowPreviews"
    private val PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB = "notificationsDoNotDisturb"
    private val PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM = "notificationsDoNotDisturbFrom"
    private val PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO = "notificationsDoNotDisturbTo"
    private val PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT = "channelDistinct"

    private var mAppContext: Context? = null

    private val sharedPreferences: SharedPreferences
        get() = mAppContext!!.getSharedPreferences("sendbird", Context.MODE_PRIVATE)

    var userId: String
        get() = sharedPreferences.getString(PREFERENCE_KEY_USER_ID, "")
        set(userId) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_USER_ID, userId).apply()
        }

    var nickname: String
        get() = sharedPreferences.getString(PREFERENCE_KEY_NICKNAME, "")
        set(nickname) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_NICKNAME, nickname).apply()
        }

    var profileUrl: String
        get() = sharedPreferences.getString(PREFERENCE_KEY_PROFILE_URL, "")
        set(profileUrl) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_PROFILE_URL, profileUrl).apply()
        }

    var connected: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_KEY_CONNECTED, false)
        set(tf) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply()
        }

    var notifications: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS, true)
        set(notifications) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS, notifications).apply()
        }

    var notificationsShowPreviews: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, true)
        set(notificationsShowPreviews) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, notificationsShowPreviews).apply()
        }

    var notificationsDoNotDisturb: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB, false)
        set(notificationsDoNotDisturb) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB, notificationsDoNotDisturb).apply()
        }

    var notificationsDoNotDisturbFrom: String
        get() = sharedPreferences.getString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM, "")
        set(notificationsDoNotDisturbFrom) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM, notificationsDoNotDisturbFrom).apply()
        }

    var notificationsDoNotDisturbTo: String
        get() = sharedPreferences.getString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO, "")
        set(notificationsDoNotDisturbTo) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO, notificationsDoNotDisturbTo).apply()
        }

    var groupChannelDistinct: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT, true)
        set(channelDistinct) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT, channelDistinct).apply()
        }

    fun init(appContext: Context) {
        mAppContext = appContext
    }

    fun clearAll() {
        val editor = sharedPreferences.edit()
        editor.clear().apply()
    }
}// Prevent instantiation
