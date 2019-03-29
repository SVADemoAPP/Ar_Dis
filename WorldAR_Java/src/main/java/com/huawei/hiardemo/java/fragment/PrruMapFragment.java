package com.huawei.hiardemo.java.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.huawei.hiardemo.java.R;
import com.huawei.hiardemo.java.activity.FloorMapActivity;
import com.huawei.hiardemo.java.bean.Position;
import com.huawei.hiardemo.java.bean.PrruInfo;
import com.huawei.hiardemo.java.framework.activity.BaseActivity;
import com.huawei.hiardemo.java.framework.utils.StringUtil;
import com.huawei.hiardemo.java.util.Constant;
import com.huawei.hiardemo.java.util.DistanceUtil;
import com.huawei.hiardemo.java.util.LogUtils;
import com.huawei.hiardemo.java.util.XmlUntils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.yoojia.imagemap.HighlightImageView1;
import net.yoojia.imagemap.ImageMap1;
import net.yoojia.imagemap.TouchImageView1;
import net.yoojia.imagemap.core.Bubble;
import net.yoojia.imagemap.core.CircleRangeShape;
import net.yoojia.imagemap.core.CircleShape;
import net.yoojia.imagemap.core.CollectPointShape;
import net.yoojia.imagemap.core.MoniPointShape;
import net.yoojia.imagemap.core.PrruGkcShape;
import net.yoojia.imagemap.core.PrruInfoShape;
import net.yoojia.imagemap.core.PushMessageShape;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;
import net.yoojia.imagemap.core.SpecialShape;

import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

public class PrruMapFragment extends Fragment {
    private int mMi = 2;
    private ImageMap1 mFloorMap;  //地图
    private Bitmap mBitmap;  //图片
    private int mWidth;  //图片宽度
    private int mHeight;   //图片高度
    private String mapPath; //加载地图路径
    private PrruInfoShape mNowSelectPrru; //点击暂存prru
    private PrruInfoShape tempPrruInfoShape;
    private PrruInfoShape redPrruInfoShape;
    private String[] mPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private View mMenuView;
    private View mMenuBind;
    private View mMenuUnBind;
    private View mMenuMove;
    private View mMenuCamera;
    private Context mContext;
    private float mScale;  //比例尺
    private CircleRangeShape mCircleShape;

    //当前地图所有未绑定的prru列表
    private List<PrruInfoShape> prruInfoShapes;
    private PrruInfoShape minDistacePrru;
    private PrruInfoShape openZxingPrru;
    private AlertDialog.Builder mPrrusetDialog;
    private float mX;
    private float mY;
    private String mpRRUId;
    private Button mConfrim;

    private int rsrpCount;


    public float getmScale() {
        return mScale;
    }

    public List<PrruInfoShape> getUnbindPrruInfo() {
        return prruInfoShapes;
    }

