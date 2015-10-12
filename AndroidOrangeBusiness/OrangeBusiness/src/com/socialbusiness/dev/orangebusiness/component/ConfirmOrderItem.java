package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.math.BigDecimal;

public class ConfirmOrderItem extends LinearLayout {
    private NetworkImageView mProductImg;
    private TextView mProductName;
    private EditText mProductPrice;
    private EditText mProductNum;
    private TextView mProductSum;

    private int currentNum;
    private float currentPrice;

    private OnEditTextDataChangeListener listener;

    private Product product;

    public ConfirmOrderItem(Context context) {
        super(context);
        initView();
    }

    public ConfirmOrderItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_confirm_order_item, this);
        findView(view);
    }

    private void findView(View view) {

        mProductImg = (NetworkImageView) view.findViewById(R.id.activity_seller_order_confirm_img);
        mProductName = (TextView) view.findViewById(R.id.activity_seller_order_confirm_product_name);
        mProductPrice = (EditText) view.findViewById(R.id.view_confirm_order_unit_price);
        mProductNum = (EditText) view.findViewById(R.id.view_confirm_order_product_num);
        mProductSum = (TextView) view.findViewById(R.id.view_confirm_order_product_sum);

        mProductImg.setDefaultImageResId(R.drawable.ic_default_mid);
        mProductImg.setErrorImageResId(R.drawable.ic_default_mid);

        registerListeners();
    }

    private void registerListeners() {

        mProductPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String price = mProductPrice.getText().toString().trim();
                if (!TextUtils.isEmpty(price)) {
                    try {
                        if (currentPrice == Float.parseFloat(price)) {
                            return;
                        }
                        currentPrice = Float.parseFloat(price);
                    } catch (Exception e) {
                        currentPrice = 0f;
                    }
                } else {
                    currentPrice = 0f;
                }
                updateSum();
                callDataChangeListener();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mProductPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String price = StringUtil.get2DecimalFromString(mProductPrice.getText().toString().trim());
                if (!TextUtils.isEmpty(price)) {
                    try {
                        mProductPrice.setText(price);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        mProductNum.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String num = mProductNum.getText().toString().trim();
                if (!TextUtils.isEmpty(num)) {
                    try {
                        if (currentNum == Integer.parseInt(num)) {
                            return;
                        }
                        currentNum = Integer.parseInt(num);
                    } catch (Exception e) {
                        currentNum = 0;
                    }
                } else {
                    currentNum = 0;
                }
                updateSum();
                callDataChangeListener();
            }

            @Override
            public void afterTextChanged(Editable s) {
//                Log.e("========","=========Start");
//                getNemberOnETZero();
//
//                updateSum();
//                callDataChangeListener();
            }
        });

//        mProductNum.setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//
//                    if (currentNum == 0) {
//                        Log.e("========","========="+v+"===="+hasFocus);
//                        currentNum=1;
//                        mProductNum.setText(String.valueOf(currentNum));
//                        updateSum();
//                        callDataChangeListener();
//                    }
//                }
//            }
//        });
    }

    private void getNemberOnETZero() {
        String num = mProductNum.getText().toString().trim();
        if (!TextUtils.isEmpty(num)) {
            try {
                int temp = Integer.parseInt(num);

//                        if (currentNum == temp) {
//                            return;
//                        }

                currentNum = temp;

                if (temp == 0) {
                    currentNum = 1;
                    mProductNum.setText(String.valueOf(currentNum));
//                            return;
                }


            } catch (Exception e) {
                currentNum = 1;
            }
        } else {
            Log.e("========", "=========end");
            currentNum = 0;
        }
    }

    public void setItemData(Product product, int quantity, float price) {
        if (product != null) {
            this.product = product;
            currentNum = quantity;
            currentPrice = price;
            mProductImg.setImageUrl(APIManager.toAbsoluteUrl(product.getCoverImage()),
                    APIManager.getInstance(getContext()).getImageLoader());
            mProductName.setText(product.name);
            mProductPrice.setText(StringUtil.getStringFromFloatKeep2(price));
            mProductNum.setText(quantity + "");
            updateSum();
        }
    }

    public void setPriceEnable(boolean value) {
        if (mProductPrice != null) {
            mProductPrice.setEnabled(value);
            mProductPrice.setBackgroundDrawable(null);
        }
    }

    private void updateSum() {
        mProductSum.setText(StringUtil.getStringFromFloatKeep2(
                new BigDecimal(currentNum).multiply(new BigDecimal(currentPrice)).doubleValue()));
    }

    private void callDataChangeListener() {
        if (listener != null) {
            listener.onEditTextChanged(currentNum, currentPrice, this.product.id);
        }
    }

    public void setOnEditTextDataChangeListener(OnEditTextDataChangeListener listener) {
        this.listener = listener;
    }

    public interface OnEditTextDataChangeListener {
        public void onEditTextChanged(int quantity, float price, String productId);
    }

}
