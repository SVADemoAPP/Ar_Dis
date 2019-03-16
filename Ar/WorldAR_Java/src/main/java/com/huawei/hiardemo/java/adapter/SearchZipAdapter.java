package com.huawei.hiardemo.java.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.huawei.hiardemo.java.R;
import com.huawei.hiardemo.java.bean.ZipFile;
import com.huawei.hiardemo.java.util.FileUtils;

import java.util.List;

/**
 * 文件查找页面Adapter
 */
public class SearchZipAdapter extends RecyclerView.Adapter<SearchZipAdapter.ViewHolder> {
    private List<ZipFile> mData;
    private OnItemClick listener;
    private String keyName;//搜索的文字

    public SearchZipAdapter(List<ZipFile> data) {
        this.mData = data;
    }

    /**
     * 设置搜索名称
     *
     * @param key
     */
    public void setKeyName(String key) {
        keyName = key;

    }

    public void setOnItemClick(OnItemClick ls) {
        listener = ls;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        private LinearLayout title_p;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.member_tv1);
            title_p = v.findViewById(R.id.title_p);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_rv_search, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (keyName != null && !keyName.equals("")) {
            try {
                String fileName = FileUtils.getFileNameNoEx(mData.get(position).getFileName()); //获取文件名（不带后缀名）
                String[] split = fileName.toLowerCase().split(keyName.toLowerCase()); //转化为小写截取
                holder.title.setText(Html.fromHtml(combinetext(split, fileName, keyName) + ".zip"));   //设置彩色
            } catch (Exception e) {
                holder.title.setText(mData.get(position).getFileName());
            }
        } else {
            holder.title.setText(mData.get(position).getFileName());
        }

        holder.title_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.setOnItemClick(mData.get(position).getPath());
                }

            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public interface OnItemClick {
        void setOnItemClick(String path);
    }

    /**
     * 获得第一个匹配的字符并且转化为红色
     *
     * @param list
     * @param fileName
     * @param key
     * @return
     */
    private String combinetext(String[] list, String fileName, String key) {
        if (fileName.toLowerCase().equals(key.toLowerCase())) { //判断如果相同直接返回红色
            return "<font color='#FF0000'>" + fileName + "</font>"; //彩色文字
        }
        int sLength = list[0].length(); //key 第一个字母位置
        int eLength = list[0].length() + key.length();//key 最后一个字母的位置
        String orginKey = fileName.substring(sLength, eLength); //获取原始的匹配文字
        String name = "<font color='#FF0000'>" + orginKey + "</font>"; //彩色文字
        String[] split = fileName.split(orginKey);
        Log.e("XHF_hh", "split[0]" + split[0]);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {  //塞入html
                sb.append(split[i]).append(name);
            } else if (i < split.length - 1) {  //塞入原始分割str
                sb.append(split[i]).append(orginKey);
            } else {
                sb.append(split[i]); //塞入末尾
            }
        }

        Log.e("XHF_NA", "x" + sb.toString());
        return sb.toString();
    }
}
