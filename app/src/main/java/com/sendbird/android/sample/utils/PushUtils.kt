package com.sendbird.android.sample.utils

import android.content.Context
import com.google.firebase.iid.FirebaseInstanceId
import com.sendbird.android.SendBird

/**
 * Created by toshimikusunoki on 2018/02/16.
 */
object PushUtils {

    fun registerPushTokenForCurrentUser(context: Context, handler: SendBird.RegisterPushTokenWithStatusHandler?) {
        SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().token, handler)
    }

    fun unregisterPushTokenForCurrentUser(context: Context, handler: SendBird.UnregisterPushTokenHandler) {
        SendBird.unregisterPushTokenForCurrentUser(FirebaseInstanceId.getInstance().token, handler)
    }

}