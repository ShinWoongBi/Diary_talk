package com.warbargic.diary_talk.Diary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.warbargic.diary_talk.R;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

/**
 * Created by kippe on 2017-03-24.
 */

public class see_diary extends Activity implements MapView.POIItemEventListener, MapView.MapViewEventListener {
    MapView mapView;
    String FriendMail;
    String date;
    ArrayList<Data> data_list;
    Adapter adapter;
    ListView listView;
    DataBase dataBase_diary, dataBase_diary_gps;
    boolean gotomelocation = true, start_pause_btn = false, start_point = true;
    double lat = 0,lon = 0;
    MapPOIItem marker, marker_me;
    MapPolyline mapPolyline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.see_diary);
        Intent intent = getIntent();
        FriendMail = intent.getStringExtra("FriendMail");
        date = intent.getStringExtra("date");
        data_list = new ArrayList<>();
        adapter = new Adapter(getApplicationContext());
        dataBase_diary = new DataBase(getApplicationContext(), "diary.db", null, 1);
        dataBase_diary_gps = new DataBase(getApplicationContext(), "diary_gps.db", null, 1);
        listView = (ListView)findViewById(R.id.listView);
        marker = new MapPOIItem();
        mapPolyline = new MapPolyline();


        dataBase_diary = new DataBase(getApplicationContext(), "diary.db", null, 1);
        mapView = new MapView(this);
        mapView.setDaumMapApiKey("d089902104a0bf1480e014a17ce6898d");

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.daum_map);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = data_list.size()-position-1;

                double lat = data_list.get(position).lat;
                double lon = data_list.get(position).lon;
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lat,lon),-1,true);
            }
        });

        listView.setAdapter(adapter);
        Set_list();


        Cursor cursor = dataBase_diary_gps.select_diary_gps(FriendMail, date);
        Log.d("abc", "abc:" + cursor.getCount());



        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();

            double ddouble = cursor.getDouble(3);
            if(start_point)
                ddouble = 0;
            if (ddouble == 0) {
                if(start_point) {
                    Log.d("point", "start");
                    lat = cursor.getDouble(3);
                    lon = cursor.getDouble(4);
                    Log.d("lat_log", lat + "_" + lon);
                    marker = new MapPOIItem();
                    marker.setItemName("출발");
                    marker.setTag(0);
                    marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.BluePin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat,lon));
                    mapView.addPOIItem(marker);
                    mapPolyline.addPoint(MapPoint.mapPointWithGeoCoord(lat, lon));
                    start_point = false;
                }else {
                    if(lat != 0) {
                        Log.d("point", "stop");
                        Log.d("lat_log", lat + "_" + lon);
                        marker = new MapPOIItem();
                        marker.setItemName("도착");
                        marker.setTag(0);
                        marker.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lon));
                        mapView.addPOIItem(marker);
                        start_point = true;
                    }
//                    start_point = true;
                }
            } else {
                Log.d("getStinrg", cursor.getDouble(3) + "");
                lat = cursor.getDouble(3);
                lon = cursor.getDouble(4);
                Log.d("lat_log", lat + "_" + lon);
                mapPolyline.addPoint(MapPoint.mapPointWithGeoCoord(lat, lon));
            }
        }
        if(cursor.getCount() > 0) {
            MapPointBounds mapPointBounds = new MapPointBounds(mapPolyline.getMapPoints());
            int padding = 100; // px
            mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
        }




    }

    @Override
    protected void onResume() {
        super.onResume();
        mapPolyline.setLineColor(Color.RED);
        mapView.addPolyline(mapPolyline);
    }

    void Set_list(){
        Cursor cursor = dataBase_diary.select_diary(FriendMail, date);
        data_list.clear();

        while(cursor.moveToNext()){
            Data data = new Data(cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getDouble(7),cursor.getDouble(8));
            data_list.add(data);
        }

        adapter.notifyDataSetChanged();
    }

    class Data {
        String picture;
        String picture_path;
        String text;
        String time;
        double lon;
        double lat;

        Data(String picture, String picture_path, String text, String time, double lat, double lon) {
            this.picture = picture;
            this.picture_path = picture_path;
            this.text = text;
            this.time = time;
            this.lon = lon;
            this.lat = lat;
        }
    }

    class Adapter extends BaseAdapter {
        Context context;
        Adapter(Context context){
            this.context = context;
        }

        @Override
        public int getCount() {
            return data_list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.diary_list_item, parent, false);
            }

            int pos = data_list.size() - position-1;

            ImageView imageView = (ImageView)convertView.findViewById(R.id.image);
            TextView text = (TextView)convertView.findViewById(R.id.text);
            TextView time = (TextView)convertView.findViewById(R.id.time);

            Log.d("adapter picture", data_list.get(pos).picture);
            Log.d("adapter picture", data_list.get(pos).picture_path);
            if(data_list.get(pos).picture.equals("NO")){
                imageView.setVisibility(View.GONE);
            }else{
                imageView.setImageBitmap(BitmapFactory.decodeFile(data_list.get(pos).picture_path));
                imageView.setVisibility(View.VISIBLE);
            }

            text.setText(data_list.get(pos).text);
            String[] time_S = data_list.get(pos).time.split("-");
            String time_r = "";
            if(12 > Integer.parseInt(time_S[0])){
                time_r += "오전";
            }else{
                time_r += "오후";
            }

            time_r += String.format("%02d",Integer.parseInt(time_S[0]))+":";
            time_r += String.format("%02d",Integer.parseInt(time_S[1]));

//            time.setText(data_list.get(pos).time);
            time.setText(time_r);


            if(data_list.get(pos).picture.equals("NO")) {
                MapPOIItem mapPOIItem;
                mapPOIItem = new MapPOIItem();
                mapPOIItem.setItemName(data_list.get(pos).text);
                mapPOIItem.setTag(0);
                mapPOIItem.setMarkerType(MapPOIItem.MarkerType.YellowPin); // 기본으로 제공하는 BluePin 마커 모양.
                mapPOIItem.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                mapPOIItem.setMapPoint(MapPoint.mapPointWithGeoCoord(data_list.get(pos).lat, data_list.get(pos).lon));
                mapView.addPOIItem(mapPOIItem);
            }else{
                MapPOIItem mapPOIItem;
                mapPOIItem = new MapPOIItem();
                mapPOIItem.setItemName(data_list.get(pos).text);
                mapPOIItem.setTag(0);
                mapPOIItem.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
                mapPOIItem.setCustomImageBitmap(BitmapFactory.decodeFile(data_list.get(pos).picture_path));
//                mapPOIItem.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                mapPOIItem.setMapPoint(MapPoint.mapPointWithGeoCoord(data_list.get(pos).lat, data_list.get(pos).lon));
                mapPOIItem.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                mapPOIItem.setCustomImageAnchor(20,20);
                mapPOIItem.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                mapView.addPOIItem(mapPOIItem);
            }

            return convertView;
        }
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
}
