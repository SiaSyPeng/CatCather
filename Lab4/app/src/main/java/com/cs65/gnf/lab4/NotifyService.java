package com.cs65.gnf.lab4;

import android.Manifest;
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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;


public class NotifyService extends Service implements LocationListener {

    final static String ACTION_STOP = "STOP";
    final static String ACTION_TRACK = "TRACK";
    final static int notificationID = 1;
    final String channelId  = "my_channel_01"; // set in createChannel, only used in API >= 26

//    private Timer timer= new Timer();

    ArrayList<Cat> listOfCats = null;

    //cat being tracked
    String catName;
    double catLat;
    double catLng;
    int catId;

    boolean fromMapActivityClick = false;

    double[] currLoc = new double[2]; //lat and long

    NotifyServiceReceiver notifyServiceReceiver; //our receiver for this service

    @Override
    public void onCreate() {
        notifyServiceReceiver = new NotifyServiceReceiver();
        super.onCreate();

        createChannel();

        //Get the cat list from internal storage. Tries twice.
        String CAT_LIST_FILE = "cat_list";
        try {


            FileInputStream fis = openFileInput(CAT_LIST_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            listOfCats = (ArrayList<Cat>) ois.readObject();
            ois.close();
            fis.close();
        }
        catch (IOException e) {
            Log.d("SERVICE ERROR","error reading file");
            //try again
            try {
                FileInputStream fis = openFileInput(CAT_LIST_FILE);
                ObjectInputStream ois = new ObjectInputStream(fis);
                listOfCats = (ArrayList<Cat>) ois.readObject();
                ois.close();
                fis.close();
            }
            catch (IOException e2) {
                Log.d("SERVICE ERROR", "second time was a bust too. SAD");
            }
            catch (ClassNotFoundException e2) {
                Log.d("SERVICE ERROR","huh. Couldn't find cat class");
            }
        }
        catch (ClassNotFoundException e) {
            Log.d("SERVICE ERROR","huh. Couldn't find cat class");
        }

        //Start a broadcast receiver for stopping the notify service from MapActivity
        IntentFilter i = new IntentFilter(ACTION_STOP);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(new NotifyServiceReceiver(),i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service ", " onStartCommand");

        startLocation();

        // filter intent and register receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TRACK);
        registerReceiver(notifyServiceReceiver, intentFilter);

        //get tracked cat  info
        catName = intent.getStringExtra("name");
        catLat = intent.getDoubleExtra("lat",43.70805181058869);
        catLng = intent.getDoubleExtra("lng",43.70805181058869);
        catName = intent.getStringExtra("name");
        catId = intent.getIntExtra("id",0);

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

    /**
     * Application criteria for selecting a location provider. See line 158 "getBestProvider"
     * https://developer.android.com/reference/android/location/Criteria.html
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setCostAllowed(true);
        return criteria;
    }

    /**
     * Only called once, starts the location services
     */
    void startLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = getCriteria();

            LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String provider = mgr.getBestProvider(criteria,true);


            //get last known location
            try {
                Location l = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currLoc[0] = l.getLatitude();
                currLoc[1] = l.getLongitude();
            }
            catch (NullPointerException e) {
                double x = 43.70805181058869;
                double y = -72.28422369807957;
                currLoc[0] = x;
                currLoc[1] = y;
            }

            //To see how often location updates should be
            String USER_PREFS = "profile_data"; //Shared with other activities
            String TIME_STRING = "minTime";

            SharedPreferences prefs = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE);
            long time = 0;
            switch(prefs.getString(TIME_STRING,"f")) {
                case "f": {
                    time = 0;
                    break;
                }
                case "m": {
                    time = 100;
                    break;
                }
                case "s": {
                    time = 1000;
                    break;
                }
            }

            mgr.requestLocationUpdates(provider,time,0f,this);
        }
    }

    private void createChannel() {

        if (android.os.Build.VERSION.SDK_INT >= 26) {

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // The user-visible name and description of the channel.
            String name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);


            // Sets the notification light color for notifications posted to this
            mChannel.enableLights(true);
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            // Sets vibration if the device supports it
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private void updateNotification() {

        //get distance
        float[] dist = new float[1];
        Location.distanceBetween(currLoc[0],currLoc[1],catLat,catLng,dist);

        String notificationTitle = "Catching "+catName;
        String notificationText = ((int) dist[0])+" meters away";

        // Click the notification goes back to map activity
        //TODO: go back to main if back stack doesn't work
        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mapIntent.putExtra("catId",catId);

        // add back stack for new map activity
        TaskStackBuilder stackBuilder= TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(mapIntent);

        //To be wrapped in a PendingIntent, because
        //it will be sent from whatever activity manages notifications;
        //this activity may not even be running.

        PendingIntent pendingIntent
                = PendingIntent.getActivity(getApplicationContext(),
                0, mapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(this, channelId)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.petted)
                .setShowWhen(true)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(channelId);
        }


        //Create a pending intent that will send a broadcast to ServiceStopReceiver
        Intent intentStop = new Intent(getApplicationContext(), ServiceStopReceiver.class);
        PendingIntent pStopSelf = PendingIntent.getBroadcast
                (getApplicationContext(), 23123123, intentStop, PendingIntent.FLAG_UPDATE_CURRENT);

        //Create an action with that pending intent
        Icon button = Icon.createWithResource(this,R.mipmap.ic_launcher);
        Notification.Action stopAct = new Notification.Action.Builder
                (button,"STOP", pStopSelf).build();

        //add that action to the notification
        builder.addAction(stopAct);



        Notification notification = builder.build();
        startForeground(notificationID, notification);
        //notificationManager.notify(notificationID, notification);

    }
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(new NotifyServiceReceiver());

        if (!fromMapActivityClick) { //if this call was not from mapActivityClick
            //Tell mapActivity to change from STOP to TRACK
            String STOP_TRACKING_ACTION = "com.cs65.gnf.stopTracking"; //changes button in MapActivity

            Intent stopIntent = new Intent();
            stopIntent.setAction(STOP_TRACKING_ACTION);

            Log.d("HERE","destroying NotifyService296");

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(stopIntent);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        currLoc[0] = location.getLatitude();
        currLoc[1] = location.getLongitude();

        for (Cat cat : listOfCats) {
            float[] dist = new float[1];
            Location.distanceBetween(currLoc[0],currLoc[1],
                    cat.getLat(),cat.getLng(),dist);

            if (cat.getName().equals(catName)) { //if this is the cat we're tracking
                //TODO update notification with distance
            }

            if (dist[0]<30) { //if in range
                String message = cat.getName() + " is close by!";
                //TODO send the other notification type with this message and a timer
            }
        }
    }

    //Interface functions we don't actually use

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    public class NotifyServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            fromMapActivityClick = true;
            stopSelf();
            try {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(notificationID);
            }
            catch (java.lang.NullPointerException e) {
                Log.d("CANCEL","nothing to cancel");
            }
        }
    }
}
