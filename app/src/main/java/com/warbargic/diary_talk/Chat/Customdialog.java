package com.warbargic.diary_talk.Chat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.warbargic.diary_talk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Customdialog extends Dialog {
    Adapter adapter;
    Context context;
    ArrayList<Bitmap> bitmaps;
    ArrayList<Chatting_room.Data> list;
    ListView listView;
    String FriendMail;
    int room_num;
    ArrayList<String> checked_mail;
    Button button;
    int CheckOn = 0;
    Boolean button_b = false;
    String MyMail;


    public Customdialog(@NonNull Context context, int room_num, String MyMail, String FriendMail) {
        super(context);
        this.context = context;
        this.room_num = room_num;
        this.FriendMail = FriendMail;
        this.MyMail = MyMail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.customdialog);

        checked_mail = new ArrayList<>();
        adapter = new Adapter(context);
        bitmaps = new ArrayList<>();
        list = new ArrayList<>();
        listView = (ListView)findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });


        listView.setAdapter(adapter);
        get_friend_list();

        button = (Button)findViewById(R.id.set_message_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_b = true;
                dismiss();
            }
        });
    }

    public String[] get_mail(){
        String[] Mails = null;
        if(FriendMail != null){
            Log.d("friend","friend:"+FriendMail);
            Mails = new String[checked_mail.size()+2];
            Mails[0] = MyMail;
            Mails[1] = FriendMail;
            for(int i = 2; i < checked_mail.size()+2; i++){
                Mails[i] = checked_mail.get(i-2);
            }

        }else{
            Mails = new String[checked_mail.size()];
            for(int i = 0; i < checked_mail.size(); i++){
                Mails[i] = checked_mail.get(i);
            }
        }


        return Mails;
    }



    void get_friend_list(){ // 친구목록 데이터 가져오기


        list.clear();
        bitmaps.clear();

        if(FriendMail == null) {

            Chatting_room.DbOpenHelper dbOpenHelper = new Chatting_room.DbOpenHelper(context);
            dbOpenHelper.open();
            Log.d("dblog", ":" + dbOpenHelper.mDBHelper.select());
            try {
                JSONArray jsonArray = new JSONArray(dbOpenHelper.mDBHelper.select());
                JSONObject jsonObject = null;
                Chatting_room.Data data = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = (JSONObject) jsonArray.get(i);

                    data = new Chatting_room.Data(jsonObject.getString("picture"), jsonObject.getString("name"), jsonObject.getString("message"), jsonObject.getString("mail"));
                    list.add(data);

                    if (jsonObject.getString("picture").equals("YES")) {

                        bitmaps.add(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/." + list.get(i).mail + ".jpg"));
                    } else {
                        bitmaps.add(null);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // 1:1일 경우
            Chatting_room.DbOpenHelper dbOpenHelper = new Chatting_room.DbOpenHelper(context);
            dbOpenHelper.open();
            try {
                JSONArray jsonArray = new JSONArray(dbOpenHelper.mDBHelper.select_notin_mail(FriendMail));
                JSONObject jsonObject = null;

                Chatting_room.Data data = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = (JSONObject) jsonArray.get(i);

                    data = new Chatting_room.Data(jsonObject.getString("picture"), jsonObject.getString("name"), jsonObject.getString("message"), jsonObject.getString("mail"));
                    list.add(data);

                    if (jsonObject.getString("picture").equals("YES")) {

                        bitmaps.add(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/." + list.get(i).mail + ".jpg"));
                    } else {
                        bitmaps.add(null);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();

    }



    class Adapter extends BaseAdapter{
        Context context;

        Adapter(Context context){
            super();
            this.context = context;
        }

        @Override
        public int getCount() {
            return list.size();
        }


        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.dialog_item, parent, false);
            }

            ImageView image_view = (ImageView) convertView.findViewById(R.id.imageView);
            TextView name_view = (TextView) convertView.findViewById(R.id.name);
            final CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checkbox);
            RelativeLayout relativeLayout = (RelativeLayout)convertView.findViewById(R.id.relativelayout);
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBox.isChecked()) {
                        checked_mail.add(list.get(position).mail);
//                        Toast.makeText(getContext(), "on"+ position, Toast.LENGTH_SHORT).show();
                        CheckOn++;
                    }else{
                        for(int i = 0; i < list.size(); i++){
                            if(checked_mail.get(i).equals(list.get(position).mail)){
                                checked_mail.remove(i);
                                break;
                            }
                        }
                        CheckOn--;

//                        Toast.makeText(getContext(), "off" + position, Toast.LENGTH_SHORT).show();
                    }

                    if(CheckOn == 0){
                        button.setBackgroundColor(Color.parseColor("#E8D7FF"));
                    }else{
                        button.setBackgroundColor(Color.parseColor("#B69DFF"));
                    }
                }
            });


            image_view.setImageResource(R.mipmap.ic_launcher);
            name_view.setText(list.get(position).name);


            try {
                if (list.get(position).picture.equals("YES")) {
                    image_view.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "" +
                            "/diary_talk/friend/." + list.get(position).mail + ".jpg"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }




            return convertView;
        }
    }
}
