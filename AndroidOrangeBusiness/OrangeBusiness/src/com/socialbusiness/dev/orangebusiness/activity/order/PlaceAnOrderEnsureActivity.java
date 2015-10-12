package com.socialbusiness.dev.orangebusiness.activity.order;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity.TabPosition;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.AddressListActivity;
import com.socialbusiness.dev.orangebusiness.adapter.PlaceAnOrderEnsureAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.FooterViewPlaceAnOrderEnsure;
import com.socialbusiness.dev.orangebusiness.component.MultiUploadPayImageLayout;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.model.Address;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.DialogUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.List;


/**
 * 下单确认
 */
public class PlaceAnOrderEnsureActivity extends BaseActivity {

    public static final String TAG = "PlaceAnOrderEnsureActivity";

    private ListView mListView;
    private RelativeLayout mConsigneeLayout;
    private TextView mConsigneeName;
    private TextView mConsigneePhone;
    private TextView mConsigneeAddr;
    private TextView mMoneySum;
    private TextView mConfirm;

    private PlaceAnOrderEnsureAdapter mAdapter;
    private FooterViewPlaceAnOrderEnsure mFooterView;
    private List<ShopCartItem> mShopCartList;
    private boolean isSelectAll;
    private String money;
    private ShopCartDbHelper helper;

    //获取默认地址
    private List<Address> mAddressList = null;
    private Address defaultAddress = null;
    private InputMethodManager imm;

    private ViewGroup root;
    private View bottom;
    private View top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_an_order_ensure);
        findView();
        setUp();
//        callAPIListAddress();
        setListeners();

    }

    private void findView() {
        mListView = (ListView) findViewById(R.id.activity_place_an_order_listview);
        mConsigneeLayout = (RelativeLayout) findViewById(R.id.activity_place_an_order_top);
        mConsigneeName = (TextView) findViewById(R.id.activity_place_an_order_consignee_name);
        mConsigneePhone = (TextView) findViewById(R.id.activity_place_an_order_consignee_phone);
        mConsigneeAddr = (TextView) findViewById(R.id.activity_place_an_order_consignee_delivery_addr);
        mMoneySum = (TextView) findViewById(R.id.activity_place_an_order_bottom_sum_value);
        mConfirm = (TextView) findViewById(R.id.activity_place_an_order_bottom_confirm);

        mConsigneeName.setText("");
        mConsigneePhone.setText("");
        mConsigneeAddr.setText("");
        mMoneySum.setText("0.00");

        root = (ViewGroup) findViewById(R.id.place_an_order_ensure_root);
        bottom = findViewById(R.id.activity_place_an_order_bottom_layout);
        top = findViewById(R.id.place_an_order_ensure_add_layout);

        root.removeView(top);
        root.removeView(bottom);
    }

    MultiUploadPayImageLayout multi_upload_pay_image;

    private void setUp() {
        setTitle(R.string.place_orders_ensure);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mShopCartList = (List<ShopCartItem>) getIntent().getSerializableExtra("orderlist");
        isSelectAll = getIntent().getBooleanExtra("isSelectAll", false);
        money = getIntent().getStringExtra("money");

        helper = new ShopCartDbHelper(this);
        mAdapter = new PlaceAnOrderEnsureAdapter(this);
        mAdapter.setShopCartItemList(mShopCartList);
        mFooterView = new FooterViewPlaceAnOrderEnsure(this);
        mListView.addFooterView(mFooterView);
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(0);

        mMoneySum = (TextView) mFooterView.findViewById(R.id.activity_place_an_order_bottom_sum_value);
        if (!TextUtils.isEmpty(money)) {
            mMoneySum.setText(money);
        }
        mFooterView.setExpressValue(getIntent().getStringExtra(PlaceAnOrderActivity.EXPRESS));
        mFooterView.setRemarksValue(getIntent().getStringExtra(PlaceAnOrderActivity.REMARK));

        defaultAddress = (Address) getIntent().getSerializableExtra("address");
        setAddressValue(defaultAddress);

        //增加footer，header
        mFooterView.addChildView(bottom);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        top.setLayoutParams(lp);
        mListView.addHeaderView(top);

        multi_upload_pay_image = (MultiUploadPayImageLayout) mFooterView.findViewById(R.id.multi_upload_pay_image);

        mFooterView.setFreightMoney(getIntent().getStringExtra(Constance.OrderFreight));
        multi_upload_pay_image.setPayMoney(getIntent().getStringExtra(Constance.PayMoney));
        multi_upload_pay_image.setPayRemark(getIntent().getStringExtra(Constance.PayRemark));
        this.imageUrls = (List<String>) getIntent().getSerializableExtra(Constance.PayImages);
        multi_upload_pay_image.loadVoucherImage(imageUrls);
        multi_upload_pay_image.setAllEnabled();
    }

    List<String> imageUrls;

    private void setListeners() {
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == mConfirm) {
                    mConfirm.setEnabled(false);
                    callAPIAddOrder();
                } else if (v == mConsigneeLayout) {
                    Intent intent = new Intent(PlaceAnOrderEnsureActivity.this,
                            AddressListActivity.class);
                    intent.putExtra("from", TAG);
                    startActivityForResult(intent, 100);
                }
            }
        };

