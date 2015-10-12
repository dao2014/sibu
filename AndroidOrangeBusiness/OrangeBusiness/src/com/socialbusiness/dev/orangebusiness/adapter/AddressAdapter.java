package com.socialbusiness.dev.orangebusiness.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.socialbusiness.dev.orangebusiness.component.AddressItem;
import com.socialbusiness.dev.orangebusiness.component.AddressItem.OnAddressStatusChangeListener;
import com.socialbusiness.dev.orangebusiness.model.Address;

public class AddressAdapter extends BaseAdapter {
    private Context context;
    private List<Address> mAddressList;

    private OnAddressCallbackListener listener;
    
    public AddressAdapter(Context context) {
        this.context = context;
    }

    public void setAddressList(List<Address> list) {
        this.mAddressList = list;
    }

    @Override
    public int getCount() {
        return (mAddressList != null ? mAddressList.size() : 0);
    }

    @Override
    public Object getItem(int position) {
        return (mAddressList != null ? mAddressList.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AddressItem addressItem;
        if (convertView == null) {
            addressItem = new AddressItem(context);
        } else {
            addressItem = (AddressItem) convertView;
        }

        if (mAddressList != null) {
        	addressItem.setAddressData(position, mAddressList.get(position));
        	setListeners(addressItem);
        }

        return addressItem;
    }

    private void setListeners(AddressItem item) {
    	if (item != null) {
    		item.setOnAddressStatusChangedListener(new OnAddressStatusChangeListener() {
				
				@Override
				public void setDefault(int position) {
					if (listener != null) {
						listener.setDefault(position);
					}
				}
				
				@Override
				public void onDelete(int position) {
					if (listener != null) {
						listener.onDelete(position);
					}
				}
			});
    	}
    }
    
    public interface OnAddressCallbackListener {
    	public void onDelete(int position);
    	public void setDefault(int position);
    }
    
    public void setOnAddressCallbackListener(OnAddressCallbackListener l) {
    	this.listener = l; 
    }
    
}
