package com.socialbusiness.dev.orangebusiness.activity.order;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity.TabPosition;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.ConfirmOrderAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.ConfirmOrderAdapter.OnItemDataChangedListener;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestAction;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.OrderConfirmFooter;
import com.socialbusiness.dev.orangebusiness.component.OrderConfirmFooter.OnClickConfirmListener;
import com.socialbusiness.dev.orangebusiness.component.OrderConfirmFooter.OnClickScanExpressCodeListener;
import com.socialbusiness.dev.orangebusiness.component.OrderConfirmFooter.OnFeeChangedListener;
import com.socialbusiness.dev.orangebusiness.component.OrderConfirmHeader;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.ListUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

//import com.socialbusiness.dev.orangebusiness.component.OrderConfirmFooter.OnTurnPurchaseListener;



public class SalesInputOrderDetailActivity extends BaseActivity {

    private static final int REQUEST_CODE_SCAN_ExPRESS_START = 11;
    private static final int REQUEST_CODE_SCAN_ExPRESS_END = 22;
    private static final int REQUEST_CODE_SCAN_PRODUCT_CODES = 33;

    private View rlAllLayout;

    private TextView mConsigneeName;
    private TextView mConsigneePhone;
    private TextView mConsigneeAddr;
    private ListView mListView;

    private OrderConfirmHeader mHeaderView;
    private OrderConfirmFooter mFooterView;
    private ConfirmOrderAdapter mAdapter;
    private List<Order> mListViewList;
    //保存二维码的Map，key为productId，value为二维码数组（临时保存）
    private HashMap<String, String[]> mScanResultCodes;

    private String tag;
    private String id;