    private List<PrruInfo> prruInfos = new ArrayList<PrruInfo>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mBitmap = ((FloorMapActivity) mContext).getBitmap();
        mapPath = ((FloorMapActivity) mContext).getMap();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_prru_map_layout, container, false);
        initView(inflate);
        initData();
        getPrruData();//获取data
        return inflate;
    }

    private void initView(View view) {
        rsrpCount = 0;
        mFloorMap = view.findViewById(R.id.imagemap); //地图对象
        mConfrim = view.findViewById(R.id.confirm);
        mConfrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //显示下行RSRP结果
                        List<PrruInfo> prruInfoList = new ArrayList<>();
                        prruInfoList.addAll(prruInfos);
                        findPrru(prruInfoList,5,15);
                    }
                }).start();
            }
        });
        mMenuView = View.inflate(mContext, R.layout.prru_menu_layout, null);
        mMenuBind = mMenuView.findViewById(R.id.menu_bind);
        mMenuUnBind = mMenuView.findViewById(R.id.menu_unbind);
        mMenuMove = mMenuView.findViewById(R.id.menu_move);
        mMenuCamera = mMenuView.findViewById(R.id.menu_camera);
        mMenuBind.setOnClickListener(onMenuClickListener);
        mMenuUnBind.setOnClickListener(onMenuClickListener);
        mMenuMove.setOnClickListener(onMenuClickListener);
        mMenuCamera.setOnClickListener(onMenuClickListener);
        mFloorMap.setBubbleView(mMenuView, new Bubble.RenderDelegate() {
            @Override
            public void onDisplay(Shape shape, View bubbleView) {
                if (shape instanceof PrruInfoShape) {
                    if (((PrruInfoShape) shape).isBind()) {
                        mMenuUnBind.setVisibility(View.VISIBLE);
                        mMenuBind.setVisibility(View.GONE);
                    } else {
                        mMenuBind.setVisibility(View.VISIBLE);
                        mMenuUnBind.setVisibility(View.GONE);

                    }
                }
            }
        });
    }

    private void initData() {

        redPrruInfoShape = new PrruInfoShape("temp", Color.GREEN, mContext);
        redPrruInfoShape.setPrruShowType(PrruInfoShape.pRRUType.temple);

        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();
        mFloorMap.setMapBitmap(mBitmap);
        mFloorMap.setAllowRotate(false); //不能转动
        mFloorMap.setAllowTranslate(false);//不能移动
        mFloorMap.setOnLongClickListener1(new TouchImageView1.OnLongClickListener1() {
            @Override
            public void onLongClick(Shape shape) {   //必须注册这个才能长按
                if (shape instanceof PrruInfoShape) {
//                    showToast("长按:" + ((PrruInfoShape) shape).getTag());
                }

            }
        });
        mFloorMap.setOnShapeClickListener(new ShapeExtension.OnShapeActionListener() {


            @Override
            public void onCollectShapeClick(CollectPointShape collectPointShape, float f, float f2) {

            }

            @Override
            public void onMoniShapeClick(MoniPointShape moniPointShape, float f, float f2) {

            }

            @Override
            public void onPrruInfoShapeClick(PrruInfoShape prruinfoshape, float f, float f2) {
//                showToast("单击:" + prruinfoshape.getTag());
                mNowSelectPrru = prruinfoshape;
            }

            @Override
            public void onPushMessageShapeClick(PushMessageShape pushMessageShape, float f, float f2) {

            }

            @Override
            public void onSpecialShapeClick(SpecialShape specialShape, float f, float f2) {

            }

            @Override
            public void outShapeClick(float f, float f2) {

            }
        });
        mFloorMap.setPrruListener(new HighlightImageView1.PrruModifyHListener() {   //监听地图上prru移动事件
            @Override
            public void startTranslate(PrruInfoShape shape, float x, float y) {
                if (tempPrruInfoShape != null) {
                    Log.e("XHF_start", "x=" + x + "-----y=" + y);
                    if (!shape.getMove()) {
                        return;
                    }
                    redPrruInfoShape.setValues(x, y);
                }

            }

            @Override
            public void moveTranslate(PrruInfoShape shape, float x, float y) {
                Log.e("XHF_move", "x=" + x + "-----y=" + y);
                if (tempPrruInfoShape != null) {
                    if (!shape.getMove()) {
                        return;
                    }
                    redPrruInfoShape.setValues(x, y);
                }
            }

            @Override
            public void endTranslate(PrruInfoShape shape, float x, float y) {
                Log.e("XHF_end", "x=" + x + "-----y=" + y);
                if (tempPrruInfoShape != null) {
                    if (!shape.getMove()) {
                        return;
                    }
                    redPrruInfoShape.setValues(x, y);
                    showNormalDialog("pRRU位置修改", "确定本次修改？");
                }

            }

            @Override
            public void clickBlank() {


            }

            @Override
            public void clickOutSide() {  //判断是点击除开prru的外部
                mFloorMap.getBubble().setVisibility(View.GONE);
                if (tempPrruInfoShape != null) { //只有在调整事件触发的时候才有  点击空的没有prrushape的位置
                    mFloorMap.removeShape("temp"); //移除红色
                    mFloorMap.addShape(tempPrruInfoShape, false);//还原
                    tempPrruInfoShape = null;
                    showToast("已取消调整");
                    mFloorMap.setShowBubble(true);
                }
            }
        });
    }

    public void setNowLocation(float x, float y) {
        mX = x;
        mY = y;
        if (mFloorMap.getShape("loc") == null) {
            mCircleShape = new CircleRangeShape("loc", Color.RED);
            mCircleShape.setRadius(10);
            mCircleShape.setRange(mMi*mScale);
            mCircleShape.setValues(x, y);
            mFloorMap.addShape(mCircleShape, false);

        } else {
            mCircleShape.setValues(x, y);
            if (prruInfoShapes != null && prruInfoShapes.size() > 0) {
                //判断距离Prru位置 如果小于1m进行弹窗
                minDistacePrru = DistanceUtil.getMinDistacePrru(Float.valueOf(mScale), mMi, prruInfoShapes, x, y, mHeight);
                if (minDistacePrru != null) {
                    if (mPrrusetDialog == null) { //控制弹窗
                        mpRRUId = minDistacePrru.getId();
                        Toast.makeText(mContext, "请在附件放置prru", Toast.LENGTH_SHORT).show(); //
                        openZxingPrru = minDistacePrru;  //查找当前prru
                        showNormalScanDialog("已找到prru", "附件有pRRU安装点要确定扫码安装么？");
                    }
                } else {

                }
            }

        }

    }

    /**
     * 设置 PrruColor
     */
    public synchronized void setPrruColorPoint(float x,float y,int prru) {
        int color;
        if (-75 < prru && prru <= 0) {  //1e8449
            color = Color.parseColor("#1e8449");
        } else if (-95 < prru && prru <= -75) { //浅绿色
            color = Color.GREEN;
        } else if (-105 < prru && prru <= -95) {  //黄色
            color = Color.YELLOW;
        } else if (-120 < prru && prru <= -105) { //红色
            color = Color.RED;
        } else {
            color = Color.BLACK;
        }
        CircleShape shape = new CircleShape("rsrp"+rsrpCount, color);
        rsrpCount++;
        shape.setValues(x, y);
        mFloorMap.addShape(shape, false);
    }

    public synchronized void addPrruInfo(float x, float y, int prru) {
        PrruInfo prruInfo = new PrruInfo();
        prruInfo.setpRRUIndex(-1);
        prruInfo.setIncludedAngle(-1);
        prruInfo.setRouteId(-1);
        prruInfo.setSlope(Double.MAX_VALUE);
        Position position = new Position();
        position.setX(x);
        position.setY(y);
        prruInfo.setPosition(position);
        prruInfo.setRsrp(prru);

        prruInfos.add(prruInfo);
    }

    /**
     * 刷新当前地图页面
     */
    public void refreshMap() {
        mFloorMap.clearShapes();
        getPrruData();
    }

    public String getSelectId() {
        return mpRRUId;
    }

    /**
     * 判断是否在范围内安装
     *
     * @return
     */
    public boolean judgeSamePrru() {
        PrruInfoShape minDistacePrru = DistanceUtil.getMinDistacePrru(Float.valueOf(mScale), mMi, prruInfoShapes, mX, mY, mHeight);
        if (minDistacePrru == null) {
            return false;
        }
        String id = minDistacePrru.getId();
        if (id.equals(mpRRUId)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean judgeSame() {
        PointF center = openZxingPrru.getCenter(); //prru坐标
        float[] real = DistanceUtil.mapToReal(mScale, center.x, center.y, mHeight);
        float[] real2 = DistanceUtil.mapToReal(mScale, mX, mY, mHeight);
        double distance = Math.sqrt((real[0] - real2[0]) * (real[0] - real2[0]) + (real[1] - real2[1]) * (real[1] - real2[1]));
        Log.e("XHF", "distance=" + distance);
        if (distance < mMi) {  //如果小于经度范围
            return true;
        } else {
            return false;
        }
    }

    public void cancelPrrusetDialog() {
        if (mPrrusetDialog != null) {
            mPrrusetDialog = null;
        }
    }

    /***
     * 从xml文件中读取数据
     */
    private void getPrruData() {
        String siteName = mapPath.substring(0, mapPath.indexOf(File.separator));
        String floorName = mapPath.substring(mapPath.indexOf(File.separator) + 1, mapPath.indexOf("."));
        Document document = XmlUntils.getDocument(Constant.DATA_PATH + File.separator + siteName + File.separator + "project.xml");
        Element rootElement = XmlUntils.getRootElement(document);
        Element floors = XmlUntils.getElementByName(rootElement, "Floors");
        List<Element> floorList = XmlUntils.getElementListByName(floors, "Floor");
        for (Element element : floorList) {
            //如果是同一楼层
            if (floorName.equals(XmlUntils.getAttributeValueByName(element, "floorCode"))) {
                Element element1 = XmlUntils.getElementByName(element, "DrawMap");
                mScale = Float.parseFloat(XmlUntils.getAttributeValueByName(element1, "scale"));
                ((FloorMapActivity) getActivity()).setScale(Float.valueOf(mScale));
                List<Element> nes = XmlUntils.getElementListByName(XmlUntils.getElementByName(element, "NEs"), "NE");
             /*   prruInfoShapes = new ArrayList<>(nes.size());
                for (Element ne : nes) {
                    PrruInfoShape prruInfoShape = new PrruInfoShape(XmlUntils.getAttributeValueByName(ne, "id"), Color.YELLOW, mContext);
                    prruInfoShape.setId(XmlUntils.getAttributeValueByName(ne, "id"));
                    prruInfoShape.setValues(Float.parseFloat(XmlUntils.getAttributeValueByName(ne, "x")), mHeight - Float.parseFloat(XmlUntils.getAttributeValueByName(ne, "y")));
                    prruInfoShape.setBind(false);
                    prruInfoShape.setMove(false);
                    prruInfoShape.setPrruShowType(PrruInfoShape.pRRUType.outArea);
                    if (StringUtil.isNullOrEmpty(XmlUntils.getAttributeValueByName(ne, "esn"))) {
                        prruInfoShape.setBind(false);
                        prruInfoShape.setPrruShowType(PrruInfoShape.pRRUType.outArea);
                        prruInfoShapes.add(prruInfoShape);
                    } else {
                        prruInfoShape.setBind(true);
                        prruInfoShape.setPrruShowType(PrruInfoShape.pRRUType.inArea);
                    }

                    mFloorMap.addShape(prruInfoShape, false);
                }*/
                break;
            }


        }
    }

    private View.OnClickListener onMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_bind:
                    if (mNowSelectPrru != null) {
                        mNowSelectPrru.setBind(true);
                        mNowSelectPrru.setPrruShowType(PrruInfoShape.pRRUType.inArea);
                    }
                    getOtherRxPermission(mPermission, new BaseActivity.PerMissonListener() { //使用前  先判断权限有没有开启


                        @Override
                        public void havePermission() {
                            ((FloorMapActivity) mContext).openZxing();
                        }

                        @Override
                        public void missPermission() {
                            showToast("请在权限管理中打开权限");
                        }
                    });
                    mFloorMap.getBubble().setVisibility(View.GONE);
                    break;
                case R.id.menu_unbind:
                    if (mNowSelectPrru != null) {
                        mNowSelectPrru.setBind(false);
                        mNowSelectPrru.setPrruShowType(PrruInfoShape.pRRUType.outArea);
                    }
                    getOtherRxPermission(mPermission, new BaseActivity.PerMissonListener() { //使用前  先判断权限有没有开启

                        @Override
                        public void havePermission() {
                            ((FloorMapActivity) mContext).openZxing();
                        }

                        @Override
                        public void missPermission() {
                            showToast("请在权限管理中打开权限");
                        }
                    });
                    mFloorMap.getBubble().setVisibility(View.GONE);
                    break;
                case R.id.menu_move:
                    float centerX = mNowSelectPrru.getCenterX();  //获取中心点xy
                    float centerY = mNowSelectPrru.getCenterY();
                    redPrruInfoShape.setValues(centerX, centerY);
                    redPrruInfoShape.setMove(true);
                    mFloorMap.addShape(redPrruInfoShape, false);
                    mMenuView.setVisibility(View.GONE);
                    tempPrruInfoShape = mNowSelectPrru;
                    mFloorMap.removeShape(mNowSelectPrru.getTag());
                    mFloorMap.setShowBubble(false);
                    showToast("请长按红色pRRU进行位置修改");
                    mFloorMap.getBubble().setVisibility(View.GONE);
                    break;
                case R.id.menu_camera:
                    //做权限判断
                    getOtherRxPermission(mPermission, new BaseActivity.PerMissonListener() { //使用前  先判断权限有没有开启

                        @Override
                        public void havePermission() {
                            ((FloorMapActivity) mContext).openCamera();
                        }

                        @Override
                        public void missPermission() {
                            showToast("请在权限管理中打开权限");
                        }
                    });
                    mFloorMap.getBubble().setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    private void showNormalScanDialog(String title, String message) {
        mPrrusetDialog = new AlertDialog.Builder(mContext);
        mPrrusetDialog.setTitle(title);
        mPrrusetDialog.setMessage(message);
        mPrrusetDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((FloorMapActivity) getActivity()).setAnchor(); //放置ar安装点
                            }
                        });
                        ((FloorMapActivity) mContext).openZxing(); //打开扫描界面
                    }
                });
        mPrrusetDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelPrrusetDialog();
                    }
                });
        // 显示
        mPrrusetDialog.show();
    }

    /**
     * 短时间显示Toast
     *
     * @param info
     */
    public void showToast(String info) {
        if (!getActivity().isFinishing()) {
            Toast toast = Toast.makeText(mContext, info, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
            toast.show();
        }

    }

    /***
     * 动态获取其他权限
     */
    public void getOtherRxPermission(String[] permission, final BaseActivity.PerMissonListener listener) {
        RxPermissions rxPermissions = new RxPermissions((FloorMapActivity) mContext); // where this is an Activity instance
        rxPermissions.request(permission)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {  //当所有权限都允许之后，返回true
                            listener.havePermission();
                        } else { //没有给权限
                            listener.missPermission();
                        }
                    }
                });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

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


    public void findPrru(List<PrruInfo> prruInfos, int pRRUNumber, int radius) {

        if(prruInfos == null || prruInfos.size() < 1){
            return;
        }

        calculateSlopeAndIncludedAngle(prruInfos);
        //将prruInfo按照RSRP大小降序排列
        Collections.sort(prruInfos, new Comparator<PrruInfo>() {
            @Override
            public int compare(PrruInfo p1, PrruInfo p2) {
                return p2.getRsrp() - p1.getRsrp();
            }
        });

        int prruNumner = 0;
        List<PrruInfo> result = new ArrayList<>(pRRUNumber);
        List<PrruInfo> tempDatas = new ArrayList<>();
        Map<Integer, List<PrruInfo>> routeMap = new HashMap<>();
        while (prruInfos.size() > 0 && prruNumner < pRRUNumber) {
            prruNumner += 1;
            PrruInfo configPrruInfo = prruInfos.get(0);
            result.add(configPrruInfo);
            Position p1 = configPrruInfo.getPosition();
            for (PrruInfo prruInfo : prruInfos) {
                double distance = calculateDistance(p1, prruInfo.getPosition());
                if (distance < radius) {
                    prruInfo.setpRRUIndex(prruNumner);
                    tempDatas.add(prruInfo);
                    if (routeMap.containsKey(prruInfo.getRouteId())) {
                        routeMap.get(prruInfo.getRouteId()).add(prruInfo);
                    } else {
                        routeMap.put(prruInfo.getRouteId(), new ArrayList(Arrays.asList(prruInfo)));
                    }
                }
            }


           /* if (routeMap.keySet().size() > 1) {
                //当前prru所在路线
                List<PrruInfo> prruInfoList = routeMap.get(configPrruInfo.getRouteId());

                algorithm(prruInfoList, routeMap, configPrruInfo, radius, tempDatas);
            }*/

            //移除prruInfos中prruIndex已经确定的prruInfo
            prruInfos.removeAll(tempDatas);

            tempDatas.clear();
            routeMap.clear();
        }

        Message msg = new Message();
        msg.what = 1;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }


    private void algorithm(List<PrruInfo> prruInfoList, Map<Integer, List<PrruInfo>> routeMap, PrruInfo configPrru, int radius, List<PrruInfo> prruInfos) {
        List<PrruInfo> cornerPrruInfoList = new ArrayList<>();
        for (PrruInfo prruInfo : prruInfoList) {
            if (!compareDouble(prruInfo.getIncludedAngle(), -1)) {
                cornerPrruInfoList.add(prruInfo);
            }
        }

        for (PrruInfo cornerPrruInfo : cornerPrruInfoList) {
            //转角点与prru距离大于0.5*R
            if (calculateDistance(configPrru.getPosition(), cornerPrruInfo.getPosition()) > 0.5 * radius) {
                double k = routeMap.get(0).get(0).getSlope();
                LogUtils.e("XHF","te-------"+routeMap.size());
                LogUtils.e("XHF","te1-------"+routeMap.get(0).size());
                for (int i = 0; i < routeMap.keySet().size(); i++) {
                    //如果该条线路和prru是同一线路则不做处理继续循环
                    LogUtils.e("XHF","te2-------"+routeMap.get(i).size());
                    if (configPrru.getRouteId() == routeMap.get(i).get(0).getRouteId()) {
                        continue;
                    }
                    if (routeMap.get(i).size() > 3) {
                        double k1 = routeMap.get(i).get(0).getSlope();
                        if (compareDouble(k, k1)) {
                            Position avgPosition = calculateAvg(routeMap.get(i));
                            if (calculateDistance(avgPosition, configPrru.getPosition()) < 8) {
                                continue;
                            }
                        }

                        for (PrruInfo prruInfo : routeMap.get(i)) {
                            if (calculateDistance(prruInfo.getPosition(), cornerPrruInfo.getPosition()) < 5) {
                                for (PrruInfo prruInfo1 : routeMap.get(i)) {
                                    prruInfo1.setpRRUIndex(-1);
                                }
                                prruInfos.removeAll(routeMap.get(i));
                                break;
                            }
                        }
                    }
                }
            }
        }


    }

    private Position calculateAvg(List<PrruInfo> prruInfos) {
        Position position = new Position();
        float x = 0;
        float y = 0;
        for (PrruInfo prruInfo : prruInfos) {
            x += prruInfo.getPosition().getX();
            y += prruInfo.getPosition().getY();
        }
        position.setX(x / prruInfos.size());
        position.setY(y / prruInfos.size());
        return position;
    }

    /**
     * 计算两点间距离
     *
     * @param p1
     * @param p2
     * @return
     */
    public double calculateDistance(Position p1, Position p2) {
        double a = p1.getX() - p2.getX();
        double b = p1.getY() - p2.getY();
        return Math.sqrt(a * a + b * b);
    }

    /**
     * 计算斜率和夹角
     *
     * @param prruInfos
     */
    private void calculateSlopeAndIncludedAngle(List<PrruInfo> prruInfos) {
        double x1;
        double y1;
        double x2;
        double y2;
        double k1;
        double k2;
        int routeId = 1;
        double includeAngle;
        //从第二个点开始循环计算
        for (int i = 1; i < prruInfos.size(); i++) {
            //上一个相邻点的坐标
            x1 = Math.floor(prruInfos.get(i - 1).getPosition().getX());
            y1 = Math.floor(prruInfos.get(i - 1).getPosition().getY());

            //该点坐标
            x2 = Math.floor(prruInfos.get(i).getPosition().getX());
            y2 = Math.floor(prruInfos.get(i).getPosition().getX());

            //上一个相邻点的斜率
            k1 = prruInfos.get(i - 1).getSlope();

            //计算并设置该点斜率
            if (compareDouble(x1, x2)) {
                k2 = Math.tan(89);
            } else {
                k2 = (y2 - y1) / (x2 - x1);
            }
            prruInfos.get(i).setSlope(k2);

            //根据斜率值归类路线
            if (compareDouble(k1, Double.MAX_VALUE)) { //斜率为默认值则表示第一个点，将其与第二个点一起归为路线一
                prruInfos.get(i - 1).setRouteId(routeId);
                prruInfos.get(i).setRouteId(routeId);
            } else if (compareDouble(k1, k2)) { //斜率一样则表示为同一路线，设置为当前线路
                prruInfos.get(i).setRouteId(routeId);
            } else { //斜率不同则表示路线变化，线路标识自加1后设置新的路线并计算夹角
                routeId += 1;
                prruInfos.get(i).setRouteId(routeId);
                if ((1 + k2 * k1) == 0) {
                    includeAngle = 90.0;
                } else {
                    double tanA = (k2 - k1) / (1 + k2 * k1);
                    if (tanA > 0) {
                        includeAngle = Math.atan(tanA);
                    } else {
                        includeAngle = Math.atan(tanA) + 180;
                    }
                }
                prruInfos.get(i - 1).setIncludedAngle(includeAngle);
                prruInfos.get(i).setIncludedAngle(includeAngle);
            }
        }
    }

    private boolean compareDouble(double d1, double d2) {
        if (Math.abs(d1 - d2) > 0.01) {
            return false;
        }
        return true;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //显示检验过的prru位置
                    for (PrruInfo prruInfo : (List<PrruInfo>) msg.obj) {
                        PrruGkcShape pgShape = new PrruGkcShape(prruInfo.getpRRUIndex(), Color.RED, getActivity());
                        pgShape.setNecodeText("0_8" + prruInfo.getpRRUIndex() + "_0");
                        pgShape.setPaintColor(Color.parseColor("#ff0000"));
                        float[] tempMXY = DistanceUtil.realToMap(mScale,prruInfo.getPosition().getX(), prruInfo.getPosition().getY(),mHeight);
                        pgShape.setValues(tempMXY[0], tempMXY[1]);
                        mFloorMap.addShape(pgShape, false);
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
