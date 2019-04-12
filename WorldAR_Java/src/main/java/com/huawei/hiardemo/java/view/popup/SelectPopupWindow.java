package com.huawei.hiardemo.java.view.popup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.huawei.hiardemo.java.R;
import com.huawei.hiardemo.java.activity.FloorMapActivity;
import com.huawei.hiardemo.java.util.LogUtils;
import com.huawei.hiardemo.java.view.PieView;

import net.yoojia.imagemap.ImageMap1;
import net.yoojia.imagemap.TouchImageView1;
import net.yoojia.imagemap.core.CircleShape;

import zhy.com.highlight.HighLight;
import zhy.com.highlight.interfaces.HighLightInterface;
import zhy.com.highlight.position.OnBaseCallback;
import zhy.com.highlight.position.OnLeftPosCallback;
import zhy.com.highlight.position.OnTopPosCallback;
import zhy.com.highlight.shape.RectLightShape;
import zhy.com.highlight.view.HightLightView;

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
    private HighLight mHightLight;
    private RelativeLayout mRl;
    private AngeleListener mListener;
    private PieView mPieView;
    private float mAngle=-1;
    public SelectPopupWindow(Context context, Bitmap mapBitmap) {
        mContext = context;
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
        mRl = view.findViewById(R.id.content);
        mPieView = view.findViewById(R.id.select_pv);
        mAmap = view.findViewById(R.id.pop_select_map);
        mAmap.setAllowRotate(false);
        mAmap.setAllowRequestTranslate(false);
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
                if(mSelectPointListener != null){
                    if(mAngle<0)
                    {
                        Toast.makeText(mContext, "请选择方向，然后点击确定", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mSelectPointF != null) {
                        mSelectPointListener.getPoint(mSelectPointF); //获取选中点
                        mListener.getAngle(mAngle);
                        hidePopupWindow();
                    } else {
                        Toast.makeText(mContext, "请在地图上选择当前位置", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mPieView.setOnPieViewTouchListener(new PieView.OnPieViewTouchListener() {


            @Override
            public void onTouch(View v, MotionEvent e, PieView.ClickedDirection d) {
                mAngle = -1f;
               switch (d)
               {
                   case UP:
                       mAngle =0;
                       break;
                   case DOWN:
                       mAngle =180;
                       break;
                   case LEFT:
                       mAngle =270;
                       break;
                   case RIGHT:
                       mAngle =90;
                       break;
                   case CENTER:
                       mAngle =-1;
                       break;
                   case UP_LEFT:
                       mAngle =315;
                       break;
                   case UP_RIGHT:
                       mAngle =45;
                       break;
                   case DOWN_LEFT:
                       mAngle =225;
                       break;
                   case DOWN_RIGHT:
                       mAngle =135;
                       break;

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((FloorMapActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showNextKnownTipView();
                    }
                });
            }
        }).start();
    }

    public void setSelectAngleListener(AngeleListener listener){
        mListener =listener;
    }
    public interface AngeleListener{
        void getAngle(float angle);
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

    public void showNextKnownTipView() {
        mHightLight = new HighLight(mContext)//
                .anchor(mRl)
                .autoRemove(false)//设置背景点击高亮布局自动移除为false 默认为true
//                .intercept(false)//设置拦截属性为false 高亮布局不影响后面布局的滑动效果
                .intercept(true)//拦截属性默认为true 使下方ClickCallback生效
                .enableNext()//开启next模式并通过show方法显示 然后通过调用next()方法切换到下一个提示布局，直到移除自身
                .setClickCallback(new HighLight.OnClickCallback() {
                    @Override
                    public void onClick() {
                        mHightLight.next();
                    }
                })
                .addHighLight(R.id.pop_confirm, R.layout.hightlight_pop_confirm, new OnBaseCallback() {
                    @Override
                    public void getPosition(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo) {
                        marginInfo.leftMargin = rectF.right - 600;
                        marginInfo.bottomMargin = bottomMargin+rectF.height()+offset;
                    }
                }, new RectLightShape())
                .addHighLight(R.id.pop_cancel, R.layout.hightlight_pop_cancel, new OnBaseCallback() {
                    @Override
                    public void getPosition(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo) {
                        marginInfo.leftMargin = rectF.right- rectF.width()/2-250;
                        marginInfo.bottomMargin = bottomMargin+rectF.height()+offset;
                    }
                }, new RectLightShape())
                .setOnRemoveCallback(new HighLightInterface.OnRemoveCallback() {//监听移除回调
                    @Override
                    public void onRemove() {

                    }
                })
                .setOnShowCallback(new HighLightInterface.OnShowCallback() {//监听显示回调
                    @Override
                    public void onShow(HightLightView hightLightView) {
                    }
                }).setOnNextCallback(new HighLightInterface.OnNextCallback() {
                    @Override
                    public void onNext(HightLightView hightLightView, View targetView, View tipView) {
                        // targetView 目标按钮 tipView添加的提示布局 可以直接找到'我知道了'按钮添加监听事件等处理
                    }
                });
        mHightLight.show();
    }


    public void setmSelectShape(float x, float y){
        mSelectShape.setValues(x,y);
        mSelectPointF.set(x,y);
    }
}
