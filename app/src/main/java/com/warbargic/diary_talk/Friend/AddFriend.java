package com.warbargic.diary_talk.Friend;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.warbargic.diary_talk.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kippe_000 on 2017-01-11.
 */

public class AddFriend extends Activity {
    EditText editText;
    Button search_btn;
    String FriendMail;
    int searchedFried = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_addfriend);

        editText = (EditText)findViewById(R.id.friend_addfriend_edit);
        search_btn = (Button)findViewById(R.id.friend_addfriend_btn);


        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendMail = editText.getText().toString();

                if (!FriendMail.equals(""))
                    (new SearchFriend()).execute();
                else{
                    Toast.makeText(getApplicationContext(), "상대방의 메일주소를 입력해주세요",Toast.LENGTH_SHORT).show();
                }
            }
        });

        ((LinearLayout)findViewById(R.id.friend_main_item_linear)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchedFried == 1){
                    Intent intent = new Intent(getApplicationContext(), AddFriend_profile.class);
                    intent.putExtra("mail", FriendMail);
                    startActivity(intent);
                }
            }
        });
    }

    class SearchFriend extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/Search.php";
        String parm = "mail="+FriendMail;
        String picture, name, message;
        Bitmap bitmap;

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            try{
                url = new URL(url_s);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(parm.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
                picture = buffer.readLine();
                name = buffer.readLine();
                message = buffer.readLine();
            }catch (Exception e){
                e.printStackTrace();
            }

            if(picture.equals("YES")) {
                url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/Profile_Image_small/" + FriendMail + ".jpg";

                try {
                    url = new URL(url_s);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.connect();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);

                } catch (Exception e) {
                    e.getMessage();
                }
            }


            return null;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            LinearLayout linearLayout = ((LinearLayout) findViewById(R.id.friend_main_item_linear));

            if(!name.equals("")) {


                ImageView image_view = (ImageView) findViewById(R.id.friend_main_item_image);

                ViewGroup.LayoutParams parm = linearLayout.getLayoutParams();
                int height = parm.height;
                Log.d("width", "width:" + height);

                ViewGroup.LayoutParams params = image_view.getLayoutParams();
                params.width = height;
                image_view.setLayoutParams(params);
                image_view.setImageResource(R.mipmap.ic_launcher);

                if (picture.equals("YES")) {
                    image_view.setImageBitmap(bitmap);
                } else {
                    image_view.setImageResource(R.mipmap.ic_launcher);
                }
                ((TextView) findViewById(R.id.friend_main_item_name)).setText(name);
                ((TextView) findViewById(R.id.friend_main_item_message)).setText(message);

                linearLayout.setVisibility(View.VISIBLE);
                searchedFried = 1;
            }else{
                linearLayout.setVisibility(View.INVISIBLE);
                searchedFried = 0;
            }
        }
    }

}
