package com.socialbusiness.dev.orangebusiness.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.MyOrderRequestListResult;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.util.Hashtable;
import java.util.List;

public class SalesDeliveredOrderActivity extends BaseActivity {

	public static final String TAG = "SalesDeliveredOrderActivity";

	private PullToRefreshExpandableListView mListView;
    private TextView total_money_title;
    private TextView total_money_value;
	private OrderAdapter mAdapter;
	private List<Order> mOrderList;

	private int mCurpage = 1;
	private int mTotalPage;
	
	private String lowerLevelUserId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seller_deliver_order);
		setUp();
		findView();
		setListener();

	}

    @Override
    protected void onResume() {
        super.onResume();
        callAPIListOrder();
    }

    private void setUp() {
		setTitle(R.string.done_deliver_order);
		mAdapter = new OrderAdapter(this);
		mAdapter.setTag(TAG);
		lowerLevelUserId = getIntent().getStringExtra("userId");
	}

	private void findView() {
		mListView = (PullToRefreshExpandableListView) findViewById(R.id.activity_deliver_order_listview);
		
		mListView.getRefreshableView().setAdapter(mAdapter);
		mListView.getRefreshableView().setDividerHeight(0);
		mListView.getRefreshableView().setGroupIndicator(null);

        total_money_title=(TextView)findViewById(R.id.total_money_title);
        total_money_title.setText("已发货订单总计：");
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
						Intent intent = new Intent(SalesDeliveredOrderActivity.this, OrderDetailActivity.class);
						intent.putExtra("id", order.id);
						intent.putExtra("tag", TAG);
						startActivity(intent);
						return true;
					}
				});

		mListView.getRefreshableView().setOnChildClickListener(
				new OnChildClickListener() {

					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {
						Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
						Intent intent = new Intent(SalesDeliveredOrderActivity.this, OrderDetailActivity.class);
						intent.putExtra("id", order.id);
						intent.putExtra("tag", TAG);
						startActivity(intent);
						return true;
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
	}

	private void callAPIListOrder() {
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("page", mCurpage + "");
		if (!TextUtils.isEmpty(lowerLevelUserId)) {
			parameters.put("userId", lowerLevelUserId);
		}
		APIManager.getInstance(this).listOrderBySellerDelivered(parameters,
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						hideNoData();
						showException(error);
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
                            total_money_value.setText(getResources().getText(R.string.yuan_symbol)+ StringUtil.get2DecimalFromString(response.totalMoney));
							if (mCurpage == 1) {
								mOrderList = response.data;
								mAdapter.setOrderList(mOrderList);
							} else if (mCurpage > 1 && mOrderList != null) {
								mOrderList.addAll(response.data);
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
	
	@Override
	public void showLoading() {
		if (mAdapter.getGroupCount() == 0) {
			super.showLoading();
		}
	}
}
