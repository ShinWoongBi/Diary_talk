package com.warbargic.diary_talk.Friend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.warbargic.diary_talk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class main extends Fragment {
    ListView listview;
    ArrayList<Data> list;
    TextView count;
    ImageView my_image;
    TextView my_name, my_message;
    ArrayList<Bitmap> bitmaps;
    Adapter adapter;
    String mail;
    boolean[] booleen;
    EditText search_edit;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_main, container, false);
        count = (TextView)view.findViewById(R.id.friend_main_count);
        my_image = (ImageView)view.findViewById(R.id.friend_main_myImage);
        my_name = (TextView)view.findViewById(R.id.friend_main_myName);
        my_message = (TextView)view.findViewById(R.id.friend_main_myMessage);
        listview = (ListView)view.findViewById(R.id.friend_main_listview);
        search_edit = (EditText)view.findViewById(R.id.search_edit);
        list = new ArrayList<>();
        bitmaps = new ArrayList<Bitmap>();
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.friend_main_myBtn);




        // 내 정보 레이아웃 크기 조정
        ViewGroup.LayoutParams parm = linearLayout.getLayoutParams();
        int height = parm.height;
        Log.d("width", "width:"+height);
        ViewGroup.LayoutParams params = my_image.getLayoutParams();
        params.width = height;
        my_image.setLayoutParams(params);



        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), my_profile.class));
            }
        });
        ((Button)view.findViewById(R.id.friend_main_AddFriend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddFriend.class));
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), friend_profile.class);
                intent.putExtra("mail", list.get(position).mail.toString());
                startActivity(intent);
            }
        });
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Search();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        //프로필 바꾸기
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MY_PROFILE",Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name","");
        String message = sharedPreferences.getString("message","");
        String picture = sharedPreferences.getString("picture","");
        Log.d("picture",":"+picture);
        my_name.setText(name);
        my_message.setText(message);
        if(picture.equals("YES")) {
            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE.jpg");
            my_image.setImageBitmap(bitmap);
//            (new GetMyPicture()).execute();
        }else {
            my_image.setBackgroundResource(R.mipmap.ic_launcher);
        }
