package com.huawei.hiardemo.java.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.huawei.hiar.ARAnchor;
import com.huawei.hiar.ARPose;
import com.huawei.hiardemo.java.R;
import com.huawei.hiardemo.java.ShareMapHelper;
import com.huawei.hiardemo.java.fragment.ARFragment;
import com.huawei.hiardemo.java.fragment.PrruMapFragment;
import com.huawei.hiardemo.java.framework.activity.BaseActivity;
import com.huawei.hiardemo.java.util.Constant;
import com.huawei.hiardemo.java.util.DistanceUtil;
import com.huawei.hiardemo.java.util.XmlUntils;
import com.huawei.hiardemo.java.view.popup.SelectPopupWindow;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class FloorMapActivity extends BaseActivity implements View.OnClickListener {
    private static final int CODE_OPEN_CAMERA = 1;
    private static final String IMAGE_ROOT_PATH = Environment.getExternalStorageState() + File.separator + "Tester";//todo
    private Context mContext;
    private boolean mFirst = false;
    private TextView mToolName;
    private LinearLayout mBack;
    private LinearLayout mAdd;


    public final int TYPE_TAKE_PHOTO = 1;//Uri获取类型判断
    private static final int CODE_TAKE_PHOTO = 1;// 拍照
    private static final int CODE_SHORT_VIDEO = 2;// 短视频
    public final int NEED_CAMERA = 0;
    private String imgPath;//图片路径
    private Uri photoUri;
    private String mapPath;
    private PrruMapFragment prruMapFragment;
    private ARFragment mArFragment;
    private File ffile;
    private Bitmap mBitmap;
    private SelectPopupWindow mSelectPopupWindow;
    private PointF mSelectPointF;
    private float mScale;
    private int mHeight;
    private String mContents;
    private String siteName;
    private String floorName;

    public void setScale(float scale) {
        mScale = scale;
    }

    @Override
    public void findView() {
        mBack = findViewById(R.id.back);
        mToolName = findViewById(R.id.tool_top_name);
        mAdd = findViewById(R.id.tool_right_add);
        mAdd.setVisibility(View.GONE);
        mBack.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(this);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public String getMap() {
        return mapPath;
    }

    /**
     * 获取选中原始坐标点
     *
     * @return
     */
    public PointF getSelectPoint() {
        if (mSelectPointF != null) {
            return mSelectPointF;
        } else {
            return null;
        }
    }

    @Override
    public void setContentLayout() {
        mContext = this;
        setContentView(R.layout.activity_floor_map);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (!mFirst) {
                mFirst = true;
                mSelectPopupWindow.showPopupWindow();
            }

        }
    }

    @Override
    public void dealLogicBeforeInitView() {
        mapPath = (String) getIntent().getExtras().get("floormap");
        siteName = mapPath.substring(0, mapPath.indexOf(File.separator));
        floorName = mapPath.substring(mapPath.indexOf(File.separator) + 1, mapPath.indexOf("."));
        mBitmap = BitmapFactory.decodeFile(Constant.DATA_PATH + File.separator + mapPath);
        mHeight = mBitmap.getHeight();
        prruMapFragment = new PrruMapFragment();
        prruMapFragment = new PrruMapFragment();
        mArFragment = new ARFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.prru_replace, prruMapFragment);
        fragmentTransaction.add(R.id.ar_replace, mArFragment);
        fragmentTransaction.commit();
        mSelectPopupWindow = new SelectPopupWindow(mContext, mBitmap);
        mSelectPopupWindow.setSelectListener(new SelectPopupWindow.SelectPointListener() {
            @Override
            public void getPoint(PointF pointF) { //有选择点返回
                Log.e("XHF", "PointF=" + pointF.toString());
                mSelectPointF = pointF;
            }

            @Override
            public void cancel() {  //没有选择
                mSelectPopupWindow.hidePopupWindow();
                finish();
            }
        });

        mArFragment.setArCameraListener(new ARFragment.ArCameraListener() {
            @Override
            public void getCameraPose(ARPose arPose) {   //获取到相机返回的实时坐标
                if (mSelectPointF != null) {
                    float tx = arPose.tx();
                    float ty = -arPose.tz();
                    float[] real = DistanceUtil.mapToReal(mScale, mSelectPointF.x, mSelectPointF.y, mHeight);
                    float[] pix = DistanceUtil.realToMap(mScale, (real[0] + tx), (real[1] + ty), mHeight);
                    prruMapFragment.setNowLocation(pix[0], pix[1]);  //设置当前坐标
                }
            }
        });
    }

    public String getSiteName() {
        return siteName;
    }

    public String getFloorName() {
        return floorName;
    }

    @Override
    public void initView() {
        mToolName.setText("寻找pRRU");
    }

    @Override
    public void dealLogicAfterInitView() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void writeToXml() {
        String xmlFilePath = Constant.DATA_PATH + File.separator + siteName + File.separator + "project.xml";
        if (prruMapFragment.judgeSame()) { //判断是否是同一个Prru 同一个存入信息
            Document document = XmlUntils.getDocument(xmlFilePath);
            Element rootElement = XmlUntils.getRootElement(document);
            Element floors = XmlUntils.getElementByName(rootElement, "Floors");
            List<Element> floorList = XmlUntils.getElementListByName(floors, "Floor");
            for (Element element : floorList) {
                //如果是同一楼层
                if (floorName.equals(XmlUntils.getAttributeValueByName(element, "floorCode"))) {
                    List<Element> nes = XmlUntils.getElementListByName(XmlUntils.getElementByName(element, "NEs"), "NE");
                    for (Element ne : nes) {
                        if (XmlUntils.getAttributeValueByName(ne, "id").equals(prruMapFragment.getSelectId())) {
                            Log.e("XHF", "");
                            XmlUntils.setAttributeValueByName(ne, "esn", mContents);
                            XmlUntils.saveDocument(document, new File(xmlFilePath));
                            break;
                        }
                    }
                    break;
                }
            }
            showToast("安装成功");
            Log.e("XHF", "b1");
            ShareMapHelper.writeBuffer(Constant.AR_PATH + File.separator + siteName, floorName + "map.data", mArFragment.getMapData());
            Collection<ARAnchor> arAnchors = mArFragment.getARAnchors();
            Log.e("XHF", "b2" + "arSize=" + arAnchors.size());
            if (arAnchors.size() > 0) {
                ShareMapHelper.svaAnchorToFile(Constant.AR_PATH + File.separator + siteName + File.separator + floorName + "ar.data", arAnchors);
            }
            Log.e("XHF", "b3" + "arSize=" + arAnchors.size());
            prruMapFragment.refreshMap();
            prruMapFragment.cancelPrrusetDialog();
        } else {
            showToastLong("安装位置与设计不符，本次安装无效");
            prruMapFragment.cancelPrrusetDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) //二维码扫描返回结果
        {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "取消", Toast.LENGTH_LONG).show();
                    prruMapFragment.cancelPrrusetDialog();
                } else {
                    Toast.makeText(this, "扫描结果: " + result.getContents(), Toast.LENGTH_LONG).show();
                    mContents = result.getContents();
                    showDialog("消息", "请在安装pRRU位置扫一扫然后点击确认");
                }
            }
        }
        switch (requestCode) {
            case CODE_TAKE_PHOTO:  //拍照返回后获取图片
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        if (data.hasExtra("data")) {
                            //有数据
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //>N
                                String photoPath = "";
                                if (data.getData() != null) {
                                    photoPath = data.getData().getPath();
                                }
                                Log.e("XHF", "have data:path" + photoPath);
                                imgPath = photoPath;

                            } else {//<7.0
                                imgPath = data.getData().getPath();

                            }
                        }
                    } else {
                        //没有数据
                        MediaScannerConnection.scanFile(mContext, new String[]{photoUri.getPath()}, null, null); //扫描文件
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //>23
                            try {
                                String photoUriPath = photoUri.getPath();
                                imgPath = photoUriPath;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            imgPath = photoUri.getPath();
                        }
                    }
                }
                Toast.makeText(mContext, "Path:" + imgPath, Toast.LENGTH_SHORT).show();
                if (imgPath != null && !imgPath.equals("")) {
                    copyPhototoPath(imgPath, IMAGE_ROOT_PATH);
                }

                break;
            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
                break;
        }
    }


    /**
     * 拍照  适配7.0上下
     */
    public void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoUri = get24MediaFileUri(TYPE_TAKE_PHOTO);
            //添加权限
            takeIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takeIntent, CODE_TAKE_PHOTO);
        } else {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoUri = getMediaFileUri(TYPE_TAKE_PHOTO);
            takeIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takeIntent, CODE_TAKE_PHOTO);
        }
    }

    //24以上版本获取
    public Uri get24MediaFileUri(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Photo");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == TYPE_TAKE_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            ffile = mediaFile;
        } else {
            return null;
        }
        return FileProvider.getUriForFile(mContext, "com.huawei.hiardemo.java.fileprovider", mediaFile);
    }

    //24以下版本获取
    public Uri getMediaFileUri(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Photo");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == TYPE_TAKE_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            ffile = mediaFile;
        } else {
            return null;
        }
        return Uri.fromFile(mediaFile);
    }

    /***
     *
     * 将拍摄的照片复制到指定位置
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    private void copyPhototoPath(String oldPath, String newPath) {
        File file = new File(oldPath);
        String name = file.getName(); //获取文件名
        File newFile = new File(newPath, name);
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(newFile);
            int n = 0;
            byte[] bb = new byte[1024];
            while ((n = in.read(bb)) != -1) {
                out.write(bb, 0, n);
            }
            out.close();// 关闭输入输出流
            in.close();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            // 回收并且置为null
            mBitmap.recycle();
            mBitmap = null;
        }
        if (mSelectPopupWindow != null) {
            mSelectPopupWindow.hidePopupWindow();
        }
        super.onDestroy();
    }

    public void openZxing() {
        // 打开扫描界面扫描条形码或二维码
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES); //设置扫描的类型  条形码 二维码均可
        intentIntegrator.setOrientationLocked(false);  //方向锁定
        intentIntegrator.setCaptureActivity(PortraitZxingActivity.class);
        intentIntegrator.setCameraId(0); //前置相机还是后置相机
        intentIntegrator.setBeepEnabled(true); //是否发出成功的声音
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
    }

    public void setAnchor() {
        mArFragment.setCameraPose();
    }

    private void showDialog(String title, String message) {
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
                        writeToXml();
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        prruMapFragment.cancelPrrusetDialog();
                    }
                });
        // 显示
        normalDialog.show();
    }
}
