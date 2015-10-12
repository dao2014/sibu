package com.socialbusiness.dev.orangebusiness.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.SalesOrderDetailItem;
import com.socialbusiness.dev.orangebusiness.component.SalesOrderDetailItem.OnDeletePIFSListener;
import com.socialbusiness.dev.orangebusiness.component.SalesOrderDetailItem.OnETDataChangeListener;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderTrade.ProductItemFromServer;

public class SalesOrderDetailAdapter extends BaseAdapter {

	private Context context;
	private List<ProductItemFromServer> productList;
	private OnDataChangedListener dataListener;
    
	public SalesOrderDetailAdapter(Context context) {
		this.context = context;
	}
	
	public void setProducts(List<ProductItemFromServer> products) {
		if (products != null) {
			this.productList = products;
		}
	}
	
	@Override
	public int getCount() {
		return productList != null ? productList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return productList != null ? productList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SalesOrderDetailItem item;
		if (convertView == null) {
			item = new SalesOrderDetailItem(context);
		} else {
			item = (SalesOrderDetailItem) convertView;
		}
		
		if (productList != null) {
			ProductItemFromServer productItem = productList.get(position);
			item.setProductItemData(productItem);
			item.setOnETDataChangeListener(new OnETDataChangeListener() {
				
				@Override
				public void onEditTextChanged() {
					if (dataListener != null) {
						dataListener.onDataChanged();
					}
				}
			});
			item.setOnDeltePIFSListener(new OnDeletePIFSListener() {
				
				@Override
				public void onDelete(ProductItemFromServer item) {
					for (int i = 0; i < productList.size(); i++) {
						if (item.productId.equals(productList.get(i).productId)) {
							productList.remove(i);
							notifyDataSetChanged();
                            if (dataListener != null) {
                                dataListener.onDataChanged();
                            }
							break;
						}
					}
				}
			});
		}
		return item;
	}
	
	public void setOnDataChangedListener(OnDataChangedListener l) {
		this.dataListener = l;
	}
	
	public interface OnDataChangedListener {
		public void onDataChanged();
	}
}
