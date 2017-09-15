// Justin Coleman
// DVP6 - 1709
// PreferencesUtil.java

package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

public class PreferencesUtil {

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String CONNECTED = "connected";

    private PreferencesUtil(){

    }

    public static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("sendbird", Context.MODE_PRIVATE);
    }

    public static void setEmail(Context context, String email){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(EMAIL, email).apply();
    }

    public static String getEmail(Context context){
        return getSharedPreferences(context).getString(EMAIL, "");
    }

    public static void setPassword(Context context, String password){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PASSWORD, password).apply();
    }

    public static String getPassword(Context context){
        return getSharedPreferences(context).getString(PASSWORD, "");
    }

    public static void setUsername(Context context, String username){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(USERNAME, username).apply();
    }

    public static String getUsername(Context context){
        return getSharedPreferences(context).getString(USERNAME, "");
    }

    public static void setConnected(Context context, boolean status){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(CONNECTED, status).apply();
    }

    public static boolean getConnected(Context context){
        return getSharedPreferences(context).getBoolean(CONNECTED, false);
    }

    public static void clearAll(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear().apply();
    }

    public static void updateUsername(String username){
        SendBird.updateCurrentUserInfo(username, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();

                }
            }
        });
    }

    public static void updateUserToken(){
        SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(),
                new SendBird.RegisterPushTokenWithStatusHandler() {
            @Override
            public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus
                    , SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }
            }
        });
    }
}
