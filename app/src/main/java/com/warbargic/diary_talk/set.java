package com.warbargic.diary_talk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.warbargic.diary_talk.Chat.Chatting_room;
import com.warbargic.diary_talk.Chat.MakeToast;
import com.warbargic.diary_talk.Diary.DataBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kippe_000 on 2017-02-02.
 */

public class set extends Activity {
    Button logout;
    Button btn;
    EditText mail_E, password_E;
    boolean change_password = false;
    Switch Switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_activity);

        logout = (Button)findViewById(R.id.set_logout);
        btn = (Button)findViewById(R.id.button2);
        mail_E = (EditText)findViewById(R.id.my_mail);
        password_E = (EditText)findViewById(R.id.password);
        Switch = (Switch)findViewById(R.id.switch1);

        final SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE", MODE_PRIVATE);
        String my_mail = sharedPreferences.getString("mail", "");
        Boolean alarm_B = sharedPreferences.getBoolean("alarm", true);
        mail_E.setText(my_mail);


        if(alarm_B == true){
            Switch.setChecked(true);
        }else{
            Switch.setChecked(false);
        }
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    editor.putBoolean("alarm", true);
                    editor.commit();
                }else{
                    editor.putBoolean("alarm", false);
                    editor.commit();
                }

//                Toast.makeText(set.this, sharedPreferences.getBoolean("alarm", true)+"", Toast.LENGTH_SHORT).show();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(change_password == false) {
                    Check_password check_password = new Check_password(mail_E.getText().toString(), password_E.getText().toString());
                    check_password.execute();
                }else{
                    Change_password change_password = new Change_password();
                    change_password.execute();
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                SharedPreferences sharedPreferences2 = getSharedPreferences("TOKEN", MODE_PRIVATE);
                SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                editor2.putString("token", null);
                editor.apply();

                SharedPreferences sharedPreferences3 = getSharedPreferences("Checked", MODE_PRIVATE);
                SharedPreferences.Editor editor3 = sharedPreferences3.edit();
                editor3.putBoolean("check", false);
                editor3.apply();

                DeleteDir(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/");

                com.warbargic.diary_talk.Chat.chat_DB chat_db = new com.warbargic.diary_talk.Chat.chat_DB(getApplicationContext(), "chat.db", null,1);
                chat_db.delete();

                DataBase dataBase = new DataBase(getApplicationContext(), "diary.db", null, 1);
                dataBase.delete();

                DataBase dataBase_gps = new DataBase(getApplicationContext(), "diary_gps.db", null, 1);
                dataBase_gps.delete_gps();

                Chatting_room.DbOpenHelper dbOpenHelper = new Chatting_room.DbOpenHelper(getApplicationContext());
                dbOpenHelper.open();
                dbOpenHelper.mDBHelper.delete_friend();


                finish();
                Intent intent = new Intent(getApplicationContext(), main_menu.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
        });
    }

    class Check_password extends AsyncTask<String, String, String>{
        String mail, password;

        Check_password(String mail, String password){
            this.mail = mail;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... strings) {

            String parm = "mail="+mail+"&password="+password;

            URL url = null;
            HttpURLConnection httpURLConnection = null;
            String buffer = null;
            try{

                url = new URL("http://tlsdndql27.vps.phps.kr/Diary_talk/login/login.php");
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(parm.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                buffer = bufferedReader.readLine();


            }catch (Exception e){
                e.printStackTrace();
            }

            return buffer;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(!s.equals("0")){
                mail_E.setFocusable(true);
                mail_E.setClickable(true);
                mail_E.setText("");
                mail_E.setHint("비밀번호(8~16자 영문 대 소문자, 숫자, 특수문자)");
                mail_E.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mail_E.setTransformationMethod(PasswordTransformationMethod.getInstance());
                password_E.setText("");
                password_E.setHint("비밀번호 재입력");

                change_password = true;
                MakeToast.makeText(getApplicationContext(), "인증완료", Toast.LENGTH_SHORT).show();
            }else{
                MakeToast.makeText(getApplicationContext(), "인증실패", Toast.LENGTH_SHORT).show();
            }
        }
    }


    class Change_password extends AsyncTask<String, String, String>{
        String password;
        String password_check;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            password = mail_E.getText().toString();
            password_check = password_E.getText().toString();
        }

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE", MODE_PRIVATE);
            String my_mail = sharedPreferences.getString("mail", "");
            String parm = "password="+password+"&mail="+my_mail;

            URL url = null;
            HttpURLConnection httpURLConnection = null;
            String buffer = null;
            try{

                url = new URL("http://tlsdndql27.vps.phps.kr/Diary_talk/change_password.php");
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(parm.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                buffer = bufferedReader.readLine();
//                while((buffer=bufferedReader.readLine()) != null){
//                    Log.d("buffer", buffer+"");
//                }


            }catch (Exception e){
                e.printStackTrace();
            }


            return buffer;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("token",s+"");

            String token = s;
            change_password = false;

            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token", token);
            System.out.println(token);
            editor.apply();

            final SharedPreferences my_sharedPreferences = getSharedPreferences("MY_PROFILE", MODE_PRIVATE);
            String my_mail = my_sharedPreferences.getString("mail", "");
            Boolean alarm_B = my_sharedPreferences.getBoolean("alarm", true);
            mail_E.setText(my_mail);
            mail_E.setClickable(false);
            mail_E.setFocusable(false);
            mail_E.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
            mail_E.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password_E.setText("");
            password_E.setHint("비밀번호(8~16자리)");

            MakeToast.makeText(getApplicationContext(), "비밀번호 변경 완료", Toast.LENGTH_SHORT).show();
        }
    }


    void DeleteDir(String path)
    {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        for(File childFile : childFileList)
        {
            if(childFile.isDirectory()) {
                DeleteDir(childFile.getAbsolutePath());     //하위 디렉토리 루프
            }
            else {
                childFile.delete();    //하위 파일삭제
            }
        }
        file.delete();    //root 삭제
    }
}
