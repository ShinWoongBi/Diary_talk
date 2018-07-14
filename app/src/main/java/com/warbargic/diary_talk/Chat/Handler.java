package com.warbargic.diary_talk.Chat;

import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by kippe_000 on 2017-02-11.
 */

public class Handler extends android.os.Handler {
    WeakReference<main> activity;

    Handler(main main){
        activity = new WeakReference<com.warbargic.diary_talk.Chat.main>(main);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        main activity = this.activity.get();
        if(activity != null){
            activity.handleMessage(msg);
        }

    }
}
