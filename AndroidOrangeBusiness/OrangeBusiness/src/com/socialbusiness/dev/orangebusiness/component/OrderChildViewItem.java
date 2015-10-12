package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesOrderDetailActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesPurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingDeliverOrderActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.math.BigDecimal;
import java.util.Hashtable;

public class OrderChildViewItem extends RelativeLayout {

    private View mAllLayout;
    private NetworkImageView mProductImage;
    private KWAutoScrollTextView mProductName;
    private TextView mProductPrice;
    private TextView mProductNum;
    private TextView mProductTotalMoney;

    private RelativeLayout mBottomLayout;
    private ImageView mSelectImage;
    private TextView mOrderNumber;
    private TextView mOrderSum;

    private View expandable_listview_order_childview_line_no_margin,expandable_listview_order_childview_line_need;

    private String tag;
    private OnOrderSelectedListener listener;
    private OnDeleteMySalesOrderCVListener deleteListener;

    public OrderChildViewItem(Context context) {
        super(context);
        initView();
    }

    public OrderChildViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        findView(inflater.inflate(R.layout.expandable_listview_order_childview, this));
    }

    private void findView(View view) {
        mAllLayout =  view.findViewById(R.id.expandable_listview_order_childview_all_layout);
        mProductImage = (NetworkImageView) view.findViewById(R.id.expandable_listview_order_childview_product_image);
        mProductName = (KWAutoScrollTextView) view.findViewById(R.id.expandable_listview_order_childview_product_name);
        mProductPrice = (TextView) view.findViewById(R.id.expandable_listview_order_childview_product_price);
        mProductNum = (TextView) view.findViewById(R.id.expandable_listview_order_childview_product_num);
        mProductTotalMoney = (TextView) view.findViewById(R.id.expandable_listview_order_childview_product_total_money);

        mBottomLayout = (RelativeLayout) view.findViewById(R.id.expandable_listview_order_childview_bottom_layout);
        mSelectImage = (ImageView) view.findViewById(R.id.expandable_listview_order_childview_select_image);
        mOrderNumber = (TextView) view.findViewById(R.id.expandable_listview_order_childview_order_number);
        mOrderSum = (TextView) view.findViewById(R.id.expandable_listview_order_childview_money_sum);

        mProductImage.setDefaultImageResId(R.drawable.ic_default_mid);
        mProductImage.setErrorImageResId(R.drawable.ic_default_mid);
        mProductImage.setScaleType(ScaleType.CENTER_CROP);

        expandable_listview_order_childview_line_no_margin=view.findViewById(R.id.expandable_listview_order_childview_line_no_margin);
        expandable_listview_order_childview_line_need=view.findViewById(R.id.expandable_listview_order_childview_line_need);
    }

    public void hideBottomViews(boolean isLastChild, String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }

        this.tag = tag;
        if (isLastChild) {
            mBottomLayout.setVisibility(View.VISIBLE);
            expandable_listview_order_childview_line_no_margin.setVisibility(View.VISIBLE);
            expandable_listview_order_childview_line_need.setVisibility(View.GONE);
            hideSelectImageByTag(tag);
        } else {
            mBottomLayout.setVisibility(View.GONE);
            expandable_listview_order_childview_line_no_margin.setVisibility(View.GONE);
            expandable_listview_order_childview_line_need.setVisibility(View.VISIBLE);
        }
    }

    private void hideSelectImageByTag(String tag) {
        if (!(tag.equals(SalesWaitingDeliverOrderActivity.TAG)||tag.equals(Waiting2PayFragment.TAG))) {
            mSelectImage.setVisibility(View.GONE);
//            mOrderNumber.setPadding(AndroidUtil.dip2px(getContext(), 10), 0, 0, 0);
        }
    }

    public void setChildViewValue(Hashtable<String, Object> productItem, final String orderId) {
        if (productItem != null) {
            Product product = (Product) productItem.get("product");
            int nums = (int) productItem.get("quantity");
            if (product == null) {
                return;
            }
            mProductImage.setImageUrl(APIManager.toAbsoluteUrl(product.getCoverImage()),
                    APIManager.getInstance(getContext()).getImageLoader());
            mProductName.setText(StringUtil.showMessage(product.name));
            mProductNum.setText(nums + "");
            mProductPrice.setText("¥" + StringUtil.getStringFromFloatKeep2(product.price));
            mProductTotalMoney.setText(StringUtil.getStringFromFloatKeep2(
                    new BigDecimal(nums).multiply(new BigDecimal(product.price)).doubleValue()));

            if (!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(orderId) && tag.equals(SalesPurchaseOrderActivity.TAG)) {
                mAllLayout.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        if (deleteListener != null) {
                            deleteListener.onDelete(orderId);
                        }
                        return false;
                    }
                });

                mAllLayout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), SalesOrderDetailActivity.class);
                        intent.putExtra("id", orderId);
                        intent.putExtra(Constant.EXTRA_KEY_TRADETYPE,
                                tag.equals(Constant.EXTRA_ORDER_FROM_SELLER)
                                        ? Order.OrderTradeType.Sell
                                        : Order.OrderTradeType.Buy);
                        getContext().startActivity(intent);
                    }
                });
            }
        }

    }

    public void setBottomValue(Order order) {
        if (order != null) {
            mOrderSum.setText(getMoneySum(order));
            mOrderNumber.setText("订单号："
                    + StringUtil.showMessage(order.code));
            if (SalesWaitingDeliverOrderActivity.TAG.equals(tag)||Waiting2PayFragment.TAG.equals(tag)) {
                doSelectOrder(order);
            }
            if (tag != null && tag.equals(SalesWaitingDeliverOrderActivity.TAG)) {
//				mSelectImage.setEnabled(order.ismerge == 1 ? false : true);
                if (order.ismerge == 1) mSelectImage.setVisibility(View.GONE);
                else mSelectImage.setEnabled(true);
            }
        }
    }

    private void doSelectOrder(Order order) {
        mSelectImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onOrderSelected();
                }
            }
        });
    }

    private String getMoneySum(Order order) {
        BigDecimal moneySum = new BigDecimal(0f);
        for (int i = 0; i < order.products.size(); i++) {
            moneySum = moneySum.add(new BigDecimal(order.products.get(i).price)
                    .multiply(new BigDecimal(order.productAmount.get(i))));
        }
        return StringUtil.getStringFromFloatKeep2(moneySum.add(new BigDecimal(order.freight)).doubleValue());
    }

    public ImageView getSelectImageView() {
        return mSelectImage;
    }

    public void setOnOrderSelectedListener(OnOrderSelectedListener l) {
        this.listener = l;
    }

    public void setOnDeleteMySalesOrderCVListener(OnDeleteMySalesOrderCVListener l) {
        this.deleteListener = l;
    }

    public interface OnOrderSelectedListener {
        public void onOrderSelected();
    }

    public interface OnDeleteMySalesOrderCVListener {
        public void onDelete(String orderId);
    }
}
