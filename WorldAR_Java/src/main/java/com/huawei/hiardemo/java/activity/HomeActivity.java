package com.huawei.hiardemo.java.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.huawei.hiardemo.java.R;
import com.huawei.hiardemo.java.adapter.RvGroupAdapter;
import com.huawei.hiardemo.java.adapter.RvMemberAdapter;
import com.huawei.hiardemo.java.bean.FloorModel;
import com.huawei.hiardemo.java.bean.SiteModel;
import com.huawei.hiardemo.java.framework.activity.BaseActivity;
import com.huawei.hiardemo.java.util.BlueUntils;
import com.huawei.hiardemo.java.util.Constant;
import com.huawei.hiardemo.java.util.FileUtils;
import com.huawei.hiardemo.java.util.ZipUtils2;
import com.huawei.hiardemo.java.view.popup.LoadingDialog;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.zaaach.toprightmenu.MenuItem;
import com.zaaach.toprightmenu.TopRightMenu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUESTCODE_FROM_ACTIVITY = 1000;  //选择文件返回code
    private static final int REQUESTCODE_SELECTOR_FILE = 300;//在文件搜索activity返回
    private static final String DIRECTION_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath(); //文件根目录
    private static final String DIRECTION_BLUETOOTH_0 = DIRECTION_ROOT + File.separator + "Bluetooth/"; //蓝牙路径1  路径不区分大小写
    private static final String DIRECTION_BLUETOOTH_1 = DIRECTION_ROOT + File.separator + "Download/Bluetooth/"; //蓝牙路径2

    private RecyclerView mRecyclerView;  //父布局
    private RvGroupAdapter mRvGroupAdapter;  //父布局adapter
    private List<SiteModel> siteList = new ArrayList<>();
    private LinearLayout mllAdd;     //右上角添加按钮
    private Context mContext;
    private TopRightMenu mTopRightMenu; //顶部右侧弹出按钮

    @Override
    public void findView() {
        mRecyclerView = findViewById(R.id.rv_group);
        mllAdd = findViewById(R.id.tool_right_add);
        mllAdd.setOnClickListener(this);
    }

    @Override
    public void setContentLayout() {
        mContext = this;
        setContentView(R.layout.activity_home);
    }


    private void initFloorMapsDir() {
        File dir = new File(Constant.SD_PATH + "/data/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photosDir = new File(Constant.SD_PATH + "/photos/");
        if (!photosDir.exists()) {
            photosDir.mkdirs();
        }
    }

    private void initSiteAndFloor() {
        siteList.clear();
        File dataFile = new File(Constant.DATA_PATH);
        List<File> fileList = Arrays.asList(dataFile.listFiles());
        int index;
        for (File file : fileList) {
            if (file.isDirectory()) {
                SiteModel siteModel = new SiteModel();
                siteModel.siteName = file.getName();
                List<File> allFiles = Arrays.asList(file.listFiles());
                for (File subFiles : allFiles) {

                    index = subFiles.getName().lastIndexOf(".");
                    if (index > -1) {
                        String fileType = subFiles.getName().toUpperCase().substring(index);
                        if (Constant.IMGFILE.contains(fileType)) {
                            FloorModel floorModel = new FloorModel();
                            floorModel.floorName = subFiles.getName().substring(0, subFiles.getName().lastIndexOf("."));
                            floorModel.floorMap = file.getName() + File.separator + subFiles.getName();
                            siteModel.floorModelList.add(floorModel);
                        }
                    }
                }
                siteList.add(siteModel);
            }

        }
    }


    @Override
    public void dealLogicBeforeInitView() {
        initFloorMapsDir();
        initSiteAndFloor();
    }

    @Override
    public void initView() {
        LoadingDialog.with(mContext)
                .initDialog()
                .setTouchOutSide(false)
                .setProgressText("请等待...");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemViewCacheSize(siteList.size());
        mRvGroupAdapter = new RvGroupAdapter(siteList, this, onMemberItemClickListener);
        mRvGroupAdapter.setOnGroupItemClickListener(onGroupItemClickListener);
        mRecyclerView.setAdapter(mRvGroupAdapter);

    }

    @Override
    public void dealLogicAfterInitView() {
        showTopRightMenu();
    }

    private RvMemberAdapter.OnMemberItemClickListener onMemberItemClickListener = new RvMemberAdapter.OnMemberItemClickListener() {
        @Override
        public void memberClick(String floorMap, int type) {
            Log.e("XHF", "floorMap=" + floorMap + " , type=" + type);
            Bundle bundle = new Bundle();
            bundle.putString("floormap", floorMap);
            openActivity(FloorMapActivity.class, bundle);
        }
    };


    private RvGroupAdapter.OnGroupItemClickListener onGroupItemClickListener = new RvGroupAdapter.OnGroupItemClickListener() {
        @Override
        public void groupClick(String siteName, int type) {
            switch (type) {
                case 0:
                    if (!BlueUntils.isBluetoothAvaliable()) {
                        showToast("本机没有找到蓝牙硬件，无法使用蓝牙功能！");
                    }
                    BluetoothAdapter mAdapter = BlueUntils.getBluetoothAdapter();

                    if (!mAdapter.isEnabled()) {
                        //弹出对话框提示用户是后打开
                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        enabler.putExtra("siteName", siteName);
                        startActivityForResult(enabler, 0);
                    } else {
                        shareFile(siteName);
                    }
                    break;
                case 1:
                    showNormalDialog("合并", "确定进行合并操作吗？");
                    break;
                case 2:
                    String path = Constant.DATA_PATH + File.separator + siteName;
                    FileUtils.deleteDir(new File(path));
                    showNormalDialog("删除", "确定进行删除操作吗？");
                    break;
                default:
                    showToast("未知的点击操作" + type);
                    break;
            }
        }
    };

    private void showNormalDialog(String title, String message) {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
//        normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle(title);
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext, "操作成功", Toast.LENGTH_SHORT).show();
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            String siteName = data.getStringExtra("siteName");
            shareFile(siteName);
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FROM_ACTIVITY) {
                ArrayList<String> paths = data.getStringArrayListExtra("paths"); //获取返回文件路径 （可以有多个）
                File file = new File(paths.get(0));
                UnZipData(file); //解压文件
            } else if (requestCode == REQUESTCODE_SELECTOR_FILE) {
                String filePath = data.getStringExtra("filePath");
                Toast.makeText(mContext, "选择" + filePath, Toast.LENGTH_SHORT).show(); //todo
                File file = new File(filePath);
                UnZipData(file); //解压文件
            }
        }
    }

    /**
     * 解压选择的文件
     *
     * @param file
     */
    private void UnZipData(File file) {
        if (FileUtils.isZipFile(file)) {
            try {
                ZipUtils2.UnZipFolder(file.getPath(), Constant.DATA_PATH);
            } catch (Exception e) {
                Log.e("XHF_ERROR", "zip=" + e.toString());
                Toast.makeText(mContext, "文件解析出错，请核对是否是该文件", Toast.LENGTH_SHORT).show();
            }
            // 刷新主界面
            initSiteAndFloor();
            mRvGroupAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(mContext, "选择的文件不正确", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(String siteName) {
        String basePath = Constant.DATA_PATH + File.separator;
        String path = basePath + siteName + ".zip";
        try {
            ZipUtils2.ZipFolder(basePath + siteName, path);
        } catch (IOException e) {
            showToast("文件压缩失败");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //调用android分享窗口
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.setPackage("com.android.bluetooth");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));//path为文件的路径
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent chooser = Intent.createChooser(intent, "Share app");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(chooser);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tool_right_add:
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mTopRightMenu != null) {
                    mTopRightMenu.showAsDropDown(mllAdd, -200, 20);    //带偏移量
                }
                break;
            default:
                break;
        }
    }


    /**
     * 初始化topRightmenu
     */
    private void showTopRightMenu() {
        mTopRightMenu = new TopRightMenu((Activity) mContext);
        //添加菜单项
        List<MenuItem> mMenuItems = new ArrayList<>();
        mMenuItems.add(new MenuItem("从蓝牙目录查找"));
        mMenuItems.add(new MenuItem("从其他目录查找"));
        mTopRightMenu
                .setHeight(264)     //默认高度480
                .setWidth(350)      //默认宽度wrap_content
                .showIcon(false)     //显示菜单图标，默认为true
                .dimBackground(true)        //背景变暗，默认为true
                .needAnimationStyle(true)   //显示动画，默认为true
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)
                .addMenuList(mMenuItems)
                .setOnMenuItemClickListener(new TopRightMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        switch (position) {
                            case 0: //蓝牙目录
//                                scanBlueToothFile();
                                new Thread(new Runnable() {  //延迟执行 避免卡住UI
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(150);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    getOtherRxPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PerMissonListener() {
                                                        @Override
                                                        public void havePermission() {
                                                            openBlueToothFileSelector();//打开蓝牙目录
                                                        }

                                                        @Override
                                                        public void missPermission() {
                                                            showToast("请在权限管理中打开存储权限");
                                                        }
                                                    });

                                                }
                                            });
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();

                                break;
                            case 1: //其他目录
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(150);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
//                                                    openFileSelector(DIRECTION_ROOT, "文件选择"); //打开文件根目录
                                                    getOtherRxPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PerMissonListener() {
                                                        @Override
                                                        public void havePermission() {
                                                            Intent intent = new Intent(mContext, FileSearchActivity.class);
                                                            startActivityForResult(intent, 300);
                                                        }

                                                        @Override
                                                        public void missPermission() {
                                                            showToast("请在权限管理中打开存储权限");
                                                        }
                                                    });

                                                }
                                            });
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                                break;
                        }

                    }
                });

    }

    /***
     * 打开蓝牙文件目录
     */
    private void openBlueToothFileSelector() {
        File file = new File(DIRECTION_BLUETOOTH_0);
        if (file.exists()) //判断文件目录是否存在
        {
            openFileSelector(DIRECTION_BLUETOOTH_0, "蓝牙目录"); //存在打开
            return;
        }
        File file2 = new File(DIRECTION_BLUETOOTH_1);
        if (file2.exists()) {
            openFileSelector(DIRECTION_BLUETOOTH_1, "蓝牙目录"); //存在打开
            return;
        }
        Toast.makeText(mContext, "该没有蓝牙目录，请选择其他目录", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (LoadingDialog.with(mContext).isShowing()) {
            LoadingDialog.with(mContext).cancelDialog();
        }
        super.onDestroy();
    }

    /**
     * 打开文件路径选择器
     *
     * @param direction 文件路径
     * @param name      title名称
     */
    private void openFileSelector(String direction, String name) {
        new LFilePicker()
                .withTitle(name)
                .withActivity((Activity) mContext)
                .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                .withStartPath(direction)//打开文件初始路径
                .withMutilyMode(false)  //false 为单选 true为多选
                .withIsGreater(false)
                .withFileSize(500 * 1024 * 10)   //文件大小过滤器
                .start();
    }


}
