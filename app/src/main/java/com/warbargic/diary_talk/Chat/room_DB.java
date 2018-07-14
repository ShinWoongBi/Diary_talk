package com.warbargic.diary_talk.Chat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kippe_000 on 2017-05-31.
 */

public class room_DB extends SQLiteOpenHelper {
    public room_DB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE room(" +
                "room_num integer not null," +
                "name varchar(20) not null," +
                "picture integer not null)";
        sqLiteDatabase.execSQL(sql);
    }

    public void insert_room(String Mails, int room_num, Context context){
        Chatting_room.DbOpenHelper dbOpenHelper = new Chatting_room.DbOpenHelper(context);
        dbOpenHelper.open();

        String name = "";
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(dbOpenHelper.mDBHelper.select_mails(Mails));

            JSONObject jsonObject = null;
            Chatting_room.Data data = null;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = (JSONObject) jsonArray.get(i);

                name += jsonObject.getString("name");

                if(i < jsonArray.length()-1)
                    name+=",";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("name", name);
        Log.d("room_num", room_num+"");

        String sql = "INSERT INTO room VALUES("+room_num+",'"+name+"',0)";
        Log.d("sql", sql);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }

    public String get_name(int room_num){
        String name = "";

        String sql = "SELECT name FROM room where room_num="+room_num;
        Log.d("sql", sql+"");
        Log.d("room_num", room_num+"");
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        cursor.moveToNext();
        if(cursor.getCount() == 0){
            name = null;
        }else {
            name = cursor.getString(0);
        }
        Log.d("name",name+"");
        return name;
    }

    public void update_name(int room_num, String name){
        String sql = "UPDATE room set name='"+name+"' where room_num="+room_num;
        Log.d("sql", sql);

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL(sql);
    }

    public int get_image(int room_num){
        int result = 0;

        String sql = "SELECT picture from room where room_num="+room_num;
        Log.d("sql", sql);
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        cursor.moveToNext();
        result = cursor.getInt(0);
        Log.d("result", result+"");

        return result;
    }

    public void update_image(int room_num){
        String sql = "UPDATE room set picture=1 where room_num="+room_num;
        Log.d("sql", sql+"");
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL(sql);
    }


//    public int check_room(int room_num){
//        int result = 0;
//
//        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
//        Cursor cursor = null;
//        String sql = "SELECT * FROM room where room_num="+room_num;
//        cursor.moveToNext();
//        cursor.ge
//        return  result;
//    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
