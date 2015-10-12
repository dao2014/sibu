package com.socialbusiness.dev.orangebusiness.fragment.order;

import android.content.Intent;
import android.text.TextUtils;
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
import com.socialbusiness.dev.orangebusiness.activity.base.WebActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.CourseClassifyListActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.OrderSearchActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.PurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesPurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.ShoppingCartListActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.User;

import java.util.Hashtable;

public class MyPurchaseFragment extends BaseFragment {

    public static final String TAG = "MyPurchaseFragment";

    private View mMyPurchaseLayout;
    private View mMyShoppingCartLayout;
    private View mWaitingConfirmLayout;
    private View mWaiting2PayLayout;
    private View mWaitingDeliverLayout;
    private View mDeliveredLayout;
    private View mReceiptedLayout;
    private View mMyInventoryLayout;
    private View mypurchaseAcountStatementLayout;

    //    private TextView mMyPurchaseNums;
    private TextView mProductNums;
    private TextView mWaitingConfirmNums;
    private TextView mWaiting2PayNums;
    private TextView mWaitingDeliverNums;
    private TextView mDeliveredNums;
    private TextView mReceiptedNums;

    @Override
    public void setContentView(LayoutInflater inflater,
                               ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.fragment_mypurchase, mLayerContextView);
        findViews(mLayerContextView);
        setListeners();
    }

    private ShopCartDbHelper mHelper;

    private void findViews(View view) {
        mHelper = new ShopCartDbHelper(getActivity());
        mMyPurchaseLayout =  view.findViewById(R.id.fragment_mypurchase_mypurchase_layout);
        mMyShoppingCartLayout =  view.findViewById(R.id.fragment_mypurchase_my_shopping_cart_layout);
        mWaitingConfirmLayout =  view.findViewById(R.id.fragment_mypurchase_waiting_confirm_order_layout);
        mWaiting2PayLayout =  view.findViewById(R.id.fragment_mypurchase_waiting_2pay_order_layout);
        mWaitingDeliverLayout =  view.findViewById(R.id.fragment_mypurchase_waiting_deliver_order_layout);
        mDeliveredLayout =  view.findViewById(R.id.fragment_mypurchase_done_deliver_order_layout);
        mMyInventoryLayout =  view.findViewById(R.id.fragment_mypurchase_my_inventory_layout);
        mReceiptedLayout =  view.findViewById(R.id.fragment_mypurchase_receipted_order_layout);
        mypurchaseAcountStatementLayout = view.findViewById(R.id.fragment_mypurchase_acount_statement_layout);

//        mMyPurchaseNums = (TextView) view.findViewById(R.id.fragment_mypurchase_mypurchase_nums);
        mProductNums = (TextView) view.findViewById(R.id.fragment_mypurchase_my_shopping_cart_nums);
        mWaitingConfirmNums = (TextView) view.findViewById(R.id.fragment_mypurchase_waiting_confirm_order_nums);
        mWaiting2PayNums = (TextView) view.findViewById(R.id.fragment_mypurchase_waiting_2pay_order_nums);
        mWaitingDeliverNums = (TextView) view.findViewById(R.id.fragment_mypurchase_waiting_deliver_order_nums);
        mDeliveredNums = (TextView) view.findViewById(R.id.fragment_mypurchase_done_deliver_order_nums);
        mReceiptedNums = (TextView) view.findViewById(R.id.fragment_mypurchase_receipted_order_nums);

        mProductNums.setVisibility(View.GONE);
        mWaitingConfirmNums.setVisibility(View.GONE);
        mWaiting2PayNums.setVisibility(View.GONE);
        mWaitingDeliverNums.setVisibility(View.GONE);
        mDeliveredNums.setVisibility(View.GONE);
        mReceiptedNums.setVisibility(View.GONE);
        hideMyPurchaseYN();

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
        mTitleImage = (ImageView)view.findViewById(R.id.view_header_mBtnTitle);
        showBackBtn(false);
        setTitle(R.string.buyer_function);
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

    private void hideMyPurchaseYN() {
        User loginUser = SettingsManager.getLoginUser();
        if (loginUser != null) {
            if (TextUtils.isEmpty(loginUser.upUserId) || Constant.UPEST_DEFAULT_USERID.equals(loginUser.upUserId)) {
                mMyPurchaseLayout.setVisibility(View.VISIBLE);
            } else {
                mMyPurchaseLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setListeners() {
        OnClickListener listeners = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (v == mMyShoppingCartLayout) { //我的购物车
                    intent.setClass(getActivity(), ShoppingCartListActivity.class);

                } else if (v == mMyPurchaseLayout) { //我的采购
                    intent.setClass(getActivity(), SalesPurchaseOrderActivity.class);
                    intent.putExtra(Constant.EXTRA_ORDER_FROM, Constant.EXTRA_ORDER_FROM_BUYER);

                } else if (v == mWaitingConfirmLayout) { //待确认
                    intent.setClass(getActivity(), PurchaseOrderActivity.class);
                    intent.putExtra("TAG", WaitingConfirmFragment.TAG);

                } else if (v == mWaiting2PayLayout) { //待付款
                    intent.setClass(getActivity(), PurchaseOrderActivity.class);
                    intent.putExtra("TAG", Waiting2PayFragment.TAG);

                } else if (v == mWaitingDeliverLayout) { // 待发货
                    intent.setClass(getActivity(), PurchaseOrderActivity.class);
                    intent.putExtra("TAG", WaitingDeliverFragment.TAG);

                } else if (v == mDeliveredLayout) { // 已发货
                    intent.setClass(getActivity(), PurchaseOrderActivity.class);
                    intent.putExtra("TAG", DeliverDoneFragment.TAG);

                } else if (v == mReceiptedLayout) { //已收货
                    intent.setClass(getActivity(), PurchaseOrderActivity.class);
                    intent.putExtra("TAG", ReceiptedFragment.TAG);

                } else if (v == mMyInventoryLayout) {  //库存
                    intent.setClass(getActivity(), CourseClassifyListActivity.class);
                    intent.putExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, Product.ProductType.KWMProductTypeStock.getIntValue());
                } else if (v == mypurchaseAcountStatementLayout) {  //对账单
                    intent.setClass(getActivity(), WebActivity.class);
                    intent.putExtra(Constance.WEB_ACTIVITY_TITLE, "采购对账单");
                    User user = SettingsManager.getLoginUser();
                    intent.putExtra(Constance.WEB_DETAIL, String.format(APIManager.STATE_ACOUNT, user.companyId, user.id, user.upUserId));
                }

                getActivity().startActivity(intent);
            }
        };

        mMyPurchaseLayout.setOnClickListener(listeners);
        mMyShoppingCartLayout.setOnClickListener(listeners);
        mWaitingConfirmLayout.setOnClickListener(listeners);
        mWaiting2PayLayout.setOnClickListener(listeners);
        mWaitingDeliverLayout.setOnClickListener(listeners);
        mDeliveredLayout.setOnClickListener(listeners);
        mReceiptedLayout.setOnClickListener(listeners);
        mMyInventoryLayout.setOnClickListener(listeners);
        mypurchaseAcountStatementLayout.setOnClickListener(listeners);
    }

    /**
     * 已收货商品数量
     *
     * @param nums int
     */
    public void setReceiptedNums(int nums) {
        if (mReceiptedNums != null) {
            if (nums > 0) {
                mReceiptedNums.setText(nums + "");
                mReceiptedNums.setVisibility(View.VISIBLE);
            } else {
                mReceiptedNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 购物车中商品数量
     *
     * @param nums int
     */
    public void setProductNums(int nums) {
        if (mProductNums != null) {
            if (nums > 0) {
                mProductNums.setText(nums + "");
                mProductNums.setVisibility(View.VISIBLE);
            } else {
                mProductNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 我的采购，待确认订单商品数量
     *
     * @param nums int
     */
    public void setWaitingConfirmOrderNums(int nums) {
        if (mWaitingConfirmNums != null) {
            if (nums > 0) {
                mWaitingConfirmNums.setText(nums + "");
                mWaitingConfirmNums.setVisibility(View.VISIBLE);
            } else {
                mWaitingConfirmNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 我的采购，待付款订单数量
     *
     * @param nums
     */
    public void setWaiting2PayOrderNums(int nums) {
        if (mWaiting2PayNums != null) {
            if (nums > 0) {
                mWaiting2PayNums.setText(nums + "");
                mWaiting2PayNums.setVisibility(View.VISIBLE);
            } else {
                mWaiting2PayNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 我的采购，待发货订单数量
     *
     * @param nums
     */
    public void setWaitingDeliverOrderNums(int nums) {
        if (mWaitingDeliverNums != null) {
            if (nums > 0) {
                mWaitingDeliverNums.setText(nums + "");
                mWaitingDeliverNums.setVisibility(View.VISIBLE);
            } else {
                mWaitingDeliverNums.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 我的采购，已发货订单数量
     *
     * @param nums
     */
    public void setDeliveredOrderNums(int nums) {
        if (mDeliveredNums != null) {
            if (nums > 0) {
                mDeliveredNums.setText(nums + "");
                mDeliveredNums.setVisibility(View.VISIBLE);
            } else {
                mDeliveredNums.setVisibility(View.GONE);
            }
        }
    }




    OnClickListener seachOnclick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getMainActivity(),
                    OrderSearchActivity.class);
            intent.putExtra(Constance.ISSELLER, 0);
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

//        if (mHelper != null && mHelper.getShopCartItemsByUserId(user.id) != null) {
//            setProductNums(mHelper.getShopCartItemsByUserId(user.id).size());
//        }
        setWaitingConfirmOrderNums(user.buyerWaitingOrderCount);
        setWaiting2PayOrderNums(user.buyerWaitingPayOrderCount);
        setWaitingDeliverOrderNums(user.buyerShopOrderCount);
        setDeliveredOrderNums(user.buyerShipOrderCount);
        setReceiptedNums(user.buyerClosedOrderCount);
    }
}
