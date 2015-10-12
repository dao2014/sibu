package com.socialbusiness.dev.orangebusiness.activity.order;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.SalesOrderDetailAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.SalesOrderDetailAdapter.OnDataChangedListener;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestAction;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.SalesOrderFooterView;
import com.socialbusiness.dev.orangebusiness.component.SalesOrderFooterView.OnClickDateListener;
import com.socialbusiness.dev.orangebusiness.component.SalesOrderFooterView.OnClickNextListener;
import com.socialbusiness.dev.orangebusiness.component.SalesOrderFooterView.OnClickScan2DimensionCodeListener;
import com.socialbusiness.dev.orangebusiness.component.SalesOrderFooterView.OnFreightChangedListener;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderDeliver.ProductItem;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderTrade;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderTrade.ProductItemFromServer;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class SalesOrderDetailActivity extends BaseActivity {

    private static final int REQUEST_CODE_SCAN_ExPRESS_START = 11;
    private static final int REQUEST_CODE_SCAN_ExPRESS_END = 22;
    private static final int REQUEST_CODE_SCAN_PRODUCT_CODES = 33;

    //	保存二维码的Map，key为productId，value为二维码数组（临时保存）
    private HashMap<String, String[]> mScanResultCodes;

    private RelativeLayout rlAllLayout;
    private SalesOrderFooterView mFooterView;
    private RelativeLayout mAddProductLayout;
    private SalesOrderDetailAdapter mAdapter;
    private ListView mListView;

    private List<ProductItemFromServer> mProductList;
    private Order.OrderTradeAdd mOrderTradeAdd;

    private String id;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    private Order.OrderTradeType tradeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order_detail);
        findViews();
        setUp();
        setListeners();
    }

    private void setUp() {
        Constance.setNull();
        mAdapter = new SalesOrderDetailAdapter(this);
        mFooterView = new SalesOrderFooterView(this);
        mListView.addFooterView(mFooterView, null, false);
        mListView.setDividerHeight(0);
        mListView.setAdapter(mAdapter);
        id = getIntent().getStringExtra("id");
        tradeType = (Order.OrderTradeType) getIntent().getSerializableExtra(Constant.EXTRA_KEY_TRADETYPE);
        mFooterView.hideViews(tradeType);
        setTitle(getResources().getString(tradeType != null && tradeType == Order.OrderTradeType.Sell
                ? R.string.sales_order_detail : R.string.purchase_order_detail));
        mOrderTradeAdd = new Order.OrderTradeAdd();
        if (!TextUtils.isEmpty(id)) {
            rlAllLayout.setVisibility(View.GONE);
            callAPIGetSalesOrder();
        }
    }

    private void findViews() {
        mAddProductLayout = (RelativeLayout) findViewById(R.id.activity_sales_order_detail_add_product_layout);
        mListView = (ListView) findViewById(R.id.activity_sales_order_detail_listview);
        rlAllLayout = (RelativeLayout) findViewById(R.id.rlAllLayout);
    }

    private void setListeners() {
        mAddProductLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SalesOrderDetailActivity.this, AddProductActivity.class);
                startActivityForResult(intent, Constant.REQUESTCODE_ADD_PRODUCT);
            }
        });

        mAdapter.setOnDataChangedListener(new OnDataChangedListener() {

            @Override
            public void onDataChanged() {
                mScanResultCodes = null; //更改数量或单价时，清空上次产品保存的二维码数组
               Constance.setNull();
                if (mProductList == null) {
                    return;
                }
                mFooterView.setMoneySum(getTotalMoney());
            }
        });

        mFooterView.setOnFreightChangedListener(new OnFreightChangedListener() {

            @Override
            public void onFreightChange() {
                if (mProductList == null) {
                    return;
                }
                mFooterView.setMoneySum(getTotalMoney());
            }
        });

        mFooterView.setOnClickDateListener(new OnClickDateListener() {

            @Override
            public void onClickDate() {
                doSelectDateAndTime();
            }
        });

        mFooterView.setOnClickScan2DCListener(new OnClickScan2DimensionCodeListener() {

            @Override
            public void OnClickScan(View v, boolean isStart) {
                Intent intent = new Intent(SalesOrderDetailActivity.this, MyCaptureActivity.class);
                startActivityForResult(intent,
                        isStart ? REQUEST_CODE_SCAN_ExPRESS_START : REQUEST_CODE_SCAN_ExPRESS_END);
            }
        });

        mFooterView.setOnclickNextListener(new OnClickNextListener() {

            @Override
            public void onClickNext() {
                doNext();
            }
        });
    }

    private void doSelectDateAndTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_date_time_dialog, null);
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.view_date_time_datepicker);
        TimePicker timePicker = (TimePicker) view.findViewById(R.id.view_date_time_timepicker);
        builder.setView(view);
        initDateAndTime();
        builder.setTitle("请选择日期和时间");
        timePicker.setIs24HourView(true);
        datePicker.init(mYear, mMonth, mDay, new OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
            }
        });

        timePicker.setCurrentHour(mHour);
        timePicker.setCurrentMinute(mMinute);
        timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
            }
        });

        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mFooterView != null) {
                    mFooterView.displayDateAndTime(mYear, mMonth, mDay, mHour, mMinute);
                }
            }
        })
                .setNegativeButton("取消", null);
        builder.create();
        builder.show();
    }

    private void initDateAndTime() {
        String dateTime = mFooterView.getOrderDate();
        if (dateTime == null) {
            setCurrentTime();
        } else {
            cuteDateTimeString(dateTime);
        }
    }

    private void setCurrentTime() { //当前系统时间
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        mHour = cal.get(Calendar.HOUR_OF_DAY);
        mMinute = cal.get(Calendar.MINUTE);
    }

    private void cuteDateTimeString(String dateTimeStr) {
        String[] dateTimeArray = dateTimeStr.split(" ");
        if (dateTimeArray != null && dateTimeArray.length == 2) {
            if (dateTimeArray[0] != null && dateTimeArray[1] != null) {
                String[] dateArray = dateTimeArray[0].split("-");
                String[] timeArray = dateTimeArray[1].split(":");
                try {
                    mYear = Integer.parseInt(dateArray[0]);
                    mMonth = Integer.parseInt(dateArray[1]) - 1;
                    mDay = Integer.parseInt(dateArray[2]);
                    mHour = Integer.parseInt(timeArray[0]);
                    mMinute = Integer.parseInt(timeArray[1]);
                    return;
                } catch (Exception e) {
                    setCurrentTime();
                    return;
                }
            }
        }
        setCurrentTime();
    }

    private void doNext() {
        if (mProductList == null || mProductList.size() == 0) {
            ToastUtil.show(SalesOrderDetailActivity.this, "请至少添加一件产品");
            return;
        }
        initOrderSalesAdd();
        Intent intent = new Intent(this, ScanProductListActivity.class);
        if (mScanResultCodes != null) {
            intent.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_CODES, mScanResultCodes);
        }
