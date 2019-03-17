package com.huawei.hiardemo.java.view.popup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.huawei.hiardemo.java.R;

import net.yoojia.imagemap.ImageMap1;
import net.yoojia.imagemap.TouchImageView1;
import net.yoojia.imagemap.core.CircleShape;

public class SelectPopupWindow {
    private static final String SELECT_ADDRESS = "select";
    private SuperPopupWindow mSelectPopupWindow;
    private Bitmap mMapBitmap;
    private ImageMap1 mAmap;
    private CircleShape mSelectShape;
    private int mCircleRadius = 10;
    private boolean firstSelect = true;
    private TextView mTvCancel;
    private PointF mSelectPointF;
    private View mTvConfirm;
    private SelectPointListener mSelectPointListener;
    private Context mContext;

    public SelectPopupWindow(Context context, Bitmap mapBitmap) {
        mMapBitmap = mapBitmap;
        mSelectPopupWindow = new SuperPopupWindow(context, R.layout.popupwindow_select_map);
        View popupView = mSelectPopupWindow.getPopupView();
        initPopupWindow(popupView);
        initData();
        mSelectPopupWindow.setBlack(0.1f);
    }

    public void setSelectListener(SelectPointListener selectPointListener) {
        mSelectPointListener = selectPointListener;
    }

    /**
     * 初始化prru
     */
    public void initPopupWindow(View view) {
        mAmap = view.findViewById(R.id.pop_select_map);
        mTvCancel = view.findViewById(R.id.pop_cancel);
        mTvConfirm = view.findViewById(R.id.pop_confirm);
        mAmap.setOnSingleClickListener(new TouchImageView1.OnSingleClickListener() {
            @Override
            public void onSingle(PointF pointF) {
                mSelectShape.setValues(pointF.x, pointF.y);
                mSelectPointF = pointF;
                if (firstSelect) {
                    firstSelect = false;
                    mAmap.addShape(mSelectShape, false);
                }
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
                if (mSelectPointListener != null) {
                    mSelectPointListener.cancel();
                }

            }
        });
        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectPointListener != null) {
                    if (mSelectPointF != null) {
                        mSelectPointListener.getPoint(mSelectPointF); //获取选中点
                        hidePopupWindow();
                    } else {
                        Toast.makeText(mContext, "请在地图上选择当前位置", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    /**
     * 初始化prru
     */
    public void initData() {
        if (mMapBitmap != null) {
            mAmap.setMapBitmap(mMapBitmap);
        }
        mSelectShape = new CircleShape(SELECT_ADDRESS, Color.RED, mCircleRadius);

    }

    /**
     * 显示
     */
    public void showPopupWindow() {
        mSelectPopupWindow.showPopupWindow();
    }

    /**
     * 隐藏
     */
    public void hidePopupWindow() {
        if (mSelectPopupWindow.isShowing()) {
            mSelectPopupWindow.hidePopupWindow();
        }
    }

    public interface SelectPointListener {
        void getPoint(PointF pointF);

        void cancel();
    }

}
