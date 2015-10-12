package com.socialbusiness.dev.orangebusiness.component;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.OrderSearchActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesOrderDetailActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesPurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingConfirmOrderActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingConfirmFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;

public class OrderGroupViewItem extends LinearLayout {

    private LinearLayout mAllLayout;
    private TextView mGroupViewSeller;
    private TextView mGroupViewTime;
    private TextView orderStateTv;
    private TextView mCancelOrder;
    private Order order;
    private Context context;
    private String tag;

    private OnCancelDoneListener cancelListener;
    private OnDeleteMySalesOrderGVLisener deleteListener;

    public OrderGroupViewItem(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public OrderGroupViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        findView(inflater.inflate(R.layout.expandable_listview_order_groupview, this));
    }

    private void findView(View view) {
        mAllLayout = (LinearLayout) view.findViewById(R.id.expandable_listview_order_groupview_all_layout);
        mGroupViewSeller = (TextView) view.findViewById(R.id.expandable_listview_order_groupview_seller);
        mGroupViewTime = (TextView) view.findViewById(R.id.expandable_listview_order_groupview_time);
        mCancelOrder = (TextView) view.findViewById(R.id.expandable_listview_order_groupview_cancel_order);
        orderStateTv = (TextView) view.findViewById(R.id.expandable_listview_order_groupview_order_state);
        mCancelOrder.setVisibility(View.GONE);
    }

    public void setValueByTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        this.tag = tag;

        if (tag.endsWith("Fragment")) {
            mGroupViewSeller.setText("卖家：");
        }

        if (tag.equals(WaitingConfirmFragment.TAG)) {
            mGroupViewTime.setVisibility(View.GONE);
            mCancelOrder.setVisibility(View.VISIBLE);
//            setCancelOrderTextColor();

        } else if (!tag.equals(SalesWaitingConfirmOrderActivity.TAG)) {
            mGroupViewTime.setText("运费：¥");
            if (tag.equals(SalesPurchaseOrderActivity.TAG)
                    && !tag.equals(Constant.EXTRA_ORDER_FROM_SELLER)) {
                //我的采购
                mGroupViewSeller.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setCancelOrderTextColor() {
        ColorStateList csl = (ColorStateList) getResources()
                .getColorStateList(R.color.sl_cancel_order_txt_color);
        if (csl != null) {
            mCancelOrder.setTextColor(csl);
        }
    }

    public void setGroupViewValue(final Order groupView) {
        if (groupView != null && !TextUtils.isEmpty(tag)) {
            this.order = groupView;
            if (tag.endsWith("Fragment")) {
                if (groupView.user != null) {
                    mGroupViewSeller.setText("卖家：" + StringUtil.showMessage(groupView.user.nickName));
                }
                if (tag.equals(WaitingConfirmFragment.TAG)) {
                    setCancelOrderListener();
                } else {
                    mGroupViewTime.setText("运费：¥" + StringUtil.getStringFromFloatKeep2(groupView.freight));
                }

            } else {
                if (groupView.user != null) {
                    mGroupViewSeller.setText("买家：" +  StringUtil.showMessage(groupView.user.nickName));
                    if (tag.equals(OrderSearchActivity.TAG) && OrderSearchActivity.isSeller == 0) {
                        mGroupViewSeller.setText("卖家：" + StringUtil.showMessage(groupView.user.nickName));
                    }
                }


                if (tag.equals(SalesWaitingConfirmOrderActivity.TAG) || tag.equals(OrderSearchActivity.TAG)) {
                    mGroupViewTime.setText("下单时间：" + StringUtil.showMessage(groupView.createTime));
                    if(tag.equals(OrderSearchActivity.TAG)){
                        mGroupViewTime.setText( groupView.status);
                        mGroupViewTime.setTextColor(getResources().getColor(R.color.red_product_price));
                    }else{
                        mGroupViewTime.setTextColor(getResources().getColor(R.color.text_color));
                    }

                } else {
                    mGroupViewTime.setText("运费：¥" + StringUtil.getStringFromFloatKeep2(groupView.freight));
                }
            }

            if (!TextUtils.isEmpty(tag) && tag.equals(SalesPurchaseOrderActivity.TAG)) { //我的零售或我的采购
                mAllLayout.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        if (deleteListener != null) {
                            deleteListener.onDelete(groupView.id);
                        }
                        return true;
                    }
                });

                mAllLayout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), SalesOrderDetailActivity.class);
                        intent.putExtra("id", groupView.id);
                        intent.putExtra(Constant.EXTRA_KEY_TRADETYPE,
                                tag.equals(Constant.EXTRA_ORDER_FROM_SELLER)
                                        ? Order.OrderTradeType.Sell
                                        : Order.OrderTradeType.Buy);
                        getContext().startActivity(intent);
                    }
                });
            }

            if (!TextUtils.isEmpty(groupView.payimage) && tag.equals(Waiting2PayFragment.TAG)) {
                orderStateTv.setVisibility(View.VISIBLE);
            } else {
                orderStateTv.setVisibility(View.GONE);
            }
        }
    }

    private void setCancelOrderListener() {
        if (order != null) {
            mCancelOrder.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Dialog cancelDialog = new AlertDialog.Builder(context)
                            .setMessage("确定要取消订单吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    callAPICancelOrder();

                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create();
                    Window window = cancelDialog.getWindow();
                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.alpha = 0.8f;
                    window.setAttributes(lp);
                    cancelDialog.show();
                }
            });
        }
    }

    private void callAPICancelOrder() {
        if (context != null) {
            ((BaseActivity) context).showLoading();
            Hashtable<String, String> parameters = new Hashtable<String, String>();
            parameters.put("ids", order.id);
            parameters.put("isSeller", "0");
            APIManager.getInstance(getContext()).cancelOrder(parameters,
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            ((BaseActivity) context).hideLoading();
                            ((BaseActivity) context).showException(error);
                        }
                    }, new Response.Listener<RequestResult<?>>() {

                        @Override
                        public void onResponse(RequestResult<?> response) {
                            ((BaseActivity) context).hideLoading();
                            if (((BaseActivity) context).hasError(response)) {
                                return;
                            }
                            if (cancelListener != null) {
                                cancelListener.onCancel(order.id);
                                ToastUtil.show(getContext(), response.message);
                            }
                        }
                    });
        }
    }

    public void setOnCancelDoneListener(OnCancelDoneListener l) {
        cancelListener = l;
    }

    public void setOnDeleteMySalesOrderListener(OnDeleteMySalesOrderGVLisener l) {
        this.deleteListener = l;
    }

    public interface OnCancelDoneListener {
        public void onCancel(String id);
    }

    public interface OnDeleteMySalesOrderGVLisener {
        public void onDelete(String id);
    }
}
