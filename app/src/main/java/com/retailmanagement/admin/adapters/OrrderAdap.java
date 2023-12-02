package com.retailmanagement.admin.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.retailmanagement.admin.R;
import com.retailmanagement.admin.SalesFragment;

import java.util.ArrayList;

public class OrrderAdap extends BaseAdapter {
    private Context context;
    private ArrayList<OrrderPojo> orderPojos;
    private SalesFragment fragment;
    private OrrderPojo orderPojo;

    public OrrderAdap(Context context, ArrayList<OrrderPojo> issuedBookPojos, SalesFragment fragment) {
        this.context = context;
        this.orderPojos = issuedBookPojos;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return orderPojos.size();
    }

    @Override
    public Object getItem(int position) {
        return orderPojos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        VHolder viewHolder = new VHolder();
        if (convertView == null) {
            ///inflate
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
            viewHolder.counterIDTV = convertView.findViewById(R.id.tv_counterid);
            viewHolder.statusTV = convertView.findViewById(R.id.tv_status_order);  //format
            viewHolder.dateTimeTV = convertView.findViewById(R.id.tv_datetime);
            viewHolder.byUserNameTV = convertView.findViewById(R.id.tv_user_orderednameid);
            viewHolder.oidTV = convertView.findViewById(R.id.tv_oid);
            viewHolder.clickDetail = convertView.findViewById(R.id.tv_detail_click);   //clicklistener
            //setTag
            convertView.setTag(viewHolder);
        } else {
            //getTag
            viewHolder = (VHolder) convertView.getTag();
        }
        //settexts
        if ((orderPojo = orderPojos.get(position)) != null) {
            viewHolder.statusTV.setText(orderPojo.getStatus().toUpperCase());
            viewHolder.dateTimeTV.setText("Ordered on: " + orderPojo.getDate());
            viewHolder.byUserNameTV.setText("Ordered by: " + orderPojo.getUname() + "(Customer Id " + orderPojo.getUid() + ")");
            viewHolder.oidTV.setText("#Order id " + orderPojo.getOid());
            viewHolder.counterIDTV.setText("Counter Number " + orderPojo.getCounterid());
            viewHolder.clickDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fragment != null)
                        fragment.openOrderDetailActivity(orderPojos.get(position));
                }
            });
            Log.i("TAG", "getView:  ");
        }   //[setText]
        return convertView;
    }

    private static class VHolder {
        public TextView clickDetail;
        private TextView counterIDTV;
        private TextView statusTV, dateTimeTV, byUserNameTV, oidTV;
    }
}

