package com.warbargic.diary_talk.Chat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.warbargic.diary_talk.MainActivity;
import com.warbargic.diary_talk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.warbargic.diary_talk.Chat.main.handler;


public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //추가한것
        sendNotification(remoteMessage.getData().get("message"));
    }

    private void sendNotification(String messageBody) {
        Log.e("friebase message",":"+messageBody);
        String message = null;
        String mail = null;
        String time = null;
        int room_num = 0;
        String member = null;
        try {
            JSONArray jsonArray = new JSONArray(messageBody);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            message = jsonObject.getString("message");
            mail = jsonObject.getString("mail");
            time = jsonObject.getString("time");
            room_num = jsonObject.getInt("room_num");
            member = jsonObject.getString("member");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("room_num",room_num+"");
        if(room_num == 0) {
            chat_DB chat_DB = new chat_DB(MyFirebaseMessagingService.this, "chat.db", null, 1);
            chat_DB.insert(mail, mail, message, time);
            chat_DB.close();
        }else{
            Log.d("room_num","room");
            chat_DB chat_DB = new chat_DB(MyFirebaseMessagingService.this, "chat.db", null, 1);
            chat_DB.insert_room(room_num,mail,message,time,member);
        }

        if(room_num == 0) {

            SharedPreferences sharedPreferences = getSharedPreferences("count", MODE_PRIVATE);
            int count = sharedPreferences.getInt(mail + "~count", 0);
            count++;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(mail + "~count", count);
            editor.apply();
        }else{
            SharedPreferences sharedPreferences = getSharedPreferences("count", MODE_PRIVATE);
            int count = sharedPreferences.getInt(room_num + "~count", 0);
            count++;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(room_num + "~count", count);
            editor.apply();
        }

        Chatting_room.Friend_DB friend_db = new Chatting_room.Friend_DB(MyFirebaseMessagingService.this, "friend.db", null, 1);

        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("name", friend_db.select_name(mail));
        msg.setData(bundle);
        handler.sendMessage(msg);
//        (new MakeToast(getApplicationContext())).showToast(message, friend_db.select_name(mail), mail);


        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MY_PROFILE", Context.MODE_PRIVATE);
        boolean alarm = sharedPreferences.getBoolean("alarm", true);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        room_DB room_db = new room_DB(getApplicationContext(), "room.db", null, 1);

        notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if(alarm){
            notificationBuilder.setSound(defaultSoundUri);
        }else{
            notificationBuilder.setSound(null);
        }

        if(room_num != 0){
            notificationBuilder.setContentTitle(room_db.get_name(room_num));
            if(room_db.get_image(room_num) == 1) {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/chat/."+room_num+".jpg"));
            }else{
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            }
        }else{
            notificationBuilder.setContentTitle(friend_db.select_name(mail));
            notificationBuilder.setLargeIcon(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/friend/."+mail+".jpg"));
        }




        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }



}