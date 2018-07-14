package com.warbargic.diary_talk.Friend;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.warbargic.diary_talk.Chat.Chatting_room;
import com.warbargic.diary_talk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class friend_profile extends Activity {
    String FriendMail;
    ImageView imageView;
    TextView name_T,mail_T,message_T;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_main_friend_profile);
        Intent intent = getIntent();
        FriendMail = intent.getStringExtra("mail");
//        Log.e("Friendmail","::"+FriendMail);
        imageView = (ImageView)findViewById(R.id.friend_my_profile_edit_imageBtn);
        name_T = (TextView)findViewById(R.id.friend_my_profile_edit_name);
        mail_T = (TextView)findViewById(R.id.friend_my_profile_edit_mail);
        message_T = (TextView)findViewById(R.id.friend_my_profile_message);

        if(intent.getBooleanExtra("chat",false)) {
            ((ImageButton) findViewById(R.id.friend_my_profile_btn)).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.friend_my_profile_text)).setVisibility(View.GONE);
        }


//        (new GetData()).execute();
        Chatting_room.Friend_DB friend_db = new Chatting_room.Friend_DB(getApplicationContext(), "friend.db", null, 1);
        String friend_profile = friend_db.select_profile(FriendMail);
        String friend_name = null,friend_picture = null,friend_message = null;
        try {
            JSONArray jsonArray = new JSONArray(friend_profile);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            friend_name = jsonObject.getString("name");
            friend_picture = jsonObject.getString("picture");
            friend_message = jsonObject.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        name_T.setText(friend_name);
        message_T.setText(friend_message);
        mail_T.setText(FriendMail);
        if(friend_picture.equals("YES"))
            imageView.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/friend/." +
                    ""+FriendMail+".jpg"));


        ((ImageButton)findViewById(R.id.friend_my_profile_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Chatting_room.class);
                intent.putExtra("mail",FriendMail);
                startActivity(intent);
                finish();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), BigImage.class);
                intent1.putExtra("mail",FriendMail);
                startActivity(intent1);
            }
        });




    }


    class DownloadFile extends AsyncTask<String, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            String SERVER_ADDRESS = "";
            HttpURLConnection connection = null;
            URL url = null;

            // 자신 프로필 사진 url가져오기abc
            SERVER_ADDRESS = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/Profile_Image_small/" + FriendMail + ".jpg";

            try{
                url = new URL(SERVER_ADDRESS);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);

            }catch(Exception e){
                e.getMessage();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            imageView.setImageBitmap(bitmap);
        }
    }
    class GetData extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/GetProfile.php";
        String parm = "mail="+FriendMail+"&token=";

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
                read = "["+buffer.readLine()+"]";// JSON
//                String a;
//                while((a=buffer.readLine())!=null){
//                    Log.d(":::","::"+a);
//                }

            }catch (Exception e){
                e.printStackTrace();
            }

            return read;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("ss","::"+s);

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

            name_T.setText(name);
            message_T.setText(message);
            if(picture.equals("YES"))
                (new DownloadFile()).execute();

        }
    }
    class AddFriend extends AsyncTask<String, String, String>{

        SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);

        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/AddFriend.php";
        String parm = "token="+ sharedPreferences.getString("token","") + "&friend=" + FriendMail;

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
                String a;
                while((a = buffer.readLine()) != null){
                    Log.d("a::",":::"+a);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            return read;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            finish();
        }
    }

}
