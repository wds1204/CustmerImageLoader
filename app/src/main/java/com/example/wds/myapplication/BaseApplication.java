package com.example.wds.myapplication;

import android.app.Application;

/**
 * Created by wds on 2017/1/27.
 * Captain 加油吧!!!
 * GitHub:https://github.com/wds1204
 * Email:wdsmyhome@hotmail.com
 */
public class BaseApplication extends Application {
    private static  BaseApplication instance;
    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
    public static BaseApplication getInstance() {
        if (instance == null) {
            instance = new BaseApplication();
        }
        return instance;

    }
}
