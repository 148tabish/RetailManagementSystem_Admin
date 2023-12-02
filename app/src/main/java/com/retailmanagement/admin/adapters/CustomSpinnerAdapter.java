package com.retailmanagement.admin.adapters;


import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.retailmanagement.admin.R;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<Pair<String, String>> {
    private Context context;
    private LayoutInflater flater;

    public CustomSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<Pair<String, String>> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    public CustomSpinnerAdapter(Context context, int resouceId, int textviewId, List<Pair<String, String>> list) {
        super(context, resouceId, textviewId, list);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return RowView(convertView, position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getDropDown(convertView, position);
    }

    private View getDropDown(View convertView, int position) {
        ViewHolderDropDown holder;
        View rowview = convertView;
        if (rowview == null) {

            holder = new ViewHolderDropDown();
            flater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowview = flater.inflate(R.layout.spinner_drop_down, null, false);

            holder.txtTitle = (TextView) rowview.findViewById(R.id.spinner_item_title);
            holder.txtSubTitle = (TextView) rowview.findViewById(R.id.spinner_item_sub_title);

            TextView mDivider = (TextView) rowview.findViewById(R.id.spinner_item_divider);
            mDivider.setVisibility(View.VISIBLE);

            rowview.setTag(holder);
        } else {
            holder = (ViewHolderDropDown) rowview.getTag();
        }

        Pair<String, String> mItem = getItem(position);
        if (mItem != null) {
            holder.txtTitle.setText(mItem.first);
            holder.txtSubTitle.setText(mItem.second);
        }

        return rowview;
    }

    private View RowView(View convertView, int position, ViewGroup parent) {


        ViewHolderDropDown holder;
        View rowview = convertView;
        if (rowview == null) {

            holder = new ViewHolderDropDown();
            flater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowview = flater.inflate(R.layout.spinner_drop_down, null, false);

            holder.txtTitle = (TextView) rowview.findViewById(R.id.spinner_item_title);
            holder.txtSubTitle = (TextView) rowview.findViewById(R.id.spinner_item_sub_title);

            rowview.setTag(holder);
        } else {
            holder = (ViewHolderDropDown) rowview.getTag();
        }

        Pair<String, String> mItem = getItem(position);
        holder.txtSubTitle.setVisibility(View.GONE);
        if (mItem != null) {
            holder.txtTitle.setText(mItem.first);

        }

        return rowview;
    }

    private class ViewHolderDropDown {
        TextView txtTitle;
        TextView txtSubTitle;
    }

}
