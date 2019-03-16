package com.huawei.hiardemo.java.util;

import android.app.Application;
import android.os.Environment;

import java.io.File;

public class DisApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Constant.SD_PATH = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append(File.separator).append("disvisible").toString();
        Constant.DATA_PATH = Constant.SD_PATH + File.separator + "data";
    }
}
