package com.socialbusiness.dev.orangebusiness.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingDeliverOrderActivity;
import com.socialbusiness.dev.orangebusiness.component.ConfirmOrderItem;
import com.socialbusiness.dev.orangebusiness.component.ConfirmOrderItem.OnEditTextDataChangeListener;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;

public class ConfirmOrderAdapter extends BaseAdapter {
	
    private Context context;
    private List<Product> productList;
    private HashMap<String, Float> priceMap;//价格
    private HashMap<String, Integer> quantityMap;//数量

    private OnItemDataChangedListener itemLitener;
    private Order mOrder;
    private String tag;
    
    public ConfirmOrderAdapter(Context context) {
        this.context = context;
    }

    public void setTag(String tag) {
    	this.tag = tag;
    }
    
    public void setOrder(Order order) {
    	this.mOrder = order;
    	this.productList = mOrder.products;
    	priceMap = new HashMap<>();
    	quantityMap = new HashMap<>();
    	for (int i = 0; i < productList.size(); i++) {
    		priceMap.put(mOrder.productIds.get(i), productList.get(i).price);
    		quantityMap.put(mOrder.productIds.get(i), mOrder.productAmount.get(i));
    	}
    }
    
    public HashMap<String, Float> getPriceMap() {
    	return priceMap;
    }
    
    public HashMap<String, Integer> getQuantityMap() {
    	return quantityMap;
    }
    
    @Override
    public int getCount() {
        return (productList != null ? productList.size() : 0);
    }

    @Override
    public Object getItem(int position) {
        return productList != null ? productList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    public String getItemIdString(int position) {
    	return mOrder != null ? mOrder.productIds.get(position) : null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ConfirmOrderItem confirmOrderItem;
        if (convertView == null) {
            confirmOrderItem = new ConfirmOrderItem(context);
        } else {
            confirmOrderItem = (ConfirmOrderItem) convertView;
        }
        
        if (productList != null && priceMap != null && quantityMap != null) {
        	Product product = productList.get(position);
        	confirmOrderItem.setItemData(product, 
        			quantityMap.get(product.id),	//最新数量
        			priceMap.get(product.id));	//最新价格
        	updateEditTextData(confirmOrderItem);
        	if (!TextUtils.isEmpty(tag) && SalesWaitingDeliverOrderActivity.TAG.equals(tag)) {
        		confirmOrderItem.setPriceEnable(false);
        	}
        }
        return confirmOrderItem;
    }

    private void updateEditTextData(final ConfirmOrderItem confirmOrderItem) {
    	
    	confirmOrderItem.setOnEditTextDataChangeListener(new OnEditTextDataChangeListener() {
			@Override
			public void onEditTextChanged(int quantity, float price,
					String productId) {
				
				quantityMap.put(productId, quantity);
				priceMap.put(productId, price);
				if (itemLitener != null) {
					itemLitener.onItemDataChanged();
				}				
			}
		});
    }
    
    public void setOnItemDataChangedListener(OnItemDataChangedListener l) {
    	this.itemLitener = l;
    }
    
    public interface OnItemDataChangedListener {
    	public void onItemDataChanged();
    }
}
