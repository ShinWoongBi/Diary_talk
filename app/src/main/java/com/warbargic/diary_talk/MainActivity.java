package com.warbargic.diary_talk;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.warbargic.diary_talk.Login.Authentication;
import com.warbargic.diary_talk.Login.Register;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText mail, password;
    CheckBox checkBox;
    public static String real_token = "adsfasdfasd";
    public static String read2;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mail = (EditText)findViewById(R.id.main_mail);
        password = (EditText)findViewById(R.id.main_password);
        checkBox = (CheckBox)findViewById(R.id.activity_main_checkbox);
        verifyStoragePermissions(MainActivity.this);

        final SharedPreferences sharedPreferences = getSharedPreferences("Checked",MODE_PRIVATE);
        Log.d("checkchekc", "ch:"+sharedPreferences.getBoolean("check",false));
        if(sharedPreferences.getBoolean("check",false)){
            finish();
            startActivity(new Intent(getApplicationContext(), main_menu.class));
        }


//        ((TextView)findViewById(R.id.textView2)).setTypeface(Typeface.createFromAsset(getAssets(), "NotoSansCJKkr-Regular.otf"));

        ((TextView)findViewById(R.id.main_register_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });


        ((Button)findViewById(R.id.main_login_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mail.getText().toString().equals("")!=true && password.getText().toString().equals("")!=true){
                    (new Login()).execute();
                }else{
                    Toast.makeText(getApplicationContext(),"모두 입력해 주십시오",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    class Login extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/login/login.php";
        String parm = "mail="+mail.getText().toString()+"&password="+password.getText().toString();

        String read5; // authentication

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            parm = "mail="+mail.getText().toString()+"&password="+password.getText().toString()+"&token="+sharedPreferences1.getString("fcm_token","");
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
                read2 = buffer.readLine();
                Log.d("read2", read2="");
                String a;
                while((a=buffer.readLine())!=null){
                    Log.d(":::","::"+a);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return read;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(!s.equals("0")) {
                if (!s.equals("1")) {
                    Log.e("token","::"+s);
                    Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("token", s);
                    System.out.println(s);
                    editor.apply();
                    real_token = s;

                    Log.d("service","start");


                    SharedPreferences sharedPreferences1 = getSharedPreferences("Checked", MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                    if(checkBox.isChecked()){
                        editor1.putBoolean("check",true);
                        Log.d("checkchekc", "ch:"+sharedPreferences1.getBoolean("check",false));
//                        finish();
                    }else{
                        editor1.putBoolean("check",false);
                    }
                    editor1.apply();

                    (new GetMyProfile()).execute();
                }else{

                    com.warbargic.diary_talk.Login.Authentication authentication = new Authentication(MainActivity.this);
                    authentication.show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"로그인 실패",Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AutoLogin extends AsyncTask<String, String, String> {
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/login/check_token.php";
        String parm = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
            parm = "token=" + sharedPreferences.getString("token", "");
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
                read = buffer.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return read;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s.equals("1")){
                startActivity(new Intent(getApplicationContext(), main_menu.class));
                com.warbargic.diary_talk.Login.Authentication authentication = new Authentication(MainActivity.this);
                authentication.show();
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "잘못된 토큰값 입니다",Toast.LENGTH_SHORT).show();
            }
        }
    }

    class GetMyProfile extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/GetProfile.php";
        String parm = "";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this,"","로그인중...",true);


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

            SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", name);
            editor.putString("mail", mail);
            editor.putString("message", message);
            editor.putString("join_date", join_date);
            editor.putString("picture", picture);
            editor.putString("authentication", authentication);
            editor.apply();

            if (picture.equals("YES")){
                (new GetMyPicture()).execute();
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

            if(s != null) {
                File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk");
                if (!file1.exists())
                    file1.mkdir();
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/.MY_PICTURE.jpg");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    s.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            progressDialog.dismiss();
            if(checkBox.isChecked()){
                finish();
            }
            startActivity(new Intent(getApplicationContext(), main_menu.class));

        }
    }




    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void  verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

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
