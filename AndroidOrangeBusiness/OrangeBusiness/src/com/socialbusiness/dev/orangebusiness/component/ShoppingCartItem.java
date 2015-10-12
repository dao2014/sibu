package com.socialbusiness.dev.orangebusiness.component;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.product.ProductDetailActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.math.BigDecimal;

public class ShoppingCartItem extends RelativeLayout {

    private RelativeLayout mAllLayout;
    private ImageView mSelectImage;
    private NetworkImageView mImage;
    private TextView mName;
    private TextView mPrice;
    private TextView mMoneySum;
    private ImageView mMinusImage;
    private ImageView mPlusImage;
    private EditText mNumET;
    private OnClickItemListener listener;
    private OnDeleteListener deleteListener;

    private ShopCartItem mShopCartItem;

    public ShoppingCartItem(Context context) {
        super(context);
        initView();
    }

    public ShoppingCartItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        findView(inflater.inflate(R.layout.item_shopping_cart, this));
        setListeners();
    }

    private void findView(View view) {
        mAllLayout = (RelativeLayout) view.findViewById(R.id.item_shopping_cart_alllayout);
        mSelectImage = (ImageView) view.findViewById(R.id.item_shopping_cart_select_image);
        mImage = (NetworkImageView) view.findViewById(R.id.item_shopping_cart_product_image);
        mName = (TextView) view.findViewById(R.id.item_shopping_cart_product_name);
        mMoneySum = (TextView) view.findViewById(R.id.item_shopping_cart_money_sum);
        mPrice = (TextView) view.findViewById(R.id.item_shopping_cart_price);
        mMinusImage = (ImageView) view.findViewById(R.id.item_shopping_cart_num_minus_image);
        mPlusImage = (ImageView) view.findViewById(R.id.item_shopping_cart_num_plus_image);
        mNumET = (EditText) view.findViewById(R.id.item_shopping_cart_num_edittext);
        mPrice.setText("");
        mMoneySum.setText("0");
        mImage.setErrorImageResId(R.drawable.ic_default_mid);
        mImage.setDefaultImageResId(R.drawable.ic_default_mid);
        mImage.setScaleType(ScaleType.CENTER_CROP);
    }

    private void setListeners() {
        OnClickListener listeners = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    if (v == mSelectImage) {
                        listener.onClickSelectImage();
                    } else if (v == mMinusImage) {
                        listener.onClickMinus();
                    } else if (v == mPlusImage) {
                        listener.onClickPlus();
                    } else if (v == mAllLayout) {
                        if (mShopCartItem != null) {
                            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                            intent.putExtra("id", mShopCartItem.id);
                            getContext().startActivity(intent);
                        }
                    }
                }
            }
        };

        mSelectImage.setOnClickListener(listeners);
        mMinusImage.setOnClickListener(listeners);
        mPlusImage.setOnClickListener(listeners);
        mAllLayout.setOnClickListener(listeners);

        mNumET.addTextChangedListener(tw);

        mAllLayout.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                doDelete();
                return true; //true时长按触发后不会再触发短按，false时先触发长按再触发短按
            }
        });
    }


    TextWatcher tw = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String numStr = s.toString().trim();
//                int nowNum;
//                if (numStr.length() == 0) {
//                    mNumET.setHint("1");
//                    nowNum = 1;
//                } else {
//                    try {
//                        nowNum = Integer.parseInt(numStr);
//                    } catch (Exception e) {
//                        nowNum = 0;
//                        e.printStackTrace();
//                    }
//                }
//
//                if (listener != null) {
//                    listener.onInputNums(nowNum);
//                }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String num = mNumET.getText().toString().trim();
//                Log.e("===","num=="+num);
            int currentNum;
            if (!TextUtils.isEmpty(num)) {
                try {
                    int temp = Integer.parseInt(num);

//                        if (currentNum == temp) {
//                            return;
//                        }

                    currentNum = temp;

                    if (temp == 0) {
                        currentNum = 1;
                        mNumET.setText(String.valueOf(currentNum));
//                            return;
                    }

                } catch (Exception e) {
                    currentNum = 1;
                }
            } else {
                Log.e("========", "=========end");
                currentNum = 0;
            }

            if (listener != null) {
                listener.onInputNums(currentNum);
            }
        }
    };

    public void doDelete() {
        Dialog isDeleteDialog = new AlertDialog.Builder(getContext())
                .setMessage(getResources().getString(R.string.is_delete_product))
                .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (deleteListener != null) {
                            deleteListener.onDelete();
                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();

        Window window = isDeleteDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.8f;
        window.setAttributes(lp);
        isDeleteDialog.show();
    }

    public void setShopCartItemData(ShopCartItem item, Boolean value) {
        if (item != null) {
            mShopCartItem = item;
//            Log.e("===","quantity=="+item.quantity);
            mImage.setImageUrl(APIManager.toAbsoluteUrl(item.getCoverImage()), APIManager.getInstance(getContext()).getImageLoader());
            mPrice.setText("¥ 0");
            if (item.quantity != 0) {
                mNumET.setText(item.quantity + "");
//
                mPrice.setText("¥ "+StringUtil.getStringFromFloatKeep2(item.price));  //临时加一个0  确认这个是后台发
            }
            mMoneySum.setText(StringUtil.getStringFromFloatKeep2(
                    new BigDecimal(item.price).multiply(new BigDecimal(item.quantity)).doubleValue()));
            mName.setText(StringUtil.showMessage(item.name));
            if (item.quantity == 0) {
                mMinusImage.setSelected(true);
                mMinusImage.setEnabled(false);
            } else if (item.quantity == 9999) {
                mPlusImage.setSelected(true);
                mPlusImage.setEnabled(false);
            }
            mSelectImage.setSelected(value);
        }
    }

    public ImageView getImageViewSelect() {
        return mSelectImage != null ? mSelectImage : null;
    }

    public ImageView getImageViewMinus() {
        return mMinusImage != null ? mMinusImage : null;
    }

    public ImageView getImageViewPlus() {
        return mPlusImage != null ? mPlusImage : null;
    }

    public EditText getNums() {
        return mNumET != null ? mNumET : null;
    }

    public TextView getMoney() {
        return mMoneySum != null ? mMoneySum : null;
    }

    public void setOnDeleteListener(OnDeleteListener l) {
        deleteListener = l;
    }

    public void setOnclickItemListeners(OnClickItemListener l) {
        listener = l;
    }

    public interface OnClickItemListener {
        public void onClickSelectImage();

        public void onClickMinus();

        public void onClickPlus();

        public void onInputNums(int nowNum);

//        public void onInputNums(int nowNum,ShopCartItem mShopCartItem);
    }

    public interface OnDeleteListener {
        public void onDelete();
    }
}
