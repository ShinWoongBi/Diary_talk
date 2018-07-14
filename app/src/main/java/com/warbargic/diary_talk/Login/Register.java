package com.warbargic.diary_talk.Login;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.warbargic.diary_talk.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kippe_000 on 2017-01-04.
 */

public class Register extends Activity {
    EditText mail, password, password_check, name;
    Button register_btn, register_check_btn;
    int able_mail, able_mail_format, able_password, able_password_format, able_name = 0;
    static public String user_mail;
    Matcher matcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        // view 선언
        mail = (EditText)findViewById(R.id.register_mail);
        password = (EditText)findViewById(R.id.register_password);
        password_check = (EditText)findViewById(R.id.register_password_check);
        name = (EditText)findViewById(R.id.register_name);
        register_btn = (Button)findViewById(R.id.main_login_btn);
        register_check_btn = (Button)findViewById(R.id.register_check_btn);


        register_check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mail.getText().toString().equals("") != true && able_mail_format == 1) {
                    (new Check()).execute();
                }else if(able_mail_format == 0){
                    Toast.makeText(getApplicationContext(), "이메일을 정확하게 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "이메일을 입력해 주십시오", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                register_check_btn.setBackgroundColor(Color.parseColor("#fabbd2"));
                register_check_btn.setClickable(true);
                register_btn();
                Pattern pattern_mail = Pattern.compile( "^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_.-])+\\.([a-zA-Z])+([a-zA-Z])+");
                matcher = pattern_mail.matcher(mail.getText().toString());
                if(matcher.matches()){
                    mail.setTextColor(Color.parseColor("#FABBD2"));
                    able_mail_format = 1;
                }else{
                    mail.setTextColor(Color.parseColor("#FF0000"));
                    able_mail_format = 0;
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                register_btn();

            }
        });


        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Toast.makeText(Register.this, "8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.", Toast.LENGTH_LONG).show();
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password_check.setTextColor(Color.parseColor("#FF0000"));
                able_password = 0;

                if(password.getText().toString().equals(password_check.getText().toString()) && !password_check.getText().toString().equals("")){
                    password_check.setTextColor(Color.parseColor("#ffd0e1"));
                    name.requestFocus();
                    able_password = 1;
                }else{
                    password_check.setTextColor(Color.parseColor("#FF0000"));
                    able_password = 0;
                }
                register_btn();

                Pattern pattern_password = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W])[^\\s]{6,16}$");
                Log.d("password", password.getText().toString());
                Matcher matcher = pattern_password.matcher(password.getText().toString());
                if(matcher.matches()){
                    able_password_format = 1;
                    password.setTextColor(Color.parseColor("#ffd0e1"));
                }else{
                    able_password_format = 0;
                    password.setTextColor(Color.parseColor("#FF0000"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                register_btn();

            }
        });

        password_check.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(password.getText().toString().equals(password_check.getText().toString())){
                    password_check.setTextColor(Color.parseColor("#ffd0e1"));
                    name.requestFocus();
                    if(able_password_format == 1) {
                        able_password = 1;
                    }
                }else{
                    password_check.setTextColor(Color.parseColor("#FF0000"));
                    able_password = 0;
                }
                register_btn();
            }

            @Override
            public void afterTextChanged(Editable s) {
                register_btn();

            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                able_name = 0;
                register_btn();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(name.getText().toString().equals("") != true) {
                    able_name = 1;
                    register_btn();
                }
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(able_mail == 1 && able_password == 1 && name.getText().toString().equals("") != true){
                    (new register()).execute();
                }
            }
        });

    }

    void register_btn(){
        if(able_mail == 1 && able_password == 1 && able_name == 1){
            register_btn.setBackgroundColor(Color.parseColor("#fabbd2"));
            register_btn.setClickable(true);
        }else{
            register_btn.setBackgroundColor(Color.parseColor("#ffd0e1"));
            register_btn.setClickable(false);
        }
    }


    class register extends AsyncTask<Void, Void, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/login/register.php";
        String parm = "mail="+mail.getText().toString()+"&password="+password_check.getText().toString()+"&name="+name.getText().toString();

        @Override
        protected String doInBackground(Void... params) {
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
                String a = null;
//                while((a = buffer.readLine()).equals("")!=true){
//                    Log.d("a","::"+a);
//                }
              read = buffer.readLine();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "인터넷을 사용도중 문제가 생겼습니다", Toast.LENGTH_SHORT).show();
            }

            return read;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("1")) {
                Toast.makeText(getApplicationContext(), "성공적으로 가입되었습니다", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "가입을 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class Check extends AsyncTask<BufferedReader, String, String> {
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/login/check_mail.php";
        String parm = "mail="+mail.getText().toString();

        @Override
        protected String doInBackground(BufferedReader... params) {
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

            if(s.equals("1")) {
                register_check_btn.setBackgroundColor(Color.parseColor("#ffd0e1"));
                register_check_btn.setClickable(false);
                Toast.makeText(getApplicationContext(), "사용하실 수 있는 계정입니다", Toast.LENGTH_LONG).show();
                password.requestFocus();
                user_mail = mail.getText().toString();
                able_mail = 1;
                register_btn();
            }else{
                Toast.makeText(getApplicationContext(), "사용하실 수 없는 계정입니다", Toast.LENGTH_LONG).show();

            }

            Log.d("read", s+":::::::");
        }
    }



}
