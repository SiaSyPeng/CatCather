package com.cs65.gnf.lab4;

import android.os.Bundle;
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent



class NotificationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.notification)

        val action = intent.getStringExtra("action")
        if (action == "stop") {
            stopTracking()
        }

        //This is used to close the notification tray
//        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//        context.sendBroadcast(it)

        //---look up the notification manager service---
//        var nm : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
//                as NotificationManager
//
//        //---cancel the notification that we started---
//        val notiId = intent.extras.getInt("notificationID")
//        nm.cancel(notiId)
        // OR just:
        // nm.cancelAll()

//        val tv = findViewById<TextView>(R.id.details)
//        tv.setText( "${tv.text}: id = ${notiId}" )
    }

    private fun stopTracking() {
        val nm : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        //---cancel the notification that we started---
        val notiId = intent.extras.getInt("notificationID")
        nm.cancel(notiId)
    }
}