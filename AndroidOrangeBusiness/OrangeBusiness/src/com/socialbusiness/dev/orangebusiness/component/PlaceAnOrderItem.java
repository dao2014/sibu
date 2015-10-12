package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.order.PlaceAnOrderActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.math.BigDecimal;

public class PlaceAnOrderItem extends RelativeLayout {

    private NetworkImageView mImage;
    private TextView mName;
    private TextView mNum;
    private EditText mPrice;
    private TextView mMoneySum;

    public PlaceAnOrderItem(Context context) {
        super(context);
        initView();
    }

    public PlaceAnOrderItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        findView(inflater.inflate(R.layout.view_place_an_order_item, this));
        setListener();
    }

    private void setListener() {
        mPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (shopCartItem == null) {
                    return;
                }
                String price = s.toString().trim();
                if (!TextUtils.isEmpty(price)) {
                    try {
                        if (shopCartItem.price == Float.parseFloat(price)) {
                            return;
                        }
                        shopCartItem.price = Float.parseFloat(price);
                    } catch (Exception e) {
                        shopCartItem.price = 0f;
                        e.printStackTrace();
                    }
                } else {
                    shopCartItem.price = 0f;
                }
                updateSum();
                callBackDataChangedListener((ShopCartItem) mPrice.getTag());
            }
        });


        mPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (shopCartItem == null) {
//                    return false;
//                }
//                String price = StringUtil.get2DecimalFromString(mPrice.getText().toString().trim());
//                if (!TextUtils.isEmpty(price)) {
//                    try {
//                        mPrice.setText(price);
//                        return true;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
                return false;
            }
        });
    }


    private void updateSum() {
        mMoneySum.setText(StringUtil.getStringFromFloatKeep2(
                new BigDecimal(shopCartItem.quantity).multiply(new BigDecimal(shopCartItem.price)).doubleValue()));
    }

    PlaceAnOrderActivity.OnEditTextChange onEditTextChange;

    public void setOnEditTextChange(PlaceAnOrderActivity.OnEditTextChange onEditTextChange) {
        this.onEditTextChange = onEditTextChange;
    }

    private void callBackDataChangedListener(ShopCartItem item) {

        if (this.onEditTextChange != null) {
            this.onEditTextChange.onEditChange(item);
        }
    }

    private TextView priceTitle;
    private TextView symbolMultiply;
    private TextView totalMoneyTitle;

    private void findView(View view) {
        mImage = (NetworkImageView) view.findViewById(R.id.view_place_an_order_product_image);
        mName = (TextView) view.findViewById(R.id.view_place_an_order_product_name);
        mPrice = (EditText) view.findViewById(R.id.view_place_an_order_product_price);
        mNum = (TextView) view.findViewById(R.id.view_place_an_order_product_num);
        mMoneySum = (TextView) view.findViewById(R.id.view_place_an_order_product_total_money);

        priceTitle = (TextView) view.findViewById(R.id.view_place_an_order_product_price_title);
        symbolMultiply = (TextView) view.findViewById(R.id.view_place_an_order_product_symbol_multiply);
        totalMoneyTitle = (TextView) view.findViewById(R.id.view_place_an_order_product_total_money_title);

        mImage.setErrorImageResId(R.drawable.default_pic);
        mImage.setDefaultImageResId(R.drawable.default_pic);
        mImage.setScaleType(ScaleType.CENTER_CROP);
    }

    ShopCartItem shopCartItem;

    public void setShopCartItemValues(ShopCartItem shopCartItem) {
        if (shopCartItem != null) {
            this.shopCartItem = shopCartItem;
            mImage.setImageUrl(APIManager.toAbsoluteUrl(shopCartItem.getCoverImage()),
                    APIManager.getInstance(getContext()).getImageLoader());
            mName.setText(shopCartItem.name);
            mNum.setText(shopCartItem.quantity + "");
            mMoneySum.setText(StringUtil.getStringFromFloatKeep2(
                    new BigDecimal(shopCartItem.quantity).multiply(new BigDecimal(shopCartItem.price)).doubleValue()));
            mPrice.setTag(shopCartItem);
            mPrice.setText(StringUtil.getStringFromFloatKeep2(shopCartItem.price));
            Log.e("===", "==" + StringUtil.getStringFromFloatKeep2(shopCartItem.price) + "==" + shopCartItem.quantity + "==" + shopCartItem.amount);

        }
    }

    public void setEnsureView() {
        this.mPrice.setEnabled(false);
        this.mPrice.setBackgroundColor(Color.TRANSPARENT);
        this.mPrice.setPadding(0, 0, 0, 0);
//        this.mPrice.setTextSize(16);
//        this.mNum.setTextColor(getContext().getResources().getColor(R.color.red_product_price));
//        this.mNum.setTextSize(16);

//        priceTitle.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
//        symbolMultiply.setTextColor(getContext().getResources().getColor(R.color.red_product_price));
        totalMoneyTitle.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
        mMoneySum.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
    }
}
