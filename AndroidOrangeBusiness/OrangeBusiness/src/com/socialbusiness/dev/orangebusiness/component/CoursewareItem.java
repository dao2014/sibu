package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.Course;

public class CoursewareItem extends LinearLayout {
	private NetworkImageView mImageView;
	private TextView mCourseTitle;
	private TextView mDate;

	public CoursewareItem(Context context) {
		super(context);
		initView();
	}

	public CoursewareItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.view_course_item, this);
		findView(view);
	}

	public void findView(View view) {
		mImageView = (NetworkImageView) view.findViewById(R.id.view_course_item_image);
		mCourseTitle = (TextView) view.findViewById(R.id.view_course_item_title);
		mDate = (TextView) view.findViewById(R.id.view_course_item_date);

        mImageView.setErrorImageResId(R.drawable.ic_default);
        mImageView.setDefaultImageResId(R.drawable.ic_default);
	}

    public void setData(Course course) {
        if (course != null) {
            mImageView.setImageUrl(APIManager.toAbsoluteUrl(course.image),
                    APIManager.getInstance(getContext()).getImageLoader());
            mCourseTitle.setText(course.title);
            mDate.setText(course.date);
        }
    }

}
