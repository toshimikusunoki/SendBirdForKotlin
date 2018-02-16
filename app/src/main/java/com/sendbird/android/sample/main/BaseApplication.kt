package com.sendbird.android.sample.main

import android.app.Application
import com.sendbird.android.SendBird

/**
 * Created by toshimikusunoki on 2018/02/16.
 */
class BaseApplication : Application() {

    companion object {
        const val VERSION = "3.0.50"
    }

    private  val APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23"

    override fun onCreate() {
        super.onCreate()
        SendBird.init(APP_ID, applicationContext)
    }
}