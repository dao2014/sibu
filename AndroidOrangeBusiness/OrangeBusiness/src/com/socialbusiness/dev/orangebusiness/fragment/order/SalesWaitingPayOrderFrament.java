package com.socialbusiness.dev.orangebusiness.fragment.order;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.order.OrderDetailActivity;
import com.socialbusiness.dev.orangebusiness.adapter.OrderAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.MyOrderRequestListResult;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by zkboos on 2015/4/27.
 */
public class SalesWaitingPayOrderFrament extends BaseFragment {

    public static final String TAG = "SalesWaiting2PayOrderActivity";
    private PullToRefreshExpandableListView mListView;
    private OrderAdapter mAdapter;
    private List<Order> mOrderList;
    private TextView total_money_title;
    private TextView total_money_value;
    private int mCurpage = 1;
    private int mTotalPage;

    @Override
    public void setContentView(LayoutInflater inflater, ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.activity_seller_done_deliver_order, mLayerContextView);
        setUp();
        findView(mLayerContextView);
        setListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        callAPIListOrder();
    }

    private void setUp() {
        mAdapter = new OrderAdapter(this.getActivity());
        mAdapter.setTag(TAG);
    }

    private void findView(View mLayerContextView) {
        mListView = (PullToRefreshExpandableListView) mLayerContextView.findViewById(R.id.activity_deliver_done_order_listview);

        mListView.getRefreshableView().setAdapter(mAdapter);
        mListView.getRefreshableView().setDividerHeight(0);
        mListView.getRefreshableView().setGroupIndicator(null);

        total_money_title = (TextView) mLayerContextView.findViewById(R.id.total_money_title);
        total_money_title.setText("待付款订单总计：");
        total_money_value = (TextView) mLayerContextView.findViewById(R.id.total_money_value);
        mLayerContextView.findViewById(R.id.total_money_layout).setBackgroundColor(getResources().getColor(R.color.wait_deliver_total_layout_bg));
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
                new ExpandableListView.OnGroupClickListener() {

                    @Override
                    public boolean onGroupClick(ExpandableListView parent,
                                                View v, int groupPosition, long id) {
                        startOrderDetailActivity(parent, groupPosition);
                        return true;
                    }
                });

        mListView.getRefreshableView().setOnChildClickListener(
                new ExpandableListView.OnChildClickListener() {

                    @Override
                    public boolean onChildClick(ExpandableListView parent,
                                                View v, int groupPosition, int childPosition,
                                                long id) {
                        startOrderDetailActivity(parent, groupPosition);
                        return true;
                    }
                });

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ExpandableListView>() {

            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ExpandableListView> refreshView) {
                refresh();
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

    private void startOrderDetailActivity(ExpandableListView parent, int groupPosition) {
        Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
        Intent intent = new Intent(this.getActivity(), OrderDetailActivity.class);
        intent.putExtra("id", order.id);
        intent.putExtra("tag", TAG);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public static final int REQUEST_CODE = 100;

    private void refresh() {
        mCurpage = 1;
        callAPIListOrder();
    }


    private void callAPIListOrder() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", mCurpage + "");
        parameters.put("isPay", "0");
        APIManager.getInstance(getActivity()).listOrderBySellerWaitingPay(parameters,
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
//                        hideNoData();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            total_money_value.setText(getResources().getText(R.string.yuan_symbol)+ StringUtil.get2DecimalFromString(response.totalMoney) );
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
                            mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        } else {
                            mListView.setMode(PullToRefreshBase.Mode.BOTH);
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
