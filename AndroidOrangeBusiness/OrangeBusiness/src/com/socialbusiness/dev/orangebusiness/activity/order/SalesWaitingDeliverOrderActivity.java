package com.socialbusiness.dev.orangebusiness.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.OrderAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.OrderAdapter.OnSelectStatusChangedListener;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.MyOrderRequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.List;

public class SalesWaitingDeliverOrderActivity extends BaseActivity {

	public static final String TAG = "SalesWaitingDeliverOrderActivity";

	private PullToRefreshExpandableListView mListView;
	private TextView mMoneySum;
	private TextView mMergePurchase;

    private TextView total_money_title;
    private TextView total_money_value;

	private OrderAdapter mAdapter;
	private List<Order> mOrderList;
	private int mCurpage = 1;
	private int mTotalPage;
	
	private boolean isRefresh = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sales_waiting_deliver_order);
		setUp();
		findView();
		setListener();
//		callAPIListOrder();
	}
    @Override
    protected void onResume() {
        super.onResume();
        callAPIListOrder();
    }
	private void setUp() {
		setTitle(R.string.seller_waiting_deliver_order);
		mAdapter = new OrderAdapter(this);
		mAdapter.setTag(TAG);
	}

	private void findView() {
		mListView = (PullToRefreshExpandableListView) findViewById(R.id.activity_sales_waiting_deliver_listview);
		mMoneySum = (TextView) findViewById(R.id.activity_sales_waiting_deliver_money);
		mMergePurchase = (TextView) findViewById(R.id.activity_sales_waiting_deliver_merge_purchase);

		mListView.getRefreshableView().setAdapter(mAdapter);
		mListView.getRefreshableView().setDividerHeight(0);
		mListView.getRefreshableView().setGroupIndicator(null);
		mMergePurchase.setEnabled(false);

        total_money_title=(TextView)findViewById(R.id.total_money_title);
        total_money_title.setText("待发货订单总计：");
        total_money_value=(TextView)findViewById(R.id.total_money_value);
	}

	private void expandAllChildView() {
		for (int i = 0; i < mListView.getRefreshableView()
				.getExpandableListAdapter().getGroupCount(); i++) {
			mListView.getRefreshableView().collapseGroup(i);
			mListView.getRefreshableView().expandGroup(i);
		}
	}

	private void setListener() {
		mListView.getRefreshableView().setOnGroupClickListener(
				new OnGroupClickListener() {

					@Override
					public boolean onGroupClick(ExpandableListView parent,
							View v, int groupPosition, long id) {
						Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
						Intent intent = new Intent(SalesWaitingDeliverOrderActivity.this, SalesInputOrderDetailActivity.class);
						intent.putExtra("id", order.id);
						intent.putExtra("tag", TAG);
						startActivity(intent);
						return true;
					}
				});

        mListView.getRefreshableView().setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
				Intent intent = new Intent(SalesWaitingDeliverOrderActivity.this, SalesInputOrderDetailActivity.class);
				intent.putExtra("id", order.id);
				intent.putExtra("tag", TAG);
				startActivity(intent);
				return false;
			}
		});

		mListView.setOnRefreshListener(new OnRefreshListener2<ExpandableListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ExpandableListView> refreshView) {
						isRefresh = true;
						mCurpage = 1;
						callAPIListOrder();
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ExpandableListView> refreshView) {
						if (mCurpage < mTotalPage && mOrderList != null) {
							mCurpage++;
							callAPIListOrder();
						}
					}
				});
		
		mAdapter.setOnSelectStatusChangedListener(new OnSelectStatusChangedListener() {
			
			@Override
			public void onSelectStatusChanged() {
				List<Boolean> selectList = mAdapter.getIsSelectedList();
				int nums = 0;//纪录被选中的数量
				if (selectList != null && selectList.size() > 0) {
					for (Boolean b : selectList) {
						if (b) {//被选中
							nums++;
						}
					}
				}
				if (nums > 1) {	//合并采购按钮的两个状态："转采购"、"合并转采购"
					mMergePurchase.setText(getResources().getString(R.string.merge_turn_purchase));
					mMergePurchase.setEnabled(true);
				} else if (nums == 1) {
					mMergePurchase.setText(getResources().getString(R.string.turn_purchase));
					mMergePurchase.setEnabled(true);
				} else {
					mMergePurchase.setText(getResources().getString(R.string.turn_purchase));
					mMergePurchase.setEnabled(false);
				}
				mMoneySum.setText(getTotalMoney());
			}
		});
		
		mMergePurchase.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callAPIMergeOrder();
			}
		});
	}
	
	private void callAPIListOrder() {
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("page", mCurpage + "");
		APIManager.getInstance(this).listOrderBySellerWaitingDeliver(
				parameters, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						showNetworkDisconnet();
						mListView.onRefreshComplete();
					}
				}, new Response.Listener<MyOrderRequestListResult<Order>>() {

					@Override
					public void onResponse(MyOrderRequestListResult<Order> response) {
						hideLoading();
						hideNoData();
						if (hasError(response)) {
							return;
						}
						if (response.data != null) {
                            total_money_value.setText(getResources().getText(R.string.yuan_symbol)+StringUtil.get2DecimalFromString(response.totalMoney));
							if (mCurpage == 1) {
								mOrderList = response.data;
								if (isRefresh) {
									isRefresh = false;
									mAdapter.updateOrderList(mOrderList);
								} else {
									mAdapter.setOrderList(mOrderList);
								}
							} else if (mCurpage > 1 && mOrderList != null) {
								mOrderList.addAll(response.data);
								mAdapter.addOrderListSelected(response.data.size());
							}

							mAdapter.notifyDataSetChanged();
							mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
						}

						if (mAdapter.getGroupCount() == 0) {
							showNoDataTips();
						} else if (mCurpage >= mTotalPage) {
							mListView.setMode(Mode.PULL_FROM_START);
						} else {
							mListView.setMode(Mode.BOTH);
						}

						expandAllChildView();
						mListView.onRefreshComplete();
					}
				});
	}

	private void callAPIMergeOrder() {
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("ids", getIds());
		APIManager.getInstance(this).mergeOrder(parameters, 
				new Response.ErrorListener() {
			
					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						showException(error);
					}
				}, new Response.Listener<RequestResult<?>>() {
					
					@Override
					public void onResponse(RequestResult<?> response) {
						hideLoading();
						if (hasError(response)) {
							return;
						}
						ToastUtil.show(SalesWaitingDeliverOrderActivity.this, response.message);
						finish();
					}
				});
	}
	
	private String getIds() {
		StringBuilder sb = new StringBuilder();
		List<Boolean> selectList = mAdapter.getIsSelectedList();
		if (selectList == null) {
			return "";
		}
		for (int i = 0; i < selectList.size(); i++) {
			if (selectList.get(i)) {
				sb.append(mOrderList.get(i).id);
				sb.append(",");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	
	/**
	 * @return 被选中订单的总金额
	 */
	private String getTotalMoney() {
		List<Boolean> selectList = mAdapter.getIsSelectedList();
		if (selectList == null) {
			return "";
		}
		BigDecimal money = new BigDecimal(0f);
		for (int i = 0; i < selectList.size(); i++) {
			if (selectList.get(i)) {
				money = money.add(getOrderTotalMoney(mOrderList.get(i)));
			}
		}
		return StringUtil.getStringFromFloatKeep2(money.doubleValue());
	}
	
	/**
	 * 得到指定订单的商品总金额
	 * @param order
	 * @return
	 */
	private BigDecimal getOrderTotalMoney(Order order) {
		List<Product> productList = order.products;
		List<Integer> quantityList = order.productAmount;
		if (productList == null || quantityList == null) {
			return new BigDecimal(0f);
		}
		BigDecimal money = new BigDecimal(0f);
		for (int i = 0; i < productList.size(); i++) {
			money = money.add(new BigDecimal(productList.get(i).price).multiply(new BigDecimal(quantityList.get(i))));
		}
		money = money.add(new BigDecimal(order.freight));
		return money;
	}
	
	@Override
	public void showLoading() {
		if (mAdapter.getGroupCount() == 0) {
			super.showLoading();
		}
	}
}
