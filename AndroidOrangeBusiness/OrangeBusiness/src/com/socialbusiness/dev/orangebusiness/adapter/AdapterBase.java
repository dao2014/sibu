package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class AdapterBase<T> extends BaseAdapter {

	private List<T> mList = new ArrayList<T>();
	public Context context;
	public LayoutInflater inflater;

	public AdapterBase(Context context) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	public List<T> getList() {
		return mList;
	}

	public void onDataChange(List<T> mList) {
		this.mList = mList;
		notifyDataSetChanged();
	}

	public void appendToList(List<T> list) {
		if (list == null||list.size()==0) {
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	public void appendToTopList(List<T> list) {
		if (list == null) {
			return;
		}
		mList.addAll(0, list);
		notifyDataSetChanged();
	}

	public void clear() {
		mList.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		}
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (position > mList.size() - 1) {
			return null;
		}
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (position == getCount() - 1) {
			onReachBottom();
		}
		return getExView(position, convertView, parent);
	}

	protected abstract View getExView(int position, View convertView, ViewGroup parent);

	protected abstract void onReachBottom();

}
