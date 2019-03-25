package com.huawei.hiardemo.java.util;

import com.android.volley.RequestQueue;

import java.util.Arrays;
import java.util.List;

public class Constant {
    public static String SD_PATH;
    public static String DATA_PATH;
    public static String AR_PATH;
    public static Integer EXPORT = 0;
    public static Integer MERGE = 1;
    public static Integer DELETE = 2;
    public static List<String> IMGFILE = Arrays.asList(".PNG",".JPG");

    public static InterRequestUtils interRequestUtils;
    public static RequestQueue mRequestQueue;

    public static String IP_ADDRESS = "https://218.4.33.215:8083";
    public static String STORE_ID = "3";
    public static String MAP_ID = "5122";
    public static String USER_ID;
}
