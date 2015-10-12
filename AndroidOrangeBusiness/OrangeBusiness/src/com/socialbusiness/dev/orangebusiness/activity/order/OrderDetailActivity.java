package com.socialbusiness.dev.orangebusiness.activity.order;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity.TabPosition;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.OrderDetailAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.KeyBordListenerRelativeLayout;
import com.socialbusiness.dev.orangebusiness.component.OrderConfirmHeader;
import com.socialbusiness.dev.orangebusiness.component.OrderDetailFooterView;
import com.socialbusiness.dev.orangebusiness.component.OrderDetailFooterView.UploadPVIClickListener;
import com.socialbusiness.dev.orangebusiness.fragment.order.DeliverDoneFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.ReceiptedFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingConfirmFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingDeliverFragment;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderType;
import com.socialbusiness.dev.orangebusiness.util.DialogUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.io.File;
import java.util.Hashtable;

public class OrderDetailActivity extends BaseActivity implements ImageChooserListener {

    private KeyBordListenerRelativeLayout rlAllLayout;
    private TextView mConsigneeName;
    private TextView mConsigneePhone;
    private TextView mConsigneeAddr;
    private ListView mListView;


    private OrderConfirmHeader mHeaderView;
    private OrderDetailFooterView mFooterView;
    private OrderDetailAdapter mAdapter;
    private View orderDetailTop;
    private String id;

    private String tag;

