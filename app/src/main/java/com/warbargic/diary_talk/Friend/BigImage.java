package com.warbargic.diary_talk.Friend;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import com.warbargic.diary_talk.R;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kippe_000 on 2017-01-12.
 */

public class BigImage extends Activity {
    String FriendMail;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bigimage_activity);
        Intent intent = getIntent();
        FriendMail = intent.getStringExtra("mail");
        imageView = (ImageView)findViewById(R.id.bigimageView);

        (new DownloadFile()).execute();
    }

    class DownloadFile extends AsyncTask<String, String, Bitmap> {
        String parm;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (FriendMail != null) {
                parm = "mail="+FriendMail;
            } else {
                SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                parm = "token=" + sharedPreferences.getString("token", "");
            }
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            String SERVER_ADDRESS = "";
            HttpURLConnection connection = null;
            URL url = null;

            SERVER_ADDRESS = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/big_picture.php";

            try{
                url = new URL(SERVER_ADDRESS);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(parm.getBytes("UTF-8"));
                outputStream.flush();

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
}
