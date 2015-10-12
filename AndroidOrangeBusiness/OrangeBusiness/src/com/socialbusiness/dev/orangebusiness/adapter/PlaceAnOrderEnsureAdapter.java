package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.PlaceAnOrderItem;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;

import java.util.List;

public class PlaceAnOrderEnsureAdapter extends BaseAdapter {

	private Context context;
	private List<ShopCartItem> shopCartItemList;
//	private PlaceAnOrderEnsureActivity activity;
	public PlaceAnOrderEnsureAdapter(Context context) {
		this.context = context;
//        if(context instanceof  PlaceAnOrderActivity)
//        activity=(PlaceAnOrderEnsureActivity)context;
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
            placeAnOrderItem.setEnsureView();
		}

//        placeAnOrderItem.setOnEditTextChange((PlaceAnOrderActivity.OnEditTextChange) this.activity.onEditTextChange);
		return placeAnOrderItem;
	}

}
