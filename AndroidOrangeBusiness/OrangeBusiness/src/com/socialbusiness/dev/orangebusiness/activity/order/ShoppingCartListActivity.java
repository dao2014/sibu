package com.socialbusiness.dev.orangebusiness.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.ShoppingCartAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.ShoppingCartAdapter.OnNoticeDataChangeListener;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCartListActivity extends BaseActivity {

    private ListView mListView;
    private ShoppingCartAdapter mAdapter;

    private TextView mPlaceAnOrder;
    private TextView mMoneySum;
    private ImageView mSelect;

    private List<ShopCartItem> mShopCartItemList;
    private ShopCartDbHelper helper;
    private boolean isSelect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart_list);
        setUp();
        findView();
        setListener();
    }

    private void setUp() {
        setTitle(R.string.shopping_cart);
        mAdapter = new ShoppingCartAdapter(this);
        helper = new ShopCartDbHelper(this);
        mShopCartItemList = new ArrayList<>();
        mShopCartItemList.addAll(helper.getAllShopCartItems());
    }

    private void findView() {
        mListView = (ListView) findViewById(R.id.activity_shopping_cart_listview);
        mPlaceAnOrder = (TextView) findViewById(R.id.activity_shopping_cart_place_orders);
        mMoneySum = (TextView) findViewById(R.id.activity_shopping_cart_money_sum);
        mSelect = (ImageView) findViewById(R.id.activity_shopping_cart_select_image);

        mAdapter.setShopCartItemList(mShopCartItemList);
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(0);

        if (mShopCartItemList.size() == 0) {
            showNoDataTips();
        } else {
            mSelect.setSelected(isSelect);
        }
        setMoneySum(mShopCartItemList);
    }

    private void setMoneySum(List<ShopCartItem> list) {
        if (list == null || list.size() == 0) {
            mMoneySum.setText("0");
            return;
        }
        BigDecimal sum = new BigDecimal(0f);
        for (int i = 0; i < list.size(); i++) {
            if (mAdapter.getIsSelectedList().get(i)) {
                sum = sum.add(new BigDecimal(list.get(i).quantity).multiply(new BigDecimal(list.get(i).price)));
            }
        }
        mMoneySum.setText(StringUtil.getStringFromFloatKeep2(sum.doubleValue()));
    }

    private void changeSelectAllState(List<Boolean> booleans) {
        for (int i = 0; i < booleans.size(); i++) {
            if (!booleans.get(i)) {
                isSelect = false;
                mSelect.setSelected(isSelect);
                return;
            }
        }
        if (booleans.size() == 0) {
            isSelect = false;
        } else {
            isSelect = true;
        }
        mSelect.setSelected(isSelect);
    }

    private void setListener() {
        mAdapter.setOnNoticeDataChangeListener(new OnNoticeDataChangeListener() {

            @Override
            public void onNoticeDataChange(boolean isDeleteItem) {


                if (isDeleteItem)
                    onResume();
                else setMoneySum(mAdapter.getShopCartItemList());
                changeSelectAllState(mAdapter.getIsSelectedList());
            }
        });

        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == mPlaceAnOrder) {
                    if (checkOrder(mAdapter.getIsSelectedList())) {

                        List list = getAnOrder();
                        if (list == null) {
                            return;
                        }
                        Intent intent = new Intent(ShoppingCartListActivity.this, PlaceAnOrderActivity.class);  //下单界面
                        intent.putExtra("orderlist", (Serializable)list);
                        intent.putExtra("isSelectAll", isSelect);
                        intent.putExtra("money", mMoneySum.getText().toString().trim());
                        startActivity(intent);
                    } else {
                        ToastUtil.show(ShoppingCartListActivity.this, "您尚未勾选任何一项");
                    }

                } else if (v == mSelect) {
                    mShopCartItemList = mAdapter.getShopCartItemList();
                    if (mShopCartItemList.size() == 0) {
                        ToastUtil.show(ShoppingCartListActivity.this, "购物车里没有商品哦");
                        isSelect = false;
                        mSelect.setSelected(false);
                        setMoneySum(mShopCartItemList);
                        return;
                    }
                    isSelect = !isSelect;
                    mSelect.setSelected(isSelect);
                    for (int i = 0; i < mShopCartItemList.size(); i++) {
                        mAdapter.getIsSelectedList().set(i, isSelect);
                    }
                    mAdapter.notifyDataSetChanged();
                    setMoneySum(mAdapter.getShopCartItemList());
                    changeSelectAllState(mAdapter.getIsSelectedList());
                }
            }
        };

        mPlaceAnOrder.setOnClickListener(listener);
        mSelect.setOnClickListener(listener);
    }

    //如果有勾选，就可以下单
    private boolean checkOrder(List<Boolean> booleans) {
        if (booleans == null) {
            return false;
        }
        for (int i = 0; i < booleans.size(); i++) {
            if (booleans.get(i)) {
                return true;
            }
        }
        return false;
    }

    private List<ShopCartItem> getAnOrder() {
        List<Boolean> booleans = mAdapter.getIsSelectedList();
        List<ShopCartItem> list = mAdapter.getShopCartItemList();
        List<ShopCartItem> orderList = new ArrayList<ShopCartItem>();
        for (int i = 0; i < booleans.size(); i++) {
            if (booleans.get(i)) {
                if (list.get(i).quantity == 0) {
                    ToastUtil.show(getApplicationContext(), list.get(i).name + "数量不能为0");
                    return null;
                }
                orderList.add(list.get(i));
            }
        }
        return orderList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null && helper != null && mShopCartItemList != null) {
//			List<ShopCartItem> cartItems = helper.getAllShopCartItems();
//			mShopCartItemList.clear();
//			if(cartItems != null && !cartItems.isEmpty()) {
//				mShopCartItemList.addAll(cartItems);
//			}
            mShopCartItemList = helper.getAllShopCartItems();
            mAdapter.setShopCartItemList(mShopCartItemList);
            mAdapter.notifyDataSetChanged();
            setMoneySum(mShopCartItemList);
        }
    }
}
