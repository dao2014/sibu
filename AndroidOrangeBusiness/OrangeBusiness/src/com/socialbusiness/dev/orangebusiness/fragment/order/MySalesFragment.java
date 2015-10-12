package com.socialbusiness.dev.orangebusiness.fragment.order;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.me.VipListActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.OrderSearchActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.ReceiptedActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesDeliveredOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesPurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingConfirmOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalseWaiting2PayOrderTabActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalseWaitingDeliverTabActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;

import java.util.Hashtable;

public class MySalesFragment extends BaseFragment {

    public static final String TAG = "MySalesFragment";
    public static final int TAG_INT = 2000;

    private View mMySalesOrderLayout;
    private View mWaitingConfirmLayout;
    private View mWaiting2PayLayout;
    private View mWaitingDeliverLayout;
    private View mDeliveredLayout;
    private View mReceiptedLayout;
    private View mysalesAcountStatementLayout;

    private TextView mMySalesOrderNum;
    private TextView mWaitingConfirmNums;
    private TextView mWaiting2PayNums;
    private TextView mWaitingDeliverNums;
    private TextView mDeliveredNums;
    private TextView mReceiptedNums;
    private TextView mMonthDeliverNums;
    private TextView mMonthDeliverSum;
    private TextView mMonthProfitNums;

