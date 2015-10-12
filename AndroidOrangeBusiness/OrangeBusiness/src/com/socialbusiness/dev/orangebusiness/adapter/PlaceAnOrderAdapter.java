package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.activity.order.PlaceAnOrderActivity;
import com.socialbusiness.dev.orangebusiness.component.PlaceAnOrderItem;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;

import java.util.List;

public class PlaceAnOrderAdapter extends BaseAdapter {

	private Context context;
	private List<ShopCartItem> shopCartItemList;
	private PlaceAnOrderActivity activity;
	public PlaceAnOrderAdapter(Context context) {
		this.context = context;
        if(context instanceof  PlaceAnOrderActivity)
        activity=(PlaceAnOrderActivity)context;
	}

	public void setShopCartItemList(List<ShopCartItem> list) {
		shopCartItemList = list;
	}
	
	@Override
	public int getCount() {
		return shopCartItemList != null ? shopCartItemList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return shopCartItemList != null ? shopCartItemList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PlaceAnOrderItem placeAnOrderItem = null;
		if (convertView == null) {
			placeAnOrderItem = new PlaceAnOrderItem(context);
		} else {
			placeAnOrderItem = (PlaceAnOrderItem) convertView;
		}

		if (shopCartItemList != null) {
			placeAnOrderItem.setShopCartItemValues(shopCartItemList.get(position));
		}

        placeAnOrderItem.setOnEditTextChange(this.activity.onEditTextChange);
		return placeAnOrderItem;
	}

}
