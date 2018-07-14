package com.warbargic.diary_talk.Login;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.warbargic.diary_talk.MainActivity;
import com.warbargic.diary_talk.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kippe_000 on 2017-01-13.
 */

public class Authentication extends Dialog {
    Button send_btn, check_btn;
    EditText editText;

    public Authentication(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_activity);
        send_btn = (Button)findViewById(R.id.authentication_SendMail);


        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                (new Send()).execute();
            }
        });


    }


    class Send extends AsyncTask<String, String, String> {
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/login/mail.php";
        String parm = "token="+ MainActivity.read2;

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
//                read = buffer.readLine();
                while((read = buffer.readLine()) != null){
                    Log.d("read", read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return read;
        }
    }

    class Check extends AsyncTask<String, String, String> {
        String url_s = "";
        String parm = "";

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
                read = buffer.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return read;
        }
    }

}
