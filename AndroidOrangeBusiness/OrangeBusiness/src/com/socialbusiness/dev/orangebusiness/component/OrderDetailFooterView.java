package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.kollway.android.imageviewer.activity.ImageViewerActivity;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.ReceiptedActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesDeliveredOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaiting2PayOrderActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.fragment.order.DeliverDoneFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.ReceiptedFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingConfirmFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingDeliverFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.ImageUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class OrderDetailFooterView extends LinearLayout implements View.OnClickListener {

    private TextView mTransportFee;
    private TextView mMoneySum;

    private View order_detail_footerview_start_number_layout, order_detail_footerview_end_number_layout;
    private TextView mCreateTime;
    private TextView mFreightWay;
    private TextView mFreightStartNumber;
    private TextView mFreightEndNumber;
    private TextView mOrderNumber;
    private TextView bottomTranstionFee;
    private TextView bottomTotalFee;

    private LinearLayout mSellerRemarkLayout;
    private View mPaysVoucherLayout;

    private TextView mBuyerRemarks;
    private TextView mSellerRemarks;
    //    private NetworkImageView mPaysVoucherImage;
    private EditText mPaysMoneyET;
    private EditText paysMoneyRemarkEdittext;
    private TextView mUploadPVI;    //PVI: pays voucher image

    private String tag;
    private UploadPVIClickListener listener;
    private ConfirmReceiptedListener confirmListener;
    private BaseActivity context;

    public OrderDetailFooterView(Context context) {
        super(context);
        initView();
    }

    public OrderDetailFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

//    ClipboardManager cmb;

    private void initView() {
        context = (BaseActivity) getContext();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        findViews(inflater.inflate(R.layout.order_detail_footer_view, this));
//        cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }


    private void findViews(View view) {
        mTransportFee = (TextView) view.findViewById(R.id.order_detail_footerview_transport_fee);
        mMoneySum = (TextView) view.findViewById(R.id.order_detail_footerview_money_sum);

        mCreateTime = (TextView) view.findViewById(R.id.order_detail_footerview_createtime);
        mFreightWay = (TextView) view.findViewById(R.id.order_detail_footerview_freight_way);
        mFreightStartNumber = (TextView) view.findViewById(R.id.order_detail_footerview_freight_start_number);
        mFreightEndNumber = (TextView) view.findViewById(R.id.order_detail_footerview_freight_end_number);
        mOrderNumber = (TextView) view.findViewById(R.id.order_detail_footerview_order_number);

        mSellerRemarkLayout = (LinearLayout) view.findViewById(R.id.order_detail_footerview_seller_remark_layout);
        mPaysVoucherLayout = view.findViewById(R.id.order_detail_footerview_pays_voucher_layout);

        mBuyerRemarks = (TextView) view.findViewById(R.id.order_detail_footerview_buyer_remark_value);
        mSellerRemarks = (TextView) view.findViewById(R.id.order_detail_footerview_seller_remark_value);
//        mPaysVoucherImage = (NetworkImageView) view.findViewById(R.id.order_detail_footerview_pays_voucher_image);
        mPaysMoneyET = (EditText) view.findViewById(R.id.order_detail_footerview_pays_money_edittext);
        mUploadPVI = (TextView) view.findViewById(R.id.order_detail_footerview_upload_pays_voucher);
        paysMoneyRemarkEdittext = (EditText) view.findViewById(R.id.order_detail_footerview_pays_money_remark_edittext);
        bottomTotalFee = (TextView) findViewById(R.id.bottom_total_fee);
        bottomTranstionFee = (TextView) findViewById(R.id.bottom_transtion_fee);

        order_detail_footerview_start_number_layout = findViewById(R.id.order_detail_footerview_start_number_layout);
        order_detail_footerview_end_number_layout = findViewById(R.id.order_detail_footerview_end_number_layout);

        findVouchers();
//        mPaysVoucherImage.setDefaultImageResId(R.drawable.default_pic);
//        mPaysVoucherImage.setErrorImageResId(R.drawable.default_pic);
    }

    ArrayList<NetworkImageView> images = new ArrayList<>();
    ArrayList<View> deletes = new ArrayList<>();

    private void findVouchers() {

        for (int i = 0; i < 6; i++) {
            NetworkImageView image = (NetworkImageView) findViewById(R.id.order_detail_footerview_pays_voucher_image + i * 2);
            image.setTag(i);
            image.setOnClickListener(this);
            images.add(image);

            View b = findViewById(R.id.order_detail_footerview_pays_voucher_image + i * 2 + 1);
            b.setTag(i);
            b.setVisibility(View.GONE);
            b.setOnClickListener(this);
            deletes.add(b);
//            Log.e("====", "" + b);
        }

//        Log.e("onMeasure", "==" + images.get(0).getWidth() + "==" + images.get(0).getMeasuredWidth() + "==" + images.get(0).getLayoutParams());

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
//
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    //根据订单填充图片
    private void loadVoucherImage(Order order) {
        int size = order.payimages.size();
        for (int i = 0; i < size; i++) {
            String url = order.payimages.get(i);
            NetworkImageView image = images.get(i);
            if (!TextUtils.isEmpty(url)) {
                deletes.get(i).setVisibility(View.VISIBLE);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                image.setDefaultImageResId(R.drawable.plus);
            }
            if (!tag.equals(Waiting2PayFragment.TAG)) {
                image.setImageResource(R.drawable.plus);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            image.setImageUrl(APIManager.toAbsoluteUrl(url),
                    APIManager.getInstance(getContext()).getImageLoader());
        }
        String value = StringUtil.getStringFromFloatKeep2(order.paymoney);
        if (!tag.equals(Waiting2PayFragment.TAG))
            mPaysMoneyET.setText((value.equals(StringUtil.DOUBLE_ZERO) ? "" : (getResources().getString(R.string.yuan_symbol) + value)));
        else mPaysMoneyET.setText((value.equals(StringUtil.DOUBLE_ZERO) ? "" : value));
        paysMoneyRemarkEdittext.setText(order.payRemark);
    }

    //选择图片填充
//    public void loadVoucherImage(Uri imageUrl) {
////        mPaysVoucherImage.setImageURI(imageUrl);
//    }

    public void loadVoucherImage(final Uri imageUrl, String mImageFilePath) {

        if (ImageUtil.getBitmapsize(mImageFilePath) > 200) {
            Bitmap bitmap = ImageUtil.getimage2(mImageFilePath);
            ImageUtil.compressImage(bitmap, mImageFilePath);
        }
        //上传图片
        this.context.showProgressDialog();
        Log.e("====", "orderTmp==" + orderTmp + "index==" + curentIndex + "filepath==" + mImageFilePath);
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("filePath", mImageFilePath);
        parameters.put("orderId", orderTmp.id);
        parameters.put("index", curentIndex + "");
        APIManager.getInstance(this.context).uploadPayImage(parameters, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                context.hideProgressDialog();
                context.showException(error);
            }
        }, new Response.Listener<RequestResult<?>>() {
            @Override
            public void onResponse(RequestResult<?> response) {
                context.hideProgressDialog();
                images.get(curentIndex).setImageURI(imageUrl);
                images.get(curentIndex).setScaleType(ImageView.ScaleType.FIT_XY);
                deletes.get(curentIndex).setVisibility(View.VISIBLE);
            }
        });
    }

    private static int curentIndex = 0;
    private static int curentDeleteIndex = -1;

    @Override
    public void onClick(View v) {

        if (v instanceof NetworkImageView) {
            curentIndex = (Integer) v.getTag();
            Log.e("==", "imageadd==" + curentIndex);
            if (listener != null) {
                listener.chosePayImage();
            }

        } else {

            Log.e("==", "delete" + v.getTag());
            curentDeleteIndex = (Integer) v.getTag();
            //删除图片
            Log.e("deleteImage====", "orderTmp==" + orderTmp.id + "index==" + curentDeleteIndex);
            Hashtable<String, String> parameters = new Hashtable<>();
            parameters.put("id", orderTmp.id);
            parameters.put("index", curentDeleteIndex + "");
            APIManager.getInstance(this.context).deletePayImage(parameters, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    context.hideProgressDialog();
                    context.showException(error);
                }
            }, new Response.Listener<RequestResult<?>>() {
                @Override
                public void onResponse(RequestResult<?> response) {
                    context.hideProgressDialog();
                    images.get(curentDeleteIndex).setImageResource(R.drawable.plus);
                    images.get(curentDeleteIndex).setScaleType(ImageView.ScaleType.FIT_XY);
                    deletes.get(curentDeleteIndex).setVisibility(View.GONE);
                }
            });
        }
    }

    private void setListeners() {
//        mPaysVoucherImage.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (listener != null) {
//                    listener.chosePayImage();
//                }
//            }
//        });

        mUploadPVI.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.updatePayImage();
                }
            }
        });
    }

    public void setUploadClickListener(UploadPVIClickListener listener) {
        this.listener = listener;
    }

    public interface UploadPVIClickListener {
        public void chosePayImage();

        public void updatePayImage();
    }

    public void setTagToHide(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        this.tag = tag;
        switch (tag) {
            case WaitingConfirmFragment.TAG:    //买家待确认订单
                mTransportFee.setVisibility(View.GONE);
                order_detail_footerview_start_number_layout.setVisibility(View.GONE);
                order_detail_footerview_end_number_layout.setVisibility(View.GONE);
                mSellerRemarkLayout.setVisibility(View.GONE);
                mPaysVoucherLayout.setVisibility(View.GONE);
                mUploadPVI.setVisibility(View.GONE);
                break;

            case Waiting2PayFragment.TAG:   //买家待付款订单
                order_detail_footerview_start_number_layout.setVisibility(View.GONE);
                order_detail_footerview_end_number_layout.setVisibility(View.GONE);
                mPaysMoneyET.setBackgroundResource(R.drawable.order_detail_edit_text_bg);
                mPaysMoneyET.setHint("必填");
                paysMoneyRemarkEdittext.setBackgroundResource(R.drawable.order_detail_edit_text_bg);
                paysMoneyRemarkEdittext.setHint("选填");
                break;

            case WaitingDeliverFragment.TAG:    //买家待发货订单
                order_detail_footerview_start_number_layout.setVisibility(View.GONE);
                order_detail_footerview_end_number_layout.setVisibility(View.GONE);
              //  setTextMargin();
                noUploadPVIButton();
                break;

            case DeliverDoneFragment.TAG:   //买家已发货订单
                mPaysMoneyET.setEnabled(false);
                paysMoneyRemarkEdittext.setEnabled(false);
                break;

            case ReceiptedFragment.TAG:     //买家已收货订单
//                order_detail_footerview_start_number_layout.setVisibility(View.GONE);
//                order_detail_footerview_end_number_layout.setVisibility(View.GONE);
                noUploadPVIButton();
                break;

            case SalesWaiting2PayOrderActivity.TAG:     //卖家待付款订单
                order_detail_footerview_start_number_layout.setVisibility(View.GONE);
                order_detail_footerview_end_number_layout.setVisibility(View.GONE);
                mPaysVoucherLayout.setVisibility(View.GONE);
//			noUploadPVIButton();
                break;

            case SalesDeliveredOrderActivity.TAG:    //卖家已发货订单
                noUploadPVIButton();
                break;

            case ReceiptedActivity.TAG:     //卖家已收货订单
//                order_detail_footerview_start_number_layout.setVisibility(View.GONE);
//                order_detail_footerview_end_number_layout.setVisibility(View.GONE);
                noUploadPVIButton();
                break;

            default:
                break;
        }
    }

    private void noUploadPVIButton() {
        mUploadPVI.setVisibility(View.GONE);
        mPaysMoneyET.setEnabled(false);
        paysMoneyRemarkEdittext.setEnabled(false);
    }

    private static Order orderTmp;

    public void setDetailValue(Order order) {
        if (TextUtils.isEmpty(tag) || order == null) {
            return;
        }
        orderTmp = order;
        switch (tag) {
            case WaitingConfirmFragment.TAG:    //买家待确认订单
                tagWaitingConfirmFragment(order);
                break;

            case Waiting2PayFragment.TAG:   //买家待付款订单
                tagWaiting2PayFragment(order);
                break;

            case WaitingDeliverFragment.TAG:    //买家待发货订单
                tagWaitingDeliverOrder(order);
                setOnImageClickListener(order);
                break;

            case DeliverDoneFragment.TAG:   //买家已发货订单
                tagDeliveredOrder(order);
                setOnImageClickListener(order);
                break;

            case ReceiptedFragment.TAG:     //买家已收货订单 （跟待发货订单详情一样）
                tagWaitingDeliverOrder(order);
                setOnImageClickListener(order);
                break;

            case SalesWaiting2PayOrderActivity.TAG: //卖家待付款订单
                tagWaiting2PayActivity(order);
                isShowPVI(order);
                break;

            case SalesDeliveredOrderActivity.TAG:   //卖家已发货订单
                tagDeliveredOrderActivity(order);
                setOnImageClickListener(order);
                break;

            case ReceiptedActivity.TAG: //卖家已收货订单 （跟待发货订单详情一样）
                tagWaitingDeliverOrder(order);
                setOnImageClickListener(order);
                break;

            default:
                break;
        }
    }

    /**
     * 设置 运货方式的 外边距
     */
    public void setTextMargin(){
        LinearLayout.LayoutParams para=new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT );
        para.setMargins(90, 0, 0, 0); //left,top,right, bottom
        mFreightWay.setLayoutParams(para);
    }

    private void isShowPVI(Order order) {
        if (TextUtils.isEmpty(order.payimage)) {
            return;
        }
        loadVoucherImage(order);
        setOnImageClickListener(order);
        mPaysMoneyET.setEnabled(false);
        paysMoneyRemarkEdittext.setEnabled(false);
        mPaysVoucherLayout.setVisibility(VISIBLE);
    }

    /**
     * 付款凭证图片点击放大
     */
    private void setOnImageClickListener(final Order order) {
//        mPaysVoucherImage.setOnClickListener();

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
            deletes.get(i).setVisibility(View.GONE);
        }
    }

    public boolean ismUploadPVIVisi(String tag) {
        return tag.equals(Waiting2PayFragment.TAG);
    }

    /**
     * 我的采购，待确认订单详情
     *
     * @param order 订单
     */
    private void tagWaitingConfirmFragment(Order order) {
        mMoneySum.setText(getResources().getString(R.string.total_money_s) + getMoneySum(order, 0f));
        setValue4(order);
    }

    /**
     * 我的采购，待付款订单详情(上传付款凭证)
     *
     * @param order 订单
     */
    private void tagWaiting2PayFragment(Order order) {
        loadVoucherImage(order);
        setValue7(order);
        setListeners();
    }

    /**
     * 我的采购，待发货订单详情（两个已收货订单）
     *
     * @param order 订单
     */
    private void tagWaitingDeliverOrder(Order order) {
        loadVoucherImage(order);
        setValue9(order);
    }

    /**
     * 我的采购，已发货订单详情
     *
     * @param order 订单
     */
    private void tagDeliveredOrder(final Order order) {
        loadVoucherImage(order);
        setValue9(order);
        mUploadPVI.setText("确认收货");
        mUploadPVI.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (confirmListener != null) {
                    confirmListener.onConfirm(order.id);
                }
            }
        });
    }

    /**
     * 我的销售，待付款订单详情
     *
     * @param order 订单
     */
    private void tagWaiting2PayActivity(final Order order) {
        setValue7(order);
        if (!TextUtils.isEmpty(order.payimage)) {
            mUploadPVI.setVisibility(View.VISIBLE);
        } else {
            mUploadPVI.setVisibility(View.GONE);
        }
        mUploadPVI.setText("确认收款");
        mUploadPVI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                context.showProgressDialog();
                APIManager.getInstance(context).orderConfirmMoney(order, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        context.hideProgressDialog();
                        context.showException(error);

                    }
                }, new Response.Listener<RequestResult<?>>() {
                    @Override
                    public void onResponse(RequestResult<?> response) {

                        context.hideProgressDialog();
                        if (response.code == RequestResult.RESULT_SUCESS) {

                            ToastUtil.show(context, "确认收款成功");
//                            context.setResult(context.RESULT_OK);
//                            context.finish();
                            goToMainActivity();
                        } else {
                            ToastUtil.show(context, "确认收款失败");
                        }
                        Log.e("===", "===" + response);
                    }
                });

            }
        });

    }

    private void goToMainActivity() {
        Intent intent = new Intent(this.context, MainActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, MainActivity.TabPosition.Order.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 我的销售，已发货订单详情
     *
     * @param order 订单
     */
    private void tagDeliveredOrderActivity(Order order) {
        loadVoucherImage(order);
        setValue9(order);
    }


    /**
     * V9: 下单时间  + 货运方式 + 订单单号 + 买家备注 + 运费 + 合计(含运费) + 卖家备注 + 货运起始单号 + 货运终止单号
     *
     * @param order 订单
     */
    private void setValue9(Order order) {
        mFreightStartNumber.setText(StringUtil.showMessage(order.expressStart));
        mFreightStartNumber.setTag(StringUtil.showMessage(order.expressStart));
        mFreightEndNumber.setText(StringUtil.showMessage(order.expressEnd));
        mFreightEndNumber.setTag(StringUtil.showMessage(order.expressEnd));
        setFreightNumberListener(order);
        setValue7(order);
       // setTextMargin();
    }

    private void setFreightNumberListener(final Order order) {
        mFreightStartNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                cmb.setText(v.getTag().toString());
//                ToastUtil.show(context,"已经复制到剪切板");

                try {
                    startWebScan(String.format(APIManager.KUAIDI_URL, URLEncoder.encode(order.express, "utf-8"), order.expressStart));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        mFreightEndNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                cmb.setText(v.getTag().toString());
//                ToastUtil.show(context,"已经复制到剪切板");
                try {
                    startWebScan(String.format(APIManager.KUAIDI_URL, URLEncoder.encode(order.express, "utf-8"), order.expressEnd));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startWebScan(String url) {
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        context.startActivity(viewIntent);
    }

    /**
     * V7: 下单时间  + 货运方式 + 订单单号 + 买家备注 + 运费 + 合计(含运费) + 卖家备注
     *
     * @param order 订单
     */
    private void setValue7(Order order) {
        mTransportFee.setText(getResources().getString(R.string.transportation_fee) + StringUtil.getStringFromFloatKeep2(order.freight));
        mMoneySum.setText(getResources().getString(R.string.all_money_include_fee)
                + getMoneySum(order, order.freight));
        mSellerRemarks.setText(StringUtil.showMessage(order.sellerremark));

        bottomTotalFee.setText(getResources().getString(R.string.yuan_symbol) + getMoneySum(order, order.freight));
        bottomTranstionFee.setText(StringUtil.getStringFromFloatKeep2(order.freight));
        setValue4(order);
    }

    /**
     * V4: 下单时间  + 货运方式 + 订单单号 + 买家备注
     *
     * @param order 订单
     */
    private void setValue4(Order order) {
        mCreateTime.setText(StringUtil.showMessage(order.createTime));
        mOrderNumber.setText(StringUtil.showMessage(order.code));
        mBuyerRemarks.setText(StringUtil.showMessage(order.remark));
        mFreightWay.setText(StringUtil.showMessage(order.express));
    }

    private BigDecimal getMoneySumFloat(Order order) {
        BigDecimal moneyBD = new BigDecimal(0f);
        List<Product> products = order.products;
        List<Integer> quantitys = order.productAmount;
        if (products != null && quantitys != null) {
            for (int i = 0; i < products.size(); i++) {
                moneyBD = moneyBD.add(new BigDecimal(products.get(i).price).multiply(new BigDecimal(quantitys.get(i))));
            }
        }
        return moneyBD;
    }

    /**
     * 不含运费的时候，传null或空字符串
     *
     * @param order   订单
     * @param freight 运费
     * @return 总计
     */
    public String getMoneySum(Order order, float freight) {
        return StringUtil.getStringFromFloatKeep2(
                getMoneySumFloat(order).add(new BigDecimal(freight)).doubleValue());
    }

    public String getPayMoney() {
        return mPaysMoneyET.getText() != null ? mPaysMoneyET.getText().toString().trim().replace(getResources().getString(R.string.yuan_symbol), "") : null;
    }

    public String getPayRemark() {
        return paysMoneyRemarkEdittext.getText() != null ? paysMoneyRemarkEdittext.getText().toString().trim() : "";
    }

    public boolean isSelectImage() {
        int size = deletes.size();
        for (int i = 0; i < size; i++) {
            if (deletes.get(i).getVisibility() == View.VISIBLE) {
                return true;
            }
        }
        return false;
    }

    public void setConfirmReceiptedListener(ConfirmReceiptedListener l) {
        this.confirmListener = l;
    }

    public interface ConfirmReceiptedListener {
        public void onConfirm(String orderId);
    }

}
