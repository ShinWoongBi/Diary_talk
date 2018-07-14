package com.warbargic.diary_talk.Diary;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.warbargic.diary_talk.Diary.service.distance;


public class monitor extends Activity implements MapView.MapViewEventListener, MapView.POIItemEventListener {
    MapView mapView;
    String FriendMail;
    static public handler monitor_handler = null;
    boolean gotomelocation = true, start_pause_btn = false, start_point = true;
    ArrayList<Data> data_list;
    String date;
    DataBase dataBase_diary, dataBase_diary_gps;
    MapPolyline mapPolyline;
    double lat = 0;
    double lon = 0;
    TextView second_T, distance_T;
    MapPOIItem marker, marker_me;
    ListView listView;
    Adapter adapter;
    ImageButton imageButton;
    Uri mlmageCaptureUri = null;
    Bitmap bitmap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_monitor);
        FriendMail = (getIntent()).getStringExtra("FriendMail");
        monitor_handler = new handler(this);
        data_list = new ArrayList<>();
        date = Get_date();
        dataBase_diary = new DataBase(getApplicationContext(), "diary.db", null, 1);
        dataBase_diary_gps = new DataBase(getApplicationContext(), "diary_gps.db", null, 1);
        mapPolyline = new MapPolyline(21);
        listView = (ListView)findViewById(R.id.listView);
        adapter = new Adapter(getApplicationContext());
        listView.setAdapter(adapter);
        second_T = (TextView)findViewById(R.id.time);
        distance_T = (TextView)findViewById(R.id.distance);
        imageButton = (ImageButton)findViewById(R.id.image_btn);
        marker_me = new MapPOIItem();



        mapView = new MapView(this);
        mapView.setDaumMapApiKey("d089902104a0bf1480e014a17ce6898d");

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.daum_map);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);
        Set_list();

        final Button text_btn = (Button)findViewById(R.id.save_text_btn);
        final EditText text_T = (EditText)findViewById(R.id.save_text);
        text_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = text_T.getText().toString();
                text_T.setText("");

                if(bitmap == null) {
                    dataBase_diary.insert_diary(FriendMail, date, "NO", "", text, Get_time(), lat, lon);
                }else{
                    int count;
                    SharedPreferences sharedPreferences = getSharedPreferences("marker_image",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    count = sharedPreferences.getInt("count",0);
                    Log.d("monitor", count+"");
                    count++;
                    editor.putInt("count",count);
                    editor.apply();
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/marker/";
                    File file = new File(path);
                    if (!file.exists())
                        file.mkdir();
                    path += "."+count+".jpg";
                    file = new File(path);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    ImageButton image_out_btn = (ImageButton)findViewById(R.id.image_out_btn);
                    image_out_btn.setVisibility(View.GONE);
                    bitmap = null;
                    dataBase_diary.insert_diary(FriendMail, date, "YES", path, text, Get_time(), lat, lon);
                }
                Set_list();
            }
        });

        final ImageButton btn_play = (ImageButton) findViewById(R.id.monitor_btn);
        text_btn.setClickable(false);
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_pause_btn) {
                    Intent intent = new Intent(getApplicationContext(), service.class);


                    stopService(intent);
                    btn_play.setBackgroundResource(R.drawable.play);
                    DataBase dataBase = new DataBase(getApplicationContext(), "diary_gps.db", null, 1);
                    dataBase.insert_diary_gps(FriendMail, date, 0, 0, Get_time());
//                    mapPolyline = new MapPolyline();
                    start_pause_btn = false;
                    text_btn.setTextColor(Color.parseColor("#CCCCCC"));
                    text_btn.setClickable(false);


                } else {
                    Intent intent = new Intent(getApplicationContext(), service.class);
                    intent.putExtra("Friend", FriendMail);
                    intent.putExtra("date", date);
                    Log.d("friendMail", FriendMail+"first");
                    startService(intent);
                    btn_play.setBackgroundResource(R.drawable.pause);
                    start_pause_btn = true;
                    text_btn.setTextColor(Color.parseColor("#000000"));
                    text_btn.setClickable(true);
                }
            }
        });
        text_T.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(text_T.getText().toString() == ""){
                    text_btn.setTextColor(Color.parseColor("#CCCCCC"));
                    text_btn.setClickable(false);
                }else{
                    text_btn.setTextColor(Color.parseColor("#000000"));
                    text_btn.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = data_list.size()-position-1;

                double lat = data_list.get(position).lat;
                double lon = data_list.get(position).lon;
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lat,lon),-1,true);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener carmeraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doTakePhotoAction();
                    }
                };

                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doTakeAlbumAction();
                    }
                };

                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                };

                if(!monitor.this.isFinishing()) {

                    AlertDialog.Builder show = new AlertDialog.Builder(monitor.this);
                    show.setTitle("업로드할 이미지 선택");
                    show.setNeutralButton("취소", cancelListener);
                    show.setNegativeButton("사진촬영", carmeraListener);
                    show.setPositiveButton("앨범선택", albumListener);
                    show.show();
                }
            }
        });




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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK)return;

        switch (requestCode){
            case 1:
                mlmageCaptureUri = data.getData();
            case 0:
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mlmageCaptureUri, "image/*");

                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 2);
                intent.putExtra("aspectY", 2);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, 2);
                break;
            case 2:

                if(resultCode != RESULT_OK)break;
                Bundle extras = data.getExtras();

                if(extras != null){
                    bitmap =  extras.getParcelable("data");
//                    bitmap = getCircleBitmap(bitmap);
                    bitmap = cropCircle(bitmap);
                    ImageButton image_out_btn = (ImageButton)findViewById(R.id.image_out_btn);
                    image_out_btn.setVisibility(View.VISIBLE);
                    image_out_btn.setImageBitmap(bitmap);

                }

                break;
        }
    }

    void doTakeAlbumAction(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, 1);
    }

    void doTakePhotoAction(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String url = "tmp" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mlmageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mlmageCaptureUri);
        startActivityForResult(intent, 0);
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


    public Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        //alpha : 0(투명) ~255 (완전 불투명)
        for(int y = 30,alpha =0; alpha>2 ;  y+=10){
            //alpha값 지정(투명도)를 지정
            paint.setAlpha(alpha);
            canvas.drawLine(0,y,100,y,paint);
        }