    @Override
    public void setContentView(LayoutInflater inflater,
                               ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.fragment_mysales, mLayerContextView);
        findViews(mLayerContextView);
        setListeners();
    }


    private void findViews(View view) {
        mMySalesOrderLayout = view.findViewById(R.id.fragment_mysales_mysales_order_layout);
        mWaitingConfirmLayout = view.findViewById(R.id.fragment_mysales_waiting_confirm_order_layout);
        mWaiting2PayLayout =  view.findViewById(R.id.fragment_mysales_waiting_2pay_order_layout);
        mWaitingDeliverLayout =  view.findViewById(R.id.fragment_mysales_waiting_deliver_order_layout);
        mDeliveredLayout = view.findViewById(R.id.fragment_mysales_done_deliver_order_layout);
        mReceiptedLayout =  view.findViewById(R.id.fragment_mysales_receipted_order_layout);
        mysalesAcountStatementLayout = view.findViewById(R.id.fragment_mysales_acount_statement_layout);

        mMySalesOrderNum = (TextView) view.findViewById(R.id.fragment_mysales_mysales_order_nums);
        mWaitingConfirmNums = (TextView) view.findViewById(R.id.fragment_mysales_waiting_confirm_order_nums);
        mWaiting2PayNums = (TextView) view.findViewById(R.id.fragment_mysales_waiting_2pay_order_nums);
        mWaitingDeliverNums = (TextView) view.findViewById(R.id.fragment_mysales_waiting_deliver_order_nums);
        mDeliveredNums = (TextView) view.findViewById(R.id.fragment_mysales_done_deliver_order_nums);
        mReceiptedNums = (TextView) view.findViewById(R.id.fragment_mysales_receipted_order_nums);

        mMonthDeliverNums = (TextView) view.findViewById(R.id.fragment_mysales_month_deliver_numsTV);
        mMonthDeliverSum = (TextView) view.findViewById(R.id.fragment_mysales_month_deliver_sumTV);
        mMonthProfitNums = (TextView) view.findViewById(R.id.fragment_mysales_month_profit_sumTV);

        mMySalesOrderNum.setVisibility(View.GONE);
        mWaitingConfirmNums.setVisibility(View.GONE);
        mWaiting2PayNums.setVisibility(View.GONE);
        mWaitingDeliverNums.setVisibility(View.GONE);
        mDeliveredNums.setVisibility(View.GONE);
        mReceiptedNums.setVisibility(View.GONE);
        initTitle(view);
    }
    private ImageView mBtnLeft;
    private TextView mTVLeftLabel;
    private TextView mTxtTitle;
    private ImageView mTitleImage;
    private ImageView mBtnRight;
    private TextView mTxtRight;
    LinearLayout mLyerLeft;
    private void initTitle(View view) {
        mLyerLeft = (LinearLayout) view.findViewById(R.id.view_header_mLyerLeft);
        mBtnLeft = (ImageView) view.findViewById(R.id.view_header_mBtnLeft);
        mBtnRight = (ImageView) view.findViewById(R.id.view_header_mBtnRight);
        mTVLeftLabel = (TextView) view.findViewById(R.id.view_header_mTVLeftLabel);
        mTxtRight = (TextView) view.findViewById(R.id.view_header_mTxtRight);
        mTxtTitle = (TextView) view.findViewById(R.id.view_header_mTxtTitle);
        mTitleImage = (ImageView) view.findViewById(R.id.view_header_mBtnTitle);
        showBackBtn(false);
        setTitle(R.string.seller_function);
        setRightIcon(R.drawable.ic_search_api_holo_dark, seachOnclick
        );
    }
    public void showBackBtn(boolean isShow){
        if(isShow){
            mLyerLeft.setVisibility(View.VISIBLE);
        }else{
            mLyerLeft.setVisibility(View.INVISIBLE);
        }
    }
    public void setTitle(int titleId){
        mTitleImage.setVisibility(View.GONE);
        mTxtTitle.setVisibility(View.VISIBLE);
        this.mTxtTitle.setText(titleId);
    }

    public void setRightIcon(int resId, OnClickListener listener){
        mBtnRight.setImageResource(resId);
        mBtnRight.setOnClickListener(listener);
        showRightBtn(true);
    }
    public void showRightBtn(boolean isShow) {
        if(isShow){
            mBtnRight.setVisibility(View.VISIBLE);
        }else{
            mBtnRight.setVisibility(View.INVISIBLE);
        }
    }

    private void setListeners() {
        OnClickListener listeners = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (v == mMySalesOrderLayout) {
                    intent.setClass(getActivity(), SalesPurchaseOrderActivity.class);
                    intent.putExtra(Constant.EXTRA_ORDER_FROM, Constant.EXTRA_ORDER_FROM_SELLER);

                } else if (v == mWaitingConfirmLayout) {
                    intent.setClass(getActivity(), SalesWaitingConfirmOrderActivity.class);  //待确认

                } else if (v == mWaiting2PayLayout) {
//                    intent.setClass(getActivity(), SalesWaiting2PayOrderActivity.class);
                    intent.setClass(getActivity(), SalseWaiting2PayOrderTabActivity.class);  //待付款

                } else if (v == mWaitingDeliverLayout) {
//                    intent.setClass(getActivity(), SalesWaitingDeliverOrderActivity.class);
                    intent.setClass(getActivity(), SalseWaitingDeliverTabActivity.class);   //待发货


                } else if (v == mDeliveredLayout) {
                    intent.setClass(getActivity(), SalesDeliveredOrderActivity.class);   //已发货

                } else if (v == mReceiptedLayout) {
                    intent.setClass(getActivity(), ReceiptedActivity.class);   //已收货
                } else if (v == mysalesAcountStatementLayout) {
                    intent.setClass(getActivity(), VipListActivity.class);  //我的客户
                    intent.putExtra(Constance.COMEFROM, TAG_INT);
                }

                getActivity().startActivity(intent);
            }
        };

        mMySalesOrderLayout.setOnClickListener(listeners);
        mWaitingConfirmLayout.setOnClickListener(listeners);
        mWaiting2PayLayout.setOnClickListener(listeners);
        mWaitingDeliverLayout.setOnClickListener(listeners);
        mDeliveredLayout.setOnClickListener(listeners);
        mReceiptedLayout.setOnClickListener(listeners);
        mysalesAcountStatementLayout.setOnClickListener(listeners);
    }

    /**
     * 已收货数量
     *
     * @param num int
     */
    public void setReceiptedNums(int num) {
        if (mReceiptedNums != null) {
            if (num > 0) {
                mReceiptedNums.setText(num + "");
                mReceiptedNums.setVisibility(View.VISIBLE);
            } else {
                mReceiptedNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 待确认订单数量
     *
     * @param num int
     */
    public void setWaitingConfirmOrderNum(int num) {
        if (mWaitingConfirmNums != null) {
            if (num > 0) {
                mWaitingConfirmNums.setText(num + "");
                mWaitingConfirmNums.setVisibility(View.VISIBLE);
            } else {
                mWaitingConfirmNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 待付款订单数量
     *
     * @param num int
     */
    public void setWaiting2PayOrderNum(int num) {
        if (mWaiting2PayNums != null) {
            if (num > 0) {
                mWaiting2PayNums.setText(num + "");
                mWaiting2PayNums.setVisibility(View.VISIBLE);
            } else {
                mWaiting2PayNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 待发货订单数量
     *
     * @param num int
     */
    public void setWaitingDeliverOrderNum(int num) {
        if (mWaitingDeliverNums != null) {
            if (num > 0) {
                mWaitingDeliverNums.setText(num + "");
                mWaitingDeliverNums.setVisibility(View.VISIBLE);
            } else {
                mWaitingDeliverNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 已发货订单数量
     *
     * @param num int
     */
    public void setDeliveredOrderNum(int num) {
        if (mDeliveredNums != null) {
            if (num > 0) {
                mDeliveredNums.setText(num + "");
                mDeliveredNums.setVisibility(View.VISIBLE);
            } else {
                mDeliveredNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 当月出货数量
     *
     * @param num int
     */
    public void setMonthDeliverNum(float num) {
//        if (mMonthDeliverNums != null) {
//            mMonthDeliverNums.setText(num + "");
//        }
        if (num > 0 && mMonthDeliverNums != null) {
//            mMonthDeliverNums.setText( StringUtil.getStringFromFloatKeep2(num));
            mMonthDeliverNums.setText( subZeroAndDot(String.valueOf(num)));
        }
    }
    /**
     * 使用java正则表达式去掉多余的.与0
     * @param s
     * @return
     */
    public  String subZeroAndDot(String s){
        if(s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }
    /**
     * 当月出货金额
     *
     * @param num float
     */
    public void setMonthDeliverSum(float num) {
        if (num > 0 && mMonthDeliverSum != null) {
//            mMonthDeliverSum.setText( StringUtil.getStringFromFloatKeep2(num));
            mMonthDeliverSum.setText( subZeroAndDot(String.valueOf(num)));
        }
    }

    /**
     * 当月利润
     *
     * @param num float
     */
    public void setMonthProfitSum(float num) {
        if (num > 0 && mMonthProfitNums != null) {
//            mMonthProfitNums.setText(""+num);
//            mMonthProfitNums.setText( StringUtil.getStringFromFloatKeep2(num));
            mMonthProfitNums.setText( subZeroAndDot(String.valueOf(num)));
        }
    }

    @Override
    public void showFragment() {
        super.showFragment();
        showRightBtn();
//        ToastUtil.show(getActivity(),"显示");
    }


    private void showRightBtn() {
        getMainActivity().setRightIcon(R.drawable.ic_search_api_holo_dark, seachOnclick
        );
    }

    OnClickListener seachOnclick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getMainActivity(),
                    OrderSearchActivity.class);
            intent.putExtra(Constance.ISSELLER, 1);
            startActivityForResult(intent, 0);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        callAPIUserRefresh();
    }

    private void callAPIUserRefresh() {
        Hashtable<String, String> parameters = new Hashtable<>();
        APIManager.getInstance(getActivity()).userRefresh(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<User>>() {

                    @Override
                    public void onResponse(RequestResult<User> response) {
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            SettingsManager.saveLoginUserWithoutSetJPush(response.data);
                            setOrderData(response.data);
                        }
                    }
                });
    }

    public void setOrderData(User user) {

        setWaitingConfirmOrderNum(user.sellerWaitingOrderCount);
        setWaiting2PayOrderNum(user.sellerWaitingPayOrderCount);
        setWaitingDeliverOrderNum(user.sellerShopOrderCount);
        setDeliveredOrderNum(user.sellerShipOrderCount);

        setMonthDeliverNum(user.monthlyOrderMoney);
        setMonthDeliverSum(user.monthlyShipMoney);
        setMonthProfitSum(user.totalOrderMoney);

        setReceiptedNums(user.sellerClosedOrderCount);
    }
}
