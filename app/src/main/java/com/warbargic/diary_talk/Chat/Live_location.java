package com.warbargic.diary_talk.Chat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.warbargic.diary_talk.R;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import static android.os.Build.VERSION_CODES.M;


public class Live_location extends Activity implements MapView.MapViewEventListener, MapView.POIItemEventListener{
    PrintWriter printWriter = null;
    Location lastKnownLocation = null;
    TextView lat_T, lon_T;
    Socket socket = null;
    String FriendMail = null;
    LocationManager lm = null;
    MapView mapView;
    MapPOIItem marker;
    MapPOIItem marker_you;
    Boolean gotomelocation = true, gotoyoulocation = true;
    double you_lat,you_lon,net_lat,net_lon,gps_lat,gps_lon,lat,lon;
    Boolean me_map_touch = false, you_map_touch = false;
    float gps_accuracy = 0,network_accuracy = 0, accuracy = 0;
    MapPOIItem now_marker;
    int net_count = 0;
    boolean once = true;

    @RequiresApi(api = M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_location);
        Intent intent = getIntent();
        FriendMail = intent.getStringExtra("FriendMail");

        mapView = new MapView(this);
        mapView.setDaumMapApiKey("d089902104a0bf1480e014a17ce6898d");

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.4020737, 127.1086766), true);


//        verifyStoragePermissions(this);
        lat_T = (TextView) findViewById(R.id.live_location_lat);
        lon_T = (TextView) findViewById(R.id.live_location_lon);


        final ToggleButton tb = (ToggleButton)findViewById(R.id.toggle1);
        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Start_Location();
                try{
                    if(tb.isChecked()){
                        if(lm != null)
                            lm.removeUpdates(locationListener);
                        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        lm.removeUpdates(locationListener);    // Stop the update if it is in progress.

                        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                        TextView kind = (TextView)findViewById(R.id.live_location_kind);
                        // wifi 또는 모바일 네트워크 어느 하나라도 연결이 되어있다면,
                        if (wifi.isConnected()) {
                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                                    1000, // 통지사이의 최소 시간간격 (miliSecond)
                                    1, // 통지사이의 최소 변경거리 (m)
                                    locationListener);
                            kind.setText("위치정보: WIFI & GPS");
                        }
//                        else {
                            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                                    1000, // 통지사이의 최소 시간간격 (miliSecond)
                                    1, // 통지사이의 최소 변경거리 (m)
                                    locationListener);
//                        }
                    }else{
                        lm.removeUpdates(locationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                    }
                }catch(SecurityException ex){
                    ex.printStackTrace();
                }
            }
        });
//        ((LinearLayout)findViewById(R.id.live_location_linear)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                you_map_touch = true;
//                me_map_touch = true;
//            }
//        });

        final Button you_position, me_position;
        you_position = (Button)findViewById(R.id.live_location_youposition_btn);
        me_position = (Button)findViewById(R.id.live_location_myposition_btn);


        you_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(you_map_touch == false){
                    you_map_touch = true;
                    you_position.setBackgroundColor(Color.parseColor("#5360FF"));
                }else {

                    me_position.setBackgroundColor(Color.parseColor("#CF4F45"));
                    you_map_touch = false;
                    me_map_touch = true;
                    you_position.setBackgroundColor(Color.parseColor("#9AA9FF"));


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                if (you_map_touch == true) {
                                    return;
                                }
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (you_map_touch == true) {
                                    return;
                                }
                                if (you_lat != 0 && you_lon != 0)
                                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(you_lat, you_lon), true);
                            }
                        }
                    }).start();
                }

            }
        });

        me_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(me_map_touch == false){
                    me_map_touch = true;
                    me_position.setBackgroundColor(Color.parseColor("#CF4F45"));
                }else {
                    you_position.setBackgroundColor(Color.parseColor("#5360FF"));

                    me_map_touch = false;
                    you_map_touch = true;
                    me_position.setBackgroundColor(Color.parseColor("#CF908B"));

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                if (me_map_touch == true) {
                                    return;
                                }
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (me_map_touch == true) {
                                    return;
                                }
                                if (lat != 0 && lon != 0)
                                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                            }
                        }
                    }).start();
                }


            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        Connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

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
        if(lm != null)
            lm.removeUpdates(locationListener);
        try {
            if(socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Connect(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    socket = new Socket("115.71.232.134",9997);
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                    String token = sharedPreferences.getString("token","");
                    printWriter.println(token);

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while(true){
                        String str = bufferedReader.readLine();
                        String[] str_cut = str.split(",");
                        you_lat = Double.valueOf(str_cut[0]).doubleValue();
                        you_lon = Double.valueOf(str_cut[1]).doubleValue();
                        Log.d("received!",":"+str);

                        if(marker_you != null)
                            mapView.removePOIItem(marker_you);
                        marker_you = new MapPOIItem();
                        marker_you.setItemName("상대");
                        marker_you.setTag(0);
//                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(a, b));
                        marker_you.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
                        marker_you.setCustomImageResourceId(R.drawable.you_location);
                        marker_you.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                        marker_you.setCustomSelectedImageResourceId(R.drawable.you_location);
                        marker_you.moveWithAnimation(MapPoint.mapPointWithGeoCoord(you_lat,you_lon), true);
                        mapView.addPOIItem(marker_you);


                        if(gotoyoulocation) {
                            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(you_lat, you_lon), true);
                            gotoyoulocation = false;
                            mapView.setZoomLevel(-1, true);
                        }

                        if(lat != 0.0 && lon != 0.0 && you_lat != 0.0 && you_lon != 0.0)
                            (new Drow_Road(lat,lon,you_lat,you_lon)).execute();
                    }
                }catch(Exception e){
                    e.getMessage();
                }
            }
        }).start();

    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String LOG = "location_log";
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



            // Get the last location, and update UI.
