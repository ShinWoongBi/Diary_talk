package com.warbargic.diary_talk.Diary;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.warbargic.diary_talk.MainActivity;
import com.warbargic.diary_talk.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.android.gms.internal.zzs.TAG;
import static com.warbargic.diary_talk.Diary.monitor.monitor_handler;


public class service extends Service {
    LocationManager mLocationManager;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1;
    String Friend, date;
    private MediaPlayer mPlayer = null;
    Location lastKnownLocation;
    boolean once = true, distance_ = false, stop_second = true, one = true;
    double last_lat, last_lon, no_lat, no_lon;
    static public int distance = 0;
    String LOG = "service_log";

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = MediaPlayer.create(this, R.raw.goaway);
//        mPlayer.start();


        Log.e(TAG, "onCreate");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Friend = intent.getStringExtra("Friend");
        date = intent.getStringExtra("date");
        Log.d("*****************","**************");
        Log.d("FriendMail",Friend+"");
        Log.d("date",date+"");
        DataBase dataBase = new DataBase(getApplicationContext(), "diary_day.db", null, 1);
        String start_data = dataBase.insert_day_time(Friend, date, -2);
        dataBase.close();
        String[] start_data_i = start_data.split("_");
        Log.d(LOG, start_data + "   " + start_data_i[0] + "   " + start_data_i[1]);
        final int second = Integer.parseInt(start_data_i[0]);
        distance = Integer.parseInt(start_data_i[1]);


        new Thread(new Runnable() {
            @Override
            public void run() {
                int second_T = second;

                int i = 0;
                while (stop_second) {
                    i += 10;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i == 1000) {
                        i = 0;
                        second_T++;

                        if (monitor_handler != null) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("second", second_T);
                            bundle.putInt("second_true", 1);
                            Message msg = new Message();
                            msg.setData(bundle);
                            monitor_handler.handleMessage(msg);
                        }
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e(TAG, "onStart");


        Intent intent1 = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent1,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("yeah")
                .setContentText("yeah")
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

    }

    @Override
    public void onDestroy() {
        Log.d("slog", "onDestroy()");
//        mPlayer.stop();
        stop_second = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }
    String Get_time() {
        Date date = new Date(System.currentTimeMillis());
        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(date));
        int minute = Integer.parseInt(new SimpleDateFormat("mm").format(date));
        int secend = Integer.parseInt(new SimpleDateFormat("ss").format(date));

        String time = hour + "-" + minute + "-" + secend;

        return time;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            String time = Get_time();


            double now_lat = location.getLatitude();
            double now_lon = location.getLongitude();
            float now_accuracy = location.getAccuracy();
            String now_provider = location.getProvider();
            int limit = -1;

            Log.d("------------","-----------");
            Log.d("now_lat", ""+now_lat);
            Log.d("now_lon",""+now_lon);
            Log.d("now_accuracy",""+now_accuracy);
            Log.d("now_provider",""+now_provider);
            Log.d("------------","-----------");
            Log.d("mail", Friend+"");
            Log.d("date", date+"");

            double lat,lon;

            if(once){
                Log.d(LOG, "******************once******************");
                if(now_accuracy > limit){
                    Log.d(LOG, "******************limit******************");
                    once = false;
                    lastKnownLocation = location;
                    lat = now_lat;
                    lon = now_lon;

                    if(monitor_handler != null){
                        Bundle bundle = new Bundle();
                        bundle.putDouble("lat", lat);
                        bundle.putDouble("lon", lon);
                        Log.d("lat", lat+"");
                        Message msg = new Message();
                        msg.setData(bundle);
                        monitor_handler.handleMessage(msg);
                    }

                    if(distance_){
                        Log.d(LOG,"true");
                        distance_ = false;
                        last_lat = lat;
                        last_lon = lon;
                    }else{
                        Log.d(LOG,"false");
                        distance_ = true;
                        no_lat = lat;
                        no_lon = lon;
                        Distance();
                    }

                    DataBase dataBase = new DataBase(getApplicationContext(), "diary_gps.db", null, 1);
                    dataBase.insert_diary_gps(Friend, date, lat, lon, time);
                    dataBase.close();
                }
            }else {
                Log.d(LOG, "******************once else******************");

                Location locationA = new Location("point A");

                locationA.setLatitude(now_lat);
                locationA.setLongitude(now_lat);

                Location locationB = new Location("point B");

                locationB.setLatitude(lastKnownLocation.getLatitude());
                locationB.setLongitude(lastKnownLocation.getLongitude());


                double distance = locationA.distanceTo(locationB) - 7552300.0;
                Log.d("distance", distance+"");


                if (distance > 0) {
                    Log.d(LOG, "******************distance******************");

                    lat = location.getLatitude();
                    lon = location.getLongitude();

                    if(monitor_handler != null){
                        Bundle bundle = new Bundle();
                        bundle.putDouble("lat", lat);
                        bundle.putDouble("lon", lon);
                        Log.d("lat", lat+"");
                        Message msg = new Message();
                        msg.setData(bundle);
                        monitor_handler.handleMessage(msg);
                    }

                    if(distance_){
                        Log.d(LOG,"true");
                        distance_ = false;
                        last_lat = lat;
                        last_lon = lon;
                    }else{
                        Log.d(LOG,"false");
                        distance_ = true;
                        no_lat = lat;
                        no_lon = lon;
                        Distance();
                    }

                    DataBase dataBase = new DataBase(getApplicationContext(), "diary_gps.db", null, 1);
//                    dataBase.insert_diary_gps(Friend, date, lat, lon, time);
                    dataBase.insert_diary_gps("", "", lat, lon, time);
                    dataBase.close();
                }


            }



        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    void Distance(){
        if(!one) {

            String distance_ = calcDistance(last_lat, last_lon, no_lat, no_lon);
            Log.d("distancedistance", distance_);
            DataBase dataBase = new DataBase(getApplicationContext(), "diary_day.db", null, 1);
            distance += Integer.parseInt(distance_);
            Log.d("distancedistance2", distance + "");
            dataBase.update_day(Friend, date, distance);
        }else{
            one = false;
        }
    }


    public static String calcDistance(double lat1, double lon1, double lat2, double lon2){
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        int rslt = Math.round(Math.round(ret) / 1000);
        String result = rslt+"";
        if(rslt == 0) result = Math.round(ret) +"";

        return result;
    }


}
