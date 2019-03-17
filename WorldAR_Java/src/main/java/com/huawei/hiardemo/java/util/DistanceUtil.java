package com.huawei.hiardemo.java.util;

import android.graphics.PointF;

import net.yoojia.imagemap.core.PrruInfoShape;

import java.util.List;

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


    /**
     * 获取距离最近的prru
     *
     * @param scale
     * @param flag m
     * @param data
     * @param x
     * @param y
     * @return
     */
    public static PrruInfoShape getMinDistacePrru(float scale, float flag, List<PrruInfoShape> data, int x, int y) {
        int min = -1;//最近prru下标；
        int minDistance = -1;
        for (int i = 0; i < data.size(); i++) {
            PrruInfoShape prruInfoShape = data.get(i);
            PointF centerPoint = prruInfoShape.getCenterPoint();
            int pixDistance = (int) Math.sqrt((centerPoint.x - x) * (centerPoint.x - x) + (centerPoint.y - y) * (centerPoint.x - x));
//            if (minDistance == -1) {
//                minDistance = pixDistance;
//                min = i;
//            }
//            if (pixDistance < minDistance) {
//                minDistance = pixDistance;
//                min = i;
//            }
            if (pixDistance <= flag * scale) {
                min = i;
            }
        }
        return data.get(min);
    }


}
