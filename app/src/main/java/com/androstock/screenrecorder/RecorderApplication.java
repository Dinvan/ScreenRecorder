package com.androstock.screenrecorder;

import android.app.Application;

import com.orm.SugarContext;

/**
 * Created by advanz101 on 16/10/17.
 */

public class RecorderApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(getApplicationContext());
    }
}