//        paint.setAlpha(0);
        int size = (bitmap.getWidth()/2);
        canvas.drawCircle(size, size, size, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public Bitmap cropCircle(Bitmap bitmap) {

        Bitmap output = Bitmap	.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);

        int size = (bitmap.getWidth() / 2);

        canvas.drawCircle(size, size, size, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;

    }




    String Get_date() {
        Date date = new Date(System.currentTimeMillis());
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(date));
        int month = Integer.parseInt(new SimpleDateFormat("MM").format(date));
        int day = Integer.parseInt(new SimpleDateFormat("dd").format(date));

        String date_ = year + "-" + month + "-" + day;

        return date_;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapPolyline.setLineColor(Color.RED);
        mapView.addPolyline(mapPolyline);

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
//        Log.e("ababasbsdds","agdsgasdgasdga");
//        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lon));
//        mapView.addPOIItem(marker);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
//        MapPointBounds mapPointBounds = new MapPointBounds(mapPolyline.getMapPoints());
//        int padding = 100; // px
//        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
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

    public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();

        if (bundle.getInt("second_true") == 1) {
            String print = "";
            int second = bundle.getInt("second");
            if (second / 360 != 0) {
                print += second / 360 + "시 ";
            }
            if ((second % 360) / 60 != 0) {
                print += (second % 360) / 60 + "분 ";
            }
            if ((second % 360) % 60 != 0) {
                print += (second % 360) % 60 + "초";
            }
            final String finalPrint = print;
            second_T.post(new Runnable() {
                @Override
                public void run() {
                    second_T.setText(finalPrint + "");
                }
            });
            DataBase dataBase = new DataBase(getApplicationContext(), "diary_day.db", null, 1);
            String start_data = dataBase.insert_day_time(FriendMail, date, -2);
            dataBase.close();
            Log.d("loog", start_data);
            String[] start_data_i = start_data.split("_");
//            final int distance = Integer.parseInt(start_data_i[1]);
            String dis = "";
            if (distance / 1000 != 0) {
                dis += (distance / 1000) + "km ";
            }
            if ((distance % 1000) != 0) {
                dis += (distance % 1000) + "m";
            }


            final String finalDis = dis;
            distance_T.post(new Runnable() {
                @Override
                public void run() {
                    distance_T.setText(finalDis);
                }
            });


        }else if(bundle.getInt("thired") == 1){
            mapView.addPolyline(mapPolyline);
        } else {

            lat = bundle.getDouble("lat");
            lon = bundle.getDouble("lon");


            if (marker_me != null)
                mapView.removePOIItem(marker_me);
            marker_me.setItemName("나");
            marker_me.setTag(1);
            marker_me.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
            marker_me.setCustomImageResourceId(R.drawable.my_location);
            marker_me.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
            marker_me.setCustomSelectedImageResourceId(R.drawable.my_location);
            marker_me.setCustomImageAutoscale(true);


            marker_me.moveWithAnimation(MapPoint.mapPointWithGeoCoord(lat, lon), true);
            mapView.addPOIItem(marker_me);
            if (gotomelocation) {
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                gotomelocation = false;
                mapView.setZoomLevel(0, true);
            }


            String time = Get_time();
            Log.d("@@@@@@@@@@","@@@@@@@@@@@");
            Log.d("Friend",FriendMail+"");
            Log.d("date", date+"");
            dataBase_diary_gps.insert_diary_gps(FriendMail, date, lat, lon, time);

            if (start_point == true) {
                mapPolyline.addPoint(MapPoint.mapPointWithGeoCoord(lat, lon));
            } else {
                mapPolyline = new MapPolyline();
                start_point = true;
            }
            mapView.addPolyline(mapPolyline);
        }
    }

    String Get_time() {
        Date date = new Date(System.currentTimeMillis());
        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(date));
        int minute = Integer.parseInt(new SimpleDateFormat("mm").format(date));
        int secend = Integer.parseInt(new SimpleDateFormat("ss").format(date));

        String time = hour + "-" + minute + "-" + secend;

        return time;
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
                mapPOIItem.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                mapPOIItem.setMapPoint(MapPoint.mapPointWithGeoCoord(data_list.get(pos).lat, data_list.get(pos).lon));
                mapView.addPOIItem(mapPOIItem);
            }

            return convertView;
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static void verifyStoragePermissions(Activity activity) {
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
