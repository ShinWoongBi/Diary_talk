package com.warbargic.diary_talk.Friend;

import android.app.Activity;
import android.content.Context;
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

/**
 * Created by kippe_000 on 2017-01-06.
 */

public class my_profile extends Activity {
    ImageView imageView;
    TextView name_T;
    TextView mail_T;
    TextView message_T;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_my_profile);
        imageView = (ImageView)findViewById(R.id.friend_my_profile_edit_imageBtn);
        name_T = (TextView)findViewById(R.id.friend_my_profile_edit_name);
        mail_T = (TextView)findViewById(R.id.friend_my_profile_edit_mail);
        message_T = (TextView)findViewById(R.id.friend_my_profile_message);

        ((ImageButton)findViewById(R.id.friend_my_profile_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), my_profile_edit.class));
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(my_profile.this, BigImage.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String message = sharedPreferences.getString("message", "");
        String mail = sharedPreferences.getString("mail", "");
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE.jpg");
        imageView.setImageBitmap(bitmap);
        name_T.setText(name);
        message_T.setText(message);
        mail_T.setText(mail);

//        (new GetMyProfile()).execute();
    }

    class GetMyProfile extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/GetProfile.php";
        String parm = "";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", Context.MODE_PRIVATE);
            parm = "token="+sharedPreferences.getString("token","");
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
            name_T.setText(name);
            message_T.setText(message);
            if(picture.equals("YES")) {
                (new GetMyPicture()).execute();
            }else {
                imageView.setBackgroundResource(R.mipmap.ic_launcher);
            }


        }
    }
    class GetMyPicture extends AsyncTask<String, String, Bitmap>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/get_myprofile_picture.php";
        String parm = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN",Context.MODE_PRIVATE);
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

            imageView.setImageBitmap(s);
        }
    }

}
