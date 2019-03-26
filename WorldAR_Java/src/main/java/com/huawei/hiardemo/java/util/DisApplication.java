package com.huawei.hiardemo.java.util;

import android.os.Environment;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import com.android.volley.toolbox.Volley;
import com.facebook.stetho.Stetho;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.io.File;

public class DisApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Constant.SD_PATH = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append(File.separator).append("disvisible").toString();
        Constant.DATA_PATH = Constant.SD_PATH + File.separator + "data";
        Constant.AR_PATH = Constant.SD_PATH + File.separator + "AR";

        Constant.mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        Constant.interRequestUtils = InterRequestUtils.getInstance(getApplicationContext());
        initPhotoError();
        FlowManager.init(this); //初始化数据库问题
        Stetho.initializeWithDefaults(getApplicationContext()); //初始化facebook chrome 持久化数据查看
        LogUtils.getInstance()
                .setDiskPath(Constant.AR_PATH+File.separator+"Log")
                .setLevel(LogUtils.VERBOSE_LEVEL)
                .setWriteFlag(true);  //写入日志文件
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
    }

    private void initPhotoError() {
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

}
