package com.warbargic.diary_talk.Diary;

import android.os.Message;

import java.lang.ref.WeakReference;

public class handler extends android.os.Handler {
    WeakReference<monitor> activity;

    handler(monitor main){
        activity = new WeakReference<monitor>(main);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        monitor activity = this.activity.get();
        if(activity != null){
            activity.handleMessage(msg);
        }

    }

}
