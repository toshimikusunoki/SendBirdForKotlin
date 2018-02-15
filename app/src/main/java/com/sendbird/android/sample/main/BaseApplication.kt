package com.sendbird.android.sample.main

import android.app.Application
import com.sendbird.android.SendBird

/**
 * Created by toshimikusunoki on 2018/02/16.
 */
class BaseApplication : Application() {

    private  val APP_ID = "2145EB0A-1DAD-4FA9-94E8-A364EDDEB6EE"
    val VERSION = "3.0.50"

    override fun onCreate() {
        super.onCreate()
        SendBird.init(APP_ID, applicationContext)
    }
}