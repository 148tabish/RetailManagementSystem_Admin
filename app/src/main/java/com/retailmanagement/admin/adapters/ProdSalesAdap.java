package com.retailmanagement.admin.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.retailmanagement.admin.BarGraphActivity;
import com.retailmanagement.admin.R;
import com.retailmanagement.admin.models.Product;

import java.util.ArrayList;
import java.util.Locale;

public class ProdSalesAdap extends BaseAdapter {
    private Context context;
    private ArrayList<Product> productPojos;

    public ProdSalesAdap(Context context, ArrayList<Product> productPojos) {
        this.context = context;
        this.productPojos = productPojos;
    }

    private BarGraphActivity activity;

    @Override
    public int getCount() {
        Log.d("TAG", "getCount() returned: " + productPojos.size());
        return productPojos.size();
    }

    @Override
    public Object getItem(int position) {
        return productPojos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewH viewH = new ViewH();
        if (convertView == null) {
            //inflate
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_prod_inbargraph, parent, false);
            viewH.idTV = convertView.findViewById(R.id.tv_pid);
            viewH.prodNameTV = convertView.findViewById(R.id.tv_prod_name);
            viewH.prodTypeTv = convertView.findViewById(R.id.tv_typename_prod);
            viewH.qtyTV = convertView.findViewById(R.id.tv_qty);

            convertView.setTag(viewH);
        } else {
            viewH = (ViewH) convertView.getTag();
        }
        //populate
        Product productPojo = productPojos.get(position);
        if ((productPojo) != null) {
            viewH.idTV.setText("# "+productPojo.getProductId());
            viewH.prodNameTV.setText(productPojo.getProductName().toUpperCase());
            viewH.prodTypeTv.setText(productPojo.getProductType());
            viewH.qtyTV.setText("Qty: "+productPojo.getProductQuantity());
        }
        return convertView;
    }

    class ViewH {
        private TextView idTV, prodNameTV, prodTypeTv, qtyTV;
    }
}
