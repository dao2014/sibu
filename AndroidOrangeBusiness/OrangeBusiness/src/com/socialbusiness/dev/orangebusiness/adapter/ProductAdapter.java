package com.socialbusiness.dev.orangebusiness.adapter;

import java.util.List;

import com.socialbusiness.dev.orangebusiness.component.ProductItem;
import com.socialbusiness.dev.orangebusiness.model.Product;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ProductAdapter extends BaseAdapter {

	private Context context;
	private List<Product> mProductList;
    private int productTypeInt;

	public ProductAdapter(Context context) {
		this.context = context;
	}
    public void setProductTypeInt(int typeInt) {
        this.productTypeInt = typeInt;
    }

	public void setProductList(List<Product> list) {
		mProductList = list;
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
		ProductItem productItem;
		if (convertView == null) {
			productItem = new ProductItem(context);
		} else {
			productItem = (ProductItem) convertView;
		}
		
		if (mProductList != null) {
			productItem.setProductItemData(mProductList.get(position),
                    productTypeInt == Product.ProductType.KWMProductTypeStock.getIntValue());
		}
		return productItem;
	}

}
