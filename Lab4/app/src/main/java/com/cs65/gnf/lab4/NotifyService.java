package com.cs65.gnf.lab4;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;


public class NotifyService extends Service {

    final static String ACTION_STOP = "STOP";
    final static String ACTION_TRACK = "TRACK";
    final static String STOP_SERVICE_BROADCAST_KEY="STOP";
    final static int RQS_STOP_SERVICE = 1;
    final static int notificationID = 1;
    final String channelId  = "my_channel_01"; // set in createChannel, only used in API >= 26

    NotifyServiceReceiver notifyServiceReceiver;

    private final String myBlog = "http://www.cs.dartmouth.edu/~campbell/cs65/cs65.html";

    @Override
    public void onCreate() {
        notifyServiceReceiver = new NotifyServiceReceiver();
        super.onCreate();

        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service ", " onStartCommand");


        // filter intent and register receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TRACK);
        registerReceiver(notifyServiceReceiver, intentFilter);

        // if we request to stop the service, stop the service!
        if (ACTION_STOP.equals(intent.getAction())) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(notificationID);
            }
            stopSelf();
        }

        updateNotification();
        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }



    private void createChannel() {

        if (android.os.Build.VERSION.SDK_INT >= 26) {

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // The id of the channel.
            //channelId = "my_channel_01";

            // The user-visible name of the channel.
            String name = getString(R.string.channel_name);
            // The user-visible description of the channel.
            String description = getString(R.string.channel_description);

            Integer importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);

            // Configure the notification channel.
            description = mChannel.getDescription();

            mChannel.enableLights(true);

            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);

            // Sets vibration if the device supports it
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private void updateNotification() {

        // Set up Notification
        Context context = getApplicationContext();
        //TODO add selectedCat?.name and distance
        String notificationTitle = "Catching ";
        String notificationText = " meters away";

        // Click the notification goes back to map activity
        //TODO: go back to main if back stack doesn't work
        Intent myIntent = new Intent(this, MapActivity.class);

        // add back stack for new map activity
        TaskStackBuilder stackBuilder= TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MapActivity.class);
        stackBuilder.addNextIntent(myIntent);
        //stackBuilder.addNextIntentWithParentStack(myIntent);

        //To be wrapped in a PendingIntent, because
        //it will be sent from whatever activity manages notifications;
        //this activity may not even be running.
        PendingIntent pendingIntent
                = PendingIntent.getActivity(getBaseContext(),
                0, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //val action = Notification.Action.Builder(icon,"STOP", pendingIntent).build()

        Notification.Builder builder = new Notification.Builder(this, channelId)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.petted)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(channelId);
        }

        //TODO: hidable
        // setup pending intent to stop this service when stop is clicked

        Intent stopSelf = new Intent(this, NotifyService.class);
        stopSelf.setAction(ACTION_STOP);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        Icon button = Icon.createWithResource(this,R.mipmap.ic_launcher);
        Notification.Action stopAct = new Notification.Action.Builder( button,"STOP", pStopSelf).build();

        builder.addAction(stopAct);



        Notification notification = builder.build();
//        notification.flags = notification.flags
//                | Notification.FLAG_ONGOING_EVENT;
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        startForeground(notificationID, notification);
        //notificationManager.notify(notificationID, notification);

    }
    @Override
    public void onDestroy() {
        this.unregisterReceiver(notifyServiceReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public class NotifyServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            int rqs = arg1.getIntExtra(STOP_SERVICE_BROADCAST_KEY, 0);

            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
                //String notiId = arg1.getIntExtra("notificationID", 0)

                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        //.cancelAll();
                        .cancel(notificationID);
            }
        }
    }

}
