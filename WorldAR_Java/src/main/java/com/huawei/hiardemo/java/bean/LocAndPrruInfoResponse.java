package com.huawei.hiardemo.java.bean;

import java.util.List;

/**
 * Created by chinasoft_gyr on 2018/11/19.
 */

public class LocAndPrruInfoResponse extends BaseReponse{
    public Data data;
    public class Data{
        public int status;
        public List<PrruSigalModel> prruData;
        public String userId;
        public Data1 data;
    }

    public class Data1{
        public float x;
        public float y;
    }
}
