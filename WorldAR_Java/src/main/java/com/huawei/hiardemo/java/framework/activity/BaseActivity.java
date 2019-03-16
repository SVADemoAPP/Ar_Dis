package com.huawei.hiardemo.java.framework.activity;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager.BadTokenException;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;


public abstract class BaseActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout();
        findView();
        getRxPermission();

    }

    public abstract void findView();

    /**
     * 设置布局，在onCreate()生命周期中回调
     */
    public abstract void setContentLayout();


    /**
     * 在实例化布局之前处理的逻辑
     */
    public abstract void dealLogicBeforeInitView();

    /**
     * 初始化VIEW，在onCreate()生命周期中回调
     */
    public abstract void initView();

    /**
     * 在实例化布局之后处理的逻辑
     */
    public abstract void dealLogicAfterInitView();

    /**
     * 得到屏幕宽度
     *
     * @return 宽度
     */
    public int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        return screenWidth;
    }

    /**
     * 得到屏幕高度
     *
     * @return 高度
     */
    public int getScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        return screenHeight;
    }

    /**
     * 短时间显示Toast
     *
     * @param info
     */
    public void showToast(String info) {
        if (!isFinishing()) {
            Toast toast = Toast.makeText(this, info, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
            toast.show();
        }

    }

    /**
     * 长时间显示Toast
     *
     * @param info
     */
    public void showToastLong(String info) {
        if (!isFinishing()) {
            Toast toast = Toast.makeText(this, info, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
            toast.show();
        }
    }

    /**
     * 短时间显示Toast
     *
     * @param resId
     */
    public void showToast(int resId) {
        if (!isFinishing()) {
            Toast toast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
            toast.show();
        }
    }

    /**
     * 长时间显示Toast
     *
     * @param resId
     */
    public void showToastLong(int resId) {
        if (!isFinishing()) {
            Toast toast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
            toast.show();
        }
    }


    /**
     * 显示正在加载的进度条
     */
    public void showProgressDialog() {
        if (!isFinishing() && progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressDialog = new ProgressDialog(BaseActivity.this);
        progressDialog.setMessage("正在加载请稍后...");
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        try {
            progressDialog.show();
        } catch (BadTokenException exception) {
            exception.printStackTrace();
        }
    }

    public void showProgressDialog(String msg) {
        if (!isFinishing() && progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressDialog = new ProgressDialog(BaseActivity.this);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        try {
            progressDialog.show();
        } catch (BadTokenException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 隐藏正在加载的进度条
     */
    public void dismissProgressDialog() {
        if (!isFinishing() && null != progressDialog && progressDialog.isShowing() == true) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    /**
     * 通过类名启动Activity
     *
     * @param pClass
     */
    protected void openActivity(Class<?> pClass) {
        if (!isFinishing()) {
            openActivity(pClass, null);
        }
    }

    /**
     * 通过类名启动Activity，并且含有Bundle数据
     *
     * @param pClass
     * @param pBundle
     */
    protected void openActivity(Class<?> pClass, Bundle pBundle) {
        if (!isFinishing()) {
            Intent intent = new Intent(this, pClass);
            if (pBundle != null) {
                intent.putExtras(pBundle);
            }
            startActivity(intent);
        }
    }

    /**
     * 通过Action启动Activity
     *
     * @param pAction
     */
    protected void openActivity(String pAction) {
        if (!isFinishing()) {
            openActivity(pAction, null);
        }
    }

    /**
     * 通过Action启动Activity，并且含有Bundle数据
     *
     * @param pAction
     * @param pBundle
     */
    protected void openActivity(String pAction, Bundle pBundle) {
        if (!isFinishing()) {
            Intent intent = new Intent(pAction);
            if (pBundle != null) {
                intent.putExtras(pBundle);
            }
            startActivity(intent);
        }
    }

    protected void openActivityForResult(Class<?> pClass, int requestCode) {
        if (!isFinishing()) {
            openActivityForResult(pClass, null, requestCode);
        }
    }

    protected void openActivityForResult(Class<?> pClass, Bundle pBundle, int requestCode) {
        if (!isFinishing()) {
            Intent intent = new Intent(this, pClass);
            if (pBundle != null) {
                intent.putExtras(pBundle);
            }
            startActivityForResult(intent, requestCode);
        }
    }

    /***
     * 动态获取权限
     */
    private void getRxPermission() {


        RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity instance
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {  //当所有权限都允许之后，返回true
                            dealLogicBeforeInitView();
                            initView();
                            dealLogicAfterInitView();
                        } else { //没有给权限
                            showToast("未授权权限，部分功能不能使用");
                        }
                    }
                });
    }

    /***
     * 动态获取其他权限
     */
    public void getOtherRxPermission(String[] permission, final PerMissonListener listener) {
        RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity instance
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

    /***
     *  权限回调接口
     */
    public interface PerMissonListener {

        void havePermission();

        void missPermission();
    }
}

