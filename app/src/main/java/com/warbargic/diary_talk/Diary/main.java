package com.warbargic.diary_talk.Diary;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.warbargic.diary_talk.Chat.Chatting_room;
import com.warbargic.diary_talk.R;

import java.util.ArrayList;

public class main extends android.support.v4.app.Fragment {
    ArrayList<DataBase.Data> arrayList;
    ArrayList<Data> real_list;
    DataBase dataBase_diary, dataBase_diary_gps;
    Adapter adapter;
    ListView listView;
    ArrayList<String> date_list;
    int list_count = 0;
    int date_size = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diary_main, container, false);
        arrayList = new ArrayList<>();
        dataBase_diary_gps = new DataBase(getContext(), "diary_gps.db", null, 1);
        adapter = new Adapter(getContext());
        listView = (ListView)view.findViewById(R.id.listView);
        date_list = new ArrayList<>();
        real_list = new ArrayList<>();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(real_list.get(i).real){
                    Log.d("real_data", "position: " +i);
                    Log.d("friend", real_list.get(i).friend+"");
                    Log.d("date", real_list.get(i).date+"");
                    Intent intent = new Intent(getActivity(), see_diary.class);
                    intent.putExtra("FriendMail", real_list.get(i).friend);
                    intent.putExtra("date", real_list.get(i).date);
                    startActivity(intent);
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        arrayList.clear();

        arrayList = dataBase_diary_gps.select_all();
        String friend;
        double lat;
        double lon;
        String date;

        for(int i = 0; i < arrayList.size(); i++){
            friend = arrayList.get(i).friend;
            lat = arrayList.get(i).lat;
            lon = arrayList.get(i).lon;
            date = arrayList.get(i).date;

            Log.d("array++++","+++++++");
            Log.d("array++++friend", friend+"");
            Log.d("array++++lat", lat+"");
            Log.d("array++++lon", lon+"");
            Log.d("array++++date", date+"");

            int count = 0;
            for (int l = 0; l < date_list.size(); l++){
                if(date_list.get(l).equals(arrayList.get(i).date)){
                    count++;
                }
            }
            if(count == 0){
                date_list.add(arrayList.get(i).date);
            }
        }
        date_size = date_list.size();
        date_list.clear();
        listView.setAdapter(adapter);
    }


    class Adapter extends BaseAdapter{
        Context context;

        Adapter(Context context){
            this.context = context;
        }

        @Override
        public int getCount() {
            return arrayList.size() + date_size;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int count = 0;
            Log.d("date_list", date_list+"");
            for (int l = 0; l < date_list.size(); l++){
                if(date_list.get(l).equals(arrayList.get(position-date_list.size()).date)){
                    count++;
                }
            }
            if(count == 0){
                Data data = new Data(null,0,0,null,false);
                real_list.add(data);
                date_list.add(arrayList.get(position-date_list.size()).date);
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = null;
                convertView = layoutInflater.inflate(R.layout.diary_item2, parent, false);

                TextView textView = (TextView) convertView.findViewById(R.id.textView);
                textView.setText(arrayList.get(position-date_list.size()+1).date);

            }else {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                Log.d("size", date_list.size() + "");
                convertView = null;
                convertView = layoutInflater.inflate(R.layout.diary_item, parent, false);

                String friend,date;
                double lat,lon;

                friend = arrayList.get(position - date_list.size()).friend;
                date = arrayList.get(position - date_list.size()).date;
                lat = arrayList.get(position - date_list.size()).lat;
                lon = arrayList.get(position - date_list.size()).lon;

                Data data = new Data(friend,lat,lon,date,true);
                real_list.add(data);


                ImageView imageView = (ImageView) convertView.findViewById(R.id.diary_main_item_image);
                TextView name_T = (TextView) convertView.findViewById(R.id.diary_main_item_name);
                TextView date_T = (TextView) convertView.findViewById(R.id.diary_main_item_message);

                imageView.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/." + friend + ".jpg"));
                Chatting_room.Friend_DB friend_db = new Chatting_room.Friend_DB(getContext(), "friend.db", null, 1);
                name_T.setText(friend_db.select_name(friend));
                date_T.setText(date);
            }
            return convertView;
        }
    }

    class Data{
        String friend;
        double lat;
        double lon;
        String date;
        boolean real;

        Data(String friend, double lat, double lon, String date, boolean real){
            this.friend= friend;
            this.lat = lat;
            this.lon = lon;
            this.date = date;
            this.real = real;
        }
    }
}
