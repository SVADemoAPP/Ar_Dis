package com.huawei.hiardemo.java.activity;

import android.graphics.PointF;
import android.os.Bundle;

import com.huawei.hiardemo.java.db.table.ARLoctionModel;
import com.huawei.hiardemo.java.db.utils.DBUtil;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.List;

public class PortraitZxingActivity extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        test();
    }

    private void test() {
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

