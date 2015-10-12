package com.socialbusiness.dev.orangebusiness.activity.order;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.OrderAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.OrderAdapter.OnDeleteOrderListener;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;

public class SalesPurchaseOrderActivity extends BaseActivity {

	public static String TAG;
	
	private PullToRefreshExpandableListView mListView;
	private TextView mAddOrderBtn;
	private OrderAdapter mAdapter;
	private List<Order> mOrderList;

	private int mCurpage = 1;
	private int mTotalPage;
    private Order.OrderTradeType tradeType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mysales_order);
		setUp();
		findView();
		setListener();
		callAPIListOrder();
	}

	private void setUp() {
        String tagStr = getIntent().getStringExtra(Constant.EXTRA_ORDER_FROM);
        if (TextUtils.isEmpty(tagStr) || tagStr.equals(Constant.EXTRA_ORDER_FROM_SELLER)) {
            TAG = Constant.EXTRA_ORDER_FROM_SELLER;
            tradeType = Order.OrderTradeType.Sell;
		    setTitle(R.string.mysales_order);
        } else {
            TAG = Constant.EXTRA_ORDER_FROM_BUYER;
            tradeType = Order.OrderTradeType.Buy;
            setTitle(R.string.my_purchase);
        }
		mAdapter = new OrderAdapter(this);
		mAdapter.setTag(TAG);
	}

	private void findView() {
		mListView = (PullToRefreshExpandableListView) findViewById(R.id.activity_mysales_order_listview);
		mAddOrderBtn = (TextView) findViewById(R.id.activity_mysales_order_btn);

		mListView.getRefreshableView().setAdapter(mAdapter);
		mListView.getRefreshableView().setDividerHeight(0);
		mListView.getRefreshableView().setGroupIndicator(null);
	}

	private void expandAllChildView() {
		for (int i = 0; i < mListView.getRefreshableView()
				.getExpandableListAdapter().getGroupCount(); i++) {
			mListView.getRefreshableView().collapseGroup(i);
			mListView.getRefreshableView().expandGroup(i);
		}
	}

	private void setListener() {
		mAddOrderBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SalesPurchaseOrderActivity.this, SalesOrderDetailActivity.class);
                intent.putExtra(Constant.EXTRA_KEY_TRADETYPE, tradeType);
				startActivity(intent);
			}
		});

		mListView.setOnRefreshListener(new OnRefreshListener2<ExpandableListView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ExpandableListView> refreshView) {
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
		
		mAdapter.setOnDeleteListener(new OnDeleteOrderListener() {
			
			@Override
			public void onDelete(String orderId) {
				if (!TextUtils.isEmpty(orderId)) {
					doDeleteOrder(orderId);
				}
			}
		});
	}
	
	private void doDeleteOrder(final String orderId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("确定要删除此订单吗？")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						callAPICancelSalesOrder(orderId);
						
					}
				})
				.setNegativeButton("取消", null)
				.create()
				.show();
	}
	
	private void callAPICancelSalesOrder(final String orderId) {
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<>();
		parameters.put("ids", orderId);
		APIManager.getInstance(this).cancelSalesOrder(tradeType, parameters,
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
						for (int i = 0; i < mOrderList.size(); i++) {
							if (mOrderList.get(i).id.equals(orderId)) {
								mOrderList.remove(i);
								break;
							}
						}
						mAdapter.notifyDataSetChanged();
						expandAllChildView();
					}
				});
	}

	private void callAPIListOrder() {
		hideNoData();
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("page", mCurpage + "");
		APIManager.getInstance(this).listSalesOrder(tradeType, parameters,
				new Response.ErrorListener() {
			
					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						showException(error);
						mListView.onRefreshComplete();
					}
				}, new Response.Listener<RequestListResult<Order.OrderTrade>>() {
					
					@Override
					public void onResponse(RequestListResult<Order.OrderTrade> response) {
						hideLoading();
						if (hasError(response)) {
							return;
						}

                        if(response.data == null){
                            return;
                        }

                        ArrayList<Order> responseOrderList = toOrderList(response.data);

                        if (mCurpage == 1) {
                            mOrderList = responseOrderList;
                            mAdapter.setOrderList(mOrderList);
                        } else if (mCurpage > 1 && mOrderList != null) {
                            mOrderList.addAll(responseOrderList);
                        }

                        mAdapter.notifyDataSetChanged();
                        mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);

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

    /**
     * 把OrderSales列表转换成Order列表
     * @param orderTradeList OrderSales列表
     * @return
     */
    private ArrayList<Order> toOrderList(ArrayList<Order.OrderTrade> orderTradeList) {

        ArrayList<Order> result = new ArrayList<Order>();
        for(Order.OrderTrade orderTrade : orderTradeList){
            Order order = new Order();
            result.add(order);

            ArrayList<Product> products = new ArrayList<Product>();
            ArrayList<Integer> productAmount = new ArrayList<Integer>();

            for(Order.OrderTrade.ProductItemFromServer item : orderTrade.products){
                products.add(item.product);
                item.product.price = item.price;
                productAmount.add(item.amount);
            }

            order.id = orderTrade.id;
            order.createTime = orderTrade.orderDate;
            order.products = products;
            order.productAmount = productAmount;
            order.freight = orderTrade.freight;
            order.code = orderTrade.code;
            order.user = new User();
            order.user.nickName = orderTrade.name;

        }
        return result;
    }

    @Override
	public void showLoading() {
		if (mAdapter.getGroupCount() == 0) {
			super.showLoading();
		}
	}
}
