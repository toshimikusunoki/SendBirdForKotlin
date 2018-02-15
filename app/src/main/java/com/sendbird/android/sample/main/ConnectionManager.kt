package com.sendbird.android.sample.main

import com.sendbird.android.SendBird

/**
 * Created by toshimikusunoki on 2018/02/16.
 */
object ConnectionManager {

    fun login(userId: String, handler: SendBird.ConnectHandler?) {
        SendBird.connect(userId) { user, e ->
            handler?.onConnected(user, e)
        }
    }

    fun logout(handler: SendBird.DisconnectHandler?) {
        SendBird.disconnect {
            handler?.onDisconnected()
        }
    }

    fun addConnectionManagementHandler(userId: String, handlerId: String, handler: ConnectionManagementHandler?) {
        SendBird.addConnectionHandler(handlerId, object : SendBird.ConnectionHandler {
            override fun onReconnectStarted() {}

            override fun onReconnectSucceeded() {
                handler?.onConnected(true)
            }

            override fun onReconnectFailed() {}
        })

        if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
            handler?.onConnected(false)
        } else if (SendBird.getConnectionState() == SendBird.ConnectionState.CLOSED) { // push notification or system kill
            SendBird.connect(userId, SendBird.ConnectHandler { user, e ->
                if (e != null) {
                    return@ConnectHandler
                }

                handler?.onConnected(false)
            })
        }
    }

    fun removeConnectionManagementHandler(handlerId: String) {
        SendBird.removeConnectionHandler(handlerId)
    }

    interface ConnectionManagementHandler {
        /**
         * A callback for when connected or reconnected to refresh.
         *
         * @param reconnect Set false if connected, true if reconnected.
         */
        fun onConnected(reconnect: Boolean)
    }
}