    private int mChooserType;
    private ImageChooserManager mImageChooserManager;
    private String mImageFilePath;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        setUp();
        findView();
        callAPIGetOrder();
        Log.e("OrderDetailActivity", "==oncreate");
    }

    private void setUp() {
        setTitle(R.string.order_detail);
        mHeaderView = new OrderConfirmHeader(this);
        mAdapter = new OrderDetailAdapter(this);
        mFooterView = new OrderDetailFooterView(this);
        id = getIntent().getStringExtra("id");
        tag = getIntent().getStringExtra("tag");
        mHeaderView.setTagToChange(tag);
    }

    private void findView() {
        rlAllLayout = (KeyBordListenerRelativeLayout) findViewById(R.id.rlAllLayout);
        mConsigneeName = (TextView) findViewById(R.id.activity_order_detail_consignee_name);
        mConsigneePhone = (TextView) findViewById(R.id.activity_order_detail_consignee_phone);
        mConsigneeAddr = (TextView) findViewById(R.id.activity_order_detail_consignee_delivery_addr);
        mListView = (ListView) findViewById(R.id.activity_order_detail_listview);

        mListView.addHeaderView(mHeaderView, null, false);
        mListView.addFooterView(mFooterView, null, false);

        mHeaderView.setTagToChange(tag);
        mFooterView.setTagToHide(tag);
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(0);
        rlAllLayout.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(tag)) {
            if (Waiting2PayFragment.TAG.equals(tag)) {
                setListeners();
            } else if (DeliverDoneFragment.TAG.equals(tag)) {
                setConfirmListener();
            }

            if (SalesWaitingDeliverOrderActivity.TAG.equals(tag) ||ReceiptedActivity.TAG.equals(tag) ||SalesDeliveredOrderActivity.TAG.equals(tag) ||SalesWaiting2PayOrderActivity.TAG.equals(tag)) {
                showRightTxt(true);
                setRightTxt("取消订单", 0, Color.WHITE);
                setRightTxtOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Dialog cancelDialog = new AlertDialog.Builder(OrderDetailActivity.this)
                                .setMessage("确定要取消订单吗？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        callAPICancelOrder();

                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create();
                        Window window = cancelDialog.getWindow();
                        WindowManager.LayoutParams lp = window.getAttributes();
                        lp.alpha = 0.8f;
                        window.setAttributes(lp);
                        cancelDialog.show();
                    }
                });
            }
        }
        orderDetailTop = findViewById(R.id.activity_order_detail_top);

        rlAllLayout.setOnKeyBordStateChange(new KeyBordListenerRelativeLayout.OnKeyBordStateChange() {
            @Override
            public void onKeyBordHide() {
                orderDetailTop.setVisibility(View.VISIBLE);
            }

            @Override
            public void onKeyBordShow() {
                orderDetailTop.setVisibility(View.GONE);
            }
        });

    }

    private void callAPICancelOrder() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("ids", id);
        parameters.put("isSeller","1");
        APIManager.getInstance(getMyApplication()).cancelOrder(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<?>>() {

                    @Override
                    public void onResponse(RequestResult<?> response) {
                        hideLoading();
                        if (hasError(response)) {
                            return;
                        }
                        finish();
                        ToastUtil.show(getMyApplication(), response.message);
                    }
                });
    }

    private void setListeners() {
        mFooterView.setUploadClickListener(new UploadPVIClickListener() {

            @Override
            public void chosePayImage() {
                if (mFooterView.ismUploadPVIVisi(tag)) {
                    addImage();
                }
            }

            @Override
            public void updatePayImage() {
//                if (!TextUtils.isEmpty(mImageFilePath)) {
                updateImage();
//                } else {
//                    ToastUtil.show(OrderDetailActivity.this, "请选择要上传的凭证图片");
//                }
            }
        });
    }

    private void setConfirmListener() {
        mFooterView.setConfirmReceiptedListener(new OrderDetailFooterView.ConfirmReceiptedListener() {

            @Override
            public void onConfirm(String orderId) {
                if (!TextUtils.isEmpty(orderId)) {
                    doConfirmReceiptedOrder(orderId);
                }
            }
        });
    }

    private void doConfirmReceiptedOrder(String orderId) {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("ids", orderId);
        APIManager.getInstance(this).confirmReceivedOrder(parameters, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                showException(error);
            }
        }, new Response.Listener<RequestResult<?>>() {

            @Override
            public void onResponse(RequestResult<?> response) {
//                hideLoading();
                if (hasError(response)) {
                    hideLoading();
                    return;
                }
                ToastUtil.show(OrderDetailActivity.this, response.message);
                mListView.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        hideLoading();
                        setResult(Constant.RESULTCODE_OK);
                        finish();
                    }
                }, 1000);
            }
        });
    }

    private void addImage() {
        DialogUtil.showTakePhotoDialog(this, null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //take picture
                    mChooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
                    mImageChooserManager = new ImageChooserManager(OrderDetailActivity.this,
                            ChooserType.REQUEST_CAPTURE_PICTURE,
                            Constant.PATH_IMAGE_TEMP_FOLDER,
                            true);
                    mImageChooserManager.setImageChooserListener(OrderDetailActivity.this);
                    try {
                        mImageFilePath = mImageChooserManager.choose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //choose photo
                    mChooserType = ChooserType.REQUEST_PICK_PICTURE;
                    mImageChooserManager = new ImageChooserManager(OrderDetailActivity.this,
                            ChooserType.REQUEST_PICK_PICTURE,
                            Constant.PATH_IMAGE_TEMP_FOLDER,
                            true);
                    mImageChooserManager.setImageChooserListener(OrderDetailActivity.this);
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("chooser_type", mChooserType);
        outState.putString("media_path", mImageFilePath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("chooser_type")) {
                mChooserType = savedInstanceState.getInt("chooser_type");
            }
            if (savedInstanceState.containsKey("media_path")) {
                mImageFilePath = savedInstanceState.getString("media_path");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private Order temp;

    private void callAPIGetOrder() {
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("id", id);
        parameters.put("isSeller", getIsSeller());
        APIManager.getInstance(this).getOrder(parameters, getOrderType(),
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<Order>>() {

                    @Override
                    public void onResponse(RequestResult<Order> response) {
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            orderId = response.data.id;
                            temp = response.data;
                            mAdapter.setOrder(response.data);
                            setViewValues(response.data);
                            mFooterView.setDetailValue(response.data);
                            mHeaderView.setHeaderDate(response.data);
                            rlAllLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private Order.OrderType getOrderType() {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        if (tag.equals(SalesWaiting2PayOrderActivity.TAG)
                || tag.equals(Waiting2PayFragment.TAG)) {
            return Order.OrderType.OrderListTypePay;

        } else if (tag.equals(SalesDeliveredOrderActivity.TAG)
                || tag.equals(DeliverDoneFragment.TAG)) {
            return Order.OrderType.OrderListTypeDelivered;

        } else if (tag.equals(WaitingConfirmFragment.TAG)) {
            return OrderType.OrderListTypeHandle;

        } else if (tag.equals(WaitingDeliverFragment.TAG)) {
            return OrderType.OrderListTypeDeliver;

        } else if (tag.equals(ReceiptedActivity.TAG)
                || tag.equals(ReceiptedFragment.TAG)) {
            return OrderType.OrderListTypeReceived;
        }
        return null;
    }

    private String getIsSeller() {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        return tag.endsWith("Fragment") ? "0" : "1";
    }

    private void setViewValues(Order orderAll) {
        if (orderAll == null || orderAll.address == null) {
            return;
        }
        mConsigneeName.setText(StringUtil.showMessage(orderAll.address.name));
        mConsigneePhone.setText(StringUtil.showMessage(orderAll.address.phone));

        if ("市辖区".equals(orderAll.address.city)
                || "县".equals(orderAll.address.city)
                || "市辖县".equals(orderAll.address.city)) {//省级市
            mConsigneeAddr.setText(StringUtil.showMessage(orderAll.address.province)
                    + StringUtil.showMessage(orderAll.address.district)
                    + StringUtil.showMessage(orderAll.address.detail));
            return;
        }

        mConsigneeAddr.setText( StringUtil.showMessage(orderAll.address.province)
                + StringUtil.showMessage(orderAll.address.city)
                + StringUtil.showMessage(orderAll.address.district)
                + StringUtil.showMessage(orderAll.address.detail));
    }

    private void reinitializeImageChooser() {
        mImageChooserManager = new ImageChooserManager(this, mChooserType, "路径", true);
        mImageChooserManager.setImageChooserListener(this);
        mImageChooserManager.reinitialize(mImageFilePath);
    }

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
                        mFooterView.loadVoucherImage(uri, mImageFilePath);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ChooserType.REQUEST_PICK_PICTURE
                    || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE) {
                //选择图片或拍照的result
                if (mImageChooserManager == null) {
                    reinitializeImageChooser();
                }
                showProgressDialog();
                mImageChooserManager.submit(requestCode, data);
            }
        }
    }

    public void updateImage() {

        if (TextUtils.isEmpty(mFooterView.getPayMoney())) {
            ToastUtil.show(this, "付款金额不能为空");
            return;
        }
        if (StringUtil.DOUBLE_ZERO.equals(StringUtil.get2DecimalFromString(mFooterView.getPayMoney()))) {

            ToastUtil.show(this, "付款金额不能为零");
            return;
        }
        if(!mFooterView.isSelectImage()){
            ToastUtil.show(this, "您还没上传付款凭证");
            return;
        }
        if (!mFooterView.getMoneySum(temp,temp.freight).equals(StringUtil.get2DecimalFromString(mFooterView.getPayMoney()))) {

            DialogUtil.showConfirm(this, "", "您的付款金额跟总计金额不一致，是否继续提交？","确定","取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmPayOrder();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            return;

        }

        confirmPayOrder();
//        APIManager.getInstance(OrderDetailActivity.this).updatePayImage(parameters,
//                new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        hideLoading();
//                        showException(error);
//                    }
//                },
//                new Response.Listener<RequestResult<?>>() {
//
//                    @Override
//                    public void onResponse(RequestResult<?> response) {
//                        hideLoading();
//                        if (hasError(response)) {
//                            return;
//                        }
//                        ToastUtil.show(getApplicationContext(), "上传图片成功");
//                        goMainActivity();
//                    }
//                });
    }

    private void confirmPayOrder() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<>();
//        parameters.put("filePath", mImageFilePath);
        parameters.put("id", orderId);
        parameters.put("payMoney", mFooterView.getPayMoney());
        parameters.put("payRemark", mFooterView.getPayRemark());
        Log.e("==","=="+orderId+"=="+mFooterView.getPayMoney()+"=="+mFooterView.getPayRemark());
        APIManager.getInstance(this).confirmUploadPay(parameters, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                showException(error);
            }
        }, new Response.Listener<RequestResult<?>>() {
            @Override
            public void onResponse(RequestResult<?> response) {
                hideLoading();
                if (hasError(response)) {
                    return;
                }
                ToastUtil.show(getApplicationContext(), "提交成功");
                finish();
                //goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent();
        intent.setClass(OrderDetailActivity.this, MainActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, TabPosition.Order.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("OrderDetailActivity", "====onDestroy");
    }

    @Override
    public void onError(final String reason) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                hideProgressDialog();
            }
        });
    }

    @Override
    public void showLoading() {
        showProgressDialog().setCancelable(false);
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }
}
