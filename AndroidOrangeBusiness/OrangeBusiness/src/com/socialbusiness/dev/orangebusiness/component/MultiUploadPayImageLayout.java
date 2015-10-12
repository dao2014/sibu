package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.ImageUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by zkboos on 2015/6/17.
 */
public class MultiUploadPayImageLayout extends LinearLayout implements View.OnClickListener {


    private BaseActivity context;

    public MultiUploadPayImageLayout(Context context) {
        super(context);
        initView();
    }


    public MultiUploadPayImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    private void initView() {
        context = (BaseActivity) getContext();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pay_image_layout, this);
        findViews();

    }

    public void setFeightTotalLayoutVisible() {
        this.feight_total_layout.setVisibility(View.VISIBLE);
    }

    public void setTotalFee(String totalFee) {
        this.bottom_total_fee.setText(totalFee);
    }

    public void setTranstionFee(String transtionFee) {
        this.bottom_transtion_fee.setText(transtionFee);
    }

    User loginUser;

    private User getLoginUser() {
        loginUser = SettingsManager.getLoginUser();
        if (loginUser == null) {
            Intent toLoginIntent = new Intent(Constant.RECEIVER_NO_LOGIN);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(toLoginIntent);
        }
        return loginUser;
    }

    private EditText mPaysMoneyET;
    private EditText paysMoneyRemarkEdittext;
    private View feight_total_layout;
    private TextView bottom_transtion_fee;
    private TextView bottom_total_fee;

    private void findViews() {
        mPaysMoneyET = (EditText) findViewById(R.id.order_detail_footerview_pays_money_edittext);
        paysMoneyRemarkEdittext = (EditText) findViewById(R.id.order_detail_footerview_pays_money_remark_edittext);
        feight_total_layout = findViewById(R.id.feight_total_layout);
        bottom_transtion_fee = (TextView) findViewById(R.id.bottom_transtion_fee);
        bottom_total_fee = (TextView) findViewById(R.id.bottom_total_fee);
        findVouchers();
        getLoginUser();
        setViewListener();
    }

    private void setViewListener() {
    }

    ArrayList<NetworkImageView> images = new ArrayList<>();
    ArrayList<View> deletes = new ArrayList<>();
    ArrayList<String> imageUrls = new ArrayList<>();

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    private void findVouchers() {

        for (int i = 0; i < 6; i++) {
            NetworkImageView image = (NetworkImageView) findViewById(R.id.order_detail_footerview_pays_voucher_image + i * 2);
            image.setTag(i);
            image.setOnClickListener(this);
            image.setImageUrl(APIManager.toAbsoluteUrl(null),
                    APIManager.getInstance(getContext()).getImageLoader());
            images.add(image);

            View b = findViewById(R.id.order_detail_footerview_pays_voucher_image + i * 2 + 1);
            b.setTag(i);
            b.setVisibility(View.GONE);
            b.setOnClickListener(this);
            deletes.add(b);

            imageUrls.add("");
        }
    }

    private String tag = "";


    public interface UploadPVIClickListener {
        public void chosePayImage();

        public void updatePayImage();
    }

    public interface OnUploadFinsh {
        public void uploadSucess(int index, String image);

        public void fial(String massage);

        public void deleteSucess(int index);
    }

    private UploadPVIClickListener listener;
    private OnUploadFinsh onUploadFinsh;
    private int curentIndex = 0;
    private int curentDeleteIndex = -1;
    private int isLocal = 0;
    private final int LOCAL_DELETE = 0;
    private final int NET_DELETE = 1;

    public void setListener(UploadPVIClickListener listener) {
        this.listener = listener;
    }

    public void setOnUploadFinsh(OnUploadFinsh onUploadFinsh) {
        this.onUploadFinsh = onUploadFinsh;
    }

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
//            Log.e("deleteImage====", "orderTmp==" + orderTmp.id + "index==" + curentDeleteIndex);

            switch (isLocal) {
                case LOCAL_DELETE:
                    context.hideProgressDialog();
                    images.get(curentDeleteIndex).setImageResource(R.drawable.plus);
                    images.get(curentDeleteIndex).setScaleType(ImageView.ScaleType.CENTER);
                    deletes.get(curentDeleteIndex).setVisibility(View.GONE);
                    imageUrls.set(curentDeleteIndex, "");

                    if (onUploadFinsh != null) {
                        onUploadFinsh.deleteSucess(curentDeleteIndex);
                    }
                    break;
                case NET_DELETE:
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
                            images.get(curentDeleteIndex).setScaleType(ImageView.ScaleType.CENTER);
                            deletes.get(curentDeleteIndex).setVisibility(View.GONE);
                            imageUrls.remove(curentDeleteIndex);
                            if (onUploadFinsh != null) {
                                onUploadFinsh.deleteSucess(curentDeleteIndex);
                            }
                        }
                    });
                    break;
            }
        }
    }

    Bitmap bitmap;

    public void loadVoucherImage(final Uri imageUrl, String mImageFilePath) {

        if (ImageUtil.getBitmapsize(mImageFilePath) > 200) {
            bitmap = ImageUtil.getimage2(mImageFilePath);
            ImageUtil.compressImage(bitmap, mImageFilePath);
        }
        //上传图片
        this.context.showProgressDialog();
        Log.e("====", "orderTmp==" + imageUrl + "index==" + curentIndex + "filepath==" + mImageFilePath);
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("filePath", mImageFilePath);
//        parameters.put("orderId", orderTmp.id);
//        parameters.put("index", curentIndex + "");
        APIManager.getInstance(this.context).orderPayImage(parameters, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                context.hideProgressDialog();
                context.showException(error);
            }
        }, new Response.Listener<RequestResult<?>>() {
            @Override
            public void onResponse(RequestResult<?> response) {
                Log.e("==", "==" + response.data);
//                imageUrls.add(curentIndex, response.data.toString());
                imageUrls.set(curentIndex, response.data.toString());
                context.hideProgressDialog();
                images.get(curentIndex).setImageURI(imageUrl);
                images.get(curentIndex).setScaleType(ImageView.ScaleType.FIT_XY);
                deletes.get(curentIndex).setVisibility(View.VISIBLE);
                if (onUploadFinsh != null) {
                    onUploadFinsh.uploadSucess(curentDeleteIndex, response.data.toString());
                }
            }
        });
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
                image.setDefaultImageResId(R.drawable.default_pic);
            }
            if (!tag.equals(Waiting2PayFragment.TAG)) {
                image.setImageResource(R.drawable.default_pic);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            image.setImageUrl(APIManager.toAbsoluteUrl(url),
                    APIManager.getInstance(getContext()).getImageLoader());
        }
        String value = StringUtil.getStringFromFloatKeep2(order.paymoney);
        mPaysMoneyET.setText(value.equals(StringUtil.DOUBLE_ZERO) ? "" : value);
        paysMoneyRemarkEdittext.setText(order.payRemark);
    }

    //根据订单填充图片
    public void loadVoucherImage(List<String> payimages) {
        int size = payimages.size();
        for (int i = 0; i < size; i++) {
            String url = payimages.get(i);
            NetworkImageView image = images.get(i);
            if (!TextUtils.isEmpty(url)) {
//                deletes.get(i).setVisibility(View.VISIBLE);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                image.setDefaultImageResId(R.drawable.default_pic);
            }
            if (!tag.equals(Waiting2PayFragment.TAG)) {
                image.setImageResource(R.drawable.default_pic);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            image.setImageUrl(APIManager.toAbsoluteUrl(url),
                    APIManager.getInstance(getContext()).getImageLoader());
        }
//
    }
    //初始化image
    public void loadVoucherImage(List<String> payimages,boolean isShowDelete) {
        int size = payimages.size();
        for (int i = 0; i < size; i++) {
            String url = payimages.get(i);
            NetworkImageView image = images.get(i);
            if (!TextUtils.isEmpty(url)) {
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                deletes.get(i).setVisibility(View.VISIBLE);
                imageUrls.set(i,url);
            }else{
                imageUrls.set(i,"");
            }
            image.setImageUrl(APIManager.toAbsoluteUrl(url),
                    APIManager.getInstance(getContext()).getImageLoader());
        }
//
    }
    /**
     * 付款凭证图片点击放大
     */
    public void setOnImageClickListener(final Order order) {
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

    Order orderTmp;

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setOrderTmp(Order orderTmp) {
        this.orderTmp = orderTmp;
    }


    public String getPayMoney() {
        return TextUtils.isEmpty(mPaysMoneyET.getText().toString().trim()) ? "0.00" : mPaysMoneyET.getText().toString().trim();
    }

    public void setPayMoney(String payMoney) {
        String value = StringUtil.get2DecimalFromString(payMoney);
        mPaysMoneyET.setText(value.equals(StringUtil.DOUBLE_ZERO) ? "" : value);

    }

    public void setPayRemark(String remark) {
        paysMoneyRemarkEdittext.setText(remark);
    }

    public String getPayRemark() {
        return paysMoneyRemarkEdittext.getText() != null ? paysMoneyRemarkEdittext.getText().toString().trim() : "";
    }

    public void setAllEnabled() {
        mPaysMoneyET.setEnabled(false);
        paysMoneyRemarkEdittext.setEnabled(false);
    }
}
