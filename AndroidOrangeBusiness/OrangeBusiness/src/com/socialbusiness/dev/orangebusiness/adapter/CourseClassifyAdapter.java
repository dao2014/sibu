package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.CourseClassifyItem;
import com.socialbusiness.dev.orangebusiness.model.Course;

import java.util.List;

public class CourseClassifyAdapter extends BaseAdapter {
	public Context context;
	public List<Course> courseList;

	public CourseClassifyAdapter(Context context) {
		this.context = context;
	}

	public void setCourseClassifyList(List<Course> list) {
		this.courseList = list;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CourseClassifyItem courseClassifyItem = null;
		if (convertView == null) {
			courseClassifyItem = new CourseClassifyItem(context);
		} else {
			courseClassifyItem = (CourseClassifyItem) convertView;
		}
		return courseClassifyItem;
	}

}
