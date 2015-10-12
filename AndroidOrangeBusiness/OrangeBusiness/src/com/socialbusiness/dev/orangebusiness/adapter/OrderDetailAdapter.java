package com.socialbusiness.dev.orangebusiness.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.OrderDetailItem;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;

public class OrderDetailAdapter extends BaseAdapter {

	private Context context;
	private List<Product> mProductList;
	private List<Integer> mQuantityList;
	
	private Order order;
	
	public OrderDetailAdapter(Context context) {
		this.context = context;
	}
	
	public void setOrder(Order order) {
		this.order = order;
		if (order != null) {
			mProductList = order.products;
			mQuantityList = order.productAmount;
		}
	}
	
	@Override
	public int getCount() {
		return mProductList != null ? mProductList.size() : 0;
	}

	@Override
	public HashMap<String, Object> getItem(int position) {
		HashMap<String, Object> value = null;
		if (mProductList != null && mQuantityList != null) {
			value = new HashMap<>();
			value.put("product", mProductList.get(position));
			value.put("quantity", mQuantityList.get(position));
		}
		return value;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		OrderDetailItem orderDetailItem;
		if (convertView == null) {
			orderDetailItem = new OrderDetailItem(context);
		} else {
			orderDetailItem = (OrderDetailItem) convertView;
		}
		
		if (getItem(position) != null) {
			orderDetailItem.setProductValue(getItem(position));
		}
		return orderDetailItem;
	}

}
