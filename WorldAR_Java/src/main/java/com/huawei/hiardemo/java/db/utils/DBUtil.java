package com.huawei.hiardemo.java.db.utils;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.huawei.hiardemo.java.db.table.ARLoctionModel;
import com.huawei.hiardemo.java.db.table.ARLoctionModel_Table;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.List;
import java.util.ListIterator;

/**
 * 数据库操作工具类
 */

public class DBUtil {
    /**
     * @param buildingName 楼宇名称
     * @param siteName     站点名称
     * @param initPoint    初始化坐标点
     * @return
     */
    public static boolean addARLocation(String buildingName, String siteName, PointF initPoint) {
        try {
            ARLoctionModel arLoctionModel = new ARLoctionModel();
            arLoctionModel.setBuildingName(buildingName);
            arLoctionModel.setSiteName(siteName);
            arLoctionModel.setLocation(initPoint.x + "," + initPoint.y);
            long insert = arLoctionModel.insert();
            return true;
        } catch (Exception e) {
            e.toString();
            return false;
        }

    }


    /**
     * @param id           id
     * @param buildingName 楼宇名称
     * @param siteName     站点名称
     * @param initPoint    初始化坐标点
     * @return
     */
    public static boolean updateRLocation(int id, String buildingName, String siteName, PointF initPoint) {
        try {
            ARLoctionModel arLoctionModel = new ARLoctionModel();
            arLoctionModel.setId(id);
            arLoctionModel.setBuildingName(buildingName);
            arLoctionModel.setSiteName(siteName);
            arLoctionModel.setLocation(initPoint.x + "," + "," + initPoint.y);
            boolean update = arLoctionModel.update();
            return update;
        } catch (Exception e) {
            e.toString();
            return false;
        }

    }

    /***
     * 事务查询（async）
     * @param buildingName 楼宇名称
     * @param siteName     站点名称
     * @return
     */
    public static void asyncQueryARLocation(String buildingName, String siteName, final DBListener dbListener) {
        OperatorGroup op = OperatorGroup.clause().and(ARLoctionModel_Table.buildingName.eq(buildingName)).and(ARLoctionModel_Table.siteName.eq(siteName));  //连接多
        SQLite.select().from(ARLoctionModel.class).where(op).async().queryListResultCallback(new QueryTransaction.QueryResultListCallback<ARLoctionModel>() {
            @Override
            public void onListQueryResult(QueryTransaction transaction, @NonNull List<ARLoctionModel> tResult) {
                dbListener.asyncQueryData(tResult);
            }
        }).execute();
    }

    /**
     * 同步查询
     *
     * @param buildingName
     * @param siteName
     */
    public static List<ARLoctionModel> syncQueryARLocation(String buildingName, String siteName) {
        OperatorGroup op = OperatorGroup.clause().and(ARLoctionModel_Table.buildingName.eq(buildingName)).and(ARLoctionModel_Table.siteName.eq(siteName));  //连接多个查询条件
        List<ARLoctionModel> arLoctionModels = SQLite.select().from(ARLoctionModel.class).where(op).queryList();
        return arLoctionModels;
    }

    /**
     * 删除
     *
     * @param arLoctionModel
     */
    public static void deleteARLocation(ARLoctionModel arLoctionModel) {
        arLoctionModel.delete();
    }

    /**
     * 删除指定id数据
     *
     * @param id
     */
    public static void deleteARLocation(int id) {
        OperatorGroup op = OperatorGroup.clause().and(ARLoctionModel_Table.id.eq(id));  //连接多个查询条件
        ARLoctionModel arLoctionModel = SQLite.select().from(ARLoctionModel.class).where(op).querySingle();
        arLoctionModel.delete();
//        ListIterator<ARLoctionModel> iterators = arLoctionModels.listIterator();
//        while (iterators.hasNext()) {
//            iterators.next().delete();
//        }
    }

    public interface DBListener {

        /**
         * 查询数据回调
         *
         * @param data 回调数据
         */
        void asyncQueryData(List<ARLoctionModel> data);
    }
}
