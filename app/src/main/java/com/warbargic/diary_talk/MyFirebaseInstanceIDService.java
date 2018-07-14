package com.warbargic.diary_talk;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.warbargic.diary_talk.MainActivity.real_token;

/**
 * Created by kippe_000 on 2017-02-03.
 */


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        // 생성등록된 토큰을 개인 앱서버에 보내 저장해 두었다가 추가 뭔가를 하고 싶으면 할 수 있도록 한다.
        sendRegistrationToServer(token);
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        SharedPreferences sharedPreferences = this.getSharedPreferences("TOKEN", Context.MODE_PRIVATE);
//        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("TOKEN", Context.);
//        Log.d("token", "::::::::::::::::::::::::::::"+sharedPreferences.getString("token","") + real_token);
//        real_token = sharedPreferences.getString("token","");
        Log.d("adsasdasd","SERVICE onCreate");
        Log.e("asdfasdf", real_token);
    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fcm_token", token);
        editor.apply();

    }
}