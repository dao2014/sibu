package com.socialbusiness.dev.orangebusiness.activity.order;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity.TabPosition;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.AddressListActivity;
import com.socialbusiness.dev.orangebusiness.adapter.PlaceAnOrderAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.FooterViewPlaceAnOrder;
import com.socialbusiness.dev.orangebusiness.component.MultiUploadPayImageLayout;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Address;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.DialogUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.SharedPreferencesUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * 下单
 */
public class PlaceAnOrderActivity extends BaseActivity implements ImageChooserListener {

    @Override
    public void onImageChosen(final ChosenImage image) {
        if (image != null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    hideProgressDialog();


                    mImageFilePath = image.getFilePathOriginal();

                    if (!TextUtils.isEmpty(mImageFilePath)) {
                        Uri uri = Uri.fromFile(new File(image.getFileThumbnailSmall()));
                        multi_upload_pay_image.loadVoucherImage(uri, mImageFilePath);
//                        mFooterView.loadVoucherImage(uri);
//
//
//                        if (ImageUtil.getBitmapsize(mImageFilePath) > 200) {
//                            Bitmap bitmap = ImageUtil.getimage2(mImageFilePath);
//                            ImageUtil.compressImage(bitmap, mImageFilePath);
//                        }
                    }

                }
            });
        }
    }

    @Override
    public void onError(String reason) {

    }

    public static final String TAG = "PlaceAnOrderActivity";

    private ListView mListView;
    private RelativeLayout mConsigneeLayout;
    private TextView mConsigneeName;
    private TextView mConsigneePhone;
    private TextView mConsigneeAddr;
    private TextView mMoneySum;
    private TextView mConfirm;

    private PlaceAnOrderAdapter mAdapter;
    private FooterViewPlaceAnOrder mFooterView;
    private List<ShopCartItem> mShopCartList;
    private boolean isSelectAll;
    private String money;
    private ShopCartDbHelper helper;

    //获取默认地址
    private List<Address> mAddressList = null;
    private Address defaultAddress = null;
    private InputMethodManager imm;
    private OnAddressRefresh onAddressRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_an_order);
        findView();
        setUp();
//        callAPIListAddress();
        setListeners();
