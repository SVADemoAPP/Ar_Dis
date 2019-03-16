package com.huawei.hiardemo.java.view.popup;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hiardemo.java.R;


public class LoadingDialog extends Dialog {

    private ImageView ivProgress;    //转圈图片
    private TextView tvInfo;         //显示文字
    private static Context mContext;         //上下文对象
    private static LoadingDialog dialog;    //单例对象

    public LoadingDialog(Context context) {
        super(context, R.style.WeixinLoadingDialog);
    }

    /**
     * @param context
     */
    public static LoadingDialog with(Context context) {
        mContext = context;
        if (dialog == null) {
            dialog = new LoadingDialog(context);
        }
        return dialog;
    }


    public LoadingDialog initDialog() {
        setContentView(R.layout.loading_dialog_view);
        ivProgress = (ImageView) findViewById(R.id.img);
        tvInfo = (TextView) findViewById(R.id.tipTextView);
        return dialog;
    }

    public LoadingDialog setTouchOutSide(boolean flag){
        dialog.setCanceledOnTouchOutside(flag);
        return dialog;
    }

    public LoadingDialog showDialog() {
        Animation animation = AnimationUtils.loadAnimation(mContext,
                R.anim.dialog_load_animation);
        // 显示动画
        ivProgress.startAnimation(animation);
        dialog.show();
        return dialog;
    }

    public LoadingDialog cancelDialog() {
        if(dialog != null){
            ivProgress.clearAnimation();
            dialog.cancel();
            dialog.dismiss();
        }
        return dialog;
    }

    public LoadingDialog setProgressText(String text) {
        tvInfo.setText(text);
        return dialog;
    }

}
