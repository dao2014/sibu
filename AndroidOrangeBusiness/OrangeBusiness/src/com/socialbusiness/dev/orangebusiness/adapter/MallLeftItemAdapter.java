package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.model.Category;

/**
 * Created by zkboos on 2015/7/3.
 */
public class MallLeftItemAdapter extends AdapterBase<Category> implements View.OnClickListener{

    public MallLeftItemAdapter(Context context) {
        super(context);
    }

    View tempLeft;
    int selecteIndex=0;

    public void setSelecteIndex(int selecteIndex) {
        this.selecteIndex = selecteIndex;
    }

    @Override
    protected View getExView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.mall_left_item, null);
        }
        TextView left_item_text=(TextView)convertView.findViewById(R.id.left_item_text);
        if(position==selecteIndex){
            if(tempLeft!=null){
                tempLeft.setBackgroundResource(R.color.transparent);
                tempLeft.findViewById(R.id.left_item_index).setVisibility(View.INVISIBLE);

            }
            tempLeft=convertView;
            tempLeft.setBackgroundResource(R.color.white);
            tempLeft.findViewById(R.id.left_item_index).setVisibility(View.VISIBLE);
            left_item_text.setTextColor(this.context.getResources().getColor(R.color.main_color));
        }else{
            left_item_text.setTextColor(this.context.getResources().getColor(R.color.mall_left_text_color));
        }

//        convertView.setOnClickListener(this);

        left_item_text.setText(this.getList().get(position).name);
        return convertView;
    }

    @Override
    protected void onReachBottom() {

    }

    @Override
    public void onClick(View v) {
        if(tempLeft!=null){
            tempLeft.setBackgroundResource(R.color.transparent);
            tempLeft.findViewById(R.id.left_item_index).setVisibility(View.INVISIBLE);
        }
        v.setBackgroundResource(R.color.white);
        v.findViewById(R.id.left_item_index).setVisibility(View.VISIBLE);
        tempLeft=v;
    }
}
