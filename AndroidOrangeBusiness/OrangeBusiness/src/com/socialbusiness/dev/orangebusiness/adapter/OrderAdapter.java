package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;

import com.socialbusiness.dev.orangebusiness.activity.order.SalesPurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingDeliverOrderActivity;
import com.socialbusiness.dev.orangebusiness.component.OrderChildViewItem;
import com.socialbusiness.dev.orangebusiness.component.OrderChildViewItem.OnDeleteMySalesOrderCVListener;
import com.socialbusiness.dev.orangebusiness.component.OrderChildViewItem.OnOrderSelectedListener;
import com.socialbusiness.dev.orangebusiness.component.OrderGroupViewItem;
import com.socialbusiness.dev.orangebusiness.component.OrderGroupViewItem.OnCancelDoneListener;
import com.socialbusiness.dev.orangebusiness.component.OrderGroupViewItem.OnDeleteMySalesOrderGVLisener;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingConfirmFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class OrderAdapter extends BaseExpandableListAdapter {

	private Context context;
	private List<Order> orderList;
	private String TAG;
	
	// 用来控制选中状态
	private List<Boolean> isSelectedList;
	private OnSelectStatusChangedListener listener;
	private OnCancelChangedListener cancelListener;
	private OnDeleteOrderListener deleteListener;
	
	public void setTag(String tag) {
		this.TAG = tag;
	}
	
	public OrderAdapter(Context context) {
		this.context = context;
	}
	
	public void setOrderList(List<Order> list) {
		if (list != null) {
			orderList = list;
			if (!TextUtils.isEmpty(TAG) && (TAG.equals(SalesWaitingDeliverOrderActivity.TAG)||TAG.equals(Waiting2PayFragment.TAG))) {
				isSelectedList = new ArrayList<Boolean>();
				initSelectedData();
			}
		}
	}

	/**
	 * 下拉刷新时，保存第一页数据的选中状态，清除其它页的数据和数据的选中状态
	 * @param list
	 */
	public void updateOrderList(List<Order> list) {
		if (list != null&&isSelectedList!=null) {
			for (int i = list.size(); i < isSelectedList.size();) {
				isSelectedList.remove(i);
				orderList.remove(i);
			}
		}
	}
	
	/**
	 * 初始化为全未选中状态
	 */
	private void initSelectedData() {
		for (int i = 0; i < orderList.size(); i++) {
			isSelectedList.add(false);
		}
	}
	
	public void addOrderListSelected(int length) {
		int temp = isSelectedList.size();
		for (int i = temp; i < (temp + length); i++) {
			isSelectedList.add(false);
		}
	}
	
	public List<Boolean> getIsSelectedList() {
		return isSelectedList;
	}
	
	public void setIsSelectedList(ArrayList<Boolean> selectedList) {
		this.isSelectedList = selectedList;
	}
	
	@Override
	public int getGroupCount() {
		return orderList != null ? orderList.size() : 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return orderList != null
				&& orderList.get(groupPosition).products != null ? orderList
				.get(groupPosition).products.size() : 0;
	}

	@Override
	public Order getGroup(int groupPosition) {
		return orderList != null ? orderList.get(groupPosition) : null;
	}

	@Override
	public Hashtable<String, Object> getChild(int groupPosition, int childPosition) {
		Hashtable<String, Object> parameters = null;

		if (orderList != null && orderList.get(groupPosition) != null && 
				orderList.get(groupPosition).products != null) {
			Order order = orderList.get(groupPosition);
			if(order != null) {
				parameters = new Hashtable<String, Object>();
				if(order.products != null && order.products.size() > childPosition) {
					Product product = order.products.get(childPosition);
					if(product != null) {
						parameters.put("product", product);
					}
				}
				if(order.productIds != null && order.productIds.size() > childPosition) {
					String productId = order.productIds.get(childPosition);
					if(productId != null) {
						parameters.put("productId", productId);
					}
				}
				if(order.productAmount != null && order.productAmount.size() > childPosition) {
					Integer amount = order.productAmount.get(childPosition);
					if(amount != null) {
						parameters.put("quantity", amount);
					}
				}
			}
		}
		return parameters;
	}

	@Override
	public long getGroupId(int groupPosition) {
		// 没有用处
		return 0;
	}
	
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// 没有用处
		return 0;
	}
	
	public String getGroupIdString(int groupPosition) {
		return orderList != null ? orderList.get(groupPosition).id : null; 
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		OrderGroupViewItem groupViewItem = null;
		if (convertView == null) {
			groupViewItem = new OrderGroupViewItem(context);
		} else {
			groupViewItem = (OrderGroupViewItem) convertView;
		}
		
		if (orderList != null) {
			groupViewItem.setValueByTag(TAG);
			groupViewItem.setGroupViewValue(getGroup(groupPosition));
			
			if (TAG.equals(SalesPurchaseOrderActivity.TAG)) {
				groupViewItem.setOnDeleteMySalesOrderListener(new OnDeleteMySalesOrderGVLisener() {
					
					@Override
					public void onDelete(String id) {
						if (deleteListener != null) {
							deleteListener.onDelete(id);
						}
					}
				});
			}
			if (TAG.equals(WaitingConfirmFragment.TAG)) {
				groupViewItem.setOnCancelDoneListener(new OnCancelDoneListener() {
					
					@Override
					public void onCancel(String id) {
						if (orderList.size() != 0) {
							Order order = null;
							for (Order temp : orderList) {
								if (temp.id.equals(id)) {
									order = temp;
								}
							}
							if (order != null) {
								orderList.remove(order);
								if (cancelListener != null) {
									cancelListener.onCancelChanged();
								}
							}
						}
					}
				});
			}
		}
		return groupViewItem;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		OrderChildViewItem childViewItem = null;
		if (convertView == null) {
			childViewItem = new OrderChildViewItem(context);
		} else {
			childViewItem = (OrderChildViewItem) convertView;
		}
		
		if (orderList != null && getChildrenCount(groupPosition) != 0) {
			childViewItem.hideBottomViews(isLastChild, TAG);
			childViewItem.setChildViewValue(getChild(groupPosition, childPosition), orderList.get(groupPosition).id);
		
			if (TAG.equals(SalesPurchaseOrderActivity.TAG)) {
				childViewItem.setOnDeleteMySalesOrderCVListener(new OnDeleteMySalesOrderCVListener() {
					
					@Override
					public void onDelete(String orderId) {
						if (deleteListener != null) {
							deleteListener.onDelete(orderId);
						}
					}
				});
			}
			
			if (isLastChild) {
				childViewItem.setBottomValue(getGroup(groupPosition));
				if (TAG.equals(SalesWaitingDeliverOrderActivity.TAG)||TAG.equals(Waiting2PayFragment.TAG)) {
					final ImageView selectImageView = childViewItem.getSelectImageView();
					
					if (selectImageView != null) {
						selectImageView.setSelected(getIsSelectedList().get(groupPosition));
						childViewItem.setOnOrderSelectedListener(new OnOrderSelectedListener() {
						
							@Override
							public void onOrderSelected() {
								selectImageView.setSelected(!selectImageView.isSelected());
								if (groupPosition <= getIsSelectedList().size()) {
									getIsSelectedList().set(groupPosition, selectImageView.isSelected()); //更新选中状态
								}
								if (listener != null) {
									listener.onSelectStatusChanged();
								}
							}
						});
					}
				}
			}
		}
		
		return childViewItem;
	}
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	public void setOnSelectStatusChangedListener(OnSelectStatusChangedListener l) {
		this.listener = l;
	}
	
	public void setOnCancelChangedListener(OnCancelChangedListener l) {
		cancelListener = l;
	}
	
	public void setOnDeleteListener(OnDeleteOrderListener l) {
		this.deleteListener = l;
	}
	
	public interface OnSelectStatusChangedListener {
		public void onSelectStatusChanged();
	}
	
	public interface OnCancelChangedListener {
		public void onCancelChanged();
	}
	
	public interface OnDeleteOrderListener {
		public void onDelete(String orderId);
	}
}
