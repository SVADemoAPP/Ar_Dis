package com.huawei.hiardemo.java.db.table;

import com.huawei.hiardemo.java.db.AppDataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;

/***
 * 用于存储AR当前定位点的Table
 */

@Table(database = AppDataBase.class)
public class ARLoctionModel extends BaseModel implements Serializable {

    @PrimaryKey(autoincrement = true)
    private int id;

    @Column
    private String buildingName;   //楼宇名称

    @Column
    private String siteName; //站点名称

    @Column
    private String location;  //当前定位点

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
