package com.warbargic.diary_talk.Diary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kippe_000 on 2017-03-08.
 */

public class DataBase extends SQLiteOpenHelper {
    String name = null;
    public DataBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.name = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if(name.equals("diary.db")) {
            db.execSQL("CREATE TABLE diary(" +
                    "num integer primary key," +
                    "friend varchar(30) not null," +
                    "date varchar(15) not null," +
                    "picture varchar(10) not null," +
                    "picture_path varchar(10) not null," +
                    "text text not null," +
                    "time varchar(15) not null," +
                    "lat varchar(20) not null," +
                    "lon varchar(20) not null)");
        }else if(name.equals("diary_gps.db")){
            Log.d("diary_gps","create");
            db.execSQL("CREATE TABLE diary_gps(" +
                    "num integer primary key," +
                    "friend varchar(30) not null," +
                    "date varchar(15) not null," +
                    "lat varchar(20) not null," +
                    "lon varchar(20) not null," +
                    "time varchar(15) not null)");
        }else{
            db.execSQL("CREATE TABLE diary_day(" +
                    "num integer primary key," +
                    "friend varchar(30) not null," +
                    "date varchar(15) not null," +
                    "distance int(11) not null," +
                    "second int(11) not null)");
        }
    }

    public void update_day(String friend, String date, int distance){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update diary_day set distance="+distance + " where friend='"+friend+"' and date='"+date+"'");

    }

    public String insert_day_time(String friend, String date, int second){
        SQLiteDatabase db_w = getWritableDatabase();
        SQLiteDatabase db_r = getReadableDatabase();

        Cursor cursor = db_r.rawQuery("select * from diary_day where friend='"+friend+"' and date='"+date+"'", null);
        int count = cursor.getCount();
        if(count == 0){
            db_w.execSQL("insert into diary_day(friend,date,distance,second) values('"+friend+"','"+date+"',0,0)");
            if(second == -2){
                Cursor cursor1 = db_r.rawQuery("select * from diary_day where friend='"+friend+"' and date='"+date+"'", null);
                cursor1.moveToNext();
                second = cursor1.getInt(1);
                int distance = cursor1.getInt(0);
                return second+"_"+distance;
            }
        }else{
            if(second >= 0) {
                db_w.execSQL("update diary_day set second=" + second + " where friend='" + friend + "' and date='" + date + "'");
            }else if(second == -2){
                Cursor cursor1 = db_r.rawQuery("select * from diary_day where friend='"+friend+"' and date='"+date+"'", null);
                cursor1.moveToNext();
                second = cursor1.getInt(1);
                int distance = cursor1.getInt(0);
                return second+"_"+distance;
            }
        }

        return "";
    }


    public void insert_diary(String Friend, String date, String picture, String picture_path, String text, String time, double lat, double lon){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("insert into diary(friend, date, picture, picture_path, text, time, lat, lon) values('"+Friend+"','"+date+"','"+picture+"','"+picture_path+"','"+text+"','"+time+"'" +
                ",'"+lat+"','"+lon+"')");
    }

    public void insert_diary_gps(String friend, String date, double lat, double lon, String time){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("insert into diary_gps(friend, date, lat, lon, time) values('"+friend+"','"+date+"','"+lat+"','"+lon+"','"+time+"')");
    }

    public Cursor select_diary(String Friend, String date){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM diary where friend='"+Friend+"' and date='"+date+"'", null);
        return cursor;
    }

    public void delete_gps(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from diary_gps");
    }

    public void delete(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from diary");
    }

    public Cursor select_diary_gps(String Friend, String date){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM diary_gps where friend='"+Friend+"' and date='"+date+"'", null);
        return cursor;
    }

    public ArrayList<Data> select_all(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Data> arrayList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM diary_gps order by num desc", null);
        String json = null;


        while(cursor.moveToNext()){
            int array_add = 0;

            for(int i = 0; i < arrayList.size(); i++) {

                Log.d("-----------------","---------------------");
                Log.d("friend", cursor.getString(1)+"");
                Log.d("lat", cursor.getDouble(3)+"");
                Log.d("lon", cursor.getDouble(4)+"");
                Log.d("date",cursor.getString(2)+"");
                if ((arrayList.get(i).friend+"").equals(cursor.getString(1) + "") && arrayList.get(i).date.equals(cursor.getString(2)))
                    array_add++;

            }

            if(array_add == 0) {

                Data data = new Data(cursor.getString(1), cursor.getDouble(3), cursor.getDouble(4), cursor.getString(2));
                if(!data.friend.equals("")) {
                    arrayList.add(data);
                }

            }
        }

        return arrayList;
    }

    class Data{
        String friend;
        double lat;
        double lon;
        String date;

        Data(String friend, double lat, double lon, String date){
            this.friend= friend;
            this.lat = lat;
            this.lon = lon;
            this.date = date;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