//        if (scanItems != null) {
//            intent.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_ITEMS, scanItems);
//            intent.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_STANDERS, selectStanders);
//        }
        intent.putExtra(Constant.EXTRA_KEY_PRODUCT_LIST, getProductList());
        intent.putExtra(Constant.EXTRA_KEY_PRODUCT_ITEM_LIST, getProductItemData());
        intent.putExtra(Constant.EXTRA_KEY_ORDER_SALES_ADD_OBJ, mOrderTradeAdd);
        intent.putExtra(Constant.EXTRA_KEY_TRADETYPE, tradeType);
        startActivityForResult(intent, REQUEST_CODE_SCAN_PRODUCT_CODES);
    }

    private ArrayList<Product> getProductList() {
        ArrayList<Product> list = new ArrayList<>();
        for (int i = 0; i < mProductList.size(); i++) {
            if (mProductList.get(i).product != null) {
                Product apiProduct = mProductList.get(i).product;
                Integer amount = mProductList.get(i).amount;
                if (amount > 0) {
                    String json = RequestAction.GSON.toJson(apiProduct, Product.class);
                    Product product = RequestAction.GSON.fromJson(json, Product.class);
                    product.price = mProductList.get(i).price;
                    product.amount = amount;
                    list.add(product);
                }
            }
        }
        return list;
    }

    private ArrayList<ProductItem> getProductItemData() {
        ArrayList<ProductItem> itemList = new ArrayList<>();
        for (int i = 0; i < mProductList.size(); i++) {
            ProductItem item = new ProductItem();
            item.amount = mProductList.get(i).amount;
            item.price = mProductList.get(i).price;
            item.productId = mProductList.get(i).productId;
            itemList.add(item);
        }
        return itemList;
    }

    private void initOrderSalesAdd() { //初始化mOrderSalesAdd的一些属性
        if (mOrderTradeAdd == null || mFooterView == null) {
            return;
        }
        if (!TextUtils.isEmpty(id)) {
            mOrderTradeAdd.id = id;
        }
        mOrderTradeAdd.orderDate = mFooterView.getOrderDate();
        mOrderTradeAdd.express = mFooterView.getFreightWay();
        mOrderTradeAdd.expressStart = mFooterView.getStart2DC();
        mOrderTradeAdd.expressEnd = mFooterView.getEnd2DC();
        mOrderTradeAdd.freight = mFooterView.getFee();
        mOrderTradeAdd.payMoney = mFooterView.getPayMoney();

        if (tradeType == Order.OrderTradeType.Sell) {
            mOrderTradeAdd.name = mFooterView.getCustomerName();
            mOrderTradeAdd.username = mFooterView.getRecipient();
            mOrderTradeAdd.phone = mFooterView.getPhone();
            mOrderTradeAdd.postCode = mFooterView.getPostcode();
            mOrderTradeAdd.address = mFooterView.getAddress();
        }
    }

    private void callAPIGetSalesOrder() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("id", id);
        APIManager.getInstance(this).getSalesOrder(tradeType, parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<OrderTrade>>() {

                    @Override
                    public void onResponse(RequestResult<OrderTrade> response) {
                        hideLoading();
                        if (hasError(response) || response.data == null) {
                            return;
                        }
                        setMScanResultCodes(response.data.products);
                        mProductList = response.data.products;
                        mAdapter.setProducts(mProductList);
                        mAdapter.notifyDataSetChanged();
                        mFooterView.setData(response.data);
                        rlAllLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void setMScanResultCodes(ArrayList<ProductItemFromServer> products) {
        if (products == null) {
            return;
        }
        mScanResultCodes = new HashMap<>();
        for (int i = 0; i < products.size(); i++) {
            mScanResultCodes.put(products.get(i).productId,
                    products.get(i).qrcodeList.toArray(new String[0]));
        }
    }

    private double getTotalMoney() {
        BigDecimal moneySum = new BigDecimal(0f);
        for (int i = 0; i < mProductList.size(); i++) {
            moneySum = moneySum.add(new BigDecimal(mProductList.get(i).amount)
                    .multiply(new BigDecimal(mProductList.get(i).price)));
        }
        return moneySum.doubleValue();
    }

    //    public ArrayList<ScanItem> scanItems;
//    HashMap<String, ArrayList<StanderItem>> selectStanders ;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String code = data.getStringExtra(MyCaptureActivity.EXTRA_KEY_CONTENT);
            if (mFooterView != null && !TextUtils.isEmpty(code)) {
                if (requestCode == REQUEST_CODE_SCAN_ExPRESS_START) {
                    mFooterView.setStart2DCText(code);
                } else if (requestCode == REQUEST_CODE_SCAN_ExPRESS_END) {
                    mFooterView.setEnd2DCText(code);
                }
            }

            if (requestCode == REQUEST_CODE_SCAN_PRODUCT_CODES) {
                mScanResultCodes = (HashMap<String, String[]>) data.getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_CODES);
//                scanItems=(ArrayList<ScanItem>)data.getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_ITEMS);
//                selectStanders=( HashMap<String, ArrayList<StanderItem>>)data.getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_STANDERS);
            }

            if (requestCode == Constant.REQUESTCODE_ADD_PRODUCT) {
                ArrayList<Product> products = (ArrayList<Product>) data.getSerializableExtra(Constant.EXTRA_KEY_PRODUCT_LIST);
                if (products != null) {
                    if (mProductList == null) {
                        mProductList = toProductItemFromServer(products);
                        mAdapter.setProducts(mProductList);
                    } else {
                        checkToAdd(products);
                        if (products.size() > 0) {
                            mProductList.addAll(toProductItemFromServer(products));
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    mFooterView.setMoneySum(getTotalMoney());
                }
            }
        }
    }

    private void checkToAdd(List<Product> list) {
        for (int i = 0; i < mProductList.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).id.equals(mProductList.get(i).productId)) {
                    mProductList.get(i).amount++;
                    list.remove(j);
                    j--;
                }
            }
        }
    }

    /**
     * List<Product>转换成List<ProductItemFromServer>
     *
     * @param list Product产品列表
     * @return
     */
    private List<ProductItemFromServer> toProductItemFromServer(List<Product> list) {
        List<ProductItemFromServer> products = new ArrayList<ProductItemFromServer>();
        for (int i = 0; i < list.size(); i++) {
            ProductItemFromServer item = new ProductItemFromServer(list.get(i));
            products.add(item);
        }
        return products;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constance.setNull();
    }
}
