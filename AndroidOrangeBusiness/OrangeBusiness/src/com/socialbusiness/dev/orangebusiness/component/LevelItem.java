package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.model.Level;

/**
 * Created by enwey on 2014/11/3.
 */
public class LevelItem extends RelativeLayout {
	private Context context;
	private TextView mVipGrade;
	private TextView mVipNum;

	public LevelItem(Context context) {
		super(context);
		this.context = context;
		initView();
	}

	public LevelItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}

	private void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.view_level_item, this);
		findView(view);
	}

	private void findView(View view) {
		mVipGrade = (TextView) view.findViewById(R.id.view_vip_item_grade);
		mVipNum = (TextView) view.findViewById(R.id.view_vip_item_number);
	}
	
	public void setLevelValue(Level level) {
		if (level != null) {
			mVipGrade.setText(level.name);
			mVipNum.setText(level.memberCount + "");
		}
	}
}
