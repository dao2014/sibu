package com.socialbusiness.dev.orangebusiness.fragment.order;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesInputOrderDetailActivity;
import com.socialbusiness.dev.orangebusiness.adapter.OrderAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.MyOrderRequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.TransitionSelectorDialog;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.List;

/**
 * 未转采购
 */
public class SalseWaitingDeliverNoTransitionFrament extends BaseFragment {

    public static final String TAG = "SalesWaitingDeliverOrderActivity";

    public class OnCancelReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Constance.INTENT_ONCANCELORDER_SECCUS.equals(intent.getAction())){
                callAPIListOrder();
            }
        }
    }
    OnCancelReceiver onCancelReceiver;
    private PullToRefreshExpandableListView mListView;
    private TextView mMoneySum;
    private TextView mMergePurchase;

    private TextView total_money_title;
    private TextView total_money_value;
    TransitionSelectorDialog dialog;
    private OrderAdapter mAdapter;
    private List<Order> mOrderList;
    private int mCurpage = 1;
    private int mTotalPage;

    private boolean isRefresh = false;

    @Override
    public void setContentView(LayoutInflater inflater, ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.activity_sales_waiting_deliver_order, mLayerContextView);
        setUp();
        findView(mLayerContextView);
        setListener();
        callAPIListOrder();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setUp() {
        mAdapter = new OrderAdapter(getActivity());
        mAdapter.setTag(TAG);
        onCancelReceiver=new OnCancelReceiver();
        IntentFilter filter=new IntentFilter(Constance.INTENT_ONCANCELORDER_SECCUS);
        getActivity().registerReceiver(onCancelReceiver,filter);
    }

    private void findView(View mLayerContextView) {
        mListView = (PullToRefreshExpandableListView) mLayerContextView.findViewById(R.id.activity_sales_waiting_deliver_listview);
        mMoneySum = (TextView) mLayerContextView.findViewById(R.id.activity_sales_waiting_deliver_money);
        mMergePurchase = (TextView) mLayerContextView.findViewById(R.id.activity_sales_waiting_deliver_merge_purchase);

        mListView.getRefreshableView().setAdapter(mAdapter);
        mListView.getRefreshableView().setDividerHeight(0);
        mListView.getRefreshableView().setGroupIndicator(null);
        mMergePurchase.setEnabled(false);

        total_money_title = (TextView) mLayerContextView.findViewById(R.id.total_money_title);
        total_money_title.setText("待发货订单总计：");
        total_money_value = (TextView) mLayerContextView.findViewById(R.id.total_money_value);
        dialog = new TransitionSelectorDialog(this.getActivity());

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
                        Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
                        Intent intent = new Intent(getActivity(), SalesInputOrderDetailActivity.class);
                        intent.putExtra("id", order.id);
                        intent.putExtra("tag", TAG);
                        startActivity(intent);
                        return true;
                    }
                });
        //确认发货
        mListView.getRefreshableView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                Order order = (Order) parent.getExpandableListAdapter().getGroup(groupPosition);
                Intent intent = new Intent(getActivity(), SalesInputOrderDetailActivity.class);
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
                if (nums > 1) {    //合并采购按钮的两个状态："转采购"、"合并转采购"
                    //mMergePurchase.setText(getResources().getString(R.string.merge_turn_purchase));  //合并转采购  暂时去掉
                    mMergePurchase.setText(getResources().getString(R.string.turn_purchase));
                    mMergePurchase.setEnabled(true);
                    mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                } else if (nums == 1) {
                    mMergePurchase.setText(getResources().getString(R.string.turn_purchase));
                    mMergePurchase.setEnabled(true);
                    mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                } else {
                    mMergePurchase.setText(getResources().getString(R.string.turn_purchase));
                    mMergePurchase.setEnabled(false);
                    mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                }
                mMoneySum.setText(getTotalMoney());
            }
        });

        mMergePurchase.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.show();
                WindowManager windowManager = getActivity().getWindowManager();
                Display display = windowManager.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.width = (int)(display.getWidth()); //设置宽度

                dialog.getWindow().setAttributes(lp);
            }
        });

        dialog.setOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                switch (v.getId()) {

                    case R.id.multi_transition:
//                        ToastUtil.show(getActivity(), "批量");
                        type = MULTI_TYPE;
                        break;
                    case R.id.merge_transition:
//                        ToastUtil.show(getActivity(), "合并");
                        type = MERGE_TYPE;
                        break;

                }

                callAPIMergeOrder();

            }
        });
    }

    int type;
    public static final int MULTI_TYPE = 1;
    public static final int MERGE_TYPE = 2;

    private void callAPIListOrder() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", mCurpage + "");
        parameters.put("isMerge", "0");
        APIManager.getInstance(getActivity()).listOrderBySellerWaitingDeliver(
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
//                        hideNoData();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            clearButton();
                            total_money_value.setText(getResources().getText(R.string.yuan_symbol)+StringUtil.get2DecimalFromString(response.totalMoney));
                            if (mCurpage == 1) {
                                mOrderList = response.data;
//                                if (isRefresh) {
//                                    isRefresh = false;
//                                    mAdapter.updateOrderList(mOrderList);
//                                } else {
                                mAdapter.setOrderList(mOrderList);
//                                }
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
                            mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        } else {
                            mListView.setMode(PullToRefreshBase.Mode.BOTH);
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




    private void callAPIMergeOrder() {


        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("ids", getIds());
        parameters.put("type", type + "");
        APIManager.getInstance(getActivity()).mergeOrderByType(parameters,
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
                        ToastUtil.show(getActivity(), response.message);
                        getActivity().finish();
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

    @Override
    public void showLoading() {
        if (mAdapter.getGroupCount() == 0) {
            super.showLoading();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(onCancelReceiver);
    }
}
