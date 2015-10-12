package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.kollway.android.imageviewer.activity.ImageViewerActivity;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingConfirmOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingDeliverOrderActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

public class OrderConfirmFooter extends LinearLayout {

    private EditText mTransportFee;
    private TextView mMoneySum;
    private TextView bottom_total_fee;
    private TextView mCreateTime;    //下单时间
    private TextView mOrderNumber;    //订单号
    private EditText mFreightWay;
    private TextView mBuyerRemark;
    private EditText mSellerRemark;

    private View mVoucherLayout;
    //    private NetworkImageView mVoucherImage;
    private EditText mPayMoneyET;
    private LinearLayout mFreightStartNumberLayout;
    private EditText mFreightStartNumber;    //货运起始单号
    private LinearLayout mFreightEndNumberLayout;
    private EditText mFreightEndNumber;        //货运终止单号

    private ImageView mStartQrCode;    //扫描货运起始单号
    private ImageView mEndQrCode;    //扫描货运终止单号
    private EditText paysMoneyRemarkEdittext;
    private TextView mConfirmBtn;

    private Order mOrder;
    private OnClickConfirmListener confirmListener;
    private OnFeeChangedListener feeListener;
    private OnClickScanExpressCodeListener mClickScanExpressCodeListener;

    private String tag;

    public OrderConfirmFooter(Context context) {
        super(context);
        initView();
    }

