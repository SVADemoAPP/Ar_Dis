package com.huawei.hiardemo.java.util;

import android.util.Log;


import com.huawei.hiardemo.java.framework.utils.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    /**
     * 压缩一个文件夹
     *
     * @throws IOException
     */
    public static void zipDirectory(String path) throws IOException {
        File file = new File(path);
        String parent = file.getParent();
        File zipFile = new File(parent, file.getName() + ".zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        zip(zos, file, file.getName());
        zos.flush();
        zos.close();
    }

    /**
     *
     * @param zos
     *            压缩输出流
     * @param file
     *            当前需要压缩的文件
     * @param path
     *            当前文件相对于压缩文件夹的路径
     * @throws IOException
     */
    private static void zip(ZipOutputStream zos, File file, String path) throws IOException {
        // 首先判断是文件，还是文件夹，文件直接写入目录进入点，文件夹则遍历
        if (file.isDirectory()) {
            ZipEntry entry = new ZipEntry(path + File.separator);// 文件夹的目录进入点必须以名称分隔符结尾
            zos.putNextEntry(entry);
            File[] files = file.listFiles();
            for (File x : files) {
                zip(zos, x, path + File.separator + x.getName());
            }
        } else {
            FileInputStream fis = new FileInputStream(file);// 目录进入点的名字是文件在压缩文件中的路径
            ZipEntry entry = new ZipEntry(path);
            zos.putNextEntry(entry);// 建立一个目录进入点

            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.flush();
            fis.close();
            zos.closeEntry();// 关闭当前目录进入点，将输入流移动下一个目录进入点
        }
    }

    /**
     * 解压文件
     *
     * @param unzip
     * @throws IOException
     */
    public static void unzip(String basePath, String unzip) {
        File file = new File(unzip);
        //如目标目果没有传解压录，则默认为文件所在目录
        if(StringUtil.isNullOrEmpty(basePath)){
            basePath = file.getParent();
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(fis);
            unzip(basePath,zis);
        }catch (IOException e){
            Log.e("解压文件出错", "文件：" + unzip);
        }


    }

    private static void unzip(String basePath, ZipInputStream zis) throws IOException {
        ZipEntry entry = zis.getNextEntry();
        if (entry != null) {
            File file = new File(basePath + File.separator + entry.getName());
            if (file.isDirectory()) {
                // 可能存在空文件夹
                if (!file.exists())
                    file.mkdirs();
                unzip(basePath,zis);
            } else {
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists())
                    parentFile.mkdirs();
                FileOutputStream fos = new FileOutputStream(file);// 输出流创建文件时必须保证父路径存在
                int len = 0;
                byte[] buf = new byte[1024];
                while ((len = zis.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
                fos.close();
                zis.closeEntry();
//                unzip(basePath,zis);
            }
        }
    }

}
