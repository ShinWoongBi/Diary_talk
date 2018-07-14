package com.warbargic.diary_talk.Friend;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.warbargic.diary_talk.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kippe_000 on 2017-01-07.
 */

public class set_message extends Activity {
    EditText message_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_message);
        message_edit = (EditText)findViewById(R.id.set_message_edit);

        SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE",MODE_PRIVATE);
        String message = sharedPreferences.getString("message", "");
        message_edit.setText(message);
        ((TextView)findViewById(R.id.set_message_count)).setText(message_edit.getText().length()+"/60");

//        (new Get_message()).execute();

        message_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((TextView)findViewById(R.id.set_message_count)).setText(message_edit.getText().length()+"/60");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ((Button)findViewById(R.id.set_message_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Change_message()).execute();
            }
        });


    }


    class Get_message extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/get_message.php";
        String parm = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN",MODE_PRIVATE);
            parm = "token="+sharedPreferences.getString("token","");
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
                read = buffer.readLine();
            }catch (Exception e){
                e.printStackTrace();
            }

            return read;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            message_edit.setText(s);
            ((TextView)findViewById(R.id.set_message_count)).setText(message_edit.getText().length()+"/60");
        }
    }
    class Change_message extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/set_message.php";
        String parm = "";
        String token = null;
        SharedPreferences sharedPreferences;
        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            message = message_edit.getText().toString();
            sharedPreferences = getSharedPreferences("TOKEN",MODE_PRIVATE);
            token = sharedPreferences.getString("token","");

            parm = "token="+token+"&message="+message;
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
                read = buffer.readLine();
            }catch (Exception e){
                e.printStackTrace();
            }

            return read;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token", s);
            editor.apply();

            SharedPreferences sharedPreferences1 = getSharedPreferences("MY_PROFILE",MODE_PRIVATE);
            SharedPreferences.Editor editor1 = sharedPreferences1.edit();
            editor1.putString("message", message);
            editor1.apply();
            finish();
        }
    }
}
