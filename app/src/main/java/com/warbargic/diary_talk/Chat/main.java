package com.warbargic.diary_talk.Chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.warbargic.diary_talk.R;

import java.util.ArrayList;

/**
 * Created by kippe_000 on 2017-01-05.
 */

public class main extends Fragment {
    ListView listView = null;
    ArrayList<chat_DB.Data> arrayList;
    Listview_Adapter adapter;
    static public Handler handler;
    static int chat_name_catch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_main, container, false);
        handler = new Handler(this);

        listView = (ListView)view.findViewById(R.id.chat_main_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(arrayList.get(position).room_num == 0) {
                    Intent intent = new Intent(getContext(), Chatting_room.class);
                    intent.putExtra("mail", arrayList.get(position).mail);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getContext(), Chatting_room.class);
                    intent.putExtra("room_num", arrayList.get(position).room_num);
                    intent.putExtra("Mail_json", arrayList.get(position).member);
                    startActivity(intent);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                room_DB room_db = new room_DB(getContext(), "room.db", null,1);

                dialog_list(room_db.get_name(arrayList.get(i).room_num), arrayList.get(i).room_num);

                return false;
            }
        });

        chat_name_catch = 1;
        return view;
    }


    public void handleMessage(Message msg) {
        if (chat_name_catch == 1) {
            listView = (ListView) getActivity().findViewById(R.id.chat_main_listview);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if(arrayList.get(position).room_num == 0) {
                        Intent intent = new Intent(getContext(), Chatting_room.class);
                        intent.putExtra("mail", arrayList.get(position).mail);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getContext(), Chatting_room.class);
                        intent.putExtra("room_num", arrayList.get(position).room_num);
                        intent.putExtra("Mail_json", arrayList.get(position).member);
                        startActivity(intent);
                    }
                }
            });
            chat_DB Chat_db = new chat_DB(getContext(), "chat.db", null, 1);
            room_DB room_db = new room_DB(getContext(), "room.db", null, 1);

            arrayList = new ArrayList<>();
            Log.d("listview_chatarray_size", ":" + arrayList.size());
            arrayList = Chat_db.select_room();

            if(arrayList.get(0).room_num != 0) {
                String check = room_db.get_name(arrayList.get(0).room_num);
                Log.d("check", check + "");
                if (check == null) {
                    Log.d("log", "member=" + arrayList.get(0).member + "  room_num=" + arrayList.get(0).room_num);
                    room_db.insert_room(arrayList.get(0).member, arrayList.get(0).room_num, getContext());
                }
            }
//        Toast.makeText(getContext(), arrayList.get(0).message, Toast.LENGTH_LONG).show();

            adapter = new Listview_Adapter(getContext());
            listView.setAdapter(adapter);
        }
        Bundle bundle = msg.getData();
        String name = bundle.getString("name");
        Chatting_room.Friend_DB friend_db = new Chatting_room.Friend_DB(getContext(), "friend.db", null, 1);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MY_PROFILE", Context.MODE_PRIVATE);
        boolean alarm = sharedPreferences.getBoolean("alarm", true);

        if(alarm) {
            if (!arrayList.get(0).mail.equals("")) {
                new MakeToast(getContext()).showToast(arrayList.get(0).message, friend_db.select_name(arrayList.get(0).mail), arrayList.get(0).mail);
            } else {
                new MakeToast(getContext(), arrayList.get(0).room_num).showToast(arrayList.get(0).message, "단체", "mail");

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        chat_DB Chat_db = new chat_DB(getContext(),"chat.db",null,1);
        arrayList = new ArrayList<>();
        Log.d("listview_chatarray_size",":"+arrayList.size());
        arrayList = Chat_db.select_room();

        adapter = new Listview_Adapter(getContext());
        listView.setAdapter(adapter);
    }

    void dialog_list(final String name, final int room_num){
        CharSequence[] menu = {"채팅방 설정", "나가기"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(name);
        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        Intent intent = new Intent(getActivity(), Set_name.class);
                        intent.putExtra("room_num", room_num);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        break;
                    case 1:

                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    class Listview_Adapter extends BaseAdapter{
        Context context = null;

        Listview_Adapter(Context context){
            this.context = context;
        }

        @Override
        public int getCount() {
            return arrayList.size();
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
            if(convertView == null){
                LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.chat_main_item, parent, false);
            }
            Log.d("mail",":"+arrayList.get(position).mail);
            TextView mail, message, time;
            message = (TextView)convertView.findViewById(R.id.chat_main_item_message);
            time = (TextView)convertView.findViewById(R.id.chat_main_item_time);
            ImageView imageView = (ImageView)convertView.findViewById(R.id.chat_main_item_image);

            LinearLayout linearLayout = (LinearLayout)convertView.findViewById(R.id.chat_main_item_linear);
            ViewGroup.LayoutParams parm = linearLayout.getLayoutParams();
            int height = parm.height;
            Log.d("width", "width:" + height);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = height;
            imageView.setLayoutParams(params);

            Chatting_room.Friend_DB friend_db = new Chatting_room.Friend_DB(getContext(),"friend.db",null,1);
            room_DB room_db = new room_DB(getContext(),"room.db",null,1);

            if(!arrayList.get(position).mail.equals("")) {
                ((TextView) convertView.findViewById(R.id.chat_main_item_name)).setText(friend_db.select_name(arrayList.get(position).mail));
            }else{
                ((TextView) convertView.findViewById(R.id.chat_main_item_name)).setText(room_db.get_name(arrayList.get(position).room_num));
            }

            if(!arrayList.get(position).mail.equals("")) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/." +
                        "" + arrayList.get(position).mail + ".jpg"));
            }else{
                int result = room_db.get_image(arrayList.get(position).room_num);
                if(result == 0) {
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }else{
                    imageView.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/chat/."+arrayList.get(position).room_num+".jpg"));
                }
            }
            message.setText(arrayList.get(position).message);
            time.setText(arrayList.get(position).time);

            int count = 0;
            if(!arrayList.get(position).mail.equals("")) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("count", Context.MODE_PRIVATE);
                count = sharedPreferences.getInt(arrayList.get(position).mail + "~count", 0);
            }else {
                SharedPreferences sharedPreferences = context.getSharedPreferences("count", Context.MODE_PRIVATE);
                count = sharedPreferences.getInt(arrayList.get(position).room_num + "~count", 0);
            }

            ImageView imageView1 = (ImageView) convertView.findViewById(R.id.chat_main_item_numimage);

            if(count != 0) {
                imageView1.setVisibility(View.VISIBLE);
                ((TextView) convertView.findViewById(R.id.chat_main_item_numtext)).setText(count+"");
            }else{
                imageView1.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

}