    private Order mOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_order_confirm);
        setUp();
        findView();
        callAPIGetOrder();
        setListeners();
    }

    private void setUp() {
        Constance.setNull();

        mHeaderView = new OrderConfirmHeader(this);
        mFooterView = new OrderConfirmFooter(this);
        mAdapter = new ConfirmOrderAdapter(this);
        tag = getIntent().getStringExtra("tag");
        id = getIntent().getStringExtra("id");
        mAdapter.setTag(tag);
        if (!TextUtils.isEmpty(tag) && (SalesWaitingDeliverOrderActivity.TAG.equals(tag) || SalesWaitingConfirmOrderActivity.TAG.equals(tag))) {
            if (SalesWaitingConfirmOrderActivity.TAG.equals(tag))
                setTitle("确认订单");
            else setTitle("确认发货");
            showRightTxt(true);
            setRightTxt("取消订单", 0, Color.WHITE);
            setRightTxtOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Dialog cancelDialog = new AlertDialog.Builder(SalesInputOrderDetailActivity.this)
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

    private void callAPICancelOrder() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("ids", id);
        parameters.put("isSeller", "1");
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
                        sendBroadcast(new Intent(Constance.INTENT_ONCANCELORDER_SECCUS));
                        finish();
                        ToastUtil.show(getMyApplication(), response.message);
                    }
                });
    }

    private void findView() {
        rlAllLayout = findViewById(R.id.rlAllLayout);
        mConsigneeName = (TextView) this.findViewById(R.id.activity_seller_order_confirm_consignee_name);
        mConsigneePhone = (TextView) this.findViewById(R.id.activity_seller_order_confirm_consignee_phone);
        mConsigneeAddr = (TextView) this.findViewById(R.id.activity_seller_order_confirm_consignee_delivery_addr);
        mListView = (ListView) this.findViewById(R.id.activity_seller_order_confirm_list);

        mListView.addHeaderView(mHeaderView, null, false);
        mListView.addFooterView(mFooterView, null, false);
        mHeaderView.setTagToChange(tag);
        mFooterView.setTagToHide(tag);
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(0);
        rlAllLayout.setVisibility(View.GONE);
    }

    private void setListeners() {
        mFooterView.setOnFeeChangedListener(new OnFeeChangedListener() {

            @Override
            public void onFeeChanged(float fee) {
                mFooterView.setMoneySum(StringUtil.getStringFromFloatKeep2(getMoneySum() + fee));
            }
        });

        mFooterView.setOnClickConfirmListener(new OnClickConfirmListener() {

            @Override
            public void onConfirmOrder() {    //确认订单
                callApiConfirmOrder();
            }

            @Override
            public void onConfirmDeliver() {    //确认发货
                callApiConfirmDeliver();
            }
        });

        mAdapter.setOnItemDataChangedListener(new OnItemDataChangedListener() {

            @Override
            public void onItemDataChanged() {
                mScanResultCodes = null;    //更改数量或单价时，清空上次产品保存的二维码数组
                Constance.setNull();
                float fee = mFooterView.getFee();
                mFooterView.setMoneySum(StringUtil.getStringFromFloatKeep2(
                        new BigDecimal(getMoneySum()).add(new BigDecimal(fee)).doubleValue()));
            }
        });

        mFooterView.setOnClickScanExpressCodeListener(new OnClickScanExpressCodeListener() {

            @Override
            public void onClickScan(View v, boolean isStart) {
                Intent intent = new Intent(SalesInputOrderDetailActivity.this, MyCaptureActivity.class);
                startActivityForResult(intent,
                        isStart ? REQUEST_CODE_SCAN_ExPRESS_START : REQUEST_CODE_SCAN_ExPRESS_END);
            }
        });
        setOnClickBackBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tag.equals(SalesWaitingDeliverOrderActivity.TAG) && Constance.isCodeEdit) {
                    Dialog cancelDialog = new AlertDialog.Builder(SalesInputOrderDetailActivity.this)
                            .setMessage("您当前已录入了一部分商品二维码，返回将丢失已录入的商品二维码数据，是否继续？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SalesInputOrderDetailActivity.super.onBackPressed();
                                    Constance.setNull();
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
                } else {
                    finish();
                }
            }
        });
    }

    private void callAPIGetOrder() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("id", id);
        parameters.put("isSeller", "1");
        APIManager.getInstance(this).getOrder(parameters, getOrderType(),
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<Order>>() {

                    @Override
                    public void onResponse(RequestResult<Order> response) {
                        hideLoading();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            mOrder = response.data;
                            mAdapter.setOrder(mOrder);
                            setViewValues(mOrder);
                            mFooterView.setDetailValue(mOrder);
                            mHeaderView.setHeaderDate(mOrder);
                            rlAllLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private Order.OrderType getOrderType() {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        if (tag.equals(SalesWaitingConfirmOrderActivity.TAG)) {
            return Order.OrderType.OrderListTypeHandle;

        } else if (tag.equals(SalesWaitingDeliverOrderActivity.TAG)) {
            return Order.OrderType.OrderListTypeDeliver;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (tag.equals(SalesWaitingDeliverOrderActivity.TAG) && Constance.isCodeEdit) {
            Dialog cancelDialog = new AlertDialog.Builder(SalesInputOrderDetailActivity.this)
                    .setMessage("您当前已录入了一部分商品二维码，返回将丢失已录入的商品二维码数据，是否继续？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SalesInputOrderDetailActivity.super.onBackPressed();
                            Constance.setNull();
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
            return;
        }
        super.onBackPressed();
    }

    private void callApiConfirmOrder() {    //确认订单
        if (mOrder == null) {
            return;
        }
        showLoading();
        Order.OrderConfirm orderConfirm = new Order.OrderConfirm();
        orderConfirm.id = mOrder.id;
        orderConfirm.productIds = mOrder.productIds;
        orderConfirm.productPrice = (ArrayList<Float>) ListUtil.getFloatListFromMap(mOrder.productIds, mAdapter.getPriceMap());
        orderConfirm.productAmount = (ArrayList<Integer>) ListUtil.getIntegerListFromMap(mOrder.productIds, mAdapter.getQuantityMap());
        orderConfirm.express = mFooterView.getExpress();
        orderConfirm.freight = mFooterView.getFee();
        orderConfirm.sellerremark = mFooterView.getSellerRemark();

        APIManager.getInstance(this).confirmOrder(orderConfirm,
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
                        ToastUtil.show(SalesInputOrderDetailActivity.this, response.message);
                        finish();
//                        goToMainActivity();
                    }
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SalesInputOrderDetailActivity.this, MainActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, TabPosition.Order.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void callApiConfirmDeliver() {    //确认发货
        if (mOrder == null || mOrder.products == null) {
            return;
        }

        ArrayList<Product> products = new ArrayList<>();
        ArrayList<Float> priceList = (ArrayList<Float>) ListUtil.getFloatListFromMap(mOrder.productIds, mAdapter.getPriceMap());
        ArrayList<Integer> inputProductAmount = (ArrayList<Integer>) ListUtil.getIntegerListFromMap(mOrder.productIds, mAdapter.getQuantityMap());

        for (int i = 0; i < mOrder.products.size(); i++) {
            Product apiProduct = mOrder.products.get(i);
            Integer amount = inputProductAmount.get(i);

//            if (amount <= 0) {
//                ToastUtil.show(getApplicationContext(), apiProduct.name + "数量不能为0");
//                return;
//            }
            if (amount > 0) {
                String json = RequestAction.GSON.toJson(apiProduct, Product.class);
                Product product = RequestAction.GSON.fromJson(json, Product.class);
                product.price = priceList.get(i);
                product.amount = amount;
                products.add(product);
            }
        }

        mOrder.freight = mFooterView.getFee();
        mOrder.express = mFooterView.getExpress();
        mOrder.sellerremark = mFooterView.getSellerRemark();
        mOrder.expressStart = mFooterView.getExpressStart();
        mOrder.expressEnd = mFooterView.getExpressEnd();

        Intent intent = new Intent(this, ScanProductListActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_PRODUCT_LIST, products);
        intent.putExtra(Constant.EXTRA_KEY_ORDER_OBJ, mOrder);
        if (mScanResultCodes != null) {
            intent.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_CODES, mScanResultCodes);
        }
//        if (scanItems != null) {
//            intent.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_ITEMS, scanItems);
//            intent.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_STANDERS, selectStanders);
//        }
        startActivityForResult(intent, REQUEST_CODE_SCAN_PRODUCT_CODES);
    }

    private double getMoneySum() {
        List<Float> priceList = ListUtil.getFloatListFromMap(mOrder.productIds, mAdapter.getPriceMap());
        List<Integer> productAmount = ListUtil.getIntegerListFromMap(mOrder.productIds, mAdapter.getQuantityMap());
        BigDecimal moneySum = new BigDecimal(0f);
        if (priceList != null && productAmount != null) {
            for (int i = 0; i < priceList.size(); i++) {
                moneySum = moneySum.add(new BigDecimal(priceList.get(i)).multiply(new BigDecimal(productAmount.get(i))));
            }
        }
        return moneySum.doubleValue();
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

        mConsigneeAddr.setText(StringUtil.showMessage(orderAll.address.province)
                + StringUtil.showMessage(orderAll.address.city)
                + StringUtil.showMessage(orderAll.address.district)
                + StringUtil.showMessage(orderAll.address.detail));
    }

    //    public ArrayList<ScanItem> scanItems;
//    HashMap<String, ArrayList<StanderItem>> selectStanders ;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && mFooterView != null) {
            String code = data.getStringExtra(MyCaptureActivity.EXTRA_KEY_CONTENT);
            if (requestCode == REQUEST_CODE_SCAN_ExPRESS_START) {
                mFooterView.setExpressStart(code);
            } else if (requestCode == REQUEST_CODE_SCAN_ExPRESS_END) {
                mFooterView.setExpressEnd(code);
            } else if (requestCode == REQUEST_CODE_SCAN_PRODUCT_CODES) {
                mScanResultCodes = (HashMap<String, String[]>) data.getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_CODES);
//                scanItems=(ArrayList<ScanItem>)data.getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_ITEMS);
//                selectStanders=( HashMap<String, ArrayList<StanderItem>>)data.getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_STANDERS);
//                Log.e("onActivityResult", "==" + scanItems);
            }
        }
    }
}
