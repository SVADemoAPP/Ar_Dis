package com.huawei.hiardemo.java;

import android.util.Log;

import com.huawei.hiar.ARAnchor;
import com.huawei.hiar.ARPose;
import com.huawei.hiar.ARSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ShareMapHelper {
    private static final String TAG = ShareMapHelper.class.getSimpleName();

    public static void writeBuffer(String filePath, String fileName, ByteBuffer byteBuffer) {
        File file = new File(filePath);
        if (!file.exists()) {   //判断有无文件夹
            file.mkdirs();      //创建文件
        }
        File file2 = new File(file, fileName);
        try {
            file2.createNewFile();  //创建新的文件
        } catch (IOException e) {
            Log.e(TAG, "Create file exception");
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            byte[] bytes = new byte[byteBuffer.limit()];
            byteBuffer.get(bytes);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Write file not found exception");
        } catch (IOException e) {
            Log.e(TAG, "Write file exception");
        }
    }

    public static byte[] readBuffer(File file) {
        byte[] map = new byte[(int) file.length()];
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(map);
                fileInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Read file exception");
            }
        }
        return map;
    }

    public static void svaAnchorToFile(String fileName, Collection<ARAnchor> arAnchors) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            StringBuilder stringBuilder = new StringBuilder();
            float[] translation = new float[3];
            float[] quaternion = new float[4];
            for (ARAnchor arAnchor : arAnchors) {
                arAnchor.getPose().getTranslation(translation, 0);
                arAnchor.getPose().getRotationQuaternion(quaternion, 0);
                stringBuilder.append(translation[0]);
                stringBuilder.append(",");
                stringBuilder.append(translation[1]);
                stringBuilder.append(",");
                stringBuilder.append(translation[2]);
                stringBuilder.append(",");
                stringBuilder.append(quaternion[0]);
                stringBuilder.append(",");
                stringBuilder.append(quaternion[1]);
                stringBuilder.append(",");
                stringBuilder.append(quaternion[2]);
                stringBuilder.append(",");
                stringBuilder.append(quaternion[3]);
                stringBuilder.append("\n");
                fileOutputStream.write(stringBuilder.toString().getBytes());
                stringBuilder.delete(0, stringBuilder.length());
            }
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Svae anchor file exception");
        }
    }

    public static List<ARAnchor> readAnchorFromFile(File fileName, ARSession arSession) {
        byte[] stream = readBuffer(fileName);
        if (stream.length == 0) {
            Log.e(TAG, "No anchor");
            return null;
        }

        String[] trans = new String(stream).split("\n");
        ArrayList<ARAnchor> arAnchors = new ArrayList<>();

        for (int i = 0; i < trans.length; i++) {
            String[] slit = trans[i].split(",");
            float[] translation = new float[3];
            float[] quaternion = new float[4];
            for (int t = 0; t < 3; t++) {
                translation[t] = Float.parseFloat(slit[t]);
            }
            for (int q = 0; q < 4; q++) {
                quaternion[q] = Float.parseFloat(slit[q + 3]);
            }
            ARPose arPose = new ARPose(translation, quaternion);
            Log.e("XHF", "-----1");
            ARAnchor arAnchor = arSession.createAnchor(arPose);
            arAnchors.add(arAnchor);
        }

        return Collections.unmodifiableList(arAnchors);
    }
}
