package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

/**
 * Created by enwey on 2014/11/28.
 */
public class OrderConfirmHeader extends LinearLayout {
	
	private TextView mLeft;
	private TextView mLeftValue;
	private TextView mRight;
	private TextView mRightValue;
	
	private String tag;
	
	public OrderConfirmHeader(Context context) {
		super(context);
		initView();
	}

	public OrderConfirmHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View headerView = inflater.inflate(R.layout.view_list_header, this);
		findView(headerView);
	}

	private void findView(View view) {
		mLeft = (TextView) view.findViewById(R.id.view_list_header_call_name);
		mLeftValue = (TextView) view.findViewById(R.id.view_list_header_name);
		mRight = (TextView) view.findViewById(R.id.view_list_header_date_text);
		mRightValue = (TextView) view.findViewById(R.id.view_list_header_date);
	}

	public void setTagToChange(String tag) {
		if (TextUtils.isEmpty(tag)) {
			return;
		}
		this.tag = tag;
		if (tag.endsWith("Fragment")) {
			mLeft.setText("卖家：");
		}
		
//		if (!SalesWaitingDeliverOrderActivity.TAG.equals(tag)) {
			mRight.setText(getResources().getString(R.string.order_status));
//			mRight.setTextColor(Color.rgb(255, 74, 74));//#ff4a4a 浅红色，价格的颜色
//		}
	}
	
	// 设置数据
	public void setHeaderDate(Order order) {
		if (TextUtils.isEmpty(tag) || order == null) {
			return;
		}
		
		if (order.user != null) {
			mLeftValue.setText(order.user.nickName);
		}
		
//		if (SalesWaitingConfirmOrderActivity.TAG.equals(tag)
//				|| SalesWaitingDeliverOrderActivity.TAG.equals(tag)) {
//        if (SalesWaitingDeliverOrderActivity.TAG.equals(tag)) {
//			mRightValue.setText(StringUtil.showMessage(order.createTime));
//		} else {
			mRightValue.setTextColor(getResources().getColor(R.color.red_product_price));//#ff4a4a 浅红色，价格的颜色
			mRightValue.setText(StringUtil.showMessage(order.status));
//		}
	}

}
