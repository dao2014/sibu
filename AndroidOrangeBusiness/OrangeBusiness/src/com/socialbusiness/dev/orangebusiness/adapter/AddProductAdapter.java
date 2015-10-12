package com.socialbusiness.dev.orangebusiness.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.AddProductItem;
import com.socialbusiness.dev.orangebusiness.component.AddProductItem.OnAddClickListener;
import com.socialbusiness.dev.orangebusiness.model.Product;

public class AddProductAdapter extends BaseAdapter {

	private List<Product> mProductList;
	private HashMap<String, Boolean> isAddMap;
	private Context context;
	
	public AddProductAdapter(Context context) {
		this.context = context;
	}
	
	public void setProductList(List<Product> list) {
		if (list != null) {
			this.mProductList = list;
			isAddMap = new HashMap<String, Boolean>();
			for (int i = 0; i < list.size(); i++) {
				isAddMap.put(list.get(i).id, false);
			}
		}
	}
	
	public void addProductList(List<Product> list) {
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				isAddMap.put(list.get(i).id, false);
			}
		}
	}
	
	/**
	 * 下拉刷新时，保存第一页数据的选中状态，清除其它页的数据和数据的添加状态
	 * @param list
	 */
	public void updateProductList(List<Product> list) {
		if (list != null) {
			mProductList = list;
			HashMap<String, Boolean> tempMap = new HashMap<String, Boolean>();
			for (int i = 0; i < list.size(); i++) {	//保留第一页（添加状态）
				if (isAddMap.containsKey(list.get(i).id)) {
					tempMap.put(list.get(i).id, isAddMap.get(list.get(i).id));
				} else {
					tempMap.put(list.get(i).id, false);
				}
			}
			if (tempMap.size() == list.size()) {
				isAddMap.clear();
				isAddMap = tempMap;
			}
		}
	}
	
	public HashMap<String, Boolean> getIsAddMap() {
		return isAddMap;
	}
	
	@Override
	public int getCount() {
		return mProductList != null ? mProductList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mProductList != null ? mProductList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AddProductItem item = null;
		if (convertView == null) {
			item = new AddProductItem(context);
		} else {
			item = (AddProductItem) convertView;
		}
		
		if (mProductList != null && isAddMap != null) {
			Product product = mProductList.get(position);
			item.setProductData(product, isAddMap.get(product.id));
			item.setOnAddClickListener(new OnAddClickListener() {
				@Override
				public void OnAddClick(String id, boolean isAdd) {
					if (id != null) {
						isAddMap.put(id, isAdd);
					}
				}
			});
		}
		return item;
	}
}
