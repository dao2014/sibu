package com.socialbusiness.dev.orangebusiness.fragment.order;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.socialbusiness.dev.orangebusiness.activity.order.MultiUploadPayImageActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.OrderDetailActivity;
import com.socialbusiness.dev.orangebusiness.adapter.OrderAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.MyOrderRequestListResult;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Waiting2PayFragment extends BaseFragment {

    public static final String TAG = "Waiting2PayFragment";

    private OrderAdapter mAdapter;
    private List<Order> mOrderList;
    private int mCurPage = 1;
    private int mTotalPage;
    private PullToRefreshExpandableListView mListView;

    @Override
    public void setContentView(LayoutInflater inflater,
                               ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.fm_order_buyer_content, mLayerContextView);
        findViews(mLayerContextView);
        setListeners();
        callAPIListOrder();
    }

    private TextView mMoneySum;
    private TextView mMergePurchase;

    private void findViews(View view) {
        mListView = (PullToRefreshExpandableListView) view.findViewById(R.id.fm_order_buyer_content_listview);
        mAdapter = new OrderAdapter(getActivity());
        mAdapter.setTag(TAG);

        mListView.getRefreshableView().setAdapter(mAdapter);
        mListView.getRefreshableView().setDividerHeight(0);
        mListView.getRefreshableView().setGroupIndicator(null);

        mMoneySum = (TextView) view.findViewById(R.id.activity_sales_waiting_deliver_money);
        mMergePurchase = (TextView) view.findViewById(R.id.activity_sales_waiting_deliver_merge_purchase);
        mMergePurchase.setEnabled(false);
        view.findViewById(R.id.activity_sales_waiting_deliver_bottom_layout).setVisibility(View.VISIBLE);
    }

    private void setListeners() {
        mListView.getRefreshableView().setOnGroupClickListener(
                new OnGroupClickListener() {

                    @Override
                    public boolean onGroupClick(ExpandableListView parent,
                                                View v, int groupPosition, long id) {
                        Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
                        Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                        intent.putExtra("id", order.id);
                        intent.putExtra("tag", TAG);
                        startActivity(intent);
                        return true; // 点击展开状态不改变
                    }
                });

        mListView.getRefreshableView().setOnChildClickListener(
                new OnChildClickListener() {

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

        mListView.setOnRefreshListener(new OnRefreshListener2<ExpandableListView>() {

            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ExpandableListView> refreshView) {
                mCurPage = 1;
                callAPIListOrder();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ExpandableListView> refreshView) {
                if (mCurPage < mTotalPage && mOrderList != null) {
                    mCurPage++;
                    callAPIListOrder();
                }
            }
        });

        mAdapter.setOnSelectStatusChangedListener(new OrderAdapter.OnSelectStatusChangedListener() {

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
                if (nums > 0) {    //合并采购按钮的两个状态："转采购"、"合并转采购"
                    mMergePurchase.setEnabled(true);
                    mListView.setMode(Mode.DISABLED);
                } else {
                    mMergePurchase.setEnabled(false);
                    mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                }
                mMoneySum.setText(getTotalMoney());
            }
        });

        mMergePurchase.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MultiUploadPayImageActivity.class);
                Log.e("===", "==" + getArrayIds());
                intent.putExtra(Constance.OrderIds, getArrayIds());
                intent.putExtra(Constance.TotalFee,mMoneySum.getText().toString());
                intent.putExtra(Constance.TransFee,totalTransfee);
                getActivity().startActivity(intent);
            }
        });
    }

    private String totalTransfee="";
    /**
     * @return 被选中订单的总金额
     */
    private String getTotalMoney() {
        List<Boolean> selectList = mAdapter.getIsSelectedList();
        if (selectList == null) {
            return "";
        }
        BigDecimal money = new BigDecimal(0f);
        BigDecimal transfee = new BigDecimal(0f);
        for (int i = 0; i < selectList.size(); i++) {
            if (selectList.get(i)) {
                money = money.add(getOrderTotalMoney(mOrderList.get(i)));
                transfee=transfee.add(new BigDecimal(mOrderList.get(i).freight));
            }
        }
        totalTransfee=StringUtil.getStringFromFloatKeep2(transfee.doubleValue());
        return StringUtil.getStringFromFloatKeep2(money.doubleValue());
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


    private ArrayList<String> getArrayIds() {
        ArrayList<String> ids = new ArrayList<>();
        List<Boolean> selectList = mAdapter.getIsSelectedList();
        if (selectList == null) {
            return ids;
        }
        for (int i = 0; i < selectList.size(); i++) {
            if (selectList.get(i)) {
                ids.add(mOrderList.get(i).id);
            }
        }
        return ids;
    }


    /**
     * 得到指定订单的商品总金额
     *
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

    private void callAPIListOrder() {
        showLoading();
        hideTips();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", mCurPage + "");
        APIManager.getInstance(getActivity()).listOrderByBuyerWaitingPay(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);
                        mListView.onRefreshComplete();
                    }
                },
                new Response.Listener<MyOrderRequestListResult<Order>>() {

                    @Override
                    public void onResponse(MyOrderRequestListResult<Order> response) {
                        hideLoading();
                        if (hasError(response)) {
                            return;
                        }
                        clearButton();
                        if (response.data != null) {
                            totalValue = response.totalMoney;
                            showOrderTatol(TAG);
                            if (mCurPage == 1) {
                                mOrderList = response.data;
                                mAdapter.setOrderList(mOrderList);
                            } else if (mCurPage > 1 && mOrderList != null) {
                                mOrderList.addAll(response.data);
                                mAdapter.addOrderListSelected(response.data.size());
                            }
                            mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
                            mAdapter.notifyDataSetChanged();
                        }
                        if (mAdapter.getGroupCount() == 0) {
                            showNoDataTips();
                        } else if (mCurPage >= mTotalPage) {
                            mListView.setMode(Mode.PULL_FROM_START);
                        } else {
                            mListView.setMode(Mode.BOTH);
                        }
                        expandAllChildView();
                        mListView.onRefreshComplete();
                    }
                });
    }

    private void clearButton() {
        mMergePurchase.setEnabled(false);
        mMoneySum.setText("0.00");
    }

    protected String getTitle() {
        return "待付款订单总计：";
    }

    private static String totalValue = "";

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

    private void expandAllChildView() {
        for (int i = 0; i < mListView.getRefreshableView()
                .getExpandableListAdapter().getGroupCount(); i++) {
            mListView.getRefreshableView().collapseGroup(i);
            mListView.getRefreshableView().expandGroup(i);
        }
    }
}
