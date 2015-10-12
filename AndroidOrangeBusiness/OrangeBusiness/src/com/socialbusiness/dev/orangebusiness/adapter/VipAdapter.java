package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.VipItem;
import com.socialbusiness.dev.orangebusiness.model.User;

import java.util.List;

/**
 * Created by enwey on 2014/11/3.
 */
public class VipAdapter extends BaseAdapter {
    private Context context;
    private List<User> mUserList;
    private String mLevelName;

    public VipAdapter(Context context) {
        this.context = context;
    }
    public void setVipList(List<User> list) {
        this.mUserList = list;
    }

    public void setLevelName(String name) {
        mLevelName = name;
    }

    @Override
    public int getCount() {
        return mUserList != null ? mUserList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mUserList != null ? mUserList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VipItem vipItem = null;
        if (convertView == null) {
            vipItem = new VipItem(context);
        } else {
            vipItem = (VipItem) convertView;
        }

        if (mUserList != null) {
            vipItem.setVipValue(mUserList.get(position), mLevelName);
        }
        return vipItem;
    }
}
