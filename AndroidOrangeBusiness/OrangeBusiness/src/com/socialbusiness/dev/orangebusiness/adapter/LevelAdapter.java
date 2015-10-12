package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.LevelItem;
import com.socialbusiness.dev.orangebusiness.component.PositionCalculator;
import com.socialbusiness.dev.orangebusiness.model.Level;

import java.util.List;

/**
 * Created by enwey on 2014/11/3.
 */
public class LevelAdapter extends BaseAdapter {
	private Context context;
	private List<Level> mLevelList;

	public LevelAdapter(Context context) {
		this.context = context;
	}

	public void setLevelList(List<Level> list) {
		this.mLevelList = list;
	}

	@Override
	public int getCount() {
		return (mLevelList != null ? mLevelList.size() : 0);
	}

	@Override
	public Object getItem(int i) {
		return (mLevelList != null ? mLevelList.get(i) : null);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LevelItem levelItem = null;
		if (convertView == null) {
			levelItem = new LevelItem(context);
		} else {
			levelItem = (LevelItem) convertView;
		}
		
		if (mLevelList != null) {
			levelItem.setLevelValue(mLevelList.get(position));
		}
		return levelItem;
	}

}
