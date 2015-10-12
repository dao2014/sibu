package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.socialbusiness.dev.orangebusiness.R;

public class FooterViewPlaceAnOrder extends LinearLayout {

    private EditText mFreightWay;
    private EditText mRemarksET;
    private EditText view_footerview_place_an_order_freight;


    public EditText getViewFooterViewPlacefreight() {
        return view_footerview_place_an_order_freight;
    }

    public FooterViewPlaceAnOrder(Context context) {
        super(context);
        initView();
    }

    public FooterViewPlaceAnOrder(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        findView(inflater.inflate(R.layout.view_footerview_place_an_order, this));
    }

    private void findView(View view) {
        mFreightWay = (EditText) view.findViewById(R.id.view_footerview_place_an_order_freight_way);
        mRemarksET = (EditText) view.findViewById(R.id.view_footerview_place_an_order_remarksET);
        view_footerview_place_an_order_freight = (EditText) view.findViewById(R.id.view_footerview_place_an_order_freight);

    }

    public String getFreightMoney() {
        return TextUtils.isEmpty(view_footerview_place_an_order_freight.getText().toString().trim()) ? "0.00" : view_footerview_place_an_order_freight.getText().toString().trim();
    }

    public String getRemarks() {
        return mRemarksET != null ? mRemarksET.getText().toString().trim() : null;
    }

    public String getExpress() {
        return mFreightWay != null ? mFreightWay.getText().toString().trim() : null;
    }

    public void setRemarksETEnable(boolean value) {
        if (mRemarksET != null) {
            mRemarksET.setEnabled(value);
        }
    }

    public void setExpressETEnable(boolean value) {
        if (mFreightWay != null) {
            mFreightWay.setEnabled(value);
        }
    }

    public void setExpressValue(String str) {
        if (mFreightWay != null && !TextUtils.isEmpty(str)) {
            mFreightWay.setText(str);
        }
    }

    public void setRemarksValue(String str) {
        if (mRemarksET != null && !TextUtils.isEmpty(str)) {
            mRemarksET.setText(str);
        }
    }
}
