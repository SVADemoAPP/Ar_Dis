package com.huawei.hiardemo.java.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.huawei.hiardemo.java.R;
import com.huawei.hiardemo.java.bean.FloorModel;

import java.util.List;

public class RvMemberAdapter extends RecyclerView.Adapter<RvMemberAdapter.MyViewHolder> {

    private List<FloorModel> floorList;
    private Context mContext;
    private OnMemberItemClickListener onMemberItemClickListener;


    public RvMemberAdapter(List<FloorModel> floorList, Context mContext) {
        this.floorList = floorList;
        this.mContext = mContext;
    }

    public void setOnMemberItemClickListener(OnMemberItemClickListener onMemberItemClickListener) {
        this.onMemberItemClickListener = onMemberItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(mContext)
                .inflate(R.layout.item_rv_member,viewGroup,false);
        MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.itemView.setTag(position);
        holder.tv1.setText(floorList.get(position).floorName);
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                onMemberItemClickListener.memberClick(floorList.get(position).floorMap,0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return floorList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv1;
        LinearLayout ll;
        public MyViewHolder(View view) {
            super(view);
            tv1=view.findViewById(R.id.member_tv1);
            ll=view.findViewById(R.id.member_ll);
        }
    }

    public interface OnMemberItemClickListener{
        void memberClick(String floorMap, int type);
    }

}
