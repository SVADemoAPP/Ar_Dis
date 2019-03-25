package com.huawei.hiardemo.java.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.huawei.hiardemo.java.framework.sharef.SharedPrefHelper;

import org.json.JSONObject;

public class PrruSubscribe {
    private Context context;

    public PrruSubscribe(Context context) {
        this.context = context;
    }

    public void toSubscription() {
        JsonObjectPostRequest newMissRequest = new JsonObjectPostRequest(Request.Method.POST,Constant.IP_ADDRESS + "/tester/api/app/subscribePrru?storeId=" + Constant.STORE_ID + "&ip=" + Constant.USER_ID, new Listener<JSONObject>() {
            public void onResponse(JSONObject jsonobj) {
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
            }
        });
        newMissRequest.setSendCookie(SharedPrefHelper.getString(context, "Cookie"));
        Constant.mRequestQueue.add(newMissRequest);
    }

    public void cancleSubscription() {
        JsonObjectPostRequest newMissRequest = new JsonObjectPostRequest(Request.Method.POST, Constant.IP_ADDRESS + "/tester/api/app/unSubscribePrru?storeId=" + Constant.STORE_ID + "&ip=" + Constant.USER_ID, new Listener<JSONObject>() {
            public void onResponse(JSONObject jsonobj) {
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
            }
        });
        newMissRequest.setSendCookie(SharedPrefHelper.getString(context, "Cookie"));
        Constant.mRequestQueue.add(newMissRequest);
    }
}
