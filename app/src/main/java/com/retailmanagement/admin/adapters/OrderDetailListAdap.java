package com.retailmanagement.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.retailmanagement.admin.R;
import com.retailmanagement.admin.models.Product;
import com.retailmanagement.admin.webservices.Utility;

import java.util.ArrayList;


public class OrderDetailListAdap extends BaseAdapter {
    private Context context;
    private ArrayList<Product> products;

    public OrderDetailListAdap(Context context, ArrayList<Product> issuedBookPojos) {
        this.context = context;
        this.products = issuedBookPojos;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_detail_inlist, parent, false);
            viewHolder.priceTV = convertView.findViewById(R.id.tv_fprice);  //format
            viewHolder.qtyTV = convertView.findViewById(R.id.tv_qty);
            viewHolder.prodIdTV = convertView.findViewById(R.id.tv_prodid);
            viewHolder.totalPrice = convertView.findViewById(R.id.tv_prod_totalprice);
            viewHolder.nameTV = convertView.findViewById(R.id.tv_bname);
            viewHolder.imageView = convertView.findViewById(R.id.iv_food_item);
            //setTag
            convertView.setTag(viewHolder);
        } else {
            //getTag
            viewHolder = (VHolder) convertView.getTag();
        }
        //settexts
        Product product;
        if ((product = products.get(position)) != null) {
            viewHolder.priceTV.setText(context.getString(R.string.price, product.getProductPrice()));
            viewHolder.qtyTV.setText("Qty: " + product.getProductQuantity());
            viewHolder.prodIdTV.setText("# "+product.getProductId());
            viewHolder.totalPrice.setText("Total "+context.getString(R.string.price,product.getTotal()));
            viewHolder.nameTV.setText(product.getProductName());
            viewHolder.imageView.setImageBitmap(Utility.getBitmapFromString(product.getProductImage()));
        }   //[setText]
        return convertView;
    }

    private static class VHolder {
        //pid,pname,pimg,quantity,price,totprice
        TextView priceTV, qtyTV,  nameTV, prodIdTV, totalPrice;
        ImageView imageView;
    }
}
