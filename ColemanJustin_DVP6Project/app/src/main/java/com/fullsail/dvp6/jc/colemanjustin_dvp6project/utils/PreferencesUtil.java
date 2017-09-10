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

    public static final String USER_ID = "userID";
    public static final String DISPLAYNAME = "displayName";
    public static final String CONNECTED = "connected";

    private PreferencesUtil(){

    }

    public static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("sendbird", Context.MODE_PRIVATE);
    }

    public static void setUserId(Context context, String userId){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(USER_ID, userId).apply();
    }

    public static String getUserId(Context context){
        return getSharedPreferences(context).getString(USER_ID, "");
    }

    public static void setDisplayName(Context context, String displayName){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(DISPLAYNAME, displayName).apply();
    }

    public static String getDisplayName(Context context){
        return getSharedPreferences(context).getString(DISPLAYNAME, "");
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

    public static void updateDisplayName(String displayName){
        SendBird.updateCurrentUserInfo(displayName, null, new SendBird.UserInfoUpdateHandler() {
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
