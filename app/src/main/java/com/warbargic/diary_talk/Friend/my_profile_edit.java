package com.warbargic.diary_talk.Friend;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warbargic.diary_talk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kippe_000 on 2017-01-06.
 */

public class my_profile_edit extends AppCompatActivity {
    Uri mlmageCaptureUri = null;
    Bitmap photo = null;
    ImageButton image;
    String small;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    LinearLayout message_btn;
    SharedPreferences sharedPreferences;
    TextView join_date_T, mail_T, message_T;
    Button name_T;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_my_profile_edit);
        verifyStoragePermissions(this);
        join_date_T = (TextView)findViewById(R.id.friend_my_profile_edit_JoinDate);
        mail_T = (TextView)findViewById(R.id.friend_my_profile_edit_mail);
        image = (ImageButton)findViewById(R.id.friend_my_profile_edit_imageBtn);
        message_btn = (LinearLayout)findViewById(R.id.friend_my_profile_edit_message_btn);
        message_T = (TextView)findViewById(R.id.friendmy_profile_edit_message);
        name_T = (Button)findViewById(R.id.friend_my_profile_edit_name);


        name_T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), set_name.class));
            }
        });
        message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), set_message.class));
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener carmeraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doTakePhotoAction();
                    }
                };

                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doTakeAlbumAction();
                    }
                };

                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                };

                if(!my_profile_edit.this.isFinishing()) {

                    AlertDialog.Builder show = new AlertDialog.Builder(my_profile_edit.this);
                    show.setTitle("업로드할 이미지 선택");
                    show.setNeutralButton("취소", cancelListener);
                    show.setNegativeButton("사진촬영", carmeraListener);
                    show.setPositiveButton("앨범선택", albumListener);
                    show.show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

//        (new GetMyProfile()).execute();
        SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE",Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name","");
        String message = sharedPreferences.getString("message","");
        String picture = sharedPreferences.getString("picture","");
        String join_date = sharedPreferences.getString("join_date","");
        String mail = sharedPreferences.getString("mail","");
        name_T.setText(name);
        message_T.setText(message);
        join_date_T.setText(join_date);
        mail_T.setText(mail);
        if(picture.equals("YES")) {
            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE.jpg");
            image.setImageBitmap(bitmap);
        }else {
            image.setBackgroundResource(R.mipmap.ic_launcher);
        }




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK)return;

        switch (requestCode){
            case 1:
                mlmageCaptureUri = data.getData();
            case 0:
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mlmageCaptureUri, "image/*");

                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 2);
                intent.putExtra("aspectY", 2);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, 2);
                break;
            case 2:

                if(resultCode != RESULT_OK)break;
                Bundle extras = data.getExtras();

                if(extras != null){
                    photo = extras.getParcelable("data");
                    image.setImageBitmap(photo);

                    String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk";
                    File file = new File(dirPath);
                    String name = "MY_PICTURE_COPY.jpg";
                    if(!file.exists())
                        file.mkdir();
                    File copy = new File(dirPath+"/."+name);
                    Log.e("copyfile", dirPath+"/."+name);
                    small = dirPath+"/."+name;
                    BufferedOutputStream out = null;
                    try {
                        copy.createNewFile();
                        out = new BufferedOutputStream(new FileOutputStream(copy));
                        photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(copy)));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                    Cursor c= getContentResolver().query(mlmageCaptureUri, new String[]{MediaStore.Images.Media.DATA},null,null,null);
                    int column_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    c.moveToFirst();
                    String absolutePath = c.getString(column_index);
                    Log.e("absolutePath", "::"+absolutePath);


                    Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                    absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE_COPY_BIG.jpg";
                    File file1 = new File(absolutePath);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file1);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    small = absolutePath;

                    UploadFile uploadFile1 = new UploadFile(absolutePath);
                    uploadFile1.execute();


                }


                break;
        }
    }

    void doTakeAlbumAction(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, 1);
    }

    void doTakePhotoAction(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String url = "tmp" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mlmageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mlmageCaptureUri);
        startActivityForResult(intent, 0);
    }




    class UploadFile extends AsyncTask<String, String, String>{
        String file_path = null;
        String Server = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/UploadToServer.php";

        UploadFile(String path){
            this.file_path = path;
//            this.file_path = "/storage/emulated/0/DCIM/Screenshots/Screenshot_20170110-114425.jpg";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(my_profile_edit.this,"","업로드 중...");
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            URL url = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            File file = new File(file_path);
            if (!file.isFile()) {
                dialog.dismiss();
                Log.e("uploadFile", "Source File not exist :"+ file_path);
            }else{
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    url = new URL(Server);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file", file_path);

                    SharedPreferences sharedPreferences = getSharedPreferences("TOKEN",MODE_PRIVATE);
                    String token = sharedPreferences.getString("token","");

                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//                    outputStream.write(("mail="+sharedPreferences.getString("mail","")).getBytes("UTF-8"));
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\""+token+"\""+lineEnd);
                    outputStream.writeBytes(lineEnd);

                    int available = fileInputStream.available(); // 파일 크기
                    int bufferSize = Math.min(available, 1 * 1024 * 1024); // 1M와 선택한 파일 크기 비교해서 작은쪽 반환
                    byte[] buffer = new byte[bufferSize];

                    while((fileInputStream.read(buffer, 0 ,bufferSize)) > 0){
                        outputStream.write(buffer, 0 ,bufferSize);
                        available = fileInputStream.available();
                        bufferSize = Math.min(available, 1*1024*1024);
                        buffer = new byte[bufferSize];
                        Log.e("asfd","asdf");
                    }
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    outputStream.flush();

//                    outputStream.write(("token="+sharedPreferences.getString("token","")).getBytes("UTF-8"));
//                    outputStream.flush();
                    outputStream.close();

                    Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE_COPY.jpg");
                    File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE.jpg");
                    FileOutputStream fileOutputStream = new FileOutputStream(file1);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    File copy = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE_COPY.jpg");
//                    copy.delete();


                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String a = reader.readLine();
                    Log.d("a", ":::" + a);
                    while((a=reader.readLine()) != null){
                        Log.d("a", ":::" + a);
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            UploadFile2 uploadFile2 = new UploadFile2(small);
            uploadFile2.execute();

        }
    }
    class UploadFile2 extends AsyncTask<String, String, String>{
        String file_path = null;
        String Server = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/UploadToServerSmall.php";

        UploadFile2(String path){
//            this.file_path = path;
            this.file_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE_COPY.jpg";
//            this.file_path = "/storage/emulated/0/DCIM/Screenshots/Screenshot_20170110-114425.jpg";
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            URL url = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            File file = new File(file_path);
            if (!file.isFile()) {
                Log.e("uploadFile", "Source File not exist :"+ file_path);
            }else{
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    url = new URL(Server);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file", file_path);


                    SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", MODE_PRIVATE);
                    String token = sharedPreferences.getString("token","");
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\""+token+"\""+lineEnd);
                    outputStream.writeBytes(lineEnd);

                    int available = fileInputStream.available(); // 파일 크기
                    int bufferSize = Math.min(available, 1 * 1024 * 1024); // 1M와 선택한 파일 크기 비교해서 작은쪽 반환
                    byte[] buffer = new byte[bufferSize];

                    while((fileInputStream.read(buffer, 0 ,bufferSize)) > 0){
                        outputStream.write(buffer, 0 ,bufferSize);
                        available = fileInputStream.available();
                        bufferSize = Math.min(available, 1*1024*1024);
                        buffer = new byte[bufferSize];
                        Log.e("asfd","asdf");
                    }
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    outputStream.flush();
                    outputStream.close();



                    File copy_big = new File(file_path);
                    copy_big.delete();


                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String a = reader.readLine();
                    Log.d("a", ":::" + a);
                    while((a=reader.readLine()) != null){
                        Log.d("a", ":::" + a);
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE.jpg");
            image.setImageBitmap(bitmap);
            progressDialog.dismiss();
        }
    }
    class GetMyPicture extends AsyncTask<String, String, Bitmap>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/get_myprofile_picture.php";
        String parm = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN", Context.MODE_PRIVATE);
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

//            image.setImageBitmap(s);
        }
    }
    class GetMyProfile extends AsyncTask<String, String, String>{
        String url_s = "http://tlsdndql27.vps.phps.kr/Diary_talk/friend/GetProfile.php";
        String parm = "";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sharedPreferences = getSharedPreferences("TOKEN",Context.MODE_PRIVATE);
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
                image.setBackgroundResource(R.mipmap.ic_launcher);
            }


        }
    }


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
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
