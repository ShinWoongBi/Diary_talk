package com.warbargic.diary_talk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.warbargic.diary_talk.Friend.main;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class main_menu extends AppCompatActivity {
    ViewPager viewPager = null;
    ImageButton friend, chat, diary;
    static public Handler2 handler2;
    TextView title;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        friend = (ImageButton)findViewById(R.id.main_menu_friend);
        chat = (ImageButton)findViewById(R.id.main_menu_chat);
        diary = (ImageButton)findViewById(R.id.main_menu_diary);
        title = (TextView)findViewById(R.id.main_menu_title);

        if(getIntent().getBooleanExtra("EXIT", false)){
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }


        List<Fragment> list = new ArrayList<>();
        list.add(new main());
        list.add(new com.warbargic.diary_talk.Chat.main());
        list.add(new com.warbargic.diary_talk.Diary.main());
        Adapter adapter = new Adapter(getSupportFragmentManager(), list);

        viewPager = (ViewPager)findViewById(R.id.main_menu_viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(viewPager.getCurrentItem() == 0){
                    friend.setImageDrawable(getResources().getDrawable(R.drawable.friend1));
                    chat.setImageDrawable(getResources().getDrawable(R.drawable.chat1));
                    diary.setImageDrawable(getResources().getDrawable(R.drawable.diary2));
                    title.setText("친구");
                }else if(viewPager.getCurrentItem() == 1){
                    friend.setImageDrawable(getResources().getDrawable(R.drawable.friend2));
                    chat.setImageDrawable(getResources().getDrawable(R.drawable.chat2));
                    diary.setImageDrawable(getResources().getDrawable(R.drawable.diary2));
                    title.setText("채팅");
                }else{
                    friend.setImageDrawable(getResources().getDrawable(R.drawable.friend2));
                    chat.setImageDrawable(getResources().getDrawable(R.drawable.chat1));
                    diary.setImageDrawable(getResources().getDrawable(R.drawable.diary1));
                    title.setText("일기");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {


            }
        });


        ((ImageButton)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), set.class));
            }
        });

        friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
                friend.setImageDrawable(getResources().getDrawable(R.drawable.friend1));
                chat.setImageDrawable(getResources().getDrawable(R.drawable.chat1));
                diary.setImageDrawable(getResources().getDrawable(R.drawable.diary2));
                title.setText("친구");
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                friend.setImageDrawable(getResources().getDrawable(R.drawable.friend2));
                chat.setImageDrawable(getResources().getDrawable(R.drawable.chat2));
                diary.setImageDrawable(getResources().getDrawable(R.drawable.diary2));
                title.setText("채팅");
            }
        });

        diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
                friend.setImageDrawable(getResources().getDrawable(R.drawable.friend2));
                chat.setImageDrawable(getResources().getDrawable(R.drawable.chat1));
                diary.setImageDrawable(getResources().getDrawable(R.drawable.diary1));
                title.setText("일기");
            }
        });


        handler2 = new Handler2(this);

    }

    public void handleMessage(Message message){
        viewPager.setCurrentItem(1);
        friend.setImageDrawable(getResources().getDrawable(R.drawable.friend2));
        chat.setImageDrawable(getResources().getDrawable(R.drawable.chat2));
        diary.setImageDrawable(getResources().getDrawable(R.drawable.diary2));
        title.setText("채팅");
    }

    public class Handler2 extends android.os.Handler{
        WeakReference<main_menu> activity;

        Handler2(main_menu main) {
            activity = new WeakReference<main_menu>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            main_menu activity = this.activity.get();
            if(activity != null){
                activity.handleMessage(msg);
            }
        }
    }


    class Adapter extends FragmentPagerAdapter{
        List<Fragment> list;

        public Adapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
