package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.me.CoursewareListActivity;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

public class CourseClassifyItem extends LinearLayout {
	public LinearLayout mLayoutLeft;
	public NetworkImageView mImageLeft;
	public TextView mTitleLeft;
	public LinearLayout mLayoutRight;
	public NetworkImageView mImageRight;
	public TextView mTitleRight;
	public Context context;

	public CourseClassifyItem(Context context) {
		super(context);
		this.context = context;
		initView();
	}

	public CourseClassifyItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}

	public void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.view_course_classify_item, this);
		findView(view);
		registerListener();
	}

	public void findView(View view) {
		mLayoutLeft = (LinearLayout) view
				.findViewById(R.id.view_courseware_list_leftLayout);
		mImageLeft = (NetworkImageView) view
				.findViewById(R.id.view_courseware_list_leftimage);
		mTitleLeft = (TextView) view
				.findViewById(R.id.view_courseware_list_lefttitle);
		mLayoutRight = (LinearLayout) view
				.findViewById(R.id.view_courseware_list_rightLayout);
		mImageRight = (NetworkImageView) view
				.findViewById(R.id.view_courseware_list_rightimage);
		mTitleRight = (TextView) view
				.findViewById(R.id.view_courseware_list_righttitle);

	}

	public void registerListener() {
		OnClickListener register = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				switch (v.getId()) {
				case R.id.view_courseware_list_leftLayout:
                    ToastUtil.show(getContext(), "我是leftItem,点击我跳转到分类课件...");
					intent = new Intent(getContext(),
							CoursewareListActivity.class);
					context.startActivity(intent);
					break;
				case R.id.view_courseware_list_rightLayout:
                    ToastUtil.show(getContext(), "我是rightItem,点击我跳转到分类课件...");
					intent = new Intent(getContext(),
							CoursewareListActivity.class);
					context.startActivity(intent);
					break;

				default:
					break;
				}
			}
		};
		mLayoutLeft.setOnClickListener(register);
		mLayoutRight.setOnClickListener(register);
	}
}