//            lastKnownLocation = location;
            double now_lat = location.getLatitude();
            double now_lon = location.getLongitude();
            float now_accuracy = location.getAccuracy();
            String now_provider = location.getProvider();

            Log.d("------------","-----------");
            Log.d("now_lat", ""+now_lat);
            Log.d("now_lon",""+now_lon);
            Log.d("now_accuracy",""+now_accuracy);
            Log.d("now_provider",""+now_provider);
            Log.d("------------","-----------");
            int limit = 10;

            TextView kind_T = (TextView)findViewById(R.id.live_location_kind);
            TextView lat_T = (TextView)findViewById(R.id.live_location_lat);
            TextView lon_T = (TextView)findViewById(R.id.live_location_lon);
            TextView accuracy_T = (TextView)findViewById(R.id.live_location_accuracy);


//            Boolean true_location = isBetterLocation(location, lastKnownLocation);
//            if(true_location){
//                lat = now_lat;
//                lon = now_lon;
//                kind_T.setText(now_provider);
//                lat_T.setText(lat+"");
//                lon_T.setText(lon+"");
//                accuracy_T.setText(accuracy+"");
//
//                if (marker != null)
//                        mapView.removePOIItem(marker);
//                marker = new MapPOIItem();
//                marker.setItemName("나");
//                marker.setTag(0);
//                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
//                marker.setCustomImageResourceId(R.drawable.my_location);
//                marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//                marker.setCustomSelectedImageResourceId(R.drawable.my_location);
//
//                marker.moveWithAnimation(MapPoint.mapPointWithGeoCoord(lat, lon), true);
//                mapView.addPOIItem(marker);
//                if (gotomelocation) {
//                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lon), true);
//                    gotomelocation = false;
//                    mapView.setZoomLevel(-1, true);
//                }
//
//            }




            if(once){
                Log.d(LOG, "******************once******************");
                if(now_accuracy > limit){
                    Log.d(LOG, "******************limit******************");
                    once = false;
                    lastKnownLocation = location;
                    lat = now_lat;
                    lon = now_lon;

                    kind_T.setText(now_provider);
                    lat_T.setText(lat+"");
                    lon_T.setText(lon+"");
                    accuracy_T.setText(accuracy+"");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            printWriter.println(FriendMail + "~" + lat + "," + lon);
                        }
                    }).start();

                    Log.d("printed", ":" + lat + "," + lon);

                    if (marker != null)
                        mapView.removePOIItem(marker);
                    marker = new MapPOIItem();
                    marker.setItemName("나");
                    marker.setTag(0);
                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
                    marker.setCustomImageResourceId(R.drawable.my_location);
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                    marker.setCustomSelectedImageResourceId(R.drawable.my_location);

                    marker.moveWithAnimation(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                    mapView.addPOIItem(marker);
                    if (gotomelocation) {
                        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                        gotomelocation = false;
                        mapView.setZoomLevel(-1, true);
                    }
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


                if (distance < 0) {
                    Log.d(LOG, "******************distance******************");

                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    kind_T.setText(location.getProvider());
                    lat_T.setText(location.getLatitude() + "");
                    lon_T.setText(location.getLongitude() + "");
                    accuracy_T.setText(location.getAccuracy() + "");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            printWriter.println(FriendMail + "~" + lat + "," + lon);
                        }
                    }).start();

                    Log.d("printed", ":" + lat + "," + lon);

                    if (marker != null)
                        mapView.removePOIItem(marker);
                    marker = new MapPOIItem();
                    marker.setItemName("나");
                    marker.setTag(0);
                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
                    marker.setCustomImageResourceId(R.drawable.my_location);
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                    marker.setCustomSelectedImageResourceId(R.drawable.my_location);

                    marker.moveWithAnimation(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                    mapView.addPOIItem(marker);
                    if (gotomelocation) {
                        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                        gotomelocation = false;
                        mapView.setZoomLevel(-1, true);
                    }

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

    int TWO_MINUTES = 2000;
    /** Determines whether one Location reading is better than the current Location fix
     * @param location The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime()-currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;
        // If it’s been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy()-currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());
        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }


    class Drow_Road extends AsyncTask<String, String, String>{
        double lat,lon,you_lat,you_lon;
        double[][] double_d;
        Drow_Road(double lat,double lon,double you_lat,double you_lon){
            this.lat = lat;
            this.lon = lon;
            this.you_lat = you_lat;
            this.you_lon = you_lon;
        }

        @Override
        protected String doInBackground(String... params) {
            String url_s = "https://apis.skplanetx.com/tmap/routes/pedestrian?";
            url_s+="version=1&startX="+lon+"&startY="+lat+"&endX="+you_lon+"&endY="+you_lat+"&startName=a&endName=b&reqCoordType=WGS84GEO&resCoordType=WGS84GEO";
            url_s+="&appKey=47e6d25a-66c1-35b9-8d89-f74e03abe45d";
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            String buffer_str = null;
            try{
                url = new URL(url_s);
//                url = new URL("https://apis.skplanetx.com/tmap/routes/pedestrian?version=1&startX=127.08427414&startY=37.62024428&endX=127.08366930&endY=37.61840417&startName=a&endName=b&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&appKey=47e6d25a-66c1-35b9-8d89-f74e03abe45d");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");

//                OutputStream outputStream = httpURLConnection.getOutputStream();
//                outputStream.write(parms.getBytes("UTF-8"));
//                outputStream.flush();
//                outputStream.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                buffer_str = reader.readLine();
//                while((buffer_str = reader.readLine()) != null){
//                    Log.d("buffer", buffer_str);
//                }
            }catch(Exception e){
                e.printStackTrace();
            }


            return buffer_str;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MapPolyline polyline = new MapPolyline();
            polyline.setTag(100);
            polyline.setLineColor(Color.BLUE);



            int count = 0;
            Log.d("count",count+"");
            try {
                if(s != null) {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = (JSONArray) jsonObject.get("features");
//                double_d = new double[count_][2];
                    count = 0;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        object = (JSONObject) object.get("geometry");
                        JSONArray array = (JSONArray) object.get("coordinates");

                        Log.d("----------", "----------");
                        Log.d("----------", "----------");

                        polyline.addPoint(MapPoint.mapPointWithGeoCoord(lat, lon));
                        if (array.length() > 2) {
                            for (int l = 0; l < array.length(); l++) {
                                JSONArray array1 = (JSONArray) array.get(l);
//                            Log.d("x"+l,array1.getString(0));
//                            Log.d("y"+l,array1.getString(1));
//                            double_d[count][0] = Double.parseDouble(array1.getString(0));
//                            double_d[count][1] = Double.parseDouble(array1.getString(1));
                                polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(array1.getString(1)), Double.parseDouble(array1.getString(0))));
                            }
                        } else {
                            JSONArray array1 = null;
                            String x = null;
                            String y = null;
                            try {
                                array1 = (JSONArray) array.get(0);
//                            Log.d("xx",array1.getString(0));
//                            Log.d("yy",array1.getString(1));
                                x = array1.getString(0);
                                y = array1.getString(1);
                            } catch (Exception e) {
//                            Log.d("xx",array.getString(0));
//                            Log.d("yy",array.getString(1));
                                x = array.getString(0);
                                y = array.getString(1);
                            }

                            Log.d("count_", count + "");
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(y), Double.parseDouble(x)));
//                        double_d[count][0] = Double.parseDouble(x);
//                        double_d[count][1] = Double.parseDouble(y);
                        }
//                    Log.d("----------","----------");
//                    Log.d("----------","----------");

                        count++;
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

//            for(int i = 0; i < count_; i++){
//                    Log.d("x",double_d[i][0]+"");
//                    Log.d("y",double_d[i][1]+"");
//            }


            polyline.addPoint(MapPoint.mapPointWithGeoCoord(you_lat, you_lon));
            mapView.removeAllPolylines();
            mapView.addPolyline(polyline);
//            MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
//            int padding = 100; // px
//            mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
        }


    }



    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static void  verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}
