package com.huawei.hiardemo.java.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.huawei.hiardemo.java.R;

import java.util.List;

public class RvSearchAdapter extends RecyclerView.Adapter<RvSearchAdapter.MyViewHolder> {

    private List<String> mList;
    private Context mContext;
    private OnSearchItemClickListener onSearchItemClickListener;


    public RvSearchAdapter(List<String> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    public void setOnSearchItemClickListener(OnSearchItemClickListener onSearchItemClickListener) {
        this.onSearchItemClickListener = onSearchItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_rv_search,viewGroup,false);
        MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.itemView.setTag(position);
        holder.tv1.setText(mList.get(position));
        holder.tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchItemClickListener.searchClick(mList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv1;
        public MyViewHolder(View view) {
            super(view);
            tv1=view.findViewById(R.id.member_tv1);
        }
    }

    public interface OnSearchItemClickListener{
        void searchClick(String str);
    }

}
