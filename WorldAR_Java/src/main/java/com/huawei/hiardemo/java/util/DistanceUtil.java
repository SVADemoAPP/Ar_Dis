package com.huawei.hiardemo.java.util;

import android.graphics.PointF;

public class DistanceUtil {
    /**
     * 获取像素转实际距离（m）
     *
     * @param scale  比例尺
     * @param pointF 点坐标
     */
    public static float[] getPixtoReal(float scale, PointF pointF) {
        float[] distance = new float[2];
        distance[0] = pointF.x / scale;
        distance[1] = pointF.y / scale;
        return distance;
    }

    /**
     * 实际距离转换成像素（pix）
     *
     * @param scale
     * @param distance
     * @return
     */
    public static int getDistancetoPix(float scale, float distance) {
        int pixDistance = (int) (distance * scale);
        return pixDistance;
    }
}
