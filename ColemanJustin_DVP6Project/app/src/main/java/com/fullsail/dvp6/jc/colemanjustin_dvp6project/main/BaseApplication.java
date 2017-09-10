package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.app.Application;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.sendbird.android.SendBird;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SendBird
        SendBird.init(getResources().getString(R.string.APP_ID), getApplicationContext());
    }
}
