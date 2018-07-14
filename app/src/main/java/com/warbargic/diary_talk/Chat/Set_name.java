package com.warbargic.diary_talk.Chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.warbargic.diary_talk.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Set_name extends Activity {
    Uri mlmageCaptureUri = null;
    EditText name_edit;
    int room_num;
    ImageButton imageButton;
    room_DB room_db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_set_name);
        name_edit = (EditText)findViewById(R.id.set_message_edit);
        imageButton = (ImageButton)findViewById(R.id.image_btn);
        room_db = new room_DB(getApplicationContext(), "room.db", null, 1);

        Intent intent = getIntent();
        room_num = intent.getIntExtra("room_num",0);
        String name = intent.getStringExtra("name");
        Log.d("num", room_num+"");
        Log.d("name", name+"");

        if(room_db.get_image(room_num) == 1){
            imageButton.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/chat/."+room_num+".jpg"));
        }else{
            imageButton.setImageResource(R.mipmap.ic_launcher);
        }
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                if(!Set_name.this.isFinishing()) {

                    AlertDialog.Builder show = new AlertDialog.Builder(Set_name.this);
                    show.setTitle("업로드할 이미지 선택");
                    show.setNeutralButton("취소", cancelListener);
                    show.setNegativeButton("사진촬영", carmeraListener);
                    show.setPositiveButton("앨범선택", albumListener);
                    show.show();
                }
            }
        });

        name_edit.setText(name);
        ((TextView)findViewById(R.id.set_message_count)).setText(name_edit.getText().toString().length()+"/20");


        name_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((TextView)findViewById(R.id.set_message_count)).setText(name_edit.getText().length()+"/20");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ((Button)findViewById(R.id.set_message_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_edit.getText().toString();
                room_DB room_db = new room_DB(getApplicationContext(), "room.db", null, 1);
                room_db.update_name(room_num, name);
                room_db.update_image(room_num);
                finish();
            }
        });
    }



    void doTakePhotoAction(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String url = "tmp" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mlmageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mlmageCaptureUri);
        startActivityForResult(intent, 0);
    }

    void doTakeAlbumAction(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("request", requestCode+"");
        Bitmap photo;

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

                if(resultCode != RESULT_OK)
                    break;

                Bundle extras = data.getExtras();

                if(extras != null){
                    photo = extras.getParcelable("data");
                    imageButton.setImageBitmap(photo);

                    String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/chat";
                    Log.d("dirpath", dirPath);
                    File file = new File(dirPath);
                    String name = room_num+".jpg";
                    Log.d("name", name);
                    if(!file.exists())
                        file.mkdir();
                    File copy = new File(dirPath+"/."+name);
                    Log.e("copyfile", dirPath+"/."+name);
                    BufferedOutputStream out = null;
                    try {
                        copy.createNewFile();
                        out = new BufferedOutputStream(new FileOutputStream(copy));
                        photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(copy)));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }


                break;
        }
    }
}
