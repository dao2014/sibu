package com.socialbusiness.dev.orangebusiness.activity.order;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.StanderSelectItemAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.headerlistview.HeaderListView;
import com.socialbusiness.dev.orangebusiness.component.headerlistview.SectionAdapter;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderDeliver;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderDeliver.ProductItem;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderDeliver.QrcodeItem;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.ScanItem;
import com.socialbusiness.dev.orangebusiness.model.StanderItem;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import kankan.wheel.widget.WheelView;

public class ScanProductListActivity extends BaseActivity {

    public static final String TAG = "ScanProductListActivity";
    private HeaderListView mListView;
    private ScanProductListAdapter mAdapter;
    private View mConfirmDeliverBtn;

    private Order mOrder;
    private ArrayList<Product> mProducts;

    private Order.OrderTradeAdd mOrderTradeAdd;
    private ArrayList<ProductItem> mProductItemList;

    //保存二维码的Map，key为productId，value为二维码数组
    private HashMap<String, String[]> mScanResultCodes;
    private Order.OrderTradeType tradeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_product_list);
        setupData();
        findViews();
        registerListeners();
    }

    //    private static final int length = 3;
    public ArrayList<ScanItem> scanItems;

    private void setupData() {
        mProducts = (ArrayList<Product>) getIntent().getSerializableExtra(Constant.EXTRA_KEY_PRODUCT_LIST);
        mOrder = (Order) getIntent().getSerializableExtra(Constant.EXTRA_KEY_ORDER_OBJ);
        mScanResultCodes = (HashMap<String, String[]>) getIntent().getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_CODES);
        mOrderTradeAdd = (Order.OrderTradeAdd) getIntent().getSerializableExtra(Constant.EXTRA_KEY_ORDER_SALES_ADD_OBJ);
        mProductItemList = (ArrayList<ProductItem>) getIntent().getSerializableExtra(Constant.EXTRA_KEY_PRODUCT_ITEM_LIST);
        tradeType = (Order.OrderTradeType) getIntent().getSerializableExtra(Constant.EXTRA_KEY_TRADETYPE);
