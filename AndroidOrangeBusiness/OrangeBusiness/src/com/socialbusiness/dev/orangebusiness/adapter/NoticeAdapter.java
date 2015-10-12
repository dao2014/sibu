package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.NoticeItem;
import com.socialbusiness.dev.orangebusiness.model.Notice;

import java.util.List;

public class NoticeAdapter extends BaseAdapter {
	public Context context;
	public List<Notice> mNoticeList;

	public NoticeAdapter(Context context) {
		this.context = context;
	}

	public void setNoticeList(List<Notice> mNoticeList) {
		this.mNoticeList = mNoticeList;
	}

	@Override
	public int getCount() {
		return (mNoticeList != null ? mNoticeList.size() : 0);
	}

	@Override
	public Object getItem(int position) {
		return (mNoticeList != null ? mNoticeList.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NoticeItem noticeItem;
		if (convertView == null) {
			noticeItem = new NoticeItem(context);
		}else {
			noticeItem = (NoticeItem) convertView;
		}

        if (mNoticeList != null) {
            noticeItem.setNoticeData(mNoticeList.get(position));
        }
		return noticeItem;
	}

}
