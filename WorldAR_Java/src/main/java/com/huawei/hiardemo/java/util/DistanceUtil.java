package com.huawei.hiardemo.java.util;

import android.graphics.PointF;
import android.util.Log;

import net.yoojia.imagemap.core.PrruInfoShape;

import java.util.List;

public class DistanceUtil {
    public static float[] realToMap(float mapScale, float rx, float ry, int height) {
        return new float[]{rx * mapScale, height - ry * mapScale};
    }


    public static float[] mapToReal(float mapScale, float mx, float my, int height) {
        return new float[]{mx / mapScale, (height - my) / mapScale};
    }

    /**
     * 获取像素转实际距离（m）
     *
     * @param scale  比例尺
     * @param pointF 点坐标
     */
    public static float[] getPixtoReal(float scale, PointF pointF, int height) {
        float[] distance = new float[2];
        distance[0] = pointF.x / scale;
        distance[1] = height - pointF.y / scale;
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
     * @param flag  m
     * @param data
     * @param x
     * @param y
     * @return
     */
    public static PrruInfoShape getMinDistacePrru(float scale, float flag, List<PrruInfoShape> data, float x, float y, int height) {
        int min = -1;//最近prru下标；
        int minDistance = -1;
        Log.e("XHF", "data=" + data.size());
        for (int i = 0; i < data.size(); i++) {
            PrruInfoShape prruInfoShape = data.get(i);
            PointF centerPoint = prruInfoShape.getCenter();
            float[] real1 = mapToReal(scale, centerPoint.x, centerPoint.y, height);
            float[] real2 = mapToReal(scale, x, y, height);
            double pixDistance = Math.sqrt((real1[0] - real2[0]) * (real1[0] - real2[0]) + (real1[1] - real2[1]) * (real1[1] - real2[1]));  //实际距离 m
//            if (minDistance == -1) {
//                minDistance = pixDistance;
//                min = i;
//            }
//            if (pixDistance < minDistance) {
//                minDistance = pixDistance;
//                min = i;
//            }
            Log.e("XHF", "pixDistance=" + pixDistance + "----------flag * scale=" + flag * scale);
            if (pixDistance <= flag) {
                min = i;
                Log.e("XHF2", "pixDistance=" + pixDistance);
                return data.get(min);
            }
        }
        return null;
    }


    public static float[] getPoint(final float x, final float y, float angle) {
        final float[] point = new float[2];
        angle = angle - 47;
        if (angle < 0) {
            angle = angle + 360;
        }
        point[0] = (float) (x * Math.cos(2 * Math.PI / 360 * angle) + y * Math.sin(2 * Math.PI / 360 * angle));
        point[1] = (float) (y * Math.cos(2 * Math.PI / 360 * angle) - x * Math.sin(2 * Math.PI / 360 * angle));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    LogUtils.d("Position-----", "X:" + x + "--Y:" + y);
                    LogUtils.d("Position-----", "X1:" + point[0] + "--Y1:" + point[1]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return point;

    }


}
