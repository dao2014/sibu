package com.socialbusiness.dev.orangebusiness.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

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
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.fragment.order.DeliverDoneFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.ReceiptedFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingConfirmFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingDeliverFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;
import java.util.List;

public class OrderSearchActivity extends BaseActivity {

    public static final String TAG = "OrderSearchActiivty";

    private PullToRefreshExpandableListView mListView;
    private OrderAdapter mAdapter;


    private int mCurpage = 1;
    private int mTotalPage;
    private List<Order> mOrderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_search);
        setUp();
        findView();
//		callAPIListOrder();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        callAPIListOrder();
    }

    String keyWord = "";
    public static int isSeller;

    private void setUp() {
        setTitle("订单搜索");
        mAdapter = new OrderAdapter(this);
        mAdapter.setTag(TAG);
        isSeller = getIntent().getIntExtra(Constance.ISSELLER, 0);
        Log.e("=====", "==" + isSeller);
    }

    EditText fragment_product_input;
    View fragment_product_searchTxt;

    private void findView() {
        mListView = (PullToRefreshExpandableListView) findViewById(R.id.activity_deliver_done_order_listview);
        mListView.getRefreshableView().setAdapter(mAdapter);
        mListView.getRefreshableView().setDividerHeight(0);
        mListView.getRefreshableView().setGroupIndicator(null);
        mListView.setMode(Mode.DISABLED);

        fragment_product_input = (EditText) findViewById(R.id.fragment_product_input);
        fragment_product_searchTxt = findViewById(R.id.fragment_product_searchTxt);
    }

    private void expandAllChildView() {
        for (int i = 0; i < mListView.getRefreshableView()
                .getExpandableListAdapter().getGroupCount(); i++) {
            mListView.getRefreshableView().collapseGroup(i);
            mListView.getRefreshableView().expandGroup(i);
        }
    }

    private void setListener() {
        mListView.getRefreshableView().setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent,
                                        View v, int groupPosition, long id) {
                Order order = (Order) parent.getExpandableListAdapter()
                        .getGroup(groupPosition);
//                Intent intent = new Intent(OrderSearchActiivty.this, SalesInputOrderDetailActivity.class);
//                intent.putExtra("id", order.id);
//                intent.putExtra("tag", TAG);
                startActivity(order);
                return true; // true时伸展状态不变
            }
        });

        mListView.getRefreshableView().setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View v, int groupPosition, int childPosition,
                                        long id) {
                Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
//                Intent intent = new Intent(OrderSearchActiivty.this, SalesInputOrderDetailActivity.class);
//                intent.putExtra("id", order.id);
//                intent.putExtra("tag", TAG);
                startActivity(order);
                return false;
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

        fragment_product_searchTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String key = fragment_product_input.getText().toString();
                if (TextUtils.isEmpty(key)) {
                    ToastUtil.show(getApplicationContext(), "请输入关键字");
                    return;
                }
                keyWord = key;
                mCurpage = 1;
                callAPIListOrder();
            }
        });
    }

    public void startActivity(Order order) {
        Intent intent = new Intent();

        if (isSeller == 0) {
            switch (order.statusCode) {
                case 1:
                    intent.setClass(this, OrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", WaitingConfirmFragment.TAG);
                    break;
                case 2:
                    intent.setClass(this, OrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", Waiting2PayFragment.TAG);
                    break;
                case 3:
                    intent.setClass(this, OrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", WaitingDeliverFragment.TAG);
                    break;
                case 4:
                    intent.setClass(this, OrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", DeliverDoneFragment.TAG);
                    break;
                case 5:
                    intent.setClass(this, OrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", ReceiptedFragment.TAG);
                    break;

            }
        } else {
            switch (order.statusCode) {
                case 1:
                    intent.setClass(this, SalesInputOrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", SalesWaitingConfirmOrderActivity.TAG);
                    break;
                case 2:
                    intent.setClass(this, OrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", SalesWaiting2PayOrderActivity.TAG);
                    break;
                case 3:
                    intent.setClass(this, SalesInputOrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", SalesWaitingDeliverOrderActivity.TAG);
                    break;
                case 4:
                    intent.setClass(this, OrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", SalesDeliveredOrderActivity.TAG);
                    break;
                case 5:
                    intent.setClass(this, OrderDetailActivity.class);
                    intent.putExtra("id", order.id);
                    intent.putExtra("tag", ReceiptedActivity.TAG);
                    break;

            }
        }

        startActivity(intent);
    }

    private void callAPIListOrder() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("isSeller", isSeller + "");
        parameters.put("keyword", keyWord);
        parameters.put("page", mCurpage + "");
//        parameters.put(APIManager.KEY_PAGE_SIZE, "1000");
        APIManager.getInstance(this).orderSearch(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
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
                            ToastUtil.show(getApplicationContext(), "没有搜索到数据");
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
