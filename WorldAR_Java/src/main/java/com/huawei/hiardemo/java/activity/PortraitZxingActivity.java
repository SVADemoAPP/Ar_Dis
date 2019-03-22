package com.huawei.hiardemo.java.activity;

import android.graphics.PointF;
import android.os.Bundle;

import com.huawei.hiardemo.java.db.table.ARLoctionModel;
import com.huawei.hiardemo.java.db.utils.DBUtil;
import com.huawei.hiardemo.java.framework.activity.BaseActivity;
import com.huawei.hiardemo.java.util.LogUtils;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.List;

public class PortraitZxingActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        test();
    }

    @Override
    public void findView() {

    }

    @Override
    public void setContentLayout() {

    }

    @Override
    public void dealLogicBeforeInitView() {

    }

    @Override
    public void initView() {

    }

    @Override
    public void dealLogicAfterInitView() {

    }

    private void test() {
        LogUtils.e("XHF","test-------------------dsd-sd---------------------------dsds-d-----------------");
        LogUtils.i("XHF","test-------------------dsd-sd---------------------------dsds-d-----info-----");
//        DBUtil.addARLocation("测试U5", "U7", new PointF(1, 7));
//        DBUtil.asyncQueryARLocation("测试U5", "U7", new DBUtil.DBListener() {
//            @Override
//            public void asyncQueryData(List<ARLoctionModel> data) {
//                List<ARLoctionModel> loctionModels = data;
//            }
//        });
//        List<ARLoctionModel> arLoctionModels = DBUtil.syncQueryARLocation("测试U5", "U7");
    }
}

