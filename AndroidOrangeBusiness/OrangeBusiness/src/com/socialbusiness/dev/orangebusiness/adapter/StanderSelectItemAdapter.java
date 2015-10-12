package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.model.StanderItem;

import java.util.ArrayList;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/**
 * Created by zkboos on 2015/4/24.
 */
public class StanderSelectItemAdapter extends AbstractWheelTextAdapter {

    public StanderSelectItemAdapter(Context context) {
        super(context, R.layout.city_holo_layout, NO_RESOURCE);

        setItemTextResource(R.id.city_name);
    }

    private ArrayList<StanderItem> standerItems;

    public void onDataChange(ArrayList<StanderItem> standerItems) {
        this.standerItems = standerItems;
//        notifyDataChangedEvent();

    }

    public StanderItem getStanderItem(int index) {
        return this.standerItems.get(index);
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        View view = super.getItem(index, cachedView, parent);
        return view;
    }

    @Override
    public int getItemsCount() {
        return this.standerItems == null ? 0 : this.standerItems.size();
    }

    @Override
    protected CharSequence getItemText(int index) {
        StanderItem item = this.standerItems.get(index);
        String text = "";
        if (item.bagSize != 0 && item.boxSize != 0) {
            text = item.boxSize + "箱 + " + item.bagSize + "盒";
        } else if (item.boxSize != 0) {
            text = item.boxSize + "箱";
        } else {
            text = item.bagSize + "盒";
        }
        return text;
    }
}
