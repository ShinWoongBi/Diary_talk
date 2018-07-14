package com.warbargic.diary_talk.Chat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kippe_000 on 2017-02-09.
 */
public class chat_DB extends SQLiteOpenHelper {

    public chat_DB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE chat(" +
                "num integer primary key," +
                "room_num integer," +
                "member mediumtext," +
                "read integer," +
                "mail varchar(30)," +
                "send_mail varchar(30) not null," +
                "message mediumtext not null," +
                "time varchar(15) not null)");
    }

    public void query(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public int room_information(int room_num){
        Cursor cursor = null;

        SQLiteDatabase database = getReadableDatabase();
        cursor = database.rawQuery("select room_num from chat where room_num="+room_num, null);
        cursor.moveToNext();
        return cursor.getInt(0);
    }

    public void delete(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from chat");
    }

    public void insert(String mail, String send_mail, String message, String time) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가


        db.execSQL("INSERT INTO chat(mail,send_mail,message,time) VALUES('"+mail+"', '" + send_mail+ "', '" + message+ "','"+time+"');");
        db.close();
    }

    public void insert_room(int room_num, String send_mail, String message, String time, String member) { // 단체 방
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가


        db.execSQL("INSERT INTO chat(room_num,send_mail,message,time,member) VALUES('"+room_num+"', '" + send_mail+ "', '" + message+ "','"+time+"','"+member+"');");
        db.close();
    }

    public String select(String friendmail){
        SQLiteDatabase db = getReadableDatabase();
        String result = "[";

        Cursor cursor = db.rawQuery("SELECT * FROM chat where mail='"+friendmail+"'", null);
        while(cursor.moveToNext()){
            result += "{\"mail\":\""+cursor.getString(4)+"\",\"send_mail\":\""+cursor.getString(5)+"" +
                    "\",\"message\":\""+cursor.getString(6)+"\",\"time\":\""+cursor.getString(7)+"\"},";
        }
        result = result.substring(0, result.length()-1);
        result += "]";
        cursor.close();

        return result;
    }

    public String select_room(int room_num){
        SQLiteDatabase db = getReadableDatabase();
        String result = "[";

        Cursor cursor = db.rawQuery("SELECT * FROM chat where room_num='"+room_num+"'", null);
        while(cursor.moveToNext()){
            result += "{\"mail\":\""+cursor.getString(5)+"\",\"message\":\""+cursor.getString(6)+"" +
                    "\",\"time\":\""+cursor.getString(7)+"\"},";
        }
        result = result.substring(0, result.length()-1);
        result += "]";
        cursor.close();

        return result;
    }

    public ArrayList<Data> select_room(){
        SQLiteDatabase db = getReadableDatabase();

        String result = "[";
        Cursor cursor = db.rawQuery("select * from chat order by num desc",null);
        ArrayList<Data> arrayList = new ArrayList<>();
        while(cursor.moveToNext()){
            int array_add = 0;

            for(int i = 0; i < arrayList.size(); i++) {

                Log.d("-----------------","---------------------");
                Log.d("int", cursor.getInt(1)+"");
                Log.d("string", cursor.getString(5)+"");
                Log.d("mail", arrayList.get(i).mail+"");
                Log.d("member",cursor.getString(2)+"");
                if (cursor.getInt(1) != 0) {
                    if (arrayList.get(i).room_num == cursor.getInt(1))
                        array_add++;
                } else {
                    if ((arrayList.get(i).mail+"").equals(cursor.getString(4) + ""))
                        array_add++;
                }
            }

            if(array_add == 0) {
                if (cursor.getInt(1) != 0) {

                    Data data = new Data("", cursor.getString(6), cursor.getString(7), cursor.getInt(1),cursor.getString(2));
                    arrayList.add(data);
                }else{
                    Data data = new Data(cursor.getString(4), cursor.getString(6), cursor.getString(7), cursor.getInt(1),cursor.getString(2));
                    arrayList.add(data);
                }
            }
        }

        return arrayList;
    }




    class Data{
        String mail;
        String message;
        String time;
        int room_num;
        String member;

        Data(String mail, String message, String time, int room_num, String member){
            this.mail = mail;
            this.message = message;
            this.time = time;
            this.room_num = room_num;
            this.member = member;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}