//        (new GetMyProfile()).execute();

        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.isConnected() || mobile.isConnected()) {
            // 친구목록 가져오기
            list.clear();
            bitmaps.clear();
            (new GetFriend()).execute();
        } else {
            list.clear();
            bitmaps.clear();
            DbOpenHelper dbOpenHelper = new DbOpenHelper(getContext());
            dbOpenHelper.open();
            Log.d("dblog",":"+dbOpenHelper.mDBHelper.select());
            try {
                JSONArray jsonArray = new JSONArray(dbOpenHelper.mDBHelper.select());
                JSONObject jsonObject = null;
                Data data = null;
                for(int i = 0; i < jsonArray.length(); i++){
                    jsonObject = (JSONObject) jsonArray.get(i);

                    data = new Data(jsonObject.getString("picture"),jsonObject.getString("name"),jsonObject.getString("message"),jsonObject.getString("mail"));
                    list.add(data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new Adapter(getActivity().getApplicationContext());
            listview.setAdapter(adapter);
            count.setText(adapter.getCount()+"");
        }



    }

    public void Search(){
        String conain = search_edit.getText().toString();
        for(int i = 0; i < adapter.getCount(); i++){
            if(list.get(i).name.contains(conain)) {
                booleen[i] = false;
            }
            else {
                booleen[i] = true;
            }
        }
        adapter.notifyDataSetChanged();
    }


    class Data{
        String picture;
        String name;
        String message;
        String mail;

        Data(String picture, String name, String message, String mail){
            this.picture = picture;
            this.name = name;
            this.message = message;
            this.mail = mail;
        }

    }

    class Adapter extends BaseAdapter{
        Context context;

        Adapter(Context context){
            super();
            this.context = context;
        }

        @Override
        public int getCount() {
            return list.size();
        }


        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.friend_main_listview_item, parent, false);
            }

            ImageView image_view = (ImageView)convertView.findViewById(R.id.friend_main_item_image);
            TextView name_view = (TextView)convertView.findViewById(R.id.friend_main_item_name);
            TextView message_view = (TextView)convertView.findViewById(R.id.friend_main_item_message);
            LinearLayout linearLayout = (LinearLayout)convertView.findViewById(R.id.friend_main_item_linear);

            {
                ViewGroup.LayoutParams parm = linearLayout.getLayoutParams();
                int height = parm.height;
                Log.d("width", "width:" + height);
                ViewGroup.LayoutParams params = image_view.getLayoutParams();
                params.width = height;
                image_view.setLayoutParams(params);
                image_view.setImageResource(R.mipmap.ic_launcher);

                name_view.setText(list.get(position).name);
                message_view.setText(list.get(position).message);

                if(booleen != null){

                    if (list.get(position).picture.equals("YES")) {
                        image_view.setImageBitmap(bitmaps.get(position));
                    }
                    DbOpenHelper dbOpenHelper = new DbOpenHelper(getContext());
                    dbOpenHelper = dbOpenHelper.open();
                    dbOpenHelper.mDBHelper.insert(list.get(position).mail, list.get(position).name, list.get(position).picture, list.get(position).message);
                    dbOpenHelper.close();

                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/");
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/." + list.get(position).mail + ".jpg");
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        bitmaps.get(position).compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }else{
                    try{
                        if (list.get(position).picture.equals("YES")) {
                            image_view.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"" +
                                    "/diary_talk/friend/."+list.get(position).mail+".jpg"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

//            }else{
//                linearLayout.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }


    class GetFriend extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/GetFriendList.php";
        String parm;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences preferences = getContext().getSharedPreferences("TOKEN",Context.MODE_PRIVATE);
            parm = "token="+preferences.getString("token","");
            Log.d("token", ":"+preferences.getString("token",""));
        }

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            String read = null;
            try{
                url = new URL(url_s);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(parm.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
                String picture, name, message, mail;
                Data data;
                String a;
                while((a=buffer.readLine()) != null){
                    picture = a; Log.e("read",":"+picture);
                    a=buffer.readLine();
                    name = a;Log.e("read",":"+name);
                    a=buffer.readLine();
                    message = a;Log.e("read",":"+message);
                    a=buffer.readLine();
                    mail = a;Log.e("read",":"+mail);
                    data = new Data(picture, name, message, mail);
                    list.add(data);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return read;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            (new GetPicture()).execute();
        }
    }
    class GetPicture extends AsyncTask<String, String, String>{
        String url_s = "";
        String parm = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TOKEN",Context.MODE_PRIVATE);
            parm = "token="+sharedPreferences.getString("token","");
        }

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            String read = null;
            try{
                String mail;
                Bitmap bitmap;
                for(int i = 0; i < list.size(); i++) {
                    bitmap = null;
                    Log.d("asdfasdfasdf","dsafasdfsd");
                    if(list.get(i).picture.equals("YES")) {

                        mail = list.get(i).mail;
                        url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/Profile_Image_small/"+ mail +".jpg";

                        url = new URL(url_s);
                        httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoInput(true);
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        outputStream.write(parm.getBytes("UTF-8"));
                        outputStream.flush();


                         InputStream inputStream = httpURLConnection.getInputStream();
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    }

                    bitmaps.add(bitmap);

                }



            }catch (Exception e){
                e.printStackTrace();
            }

            return read;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            count.setText(list.size());
            booleen = new boolean[list.size()];
            for(int i = 0; i < booleen.length; i++)
                booleen[i] = true;

            adapter = new Adapter(getActivity().getApplicationContext());
            listview.setAdapter(adapter);
            count.setText(adapter.getCount()+"");

        }
    }
    class GetMyPicture extends AsyncTask<String, String, Bitmap>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/get_myprofile_picture.php";
        String parm = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TOKEN",Context.MODE_PRIVATE);
            parm = "token="+sharedPreferences.getString("token","");
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            String read = null;

            Bitmap bitmap = null;
            try{
                url = new URL(url_s);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(parm.getBytes("UTF-8"));
                outputStream.flush();


                InputStream inputStream = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }catch (Exception e){
                e.printStackTrace();
            }

            return bitmap;
        }



        @Override
        protected void onPostExecute(Bitmap s) {
            super.onPostExecute(s);

            my_image.setImageBitmap(s);
        }
    }
    class GetMyProfile extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/GetProfile.php";
        String parm = "";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TOKEN",Context.MODE_PRIVATE);
            parm = "token="+sharedPreferences.getString("token","")+"&mail=";
        }

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            String read = null;
            try {
                url = new URL(url_s);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(parm.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
                read = "["+buffer.readLine()+"]";
//                String a;
//                while((a=buffer.readLine())!=null){
//                    Log.d(":::","::"+a);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return read;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("jsonjson", ":::"+s);


            String mail= null;
            String name = null;
            String message= null;
            String join_date= null;
            String picture= null;
            String authentication= null;
            try {
                JSONArray array = new JSONArray(s);
                JSONObject jsonObject = array.getJSONObject(0);
                mail = jsonObject.getString("mail");
                name = jsonObject.getString("name");
                message = jsonObject.getString("message");
                join_date = jsonObject.getString("join_date");
                picture = jsonObject.getString("picture");
                authentication = jsonObject.getString("authentication");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 내정보 설정
            my_name.setText(name);
            my_message.setText(message);
            if(picture.equals("YES")) {
                (new GetMyPicture()).execute();
            }else {
                my_image.setBackgroundResource(R.mipmap.ic_launcher);
            }


        }
    }


    public class DbOpenHelper {

        private static final String DATABASE_NAME = "friend.db";
        private static final int DATABASE_VERSION = 1;
        public SQLiteDatabase mDB;
        private DatabaseHelper mDBHelper;
        private Context mCtx;

        private class DatabaseHelper extends SQLiteOpenHelper{

            // 생성자
            public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
                super(context, name, factory, version);
            }

            // 최초 DB를 만들때 한번만 호출된다.
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE friend(" +
                        "mail varchar(30) not null," +
                        "name varchar(20) not null," +
                        "picture varchar(20) not null," +
                        "message varchar(60) not null)");
            }

            // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE friend");
                onCreate(db);
            }

            public void insert(String mail, String name, String picture, String message) {
                // 읽고 쓰기가 가능하게 DB 열기
                SQLiteDatabase db = getWritableDatabase();
                // DB에 입력한 값으로 행 추가
                db.execSQL("delete from friend where mail='"+mail+"'");
                db.execSQL("INSERT INTO friend(mail,name,picture,message) VALUES('"+mail+"', '" + name+ "', '" + picture+ "', '" + message+ "');");
                db.close();
            }

            public String select(){
                SQLiteDatabase db = getReadableDatabase();
                String result = "[";

                Cursor cursor = db.rawQuery("SELECT * FROM friend", null);
                while(cursor.moveToNext()){
                    result += "{\"mail\":\""+cursor.getString(0)+"\",\"name\":\""+cursor.getString(1)+"" +
                            "\",\"picture\":\""+cursor.getString(2)+"\",\"message\":\""+cursor.getString(3)+"\"},";
                }
                result = result.substring(0, result.length()-1);
                result += "]";
                cursor.close();
                db.close();

                return result;
            }

        }

        public DbOpenHelper(Context context){
            this.mCtx = context;
        }

        public DbOpenHelper open() throws SQLException {
            mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
            mDB = mDBHelper.getWritableDatabase();
            return this;
        }



        public void close(){
            mDB.close();
        }

    }

}
