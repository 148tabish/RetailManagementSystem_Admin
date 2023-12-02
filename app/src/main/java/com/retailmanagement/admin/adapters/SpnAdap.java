package com.retailmanagement.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.retailmanagement.admin.R;

import java.util.ArrayList;

public class SpnAdap extends BaseAdapter {
    private Context context;
    private ArrayList<String> spnClassList;

    public SpnAdap(Context context, ArrayList<String> spnClassList) {
        this.context = context;
        this.spnClassList = spnClassList;
    }

    @Override
    public int getCount() {
        return spnClassList.size();
    }

    @Override
    public Object getItem(int position) {
        return spnClassList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false);
        TextView tV = view.findViewById(R.id.tv_spn_type);
        tV.setText(spnClassList.get(position));
        return view;
    }
}
