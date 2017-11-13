package com.cs65.gnf.lab4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * StopReceiver that listens to if the STOP button in the notification has been pressed, and if so,
 * cancels the service and sends a broadcast changing the track button in map activity
 */
class ServiceStopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        //stop the service
        val service = Intent(context, NotifyService::class.java)
        context?.stopService(service)
    }
}