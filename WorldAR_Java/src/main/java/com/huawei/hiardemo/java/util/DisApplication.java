package com.huawei.hiardemo.java.util;

import android.app.Application;
import android.os.Environment;
import android.os.StrictMode;

import java.io.File;

public class DisApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Constant.SD_PATH = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append(File.separator).append("disvisible").toString();
        Constant.DATA_PATH = Constant.SD_PATH + File.separator + "data";
        initPhotoError();
    }
    private void initPhotoError(){
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

}