//        setValues();

        onAddressRefresh = new OnAddressRefresh();
        IntentFilter filter = new IntentFilter(Constance.INTENT_ONADDRESSREFRESH_SECCUS);
        registerReceiver(onAddressRefresh, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onAddressRefresh);
    }

    public class OnAddressRefresh extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (Constance.INTENT_ONADDRESSREFRESH_SECCUS.equals(intent.getAction())) {
                callAPIListAddress();
            }
        }
    }

    private void setValues() {

        String freight = SharedPreferencesUtil.getString(getApplicationContext(), Constance.OrderFreight + loginUser.account);
        if (!StringUtil.DOUBLE_ZERO.equals(freight))
            mFooterView.getViewFooterViewPlacefreight().setText(freight);

        String payMoney = SharedPreferencesUtil.getString(getApplicationContext(), Constance.PayMoney + loginUser.account);
        if (!StringUtil.DOUBLE_ZERO.equals(payMoney))
            multi_upload_pay_image.setPayMoney(payMoney);

        String payRemark = SharedPreferencesUtil.getString(getApplicationContext(), Constance.PayRemark + loginUser.account);
        multi_upload_pay_image.setPayRemark(payRemark);

        String payImages = SharedPreferencesUtil.getString(getApplicationContext(), Constance.PayImages + loginUser.account);
        Log.e("palceanorder", "==" + payImages);
        ArrayList<String> urls = new Gson().fromJson(payImages, ArrayList.class);
        Log.e("palceanorder", "==" + urls);
        if (urls != null)
            multi_upload_pay_image.loadVoucherImage(urls, false);
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
//        mConsigneeAddr.setText(getResources().getString(R.string.delivery_address) + "");
        mConsigneeAddr.setText( "");
        mMoneySum.setText("0.00");
    }

    MultiUploadPayImageLayout multi_upload_pay_image;

    private int mChooserType;
    private ImageChooserManager mImageChooserManager;
    private String mImageFilePath;

    private void setUp() {
        setTitle(R.string.place_orders);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mShopCartList = (List<ShopCartItem>) getIntent().getSerializableExtra("orderlist");
        isSelectAll = getIntent().getBooleanExtra("isSelectAll", false);
        money = getIntent().getStringExtra("money");
        if (!TextUtils.isEmpty(money)) {
            mMoneySum.setText(money);
        }
        helper = new ShopCartDbHelper(this);
        mAdapter = new PlaceAnOrderAdapter(this);
        mAdapter.setShopCartItemList(mShopCartList);
        mFooterView = new FooterViewPlaceAnOrder(this);
        mListView.addFooterView(mFooterView);
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(0);

        multi_upload_pay_image = (MultiUploadPayImageLayout) mFooterView.findViewById(R.id.multi_upload_pay_image);

        getLoginUser();
    }

    public static final String EXPRESS = "express";
    public static final String REMARK = "remark";

    private void setListeners() {
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == mConfirm) {
//                    mConfirm.setEnabled(false);
//                    callAPIAddOrder();
//                    if (!mMoneySum.getText().toString().equals(StringUtil.getStringFromFloatKeep2(new BigDecimal(multi_upload_pay_image.getPayMoney()).doubleValue()))) {
//
//                        DialogUtil.showConfirm(PlaceAnOrderActivity.this, "", "您的付款金额跟总计金额不一致，是否继续？", "确定", "取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                startEnsure();
//                            }
//                        }, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        });
//                        return;
//
//                    }
                    startEnsure();
                } else if (v == mConsigneeLayout) {
                    Intent intent = new Intent(PlaceAnOrderActivity.this,
                            AddressListActivity.class);
                    intent.putExtra("from", TAG);
                    startActivityForResult(intent, ADDRESS_REQUEST_CODE);
                }
            }
        };

        mConsigneeLayout.setOnClickListener(listener);
        mConfirm.setOnClickListener(listener);

        this.multi_upload_pay_image.setListener(new MultiUploadPayImageLayout.UploadPVIClickListener() {
            @Override
            public void chosePayImage() {

                addImage();
            }

            @Override
            public void updatePayImage() {

            }
        });
        mFooterView.getViewFooterViewPlacefreight().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                SharedPreferencesUtil.putString(getApplicationContext(), Constance.OrderFreight+loginUser.account, s.toString());
                if (TextUtils.isEmpty(s)) {
                    mMoneySum.setText(money);
                    return;
                }
                mMoneySum.setText(StringUtil.getStringFromFloatKeep2(new BigDecimal(money).add(new BigDecimal(s.toString())).doubleValue()));
            }
        });

    }

    private final int ADDRESS_REQUEST_CODE = 100;
    User loginUser;

    private User getLoginUser() {
        loginUser = SettingsManager.getLoginUser();
        if (loginUser == null) {
            Intent toLoginIntent = new Intent(Constant.RECEIVER_NO_LOGIN);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(toLoginIntent);
        }
        return loginUser;
    }

    /**
     * 条转到 下单确认
     */
    private void startEnsure() {
        Intent intent = new Intent(this, PlaceAnOrderEnsureActivity.class);
        intent.putExtra("orderlist", (Serializable) mShopCartList);
        intent.putExtra("isSelectAll", isSelectAll);
        intent.putExtra(EXPRESS, mFooterView.getExpress());
        intent.putExtra(REMARK, mFooterView.getRemarks());
        intent.putExtra("money", mMoneySum.getText().toString().trim());
        intent.putExtra("address", defaultAddress);

        intent.putExtra(Constance.PayMoney, multi_upload_pay_image.getPayMoney());
        intent.putExtra(Constance.PayRemark, multi_upload_pay_image.getPayRemark());
        intent.putExtra(Constance.PayImages, multi_upload_pay_image.getImageUrls());
        intent.putExtra(Constance.OrderFreight, mFooterView.getFreightMoney());
//        savePreferences();
        startActivity(intent);
    }

    private void savePreferences() {
        new Thread() {
            @Override
            public void run() {
                SharedPreferencesUtil.putString(getApplicationContext(), Constance.OrderFreight + loginUser.account, mFooterView.getFreightMoney());
                SharedPreferencesUtil.putString(getApplicationContext(), Constance.PayMoney + loginUser.account, multi_upload_pay_image.getPayMoney());
                SharedPreferencesUtil.putString(getApplicationContext(), Constance.PayRemark + loginUser.account, multi_upload_pay_image.getPayRemark());
                SharedPreferencesUtil.putString(getApplicationContext(), Constance.PayImages + loginUser.account, new Gson().toJson(multi_upload_pay_image.getImageUrls()));
            }
        }.start();
    }

    private void addImage() {
        DialogUtil.showTakePhotoDialog(this, null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //take picture
                    mChooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
                    mImageChooserManager = new ImageChooserManager(PlaceAnOrderActivity.this,
                            ChooserType.REQUEST_CAPTURE_PICTURE,
                            Constant.PATH_IMAGE_TEMP_FOLDER,
                            true);
                    mImageChooserManager.setImageChooserListener(PlaceAnOrderActivity.this);
                    try {
                        mImageFilePath = mImageChooserManager.choose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //choose photo
                    mChooserType = ChooserType.REQUEST_PICK_PICTURE;
                    mImageChooserManager = new ImageChooserManager(PlaceAnOrderActivity.this,
                            ChooserType.REQUEST_PICK_PICTURE,
                            Constant.PATH_IMAGE_TEMP_FOLDER,
                            true);
                    mImageChooserManager.setImageChooserListener(PlaceAnOrderActivity.this);
                    try {
                        mImageFilePath = mImageChooserManager.choose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (RESULT_OK == resultCode) {
            Address addr = null;
            if (intent != null)
                addr = (Address) intent.getSerializableExtra("address");
            if (addr != null) {
                setAddressValue(addr);
                defaultAddress = addr;
            }

            if (requestCode == ChooserType.REQUEST_PICK_PICTURE
                    || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE) {
                //选择图片或拍照的result
                if (mImageChooserManager == null) {
                    return;
                }
                showProgressDialog();
                mImageChooserManager.submit(requestCode, intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mConsigneeName != null && TextUtils.isEmpty(mConsigneeName.getText().toString().trim())) {
            callAPIListAddress();
        }
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
        public void onEditChange(final ShopCartItem item) {
            mMoneySum.setText(StringUtil.getStringFromFloatKeep2(getTotalMoney()));
            new Thread() {
                @Override
                public void run() {
                    helper.updateShopCartPrice(item);
                }
            }.start();

        }
    };

    private double getTotalMoney() {
        BigDecimal moneySum = new BigDecimal(0f);
        for (int i = 0; i < mShopCartList.size(); i++) {
            moneySum = moneySum.add(new BigDecimal(mShopCartList.get(i).quantity)
                    .multiply(new BigDecimal(mShopCartList.get(i).price)));
        }
//        Log.e("==",mFooterView.getFreightMoney());
        moneySum = moneySum.add(new BigDecimal(mFooterView.getFreightMoney()));//加运费
//        Log.e("==",moneySum.toString());
        return moneySum.doubleValue();
    }

    private void callAPIAddOrder() {
        if (defaultAddress == null) {
            mConfirm.setEnabled(true);
            ToastUtil.show(PlaceAnOrderActivity.this, "请添加收货地址");
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
                        if (!TextUtils.isEmpty(response.message)) {
                            ToastUtil.show(PlaceAnOrderActivity.this, response.message);
                        }
                        if (isSelectAll) {
                            helper.clearShopCart();
                        } else if (mShopCartList != null) {
                            helper.deleteShopCardItems(mShopCartList);
                        }
                        Intent intent = new Intent();
                        intent.setClass(PlaceAnOrderActivity.this, MainActivity.class);
                        intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, TabPosition.Order.name());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
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
