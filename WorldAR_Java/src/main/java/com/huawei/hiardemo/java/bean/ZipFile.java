package com.huawei.hiardemo.java.bean;

public class ZipFile {
    private String path; //文件路径
    private String fileName; //文件名称


    public ZipFile() {
    }

    public ZipFile(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "ZipFile{" +
                "path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