//        scanItems = (ArrayList<ScanItem>) getIntent().getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_ITEMS);
//        selectStanders = (HashMap<String, ArrayList<StanderItem>>) getIntent().getSerializableExtra(Constant.EXTRA_KEY_SCAN_RESULT_STANDERS);

        scanItems = Constance.scanItems;
        selectStanders = Constance.selectStanders;
        if (scanItems == null) {
            scanItems = new ArrayList<>();
            selectStanders = new HashMap<>();
            Constance.scanItems = scanItems;
            Constance.selectStanders = selectStanders;
            computerScanitem();
        } else {
            orderBy();
        }

        if (mProducts != null && mOrder != null) {
            if (mScanResultCodes == null) {
                mScanResultCodes = new HashMap<>();
                for (Product product : mProducts) {
                    String[] codesOfOneProduct = new String[product.amount];
                    mScanResultCodes.put(product.id, codesOfOneProduct);
                }
            }
        } else if (mOrderTradeAdd != null && mProductItemList != null && mProducts != null && tradeType != null) { //我的零售订单产品二维码
            if (mScanResultCodes == null) {
                mScanResultCodes = new HashMap<>();
                for (ProductItem productItem : mProductItemList) {
                    String[] codesOfOneProduct = new String[productItem.amount];
                    mScanResultCodes.put(productItem.productId, codesOfOneProduct);
                }
            }
        } else {
            ToastUtil.show(getApplicationContext(), "订单异常");
            finish();
        }

    }

    HashMap<String, ArrayList<ScanItem>> keyProducts = new HashMap<>();
    HashMap<String, ArrayList<StanderItem>> selectStanders;

    //根据产品列表计算xx箱xx盒
    private void computerScanitem() {
        int size = mProducts.size();
//        Log.e("mProducts", "==" + size);
        for (int i = 0; i < size; i++) {

            ArrayList<ScanItem> items = new ArrayList<>();
            ArrayList<StanderItem> standers = new ArrayList<>();
            Product product = mProducts.get(i);
//            int count = 0;

            int boxSize;
            int bagSize;
            int mod = product.amount % product.boxNum;
            if (mod == 0) {//整数箱
                boxSize = product.amount / product.boxNum;
                for (int j = 0; j < boxSize; j++) {
                    ScanItem item = new ScanItem();
                    if (j == 0) {
                        item.isOneHeader = true;
                    }
                    item.isBox = true;
                    item.boxIndex = j + 1;
                    item.product = product;
                    item.boxSize = boxSize;
                    items.add(item);
                    scanItems.add(item);
                }

                getStanders(standers, product, boxSize);

            } else if (product.amount < product.boxNum) {//小于一箱子的容量

                for (int j = 0; j < product.amount; j++) {
                    ScanItem item = new ScanItem();
                    if (j == 0) {
                        item.isOneHeader = true;
                    }
                    item.isBag = true;
                    item.bagIndex = j + 1;
                    item.product = product;
                    item.bagSize = product.amount;
                    items.add(item);
                    scanItems.add(item);
                }

                getStanders(standers, product, 0);

            } else {//整数箱有结余
                boxSize = product.amount / product.boxNum;
                bagSize = product.amount % product.boxNum;
                for (int j = 0; j < boxSize; j++) {
                    ScanItem item = new ScanItem();
                    if (j == 0) {
                        item.isOneHeader = true;
                    }
                    item.isBox = true;
                    item.boxIndex = j + 1;
                    item.product = product;
                    item.boxSize = boxSize;
                    item.bagSize = bagSize;
                    items.add(item);
                    scanItems.add(item);
                }

                getStanders(standers, product, boxSize);


                for (int j = 0; j < bagSize; j++) {
                    ScanItem item = new ScanItem();
                    if (j == 0) {
                        item.isSecondHeader = true;
                    }
                    item.isBag = true;
                    item.bagIndex = j + 1;
                    item.product = product;
                    item.boxSize = boxSize;
                    item.bagSize = bagSize;
                    items.add(item);
                    scanItems.add(item);
                }


            }
            selectStanders.put(product.id, standers);
            keyProducts.put(product.id, items);
        }
//        Log.e("scanitems", "==" + scanItems);
    }

    //计算箱盒组合
    private void getStanders(ArrayList<StanderItem> standers, Product product, int boxSize) {
        for (int j = boxSize; j >= 0; j--) {

            StanderItem item = new StanderItem();
            item.product = product;
            item.boxSize = j;
            item.bagSize = product.amount - j * product.boxNum;
            standers.add(item);
        }
    }


    /**
     * 根据选中的箱盒组合计算items
     *
     * @param boxSize 箱数
     * @param bagSize 盒数
     * @param product 产品
     * @return
     */
    private ArrayList<ScanItem> getItemsByStanderSize(int boxSize, int bagSize, Product product) {

        int index = removeItemsByProduct(product);
        ArrayList<ScanItem> scanItemsTmp = new ArrayList<>();
        for (int j = 0; j < boxSize; j++) {
            ScanItem item = new ScanItem();
            if (j == 0) {
                item.isOneHeader = true;
            }
            item.isBox = true;
            item.boxIndex = j + 1;
            item.product = product;
            item.boxSize = boxSize;
            item.bagSize = bagSize;
            scanItemsTmp.add(item);
        }


        for (int j = 0; j < bagSize; j++) {
            ScanItem item = new ScanItem();
            if (j == 0) {
                item.isSecondHeader = true;
            }
            item.isBag = true;
            item.bagIndex = j + 1;
            item.product = product;
            item.boxSize = boxSize;
            item.bagSize = bagSize;
            scanItemsTmp.add(item);
        }
        keyProducts.remove(product.id);
        keyProducts.put(product.id, scanItemsTmp);
        scanItems.addAll(index, scanItemsTmp);
        return scanItemsTmp;
    }

    /**
     * 删除product对应的items
     *
     * @param product
     * @return
     */
    private int removeItemsByProduct(Product product) {
        int size = scanItems.size();
        int index = 0;
        for (int i = size - 1; i >= 0; i--) {
            ScanItem item = scanItems.get(i);
            if (item.product.id.equals(product.id)) {
                scanItems.remove(i);
                index = i;
            }
        }

        return index;
    }

    private ListView scan_product_list_productLV;
    private ScanProductListItemAdapter itemAdapter;

    private View wheel_layout;
    private WheelView stander_view;
    private View selec_stander_btn;
    private StanderSelectItemAdapter standerItemAdapter;

    private void findViews() {
        wheel_layout = findViewById(R.id.wheel_layout);
        stander_view = (WheelView) findViewById(R.id.stander_view);
        selec_stander_btn = findViewById(R.id.selec_stander_btn);
        standerItemAdapter = new StanderSelectItemAdapter(this);
        stander_view.setVisibleItems(4); // Number of items
        stander_view.setWheelBackground(R.drawable.wheel_bg_holo);
        stander_view.setWheelForeground(R.drawable.wheel_val_holo);
        stander_view.setShadowColor(0xFfffffff, 0x88ffffff, 0x00ffffff);
        stander_view.setViewAdapter(standerItemAdapter);
        stander_view.setCurrentItem(0);

        mListView = (HeaderListView) findViewById(R.id.activity_scan_product_list_productLV);

        mConfirmDeliverBtn = findViewById(R.id.activity_scan_product_list_confrimDeliverBtn);

        if (mOrderTradeAdd != null && mProductItemList != null) {
            ((TextView) mConfirmDeliverBtn).setText("保存");
        }

        if (mProducts != null) {
            mAdapter = new ScanProductListAdapter(this);
            mListView.setAdapter(mAdapter);
        }

        mListView.setScrollBarVisible(false);
        setTitle("录入商品二维码");


        scan_product_list_productLV = (ListView) findViewById(R.id.scan_product_list_productLV);
        itemAdapter = new ScanProductListItemAdapter();
        scan_product_list_productLV.setAdapter(itemAdapter);
    }

    private void registerListeners() {
        mConfirmDeliverBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOrderTradeAdd != null && mProductItemList != null) {
                    callAPIAddOrUpdateSalesOrder();
                    return;
                }
                callConfirmDeliverAPI();
            }
        });

        selec_stander_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog cancelDialog = new AlertDialog.Builder(ScanProductListActivity.this)
                        .setMessage("修改该配置，将会清空该产品已扫描的商品二维码，是否继续？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StanderItem item = standerItemAdapter.getStanderItem(stander_view.getCurrentItem());

                                wheel_layout.setVisibility(View.GONE);
                                getItemsByStanderSize(item.boxSize, item.bagSize, item.product);
                                itemAdapter.notifyDataSetChanged();
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
                Log.e("===", "==" + stander_view.getCurrentItem());

            }
        });
        wheel_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wheel_layout.setVisibility(View.GONE);
            }
        });
    }

    private void callAPIAddOrUpdateSalesOrder() {
        showProgressDialog().setCancelable(false);
        ArrayList<QrcodeItem> qrCodeItems = new ArrayList<Order.OrderDeliver.QrcodeItem>();
        for (Product product : mProducts) {
            String[] codes = mScanResultCodes.get(product.id);
            if (codes != null) {
                for (String code : codes) {
                    QrcodeItem qrCodeItem = new QrcodeItem(code, product);
                    qrCodeItems.add(qrCodeItem);
                }
            }
        }
        mOrderTradeAdd.qrcodes = qrCodeItems;

        APIManager.getInstance(getApplicationContext()).addOrUpdateSalesOrder(tradeType, mOrderTradeAdd,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<?>>() {

                    @Override
                    public void onResponse(RequestResult<?> response) {
                        hideProgressDialog();
                        if (hasError(response)) {
                            return;
                        }
                        ToastUtil.show(ScanProductListActivity.this, response.message);
                        Intent intent = new Intent(ScanProductListActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, MainActivity.TabPosition.Order.name());
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void callConfirmDeliverAPI() {
        showProgressDialog().setCancelable(false);
        OrderDeliver orderDeliver = new OrderDeliver();
//        ArrayList<QrcodeItem> qrCodeItems = new ArrayList<Order.OrderDeliver.QrcodeItem>();
        ArrayList<ProductItem> products = new ArrayList<>();

        for (Product product : mProducts) {
            ProductItem item = new ProductItem();
            item.amount = product.amount;
            item.price = product.price;
            item.productId = product.id;

            ArrayList<ScanItem> items = keyProducts.get(product.id);
            int size = items.size();
            ArrayList<String> boxCodes = new ArrayList<>();
            ArrayList<String> bagCodes = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                ScanItem scanitem = items.get(i);
                if (scanitem.isBox)
                    boxCodes.add(scanitem.code);
                else bagCodes.add(scanitem.code);
            }
            item.qrcodeList = bagCodes;
            item.qrcodeList2 = boxCodes;
            products.add(item);
        }
        orderDeliver.products = products;
        orderDeliver.id = mOrder.id;
//        orderDeliver.qrcodes = qrCodeItems;
        orderDeliver.express = mOrder.express;
        orderDeliver.expressStart = mOrder.expressStart;
        orderDeliver.expressEnd = mOrder.expressEnd;
        orderDeliver.freight = mOrder.freight;
        orderDeliver.sellerremark = mOrder.sellerremark;
        Log.e("=======", "==" + orderDeliver.newToJson());
        APIManager.getInstance(getApplicationContext()).deliverOrder(
                orderDeliver,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                    }
                },
                new Response.Listener<RequestResult<?>>() {

                    @Override
                    public void onResponse(RequestResult<?> response) {
                        hideProgressDialog();
                        if (response != null) {
                            ToastUtil.show(getApplicationContext(), response.message);
                            if (response.code == 0) {
                                Intent intent = new Intent(ScanProductListActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, MainActivity.TabPosition.Order.name());
                                startActivity(intent);

                            }
                            finish();
                        } else {
                            ToastUtil.show(getApplicationContext(), "确认收货失败，请重试");
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (mAdapter == null) {
            return;
        }
//
        ProductCodeData productCodeData = (ProductCodeData) data.getSerializableExtra(MyCaptureActivity.EXTRA_KEY_DATA);
//        boolean isValidate = (productCodeData != null) && (mScanResultCodes.size() == productCodeData.codeMap.size());
//        if (isValidate) {
//            mScanResultCodes = productCodeData.codeMap;
//            mAdapter.notifyDataSetChanged();
//        }


        Constance.scanItems.clear();
        Constance.scanItems = scanItems = productCodeData.scanItems;
        itemAdapter.notifyDataSetChanged();
        orderBy();
    }

    private void orderBy() {
        new Thread() {
            @Override
            public void run() {
                keyProducts.clear();
                int size = scanItems.size();
                for (int i = 0; i < size; i++) {
                    ScanItem item = scanItems.get(i);
                    ArrayList<ScanItem> items = keyProducts.get(item.product.id);
                    if (items == null) {
                        items = new ArrayList<ScanItem>();
                        items.add(item);
                        keyProducts.put(item.product.id, items);
                    } else {
                        items.add(item);
                    }
                }
            }
        }.start();
    }

//    private boolean checkHasRepeatCode(final ProductCodeData data, final String input) {
//        if (data.existsCode(input)) {
//            new AlertDialog.Builder(this)
//                    .setCancelable(false)
//                    .setMessage("产品列表中已经含有此二维码的商品，可能会导致重复，是否仍要录入？")
//                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            data.setCurrentCode(input);
//                            mScanResultCodes = data.codeMap;
//                            mAdapter.notifyDataSetChanged();
//                        }
//                    })
//                    .setNegativeButton("取消", null)
//                    .create()
//                    .show();
//            return true;
//        }
//        return false;
//    }

//	private boolean isProductAllEmptyCode() {
//		Set<String> keySet = mScanResultCodes.keySet();
//		for(String key : keySet) {
//			String[] codes = mScanResultCodes.get(key);
//			if(codes == null) {
//				return true;
//			}
//			boolean allEmpty = true;
//			for(String code : codes) {
//				if(!TextUtils.isEmpty(code)) {
//					allEmpty = false;
//					break ;
//				}
//			}
//			if(allEmpty) {
//				return true;
//			}
//		}
//		return false;
//	}

    @Override
    public void finish() {
        if (scanItems != null) {
            Intent data = new Intent();
//            data.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_CODES, mScanResultCodes);
//            data.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_ITEMS, scanItems);
//            data.putExtra(Constant.EXTRA_KEY_SCAN_RESULT_STANDERS, selectStanders);
//            Log.e("finish", "==" + scanItems);
            setResult(RESULT_OK, data);
        }
        super.finish();
    }


    @Override
    public void onBackPressed() {
        if (wheel_layout.getVisibility() == View.VISIBLE) {
            wheel_layout.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    private void openScanner(ProductCodeData productCodeData) {

        Intent intent = new Intent(this, MyCaptureActivity.class);
        intent.putExtra(MyCaptureActivity.EXTRA_KEY_DATA, productCodeData);
        intent.putExtra(MyCaptureActivity.COMEFROME, TAG);
        this.startActivityForResult(intent, 1);
    }

    private static class ScanProductListAdapter extends SectionAdapter {

        private ScanProductListActivity activity;

        private ScanProductListAdapter(ScanProductListActivity activity) {
            this.activity = activity;
        }

        @Override
        public int numberOfSections() {
            if (activity.mProducts == null || activity.mProducts.isEmpty()) {
                return 0;
            }
            return activity.mProducts.size();
//            return activity.scanItems.size();
        }

        @Override
        public int numberOfRows(int section) {
            if (activity.mProducts == null || section < 0 || activity.mProducts.size() < section) {
                return 0;
            }
            return activity.mProducts.get(section).amount;
//            Log.e("numberOfRows", "===" + section);
//
//            if (section < 0) {
//                return 0;
//            }
//            return activity.scanItems.get(section).childerSize;
        }

        @Override
        public boolean hasSectionHeaderView(int section) {
            return true;
        }

//        @Override
//        public String getSectionHeaderItem(int section) {
//            if (activity.mProducts == null || activity.mProducts.size() < section) {
//                return "";
//            }
//            Product product = activity.mProducts.get(section);
////			return String.format("%s (x%d)", product.name, product.amount);
//            return product.name;
//        }


        @Override
        public View getSectionHeaderView(int section, View convertView,
                                         ViewGroup parent) {
            Context context = parent.getContext();
            HeaderViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row_scan_item_header, null);
                viewHolder = new HeaderViewHolder();
                viewHolder.mHeaderTV = (TextView) convertView.findViewById(R.id.row_contact_item_header_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (HeaderViewHolder) convertView.getTag();
            }

            ScanItem item = activity.scanItems.get(section);

            viewHolder.mHeaderTV.setText(item.product.name);

            return convertView;
        }

        @Override
        public View getRowView(final int section, int row, View convertView,
                               ViewGroup parent) {
            Context context = parent.getContext();
            ItemViewHolder holder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row_scan_product_list_item, null);
                holder = new ItemViewHolder();
                holder.mNumTV = (TextView) convertView.findViewById(R.id.row_scan_product_list_item_numTV);
                holder.mProductCodeET = (TextView) convertView.findViewById(R.id.row_scan_product_list_item_productCodeTV);
                holder.mScanBtnIV = (ImageView) convertView.findViewById(R.id.row_scan_product_list_item_scanBtnIV);
                convertView.setTag(holder);
            } else {
                holder = (ItemViewHolder) convertView.getTag();
            }
            ScanItem item = activity.scanItems.get(section);

            if (row == 0) {
                View view = convertView.findViewById(R.id.row_scan_item_title_layout);
                view.setVisibility(View.VISIBLE);
                if (item.isSecondHeader)
                    view.setBackgroundResource(R.color.green_down_item);
                convertView.findViewById(R.id.row_scan_item_title_line).setVisibility(View.VISIBLE);
            }
            holder.mNumTV.setText(String.valueOf(row + 1));

            Product product = item.product;
            final String productId = product.id;
            final int rowIndex = row;

            //设置扫到的二维码
//            String[] codesOfOneProduct = activity.mScanResultCodes.get(productId);
//            if (codesOfOneProduct == null) {
//                codesOfOneProduct = new String[product.amount];
//                activity.mScanResultCodes.put(productId, codesOfOneProduct);
//            }
//            final String qrCodeStr = codesOfOneProduct[row];
//            if (!TextUtils.isEmpty(qrCodeStr)) {
//                holder.mProductCodeET.setText(qrCodeStr);
//            } else {
//                holder.mProductCodeET.setText("");
//            }

            holder.mScanBtnIV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
//                    ProductCodeData productCodeData = new ProductCodeData();
//                    productCodeData.rowIndex = rowIndex;
//                    activity.openScanner(productCodeData);
                }
            });

            holder.mProductCodeET.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final ScanProductListActivity activity = (ScanProductListActivity) v.getContext();
                    View view = LayoutInflater.from(activity).inflate(R.layout.dialog_input_code, null);
                    final EditText editText = (EditText) view.findViewById(R.id.dialog_input_code_editText);
//                    if (!TextUtils.isEmpty(qrCodeStr)) {
//                        editText.setText(qrCodeStr);
//                    }
                    new AlertDialog.Builder(activity)
                            .setCancelable(false)
                            .setTitle("请手动输入商品编号")
                            .setView(view)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    String code = editText.getText().toString().trim();
//                                    ProductCodeData productCodeData = new ProductCodeData(activity.mProducts, activity.mScanResultCodes);
//                                    productCodeData.sectionIndex = section;
//                                    productCodeData.rowIndex = rowIndex;
//                                    if (!activity.checkHasRepeatCode(productCodeData, code)) {
//                                        String[] codes = activity.mScanResultCodes.get(productId);
//                                        codes[rowIndex] = code;
//                                        notifyDataSetChanged();
//                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();
                }
            });

            return convertView;
        }

        @Override
        public Object getRowItem(int section, int row) {
            return null;
        }

        private class ItemViewHolder {
            TextView mNumTV;
            TextView mProductCodeET;
            ImageView mScanBtnIV;
        }

        private class HeaderViewHolder {
            TextView mHeaderTV;
        }
    }

    public class ScanProductListItemAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return scanItems.size();
        }

        @Override
        public Object getItem(int position) {
            return scanItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ItemViewHolder holder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(ScanProductListActivity.this).inflate(R.layout.row_scan_product_list_item, null);
                holder = new ItemViewHolder();
                holder.mNumTV = (TextView) convertView.findViewById(R.id.row_scan_product_list_item_numTV);
                holder.mProductCodeET = (TextView) convertView.findViewById(R.id.row_scan_product_list_item_productCodeTV);
                holder.mScanBtnIV = (ImageView) convertView.findViewById(R.id.row_scan_product_list_item_scanBtnIV);
                holder.titleLayout = convertView.findViewById(R.id.row_scan_item_title_layout);
                holder.standerLayout = convertView.findViewById(R.id.row_scan_item_header_stander_layout);
                holder.headerName = (TextView) convertView.findViewById(R.id.row_contact_item_header_text);
                holder.headerTitle = (TextView) convertView.findViewById(R.id.row_scan_item_header_title);
                holder.headerStander = (TextView) convertView.findViewById(R.id.row_scan_item_header_stander);
                holder.selcetStander = convertView.findViewById(R.id.row_scan_item_header_selcet_stander);
                convertView.setTag(holder);
            } else {
                holder = (ItemViewHolder) convertView.getTag();
            }
            final ScanItem item = scanItems.get(position);

            if (item.isOneHeader) {
                if (item.boxSize == 0) {
                    setHeader(convertView, holder, item, R.color.green_down_item, "扫盒");
                } else
                    setHeader(convertView, holder, item, R.color.light_blue, "扫箱");
            } else {
                holder.standerLayout.setVisibility(View.GONE);
                convertView.findViewById(R.id.row_scan_item_title_line).setVisibility(View.GONE);
                holder.titleLayout.setVisibility(View.GONE);
            }

            if (item.isSecondHeader) {
                if (item.boxSize == 0) {
                    setHeader(convertView, holder, item, R.color.green_down_item, "扫盒");
                } else {
                    holder.standerLayout.setVisibility(View.GONE);
                }
                holder.titleLayout.setVisibility(View.VISIBLE);
                holder.titleLayout.setBackgroundResource(R.color.green_down_item);
                holder.headerTitle.setText("扫盒");

            }

            if (item.isBag) {
                holder.mNumTV.setText(item.bagIndex + "");
                holder.mNumTV.setBackgroundResource(R.drawable.circle_bg_green);
            }
            if (item.isBox) {
                holder.mNumTV.setText(item.boxIndex + "");
                holder.mNumTV.setBackgroundResource(R.drawable.circle_bg_blue);
            }

            holder.headerName.setText(item.product.name);
            holder.mProductCodeET.setText(item.code);
            holder.mScanBtnIV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ProductCodeData productCodeData = new ProductCodeData(scanItems);
                    productCodeData.startIndex = position;
                    productCodeData.rowIndex = position;
                    openScanner(productCodeData);
                }
            });
            holder.mProductCodeET.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final ScanProductListActivity activity = (ScanProductListActivity) v.getContext();
                    View view = LayoutInflater.from(activity).inflate(R.layout.dialog_input_code, null);
                    final EditText editText = (EditText) view.findViewById(R.id.dialog_input_code_editText);
                    if (!TextUtils.isEmpty(item.code)) {
                        editText.setText(item.code);
                    }
                    new AlertDialog.Builder(activity)
                            .setCancelable(false)
                            .setTitle("请手动输入商品编号")
                            .setView(view)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Constance.isCodeEdit=true;
                                    item.code = editText.getText().toString().trim();
                                    notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();
                }
            });

            holder.selcetStander.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    standerItemAdapter.onDataChange(selectStanders.get(item.product.id));
                    stander_view.setViewAdapter(standerItemAdapter);
                    wheel_layout.setVisibility(View.VISIBLE);
                }
            });
            return convertView;
        }

        private void setHeader(View convertView, ItemViewHolder holder, ScanItem item, int backGround, String title) {
            if (selectStanders.get(item.product.id).size() == 1) {
                holder.selcetStander.setVisibility(View.GONE);
            }else{
                holder.selcetStander.setVisibility(View.VISIBLE);
            }
            holder.standerLayout.setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.row_scan_item_title_line).setVisibility(View.VISIBLE);
            holder.titleLayout.setVisibility(View.VISIBLE);
            holder.titleLayout.setBackgroundResource(backGround);
            holder.headerTitle.setText(title);
            if (item.bagSize != 0 && item.boxSize != 0) {
                holder.headerStander.setText(item.boxSize + "箱 + " + item.bagSize + "盒");
            } else if (item.boxSize != 0) {
                holder.headerStander.setText(item.boxSize + "箱");
            } else {
                holder.headerStander.setText(item.bagSize + "盒");
            }
        }

        private class ItemViewHolder {
            TextView mNumTV;
            TextView mProductCodeET;
            ImageView mScanBtnIV;
            View titleLayout;
            View standerLayout;
            TextView headerName;
            TextView headerTitle;
            TextView headerStander;
            View selcetStander;
        }

    }

    public static class ProductCodeData implements Serializable {

        public int rowIndex;
        public int startIndex;
        private ArrayList<ScanItem> scanItems;

        public ArrayList<ScanItem> getScanItems() {
            return scanItems;
        }

        public ProductCodeData(ArrayList<ScanItem> items) {
            this.scanItems = items;
        }

        /**
         * 判断下一个编号是否为空
         *
         * @return
         */
        public synchronized boolean isNextRowCodeEmpty() {
//
            if (increaseRowIndex())
                return TextUtils.isEmpty(scanItems.get(rowIndex).code);
            else rowIndex--;
            return false;
        }

        public synchronized boolean increaseRowIndex() {
//
            if ((rowIndex + 1) < scanItems.size()) {
                rowIndex++;
                return true;
            }
            return false;
        }

        public synchronized String getCurrentCode() {
            return scanItems.get(rowIndex).code;
        }

        public synchronized void setCurrentCode(String code) {
            ScanItem item = scanItems.get(rowIndex);
            item.code = code;
            Constance.isCodeEdit=true;
        }

        public boolean existsCode(String input) {

            for (ScanItem item : scanItems) {
                if (input.equals(item.code)) {
                    return true;
                }
            }
            return false;
        }

        public String getProductName() {
            return "扫描产品：" + scanItems.get(rowIndex).product.name;
        }

        public String getAlwayScanSize() {

            ScanItem item = scanItems.get(rowIndex);
//            return "已扫 " + (item.boxIndex == 0 ? "" : (item.boxIndex - 1) + " 箱，") + (item.bagIndex == 0 ? "" : (item.bagIndex - 1) + " 盒");
            if (item.isBox)
                return "已扫 " + (item.boxIndex - 1) + " 箱，" + "0 盒";
            else
                return "已扫 " + (item.boxSize == 0 ? 0 : item.boxSize) + " 箱，" + (item.bagIndex - 1) + " 盒";
        }

        public String getNotScanSize() {
            ScanItem item = scanItems.get(rowIndex);
            if (item.isBox)
                return "还需扫 " + (item.boxSize - item.boxIndex + 1) + " 箱，" + (item.bagSize == 0 ? "0" : (item.bagSize)) + " 盒";
            else return "还需扫 " + "0 箱，" + (item.bagSize - item.bagIndex + 1) + " 盒";

//            return "还需扫 " + (item.boxSize==0?0:(item.boxSize - item.boxIndex + 1)) + " 箱，" + (item.bagSize==0?0:(item.bagSize - item.bagIndex + 1)) + " 盒";
        }

        public String getScanProductSatus() {
            ScanItem item = scanItems.get(rowIndex);
            if (item.isBox)
                return "正在扫描箱码";
            else {
                return "正在扫描盒码";
            }
        }
    }

}