    public OrderConfirmFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View footerView = inflater.inflate(R.layout.view_list_footer, this);
        findView(footerView);
    }

    private void findView(View view) {
        mTransportFee = (EditText) view.findViewById(R.id.view_list_footer_transport_fee);
        mMoneySum = (TextView) view.findViewById(R.id.view_list_footer_money_sum);
        mCreateTime = (TextView) view.findViewById(R.id.view_list_footer_createtime);
        mOrderNumber = (TextView) view.findViewById(R.id.view_list_footer_order_number);
        mFreightWay = (EditText) view.findViewById(R.id.view_list_footer_freight_way);
        mBuyerRemark = (TextView) view.findViewById(R.id.view_list_footer_buyer_remark);
        mSellerRemark = (EditText) view.findViewById(R.id.view_list_footer_seller_remark);
        mVoucherLayout = view.findViewById(R.id.view_list_footer_vouchers_layout);
//        mVoucherImage = (NetworkImageView) view.findViewById(R.id.view_list_footer_vouchers_image);
        mPayMoneyET = (EditText) view.findViewById(R.id.view_list_footer_pays_money_edittext);

        mFreightStartNumberLayout = (LinearLayout) view.findViewById(R.id.view_list_footer_freight_start_number_layout);
        mFreightStartNumber = (EditText) view.findViewById(R.id.view_list_footer_freight_start_num);
        mFreightEndNumberLayout = (LinearLayout) view.findViewById(R.id.view_list_footer_freight_number_end_layout);
        mFreightEndNumber = (EditText) view.findViewById(R.id.view_list_footer_freight_end_num);
        mStartQrCode = (ImageView) view.findViewById(R.id.view_list_footer_freight_start_num_image);
        mEndQrCode = (ImageView) view.findViewById(R.id.view_list_footer_freight_end_num_image);

        mConfirmBtn = (TextView) view.findViewById(R.id.view_list_footer_bottom_btn);

        paysMoneyRemarkEdittext = (EditText) view.findViewById(R.id.order_detail_footerview_pays_money_remark_edittext);

        bottom_total_fee = (TextView) view.findViewById(R.id.bottom_total_fee);
        findVouchers();
//        mVoucherImage.setDefaultImageResId(R.drawable.default_pic);
//        mVoucherImage.setErrorImageResId(R.drawable.default_pic);
    }

    ArrayList<NetworkImageView> images = new ArrayList<>();
    ArrayList<View> deletes = new ArrayList<>();

    private void findVouchers() {

        for (int i = 0; i < 6; i++) {
            NetworkImageView image = (NetworkImageView) findViewById(R.id.order_detail_footerview_pays_voucher_image + i * 2);
            image.setTag(i);
            images.add(image);

            View b = findViewById(R.id.order_detail_footerview_pays_voucher_image + i * 2 + 1);
            b.setTag(i);
            b.setVisibility(View.GONE);
            deletes.add(b);
//            Log.e("====", "" + b);
        }
        ViewTreeObserver vto = images.get(0).getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                images.get(0).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int size = images.size();
                for (int i = 0; i < size; i++) {
                    ImageView iv = images.get(i);
                    ViewGroup.LayoutParams lp = iv.getLayoutParams();
                    lp.height = images.get(0).getWidth();
                    iv.setLayoutParams(lp);
                }
            }
        });
    }

    public void setTagToHide(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        this.tag = tag;
        if (SalesWaitingConfirmOrderActivity.TAG.equals(tag)) {//确认订单
            mVoucherLayout.setVisibility(View.GONE);
            mFreightStartNumberLayout.setVisibility(View.GONE);
            mFreightEndNumberLayout.setVisibility(View.GONE);
            mConfirmBtn.setText(getResources().getString(R.string.confirm_order));
        }
    }

    public void setDetailValue(Order order) {
        if (TextUtils.isEmpty(tag) || order == null) {
            return;
        }
        mOrder = order;
        mBuyerRemark.setText(StringUtil.showMessage(order.remark));
        if (!StringUtil.getStringFromFloatKeep2(order.freight).equals(StringUtil.DOUBLE_ZERO))
            mTransportFee.setText(StringUtil.getStringFromFloatKeep2(order.freight));
        mFreightWay.setText(StringUtil.showMessage(order.express));
        mCreateTime.setText(StringUtil.showMessage(order.createTime));
        mOrderNumber.setText(StringUtil.showMessage(order.code));
        firstSetMoney();

        if (SalesWaitingDeliverOrderActivity.TAG.equals(tag)) {//确认发货
            mSellerRemark.setText(StringUtil.showMessage(order.sellerremark));
            int size = order.payimages.size();
            for (int i = 0; i < size; i++) {
                String url = order.payimages.get(i);
                if (!TextUtils.isEmpty(url))
                    images.get(i).setImageUrl(APIManager.toAbsoluteUrl(url),
                            APIManager.getInstance(getContext()).getImageLoader());
                else {
//                    images.get(i).setImageBitmap(null);
//                    images.get(i).setBackground(null);
//                    images.get(i).setVisibility(View.GONE);
                }
            }
            mPayMoneyET.setText(getResources().getString(R.string.yuan_symbol) + StringUtil.getStringFromFloatKeep2(order.paymoney));
            paysMoneyRemarkEdittext.setText(order.payRemark);
            setOnImageClickListener(order);
        }
        registerListener();
        doClickConfirmBtn(tag);
    }

    /**
     * 付款凭证图片点击放大
     */
    private void setOnImageClickListener(final Order order) {
//        mVoucherImage.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                ArrayList<String> urls = new ArrayList<>(1);
//                urls.add(APIManager.toAbsoluteUrl(order.payimage));
//                Intent intent = new Intent(getContext(), ImageViewerActivity.class);
//                intent.putStringArrayListExtra(ImageViewerActivity.KEY_URLS, urls);
//                intent.putExtra(ImageViewerActivity.KEY_INDEX, 0);
//                getContext().startActivity(intent);
//            }
//        });
        View.OnClickListener l = new OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<String> urls = new ArrayList<>(1);
                int index = (Integer) v.getTag();
                if (TextUtils.isEmpty(order.payimages.get(index))) {
                    return;
                }
                urls.add(APIManager.toAbsoluteUrl(order.payimages.get(index)));
                Intent intent = new Intent(getContext(), ImageViewerActivity.class);
                intent.putStringArrayListExtra(ImageViewerActivity.KEY_URLS, urls);
                intent.putExtra(ImageViewerActivity.KEY_INDEX, 0);
                getContext().startActivity(intent);
            }
        };

        int size = images.size();
        for (int i = 0; i < size; i++) {

            images.get(i).setOnClickListener(l);
        }
    }

    private void registerListener() {
        mTransportFee.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float fee;
                String str = mTransportFee.getText().toString().trim();
                if (!TextUtils.isEmpty(str)) {
                    try {
                        fee = Float.parseFloat(str);
                    } catch (Exception e) {
                        fee = 0f;
                    }
                } else {
                    fee = 0f;
                }
                if (feeListener != null) {
                    feeListener.onFeeChanged(fee);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mTransportFee.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String price = StringUtil.get2DecimalFromString(mTransportFee.getText().toString().trim());
                if (!TextUtils.isEmpty(price)) {
                    try {
                        mTransportFee.setText(price);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mClickScanExpressCodeListener != null) {
                    mClickScanExpressCodeListener.onClickScan(v,
                            v.getId() == R.id.view_list_footer_freight_start_num_image);
                }
            }
        };
        mStartQrCode.setOnClickListener(onClickListener);
        mEndQrCode.setOnClickListener(onClickListener);
    }

    private void firstSetMoney() {
        BigDecimal money = new BigDecimal(0f);
        if (mOrder.productIds != null && mOrder.productIds.size() > 0
                && mOrder.productAmount != null && mOrder.productAmount.size() > 0
                && mOrder.products != null && mOrder.products.size() > 0) {
            for (int i = 0; i < mOrder.productIds.size(); i++) {
                money = money.add(new BigDecimal(mOrder.productAmount.get(i))
                        .multiply(new BigDecimal(mOrder.products.get(i).price)));
            }
            setMoneySum(StringUtil.getStringFromFloatKeep2(money.add(new BigDecimal(mOrder.freight)).doubleValue()));
        }
    }

    public void setMoneySum(String money) {
        mMoneySum.setText(StringUtil.showMessage(money));
        bottom_total_fee.setText(getResources().getString(R.string.yuan_symbol) + StringUtil.showMessage(money));
    }

    public void setExpressStart(String code) {
        if (mFreightStartNumber != null) {
            mFreightStartNumber.setText(StringUtil.showMessage(code));
        }
    }

    public void setExpressEnd(String code) {
        if (mFreightEndNumber != null) {
            mFreightEndNumber.setText(StringUtil.showMessage(code));
        }
    }

    private void doClickConfirmBtn(final String tag) {
        mConfirmBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (confirmListener == null) {
                    return;
                }
                if (SalesWaitingConfirmOrderActivity.TAG.equals(tag)) {//确认订单
                    confirmListener.onConfirmOrder();

                } else {    //确认发货（下一步）
                    confirmListener.onConfirmDeliver();
                }
            }
        });
    }

    public String getExpress() { //货运方式
        return mFreightWay != null ? mFreightWay.getText().toString().trim() : null;
    }

    public Float getFee() { // 运费
        String str = mTransportFee.getText().toString().trim();
        float fee = 0f;
        if (!TextUtils.isEmpty(str)) {
            fee = Float.parseFloat(str);
        }
        return fee;
    }

    public String getSellerRemark() {
        return mSellerRemark != null ? mSellerRemark.getText().toString().trim() : null;
    }

    public String getExpressStart() {
        return mFreightStartNumber != null ? mFreightStartNumber.getText().toString().trim() : null;
    }

    public String getExpressEnd() {
        return mFreightEndNumber != null ? mFreightEndNumber.getText().toString().trim() : null;
    }

    public void setOnClickConfirmListener(OnClickConfirmListener l) {
        this.confirmListener = l;
    }

    public void setOnFeeChangedListener(OnFeeChangedListener l) {
        this.feeListener = l;
    }

    public void setOnClickScanExpressCodeListener(OnClickScanExpressCodeListener l) {
        mClickScanExpressCodeListener = l;
    }

    public interface OnClickConfirmListener {
        public void onConfirmOrder();

        public void onConfirmDeliver();
    }

    public interface OnFeeChangedListener {
        public void onFeeChanged(float fee);
    }

    public static interface OnClickScanExpressCodeListener {
        void onClickScan(View v, boolean isStart);
    }
}
