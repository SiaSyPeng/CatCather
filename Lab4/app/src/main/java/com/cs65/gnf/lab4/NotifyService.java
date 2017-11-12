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
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.ActivityCompat;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;


public class NotifyService extends Service implements LocationListener {

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE_BROADCAST_KEY="StopServiceBroadcastKey";
    final static int RQS_STOP_SERVICE = 1;
    final static int notificationID = 1;
    final String channelId  = "my_channel_01"; // set in createChannel, only used in API >= 26

    ArrayList<Cat> listOfCats = null;

    //cat being tracked
    String catName;
    Double catLat;
    Double catLng;

    double[] currLoc = new double[2]; //lat and long

    NotifyServiceReceiver notifyServiceReceiver; //our receiver for this service

    private final String myBlog = "http://www.cs.dartmouth.edu/~campbell/cs65/cs65.html";

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service ", " onStartCommand");

        startLocation();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);

        //get tracked cat  info
        catName = intent.getStringExtra("name");
        catLat = intent.getDoubleExtra("lat",43.70805181058869);
        catLng = intent.getDoubleExtra("lng",43.70805181058869);

        //get distance
        float[] dist = new float[1];
        Location.distanceBetween(currLoc[0],currLoc[1],catLat,catLng,dist);

        // Send Notification
        String notificationTitle = "Tracking "+catName;
        String notificationText = ((int) dist[0]) + " meters away";
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myBlog));

        TaskStackBuilder stackBuilder= TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(myIntent);

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

        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText).setSmallIcon(R.drawable.petted)
                .setContentIntent(pendingIntent).build();

        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(notificationID, notification);

        return super.onStartCommand(intent, flags, startId);
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

            // The id of the channel.
            //channelId = "my_channel_01";

            // The user-visible name of the channel.
            String name = getString(R.string.channel_name);
            // The user-visible description of the channel.
            String description = getString(R.string.channel_description);

            int importance = NotificationManager.IMPORTANCE_LOW;
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

    @Override
    public void onDestroy() {
        this.unregisterReceiver(notifyServiceReceiver);
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
            int rqs = arg1.getIntExtra(STOP_SERVICE_BROADCAST_KEY, 0);

            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
                //String notiId = arg1.getIntExtra("notificationID", 0)

                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .cancelAll();
                        //.cancel(notiId);
            }
        }
    }

}
