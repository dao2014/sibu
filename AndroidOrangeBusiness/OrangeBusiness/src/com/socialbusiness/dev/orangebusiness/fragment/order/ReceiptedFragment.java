package com.socialbusiness.dev.orangebusiness.fragment.order;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.order.OrderDetailActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.PurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.adapter.OrderAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.MyOrderRequestListResult;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by niuchaohua on 1/16/15.
 */
public class ReceiptedFragment extends BaseFragment {


    public static final String TAG = "ReceiptedFragment";

    private PullToRefreshExpandableListView mListView;
    private OrderAdapter mAdapter;
    private List<Order> mOrderList;
    private int mCurpage = 1;
    private int mTotalPage;

    @Override
    public void setContentView(LayoutInflater inflater, ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.fm_order_buyer_content, mLayerContextView);
        findView(mLayerContextView);
        setListener();
        callAPIListOrder();
    }

    private void findView(View view) {
        mListView = (PullToRefreshExpandableListView) view
                .findViewById(R.id.fm_order_buyer_content_listview);
        mAdapter = new OrderAdapter(getActivity());
        mAdapter.setTag(TAG);

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
        mListView.getRefreshableView().setOnGroupClickListener(
                new ExpandableListView.OnGroupClickListener() {

                    @Override
                    public boolean onGroupClick(ExpandableListView parent,
                                                View v, int groupPosition, long id) {//TODO
                        Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
                        Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                        intent.putExtra("id", order.id);
                        intent.putExtra("tag", TAG);
                        startActivity(intent);
                        return true; // 点击展开状态不改变
                    }
                });

        mListView.getRefreshableView().setOnChildClickListener(
                new ExpandableListView.OnChildClickListener() {

                    @Override
                    public boolean onChildClick(ExpandableListView parent,
                                                View v, int groupPosition, int childPosition, long id) {
                        Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
                        Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                        intent.putExtra("id", order.id);
                        intent.putExtra("tag", TAG);
                        startActivity(intent);
                        return false;
                    }
                });

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ExpandableListView>() {

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

        mAdapter.setOnCancelChangedListener(new OrderAdapter.OnCancelChangedListener() {

            @Override
            public void onCancelChanged() {
                mAdapter.notifyDataSetChanged();
                expandAllChildView();
            }
        });
    }

    private void callAPIListOrder() {
        showLoading();
        hideTips();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", mCurpage + "");
        String lowerLevelUserId = ((PurchaseOrderActivity) getActivity()).getUserId();
        if (!TextUtils.isEmpty(lowerLevelUserId)) {
            parameters.put("userId", lowerLevelUserId);
        }
        APIManager.getInstance(getActivity()).listOrderByBuyerReceived(
                parameters, new Response.ErrorListener() {

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
                        hideTips();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            totalValue=response.totalMoney;
                            showOrderTatol(TAG);
                            if (mCurpage == 1) {
                                mOrderList = response.data;
                                mAdapter.setOrderList(mOrderList);
                            } else if (mCurpage > 1 && mOrderList != null) {
                                mOrderList.addAll(response.data);
                            }

                            mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
                            mAdapter.notifyDataSetChanged();
                        }
                        if (mAdapter.getGroupCount() == 0) {
                            showNoDataTips();
                            mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
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

    protected String getTitle() {
        return "已收货订单总计：";
    }
    private static String totalValue="";

    @Override
    protected String getValue() {
        return totalValue;
    }
    @Override
    public void showLoading() {
        if (mAdapter.getGroupCount() == 0) {
            super.showLoading();
        }
    }
}
