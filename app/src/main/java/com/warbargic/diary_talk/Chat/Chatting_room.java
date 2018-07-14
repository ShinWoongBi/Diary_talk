package com.warbargic.diary_talk.Chat;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.warbargic.diary_talk.R;
import com.warbargic.diary_talk.main_menu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Chatting_room extends Activity {
    PrintWriter printWriter = null;
    ListView listView = null, friend_list = null;
    Adapter adapter = null;
    Adapter_friend adapter_friend = null;
    Socket socket = null;
    ArrayList<String> arrayList = null, mail_list = null;
    ArrayList<Data> list;
    ArrayList<Boolean> booleen = null;
    ArrayList<String> time_list = null;
    ArrayList<Bitmap> bitmaps = null;
    String FriendMail = null;
    String FriendName;
    String intent_new = "";
    int room_num = -1;
    String Mail_json = null;
//    ListView lv_activity_main_nav_list;
    DrawerLayout drawerLayout;
    ArrayList<Integer> image_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);
        final EditText send_edit = (EditText)findViewById(R.id.chat_room_edit);
        final Button send_btn = (Button)findViewById(R.id.chat_room_btn);
        listView = (ListView)findViewById(R.id.chat_room_list);
        arrayList = new ArrayList<>();
        mail_list = new ArrayList<>();
        booleen = new ArrayList<>();
        time_list = new ArrayList<>();
        list = new ArrayList<>();
        final Intent intent = getIntent();
        FriendMail = intent.getStringExtra("mail");
        room_num = intent.getIntExtra("room_num",0);
        intent_new = intent.getStringExtra("new");
        Mail_json = intent.getStringExtra("Mail_json");
        image_list = new ArrayList<>();
        bitmaps = new ArrayList<>();

        Log.d("intent","mail:"+FriendMail+"  room_num:"+room_num+"  intent_new:"+intent_new+"  Mail_json:"+Mail_json);

        drawerLayout = (DrawerLayout)findViewById(R.id.dl_activity_main_drawer);
        image_list.add(R.drawable.room_map);
        image_list.add(R.drawable.write);


        adapter_friend = new Adapter_friend(getApplicationContext()); // 친구 목록 list
        friend_list = (ListView)findViewById(R.id.friend_list);
        friend_list.setAdapter(adapter_friend);
        get_friend_list();
        friend_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    // 친구추가 다이얼 로그
                    SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE", MODE_PRIVATE);
                    String my_mail = sharedPreferences.getString("mail", "");
                    Customdialog customdialog = new Customdialog(Chatting_room.this, room_num, my_mail, FriendMail);
                    Log.d("chatroom", FriendMail+" " );
                    customdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            Customdialog customdialog1 = (Customdialog) dialogInterface;
                            if(((Customdialog) dialogInterface).button_b == true) {

                                String[] Mails = customdialog1.get_mail();
                                for (int i = 0; i < Mails.length; i++) {
                                    Log.d("Mails", Mails[i]);
                                }
                                if (FriendMail != null) {
                                    // 처음 만들어진 방

                                    String Mail_json = "{";
                                    for (int i = 0; i < Mails.length; i++) {
                                        if (i == 0) {
                                            Mail_json += "\"mail" + i + "\":\"" + Mails[i] + "\"";
                                        } else {
                                            Mail_json += ",\"mail" + i + "\":\"" + Mails[i] + "\"";
                                        }
                                    }
                                    Mail_json += "}";

                                    Get_room_num get_room_num = new Get_room_num(Mail_json);
                                    get_room_num.execute();
                                }
                            }
                        }
                    });
                    customdialog.show();

                }else if(i == 1){
                    // 자신의 프로필
                }
                else{
                    i--;

                    // 친구 프로필 이동
                }
            }
        });


        adapter = new Adapter(getApplicationContext());
        listView.setAdapter(adapter);

        if(FriendMail != null) {
            Friend_DB friend_db = new Friend_DB(getApplicationContext(), "friend.db", null, 1);
            FriendName = friend_db.select_name(FriendMail);
            friend_db.close();
            ((Button) findViewById(R.id.chat_room_top_name)).setText(FriendName);

            chat_DB chat_DB = new chat_DB(getApplicationContext(), "chat.db", null, 1);
            String result = chat_DB.select(FriendMail);
            chat_DB.close();
            Log.e("asdfasdf", ":" + result);
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE", MODE_PRIVATE);
                String my_mail = sharedPreferences.getString("mail", "");
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = (JSONObject) jsonArray.get(i);
                    arrayList.add(jsonObject.getString("message"));
                    Log.d("json", ":" + jsonObject.get("send_mail"));
                    if (jsonObject.getString("send_mail").equals(my_mail)) {
                        booleen.add(true);
                    } else {
                        booleen.add(false);
                    }
                    time_list.add(jsonObject.getString("time"));
                }
            } catch (Exception e) {
                e.getMessage();
            }
            adapter.notifyDataSetChanged();
            listView.setSmoothScrollbarEnabled(true);
            listView.setSelection(arrayList.size());
        }else{

            chat_DB chat_DB = new chat_DB(getApplicationContext(), "chat.db", null, 1);
            String result = chat_DB.select_room(room_num);
            chat_DB.close();
            Log.e("asdfasdf", ":" + result);
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE", MODE_PRIVATE);
                String my_mail = sharedPreferences.getString("mail", "");
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = (JSONObject) jsonArray.get(i);
                    arrayList.add(jsonObject.getString("message"));
                    Log.d("json", ":" + jsonObject.get("mail"));
                    if (jsonObject.getString("mail").equals(my_mail)) {
                        booleen.add(true);
                    } else {
                        booleen.add(false);
                    }
                    mail_list.add(jsonObject.getString("mail"));
                    time_list.add(jsonObject.getString("time"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
            listView.setSmoothScrollbarEnabled(true);
            listView.setSelection(arrayList.size());
        }


        SharedPreferences sharedPreferences = getSharedPreferences("count",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(room_num == 0)
            editor.putInt(FriendMail+"~count", 0);
        else
            editor.putInt(room_num+"~count", 0);
        editor.apply();


        send_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(send_edit.getText().toString().equals("")){
                    send_btn.setTextColor(Color.parseColor("#CCCCCC"));
                    send_btn.setClickable(false);
                }else{
                    send_btn.setTextColor(Color.parseColor("#000000"));
                    send_btn.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date(System.currentTimeMillis());
                int hour = Integer.parseInt(new SimpleDateFormat("HH").format(date));
                int min = Integer.parseInt(new SimpleDateFormat("mm").format(date));
                String min_ = String.format("%02d", min);
//                Toast.makeText(getApplicationContext(), "hour:min:"+hour+":"+min,Toast.LENGTH_LONG).show();
                String or = null;
                if(hour < 12)
                    or = "오전";
                else {
                    or = "오후";
                    hour-=12;
                }
                final String time = or+" "+hour+":"+min_;


                SharedPreferences sharedPreferences = getSharedPreferences("MY_PROFILE",MODE_PRIVATE);
                final String token = sharedPreferences.getString("mail","");
                Log.e("tokentoken",":"+token);
                final String message = send_edit.getText().toString();
                send_edit.setText("");
                if(!message.equals("")) {
//                    chat_DB chat_DB = new chat_DB(getApplicationContext(), "chat.db", null , 1);
//                    SharedPreferences sharedPreferences1 = getSharedPreferences("MY_PROFILE",MODE_PRIVATE);
//                    chat_DB.insert(FriendMail, sharedPreferences1.getString("mail",""),message,time);

                    Log.d("printed",":"+"{token:" + token + ",friend:"+FriendMail+",message:" + message + ",time:"+time+"}");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(FriendMail != null) {
                                printWriter.println("{\"token\":\"" + token + "\",\"friend\":\"" + FriendMail + "\",\"message\":\"" + message + "\",\"time\":\"" + time + "\"}");
                            }else{
                                Log.d("print",":"+"{\"token\":\"" + token + "\",\"room_num\":\"" + room_num+ "\",\"message\":\"" + message + "\",\"time\":\"" + time + "\"}");
                                printWriter.println("{\"token\":\"" + token + "\",\"room_num\":\"" + room_num+ "\",\"message\":\"" + message + "\",\"time\":\"" + time + "\"}");
                            }
                        }
                    }).start();

//                    arrayList.add("[{\"message\":\""+message+"\",\"type\":\"0\"}]");
                    arrayList.add(message);
                    booleen.add(true);
                    time_list.add(time);
                    adapter.notifyDataSetChanged();
                    listView.setSmoothScrollbarEnabled(true);
                    listView.setSelection(arrayList.size());
                }

                SharedPreferences my_profile = getSharedPreferences("MY_PROFILE", MODE_PRIVATE);
                chat_DB chat_DB = new chat_DB(getApplicationContext(), "chat.db", null, 1);
                if(FriendMail != null) {
                    chat_DB.insert(FriendMail, my_profile.getString("mail",""), message, time);
                }else {
                    String mail = my_profile.getString("mail", "");
                    chat_DB.insert_room(room_num, mail, message, time, Mail_json);
                    mail_list.add(mail);
                }

                chat_DB.close();
            }
        });
    }


    class Get_room_num extends AsyncTask<String, String, String>{
        String Mail_json;

        Get_room_num(String Mail_json){
            this.Mail_json = Mail_json;
        }

        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection httpURLConnection = null;
            URL url = null;
            String param = "Mail_json="+Mail_json;

            String room_num = null;
            try{
                url = new URL("http://tlsdndql27.vps.phps.kr/Diary_talk/chat/get_room_num.php");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(param.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                String buffer = "";
//                while((buffer = reader.readLine()) != null){
//                    Log.d("buffer",buffer);
//                }

                room_num = reader.readLine();

            }catch (Exception e){
                e.printStackTrace();
            }

            return room_num;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            int room_num = Integer.parseInt(s);

            room_DB room_db = new room_DB(getApplicationContext(),"room.db",null, 1);
            room_db.insert_room("["+Mail_json+"]", room_num, getApplicationContext());


            Intent intent = new Intent(Chatting_room.this, Chatting_room.class);
            intent.putExtra("Mail_json","["+Mail_json+"]");
            intent.putExtra("room_num", room_num);
            intent.putExtra("new", "new");
            startActivity(intent);
            finish();
        }
    }


    void get_friend_list(){ // 친구목록 데이터 가져오기


        list.clear();
        bitmaps.clear();

        if(FriendMail == null) {

            DbOpenHelper dbOpenHelper = new DbOpenHelper(getApplicationContext());
            dbOpenHelper.open();
            Log.d("dblog", ":" + dbOpenHelper.mDBHelper.select());
            try {
                JSONArray jsonArray = new JSONArray(dbOpenHelper.mDBHelper.select_mails(Mail_json));
                JSONObject jsonObject = null;
                Data data = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = (JSONObject) jsonArray.get(i);

                    data = new Data(jsonObject.getString("picture"), jsonObject.getString("name"), jsonObject.getString("message"), jsonObject.getString("mail"));
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
            DbOpenHelper dbOpenHelper = new DbOpenHelper(getApplicationContext());
            dbOpenHelper.open();
            try {
                JSONArray jsonArray = new JSONArray(dbOpenHelper.mDBHelper.select_mail(FriendMail));
                JSONObject jsonObject = null;

                Data data = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = (JSONObject) jsonArray.get(i);

                    data = new Data(jsonObject.getString("picture"), jsonObject.getString("name"), jsonObject.getString("message"), jsonObject.getString("mail"));
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
//        adapter = new Adapter(getApplicationContext());
        adapter_friend.notifyDataSetChanged();
//        count.setText(adapter.getCount()+"");

    }

    static class Data{
        String picture;
        String name;
        String message;
        String mail;

        Data(String picture, String name, String message, String mail){
            this.picture = picture;
            this.name = name;
            this.message = message;
            this.mail = mail;
        }

    }

    public void chat_room_onclick(View view){
        switch (view.getId()){
            case R.id.Live_location:
                Intent intent1 = new Intent(getApplicationContext(), Live_location.class);
                intent1.putExtra("FriendMail",FriendMail);
                startActivity(intent1);

                break;
            case R.id.monitor:
                Intent intent2 = new Intent(getApplicationContext(), com.warbargic.diary_talk.Diary.monitor.class);
                intent2.putExtra("FriendMail",FriendMail);
                startActivity(intent2);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        Connect();
    }

    void Connect() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();

                BufferedReader reader = null;
                try{
                    socket = new Socket("115.71.232.134", 9999);
                    Log.d("Connected!!", ":" + socket.isConnected());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    SharedPreferences sharedPreferences = getSharedPreferences("TOKEN",MODE_PRIVATE);

                    // room=1 1:1방 room=2 단체방    room_num 단체방이라면 방의 번호
                    // 이 두 데이터가 필요 room데이터는 방의 인원을 내부DB에서 가져오면 얻을 수 있음
                    // token도 보내야함
                    // 파라미터 정리 room, room_num, token 3가지
                    // room_num도 마찬가지로 내부DB

//                    chat_DB chat_DB = new chat_DB(getApplicationContext(), "chat.db", null , 1);
//                    int room_num = chat_DB.room_information()
                    String token = sharedPreferences.getString("token","");
                    int room = 0;
                    Log.d("room_num",room_num+"");
                    if(room_num != 0)
                        room = 2;
                    else
                        room = 1;

//                    String param = "{\"token\":\"" + token + "\",\"room\":\""+room+"\",\"room_num\":\"" + room_num + "\"}";
                    String param = "{\"token\":\"" + token + "\",\"room\":\""+room+"\",\"room_num\":\"" + room_num + "\"}";
                    Log.d("param", param);
//                    printWriter.println(sharedPreferences.getString("token",""));
                    printWriter.println(param);
                }catch(Exception e){
                    e.getMessage();
                }

                try{
                    String read = null;
                    while (true) {
                        if(socket.isConnected())
                            read = reader.readLine();
                        Log.e("read", ":" + read);

                        JSONArray jsonArray = new JSONArray(read);
                        JSONObject jsonObject = (JSONObject) jsonArray.getJSONObject(0);
                        final String message = jsonObject.getString("message");
                        final String time = jsonObject.getString("time");
                        String mail = jsonObject.getString("mail");

                        Date date = new Date(System.currentTimeMillis());
                        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(date));
                        int min = Integer.parseInt(new SimpleDateFormat("mm").format(date));
                        String min_ = String.format("%02d", min);
//                Toast.makeText(getApplicationContext(), "hour:min:"+hour+":"+min,Toast.LENGTH_LONG).show();
                        String or = null;
                        if(hour < 12)
                            or = "오전";
                        else {
                            or = "오후";
                            hour-=12;
                        }
                        String time1 = or+" "+hour+":"+min_;

                        arrayList.add(message);
                        booleen.add(false);
                        time_list.add(time1);

                        chat_DB chat_DB = new chat_DB(getApplicationContext(), "chat.db", null, 1);
                        if(room_num == 0) {
                            chat_DB.insert(FriendMail, FriendMail, message, time1);
                        }else{
                            mail_list.add(mail);
                            chat_DB.insert_room(room_num, mail, message, time, Mail_json);
                        }
                        chat_DB.close();

                        // noti
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                listView.setSmoothScrollbarEnabled(true);
                                listView.setSelection(arrayList.size());
                            }
                        });



                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();


        if(socket != null) {
            System.out.println("closeed");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        main_menu.handler2.sendMessage(main_menu.handler2.obtainMessage());

    }

    class Adapter extends BaseAdapter {
        Context context = null;

        Adapter(Context context){
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_room_listitem2, parent, false);

//                if(booleen.get(position)) {
//                    Log.d("true",":"+true);
//                    convertView = inflater.inflate(R.layout.chat_room_listitem2, parent, false);
//                }else{
//                    Log.d("false",":"+false);
//                    convertView = inflater.inflate(R.layout.chat_room_listitem, parent, false);
//                }
            }

            TextView textView = (TextView) convertView.findViewById(R.id.chat_room_listitem_text);
            textView.setText(arrayList.get(position));
            LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.chat_room_listitem_linear);
            ViewGroup.LayoutParams params = textView.getLayoutParams();
            ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.chat_room_listitem_imagebutton);
            int width = params.width;
            params.width = width;
            if(booleen.get(position)) {
                linearLayout.setGravity(Gravity.RIGHT);
                textView.setBackgroundResource(R.drawable.me);
                ((TextView)convertView.findViewById(R.id.chat_room_listitem_name)).setVisibility(View.GONE);
                imageButton.setVisibility(View.GONE);
                textView.setLayoutParams(params);


            }else{
                ((TextView)convertView.findViewById(R.id.chat_room_listitem_name)).setVisibility(View.VISIBLE);
                imageButton.setVisibility(View.VISIBLE);
                linearLayout.setGravity(Gravity.LEFT);
                textView.setBackgroundResource(R.drawable.you);
                textView.setLayoutParams(params);

                if(room_num == 0) {
                    ((TextView) convertView.findViewById(R.id.chat_room_listitem_name)).setText(FriendName);
                    imageButton.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/." +
                            "" + FriendMail + ".jpg"));
                    imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), com.warbargic.diary_talk.Friend.friend_profile.class);
                            intent.putExtra("mail", FriendMail);
                            intent.putExtra("chat", true);
                            startActivity(intent);
                        }
                    });
                }else{
                    Friend_DB friend_db = new Friend_DB(getApplicationContext(), "friend.db", null, 1);
                    ((TextView) convertView.findViewById(R.id.chat_room_listitem_name)).setText(friend_db.select_name(mail_list.get(position)));
                    imageButton.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/." +
                            "" + mail_list.get(position) + ".jpg"));
                    imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), com.warbargic.diary_talk.Friend.friend_profile.class);
                            intent.putExtra("mail", mail_list.get(position));
                            intent.putExtra("chat", true);
                            startActivity(intent);
                        }
                    });
                }

            }

            TextView textView1 = (TextView)convertView.findViewById(R.id.chat_room_listitem_time);
            textView1.setText(time_list.get(position));
            return convertView;
        }
    }


    static public class Friend_DB extends SQLiteOpenHelper {

        public Friend_DB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE friend(" +
                    "mail varchar(30) not null," +
                    "name varchar(20) not null," +
                    "picture varchar(20) not null," +
                    "message varchar(60) not null)");
            db.close();
        }

        public String select_name(String mail){
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Log.d("mail", mail);
            Cursor cursor = sqLiteDatabase.rawQuery("select * from friend where mail='"+mail+"'", null);
            cursor.moveToNext();
            Log.d("mail",":"+cursor.getString(1));

            String name = cursor.getString(1);
            cursor.close();
            sqLiteDatabase.close();
            return name;
        }

        public String select_profile(String mail){
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor cursor = sqLiteDatabase.rawQuery("select * from friend where mail='"+mail+"'",null);
            cursor.moveToNext();

            String json = "[{\"name\":\""+cursor.getString(1)+"\",\"picture\":\""+cursor.getString(2)+"\",\"message\":\""+cursor.getString(3)+"\"}]";

            cursor.close();
            sqLiteDatabase.close();
            return json;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }



    class Adapter_friend extends BaseAdapter{
        Context context;

        Adapter_friend(Context context){
            super();
            this.context = context;
        }

        @Override
        public int getCount() {
            return list.size()+2;
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
        public View getView(int position, View convertView, ViewGroup parent) {

            if (position == 0) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_room_addfriend, parent, false);

            }else if(position == 1){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_room_addfriend, parent, false);

                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                TextView textView = (TextView)convertView.findViewById(R.id.name);

                imageView.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/diary_talk/.MY_PICTURE.jpg"));
                textView.setText("나");
            }else {
                position-=2;

                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.friend_main_listview_item, parent, false);
                }

                ImageView image_view = (ImageView) convertView.findViewById(R.id.friend_main_item_image);
                TextView name_view = (TextView) convertView.findViewById(R.id.friend_main_item_name);
                TextView message_view = (TextView) convertView.findViewById(R.id.friend_main_item_message);
                LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.friend_main_item_linear);

                {
                    ViewGroup.LayoutParams parm = linearLayout.getLayoutParams();
                    int height = parm.height;
                    Log.d("width", "width:" + height);
                    ViewGroup.LayoutParams params = image_view.getLayoutParams();
                    params.width = height;
                    image_view.setLayoutParams(params);
                    image_view.setImageResource(R.mipmap.ic_launcher);

                    name_view.setText(list.get(position).name);
                    message_view.setVisibility(View.GONE);

                    if (booleen != null) {

                        if (list.get(position).picture.equals("YES")) {
                            image_view.setImageBitmap(bitmaps.get(position));
                        }
                        DbOpenHelper dbOpenHelper = new DbOpenHelper(getApplicationContext());
                        dbOpenHelper = dbOpenHelper.open();
                        dbOpenHelper.mDBHelper.insert(list.get(position).mail, list.get(position).name, list.get(position).picture, list.get(position).message);
                        dbOpenHelper.close();

                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/.");
                        if (!file.exists()) {
                            file.mkdir();
                        }
                        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/diary_talk/friend/." + list.get(position).mail + ".jpg");
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            bitmaps.get(position).compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    } else {
                        try {
                            if (list.get(position).picture.equals("YES")) {
                                image_view.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "" +
                                        "/diary_talk/friend/." + list.get(position).mail + ".jpg"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

            return convertView;
        }
    }


    public static class DbOpenHelper {

        private static final String DATABASE_NAME = "friend.db";
        private static final int DATABASE_VERSION = 1;
        public SQLiteDatabase mDB;
        public DatabaseHelper mDBHelper;
        private Context mCtx;

        public class DatabaseHelper extends SQLiteOpenHelper{

            // 생성자
            public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
                super(context, name, factory, version);
            }

            // 최초 DB를 만들때 한번만 호출된다.
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE friend(" +
                        "mail varchar(30) not null," +
                        "name varchar(20) not null," +
                        "picture varchar(20) not null," +
                        "message varchar(60) not null)");
            }

            // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE friend");
                onCreate(db);
            }

            public void insert(String mail, String name, String picture, String message) {
                // 읽고 쓰기가 가능하게 DB 열기
                SQLiteDatabase db = getWritableDatabase();
                // DB에 입력한 값으로 행 추가
                db.execSQL("delete from friend where mail='"+mail+"'");
                db.execSQL("INSERT INTO friend(mail,name,picture,message) VALUES('"+mail+"', '" + name+ "', '" + picture+ "', '" + message+ "');");
                db.close();
            }




            public String select(){
                SQLiteDatabase db = getReadableDatabase();
                String result = "[";

                Cursor cursor = db.rawQuery("SELECT * FROM friend", null);
                while(cursor.moveToNext()){
                    result += "{\"mail\":\""+cursor.getString(0)+"\",\"name\":\""+cursor.getString(1)+"" +
                            "\",\"picture\":\""+cursor.getString(2)+"\",\"message\":\""+cursor.getString(3)+"\"},";
                }
                result = result.substring(0, result.length()-1);
                result += "]";
                cursor.close();
                db.close();

                return result;
            }

            public String select_mails(String Mail_json){

                String[] Mail_s = null;
                Log.d("mail_json", Mail_json+"");
                try {
                    JSONArray jsonArray = new JSONArray(Mail_json);
                    JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                    Mail_s = new String[jsonObject.length()];

                    for(int i = 0; i < jsonObject.length(); i++){
                        Mail_s[i] = jsonObject.getString("mail"+i);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                SQLiteDatabase db = getReadableDatabase();
                String result = "[";

                String sql = "SELECT * FROM friend where";
                for(int i = 0; i < Mail_s.length; i++){
                    if(i == 0){
                        sql += " mail='" + Mail_s[i] + "'";
                    }else {
                        sql += " or mail='" + Mail_s[i] + "'";
                    }
                }


                Cursor cursor = db.rawQuery(sql, null);
                while(cursor.moveToNext()){
                    result += "{\"mail\":\""+cursor.getString(0)+"\",\"name\":\""+cursor.getString(1)+"" +
                            "\",\"picture\":\""+cursor.getString(2)+"\",\"message\":\""+cursor.getString(3)+"\"},";
                }

                result = result.substring(0, result.length()-1);
                result += "]";
                cursor.close();
                db.close();

                return result;
            }


            public String select_mail(String mail){
                SQLiteDatabase db = getReadableDatabase();
                String result = "[";

                Cursor cursor = db.rawQuery("SELECT * FROM friend where mail='"+mail+"'", null);
                while(cursor.moveToNext()){
                    result += "{\"mail\":\""+cursor.getString(0)+"\",\"name\":\""+cursor.getString(1)+"" +
                            "\",\"picture\":\""+cursor.getString(2)+"\",\"message\":\""+cursor.getString(3)+"\"},";
                }
                result = result.substring(0, result.length()-1);
                result += "]";
                cursor.close();
                db.close();

                return result;
            }

            public String select_notin_mail(String mail){
                SQLiteDatabase db = getReadableDatabase();
                String result = "[";

                Cursor cursor = db.rawQuery("SELECT * FROM friend where mail not in ('"+mail+"')", null);
                while(cursor.moveToNext()){
                    result += "{\"mail\":\""+cursor.getString(0)+"\",\"name\":\""+cursor.getString(1)+"" +
                            "\",\"picture\":\""+cursor.getString(2)+"\",\"message\":\""+cursor.getString(3)+"\"},";
                }
                result = result.substring(0, result.length()-1);
                result += "]";
                cursor.close();
                db.close();

                return result;
            }

            public void delete_friend(){
                SQLiteDatabase sqLiteDatabase = getWritableDatabase();

                sqLiteDatabase.execSQL("DELETE FROM friend");
            }

        }

        public DbOpenHelper(Context context){
            this.mCtx = context;
        }

        public DbOpenHelper open() throws SQLException {
            mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
            mDB = mDBHelper.getWritableDatabase();
            return this;
        }



        public void close(){
            mDB.close();
        }

    }


}
