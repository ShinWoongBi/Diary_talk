package com.warbargic.diary_talk.Chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.warbargic.diary_talk.R;

/**
 * Created by kippe_000 on 2017-02-11.
 */

public class MakeToast extends Toast {
    Context context;
    int room_num;

    public MakeToast(Context context) {
        super(context);

        this.context = context;
    }

    public MakeToast(Context context, int room_num){
        super(context);

        this.context = context;
        this.room_num = room_num;
    }


    public void showToast(String message, String name, String mail){
        LayoutInflater inflater = null;
        View v = null;
        if(inflater == null){
            Activity act = (Activity)context;
            inflater = act.getLayoutInflater();
            v = inflater.inflate(R.layout.make_toast, null);
        }


        TextView name_T = (TextView)v.findViewById(R.id.make_toast_name);
        TextView message_T = (TextView)v.findViewById(R.id.make_toast_message);
        ImageView imageView = (ImageView)v.findViewById(R.id.make_toast_image);


        if(room_num == 0){
            name_T.setText(name);
            message_T.setText(message);
            imageView.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"" +
                    "/diary_talk/friend/."+mail+".jpg"));
        }else{
            room_DB room_db = new room_DB(context, "room.db", null, 1);
            name = room_db.get_name(room_num);
            int result = room_db.get_image(room_num);

            name_T.setText(name);
            message_T.setText(message);
            if(result == 0){
                imageView.setImageResource(R.mipmap.ic_launcher);
            }else{
                imageView.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/chat/."+room_num+".jpg"));
            }
        }
        Show(this, v);
    }

    private void Show(Toast toast, View v){
        toast.setGravity(Gravity.TOP,0,400);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(v);
        toast.show();
    }
}
