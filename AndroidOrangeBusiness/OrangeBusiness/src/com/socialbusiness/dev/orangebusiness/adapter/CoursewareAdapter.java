package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.CoursewareItem;
import com.socialbusiness.dev.orangebusiness.model.Course;

import java.util.List;

public class CoursewareAdapter extends BaseAdapter {
	private Context context;
	private List<Course> mCourseList;

	public CoursewareAdapter(Context context) {
		this.context = context;
	}

	public void setCoursewareList(List<Course> list) {
		this.mCourseList = list;
	}

	@Override
	public int getCount() {
		return (mCourseList != null ? mCourseList.size() : 0);
	}
	@Override
	public Object getItem(int position) {
		return  (mCourseList != null ? mCourseList.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CoursewareItem coursewareItem = null;
		if (convertView == null) {
			coursewareItem = new CoursewareItem(context);
		} else {
			coursewareItem = (CoursewareItem) convertView;
		}

        if (mCourseList != null) {
            coursewareItem.setData(mCourseList.get(position));
        }
		return coursewareItem;
	}

}