//        mConsigneeLayout.setOnClickListener(listener);
        mConfirm.setOnClickListener(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (RESULT_OK == resultCode) {
            Address addr = (Address) intent.getSerializableExtra("address");
            if (addr != null) {
                setAddressValue(addr);
                defaultAddress = addr;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mConsigneeName != null && TextUtils.isEmpty(mConsigneeName.getText().toString().trim())) {
//            callAPIListAddress();
//        }
    }

    private void callAPIListAddress() {
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", "1");
        APIManager.getInstance(this).listAddress(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showException(error);
                    }
                }, new Response.Listener<RequestListResult<Address>>() {

                    @Override
                    public void onResponse(RequestListResult<Address> response) {
                        if (hasError(response)) {
                            return;
                        }
                        mAddressList = response.data;
                        setDefaultAddress(mAddressList);
                        setAddressValue(defaultAddress);
                    }
                });
    }

    public interface OnEditTextChange {
        public void onEditChange(ShopCartItem item);
    }

    public OnEditTextChange onEditTextChange = new OnEditTextChange() {
        @Override
        public void onEditChange(ShopCartItem item) {
            mMoneySum.setText(StringUtil.getStringFromFloatKeep2(getTotalMoney()));
            helper.updateShopCartPrice(item);
        }
    };

    private double getTotalMoney() {
        BigDecimal moneySum = new BigDecimal(0f);
        for (int i = 0; i < mShopCartList.size(); i++) {
            moneySum = moneySum.add(new BigDecimal(mShopCartList.get(i).quantity)
                    .multiply(new BigDecimal(mShopCartList.get(i).price)));
        }
        return moneySum.doubleValue();
    }

    private void callAPIAddOrder() {
        if (defaultAddress == null) {
            mConfirm.setEnabled(true);
            ToastUtil.show(PlaceAnOrderEnsureActivity.this, "请添加收货地址");
            return;
        }
        Order.OrderAdd orderAdd = new Order.OrderAdd();
        orderAdd.addressId = defaultAddress.id;
        orderAdd.remark = mFooterView.getRemarks();
        orderAdd.express = mFooterView.getExpress();
        for (int i = 0; i < mShopCartList.size(); i++) {
            orderAdd.productIds.add(mShopCartList.get(i).id);
            orderAdd.productAmount.add(mShopCartList.get(i).quantity);

            orderAdd.productPrice.add(mShopCartList.get(i).price);
        }
        orderAdd.freight=mFooterView.getFreightMoney();
        orderAdd.payMoney=Float.valueOf(multi_upload_pay_image.getPayMoney());
        orderAdd.payRemark=multi_upload_pay_image.getPayRemark();
        orderAdd.payImages=this.imageUrls;
        showLoading();
        APIManager.getInstance(this).addOrder(orderAdd,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mConfirm.setEnabled(true);
                        hideLoading();
                        hideNoData();
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<?>>() {

                    @Override
                    public void onResponse(RequestResult<?> response) {
                        hideLoading();
                        hideNoData();

                        if (hasError(response)) {
                            mConfirm.setEnabled(true);
                            return;
                        }
//                        if (!TextUtils.isEmpty(response.message)) {
//                            ToastUtil.show(PlaceAnOrderEnsureActivity.this, response.message);
//                        }
                        if (isSelectAll) {
                            helper.clearShopCart();
                        } else if (mShopCartList != null) {
                            helper.deleteShopCardItems(mShopCartList);
                        }

                        DialogUtil.showConfirm(PlaceAnOrderEnsureActivity.this, "", "您的订单已经提交成功，请等待上级确认", "确定", null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent();
                                intent.setClass(PlaceAnOrderEnsureActivity.this, MainActivity.class);
                                intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, TabPosition.Order.name());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }, null).show();

                    }
                });
    }

    private void setDefaultAddress(List<Address> list) {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isDefault) {
                    defaultAddress = list.get(i);
                    break;
                }
            }

            if (defaultAddress == null && list.size() > 0) {
                defaultAddress = list.get(0);
            }
        }
    }

    private void setAddressValue(Address addr) {
        if (addr != null) {
            mConsigneeName.setText(addr.name);
            mConsigneePhone.setText(addr.phone);
            mConsigneeAddr.setText(StringUtil.showMessage(addr.province) + StringUtil.showMessage(addr.city)
                    + StringUtil.showMessage(addr.district) + StringUtil.showMessage(addr.detail));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN
                || ev.getAction() == MotionEvent.ACTION_MOVE
                || ev.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
            View v = getCurrentFocus();
            if (AndroidUtil.isShouldHideInput(v, ev) && imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            return super.dispatchTouchEvent(ev);
        }

        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

}
