package com.huawei.hiardemo.java.framework.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dealLogicBeforeInitView();
        initView();
        dealLogicAfterInitView();
    }

    /**
     * 初始化VIEW，在onCreate()生命周期中回调
     */
    public abstract void initView();

    /**
     * 在实例化布局之前处理的逻辑
     */
    public abstract void dealLogicBeforeInitView();

    /**
     * 在实例化布局之后处理的逻辑
     */
    public abstract void dealLogicAfterInitView();


    /**
     * 通过类名启动Activity
     *
     * @param pClass
     */
    protected void openActivity(Class<?> pClass) {
        openActivity(pClass, null);
    }

    /**
     * 通过类名启动Activity，并且含有Bundle数据
     *
     * @param pClass
     * @param pBundle
     */
    protected void openActivity(Class<?> pClass, Bundle pBundle) {
        Intent intent = new Intent(this.getActivity(), pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        this.getActivity().startActivity(intent);
    }
}
