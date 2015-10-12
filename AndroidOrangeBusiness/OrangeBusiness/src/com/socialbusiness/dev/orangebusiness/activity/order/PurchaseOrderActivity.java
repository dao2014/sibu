package com.socialbusiness.dev.orangebusiness.activity.order;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.component.FixedViewPager;
import com.socialbusiness.dev.orangebusiness.component.FixedViewPager.FixedViewPagerLintener;
import com.socialbusiness.dev.orangebusiness.component.TabIndicator;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.DeliverDoneFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.ReceiptedFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingConfirmFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingDeliverFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.util.ArrayList;

public class PurchaseOrderActivity extends BaseActivity {

    public static final String TAG = "BuyerActivity";
    private static final int WAITING_CONFIRM = 0;
    private static final int WAITING_2PAY = 1;
    private static final int WAITING_DELIVER = 2;
    private static final int DELIVER_DONE = 3;
    private static final int RECEIPTED = 4;

    private TextView mTabWaitingConfirm;
    private TextView mTabWaiting2Pay;
    private TextView mTabWaitingDeliver;
    private TextView mTabDeliverDone;
    private TextView mTabReceipted;
    private TextView total_money_title;
    private TextView total_money_value;

    private ArrayList<Fragment> fragmentList;
    private FixedViewPager mViewPager;
    private TabIndicator mTabIndicator;

    private int currentIndex;
    private String currentTag;
    private String lowerLevelUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_buyer);
        findView();
        setUp();
        initViewPager();
        setListeners();
        updateTabView();
    }

    private void findView() {
        mTabWaitingConfirm = (TextView) findViewById(R.id.activity_purchase_order_waiting_confirm);
        mTabWaiting2Pay = (TextView) findViewById(R.id.activity_purchase_order_waiting_2pay);
        mTabWaitingDeliver = (TextView) findViewById(R.id.activity_purchase_order_waiting_deliver);
        mTabDeliverDone = (TextView) findViewById(R.id.activity_purchase_order_delivered);
        mTabReceipted = (TextView) findViewById(R.id.activity_purchase_order_receipted);
        mTabIndicator = (TabIndicator) findViewById(R.id.activity_purchase_order_mTabIndicator);
        mTabIndicator.setTextView(mTabWaitingConfirm);
        mViewPager = (FixedViewPager) findViewById(R.id.activity_purchase_order_mViewPager);
        Log.e("findView", "==");
        total_money_title = (TextView) findViewById(R.id.total_money_title);
        total_money_value = (TextView) findViewById(R.id.total_money_value);

        mViewPager.setOffscreenPageLimit(5);
    }

    public void setTotalData(String title, String value) {
        total_money_title.setText(title);
        total_money_value.setText(getResources().getText(R.string.yuan_symbol) + StringUtil.get2DecimalFromString(value));
    }

    private void setUp() {
        lowerLevelUserId = getIntent().getStringExtra("userId");
        if (!TextUtils.isEmpty(lowerLevelUserId)
                && !lowerLevelUserId.equals(SettingsManager.getLoginUser().id)) {
            setTitle(R.string.purchase_order);
        } else {
            setTitle(R.string.purchase_order);
        }
    }

    private void initViewPager() {
        fragmentList = new ArrayList<Fragment>();
        WaitingConfirmFragment waitingConfirmFragment = new WaitingConfirmFragment();
        waitingConfirmFragment.setPurchaseOrderActivity(this);

        Waiting2PayFragment waiting2PayFragment = new Waiting2PayFragment();
        waiting2PayFragment.setPurchaseOrderActivity(this);

        WaitingDeliverFragment waitingDeliverFragment = new WaitingDeliverFragment();
        waitingDeliverFragment.setPurchaseOrderActivity(this);

        DeliverDoneFragment doneDeliverFragment = new DeliverDoneFragment();
        doneDeliverFragment.setPurchaseOrderActivity(this);

        ReceiptedFragment receiptedFragment = new ReceiptedFragment();
        receiptedFragment.setPurchaseOrderActivity(this);

        fragmentList.add(waitingConfirmFragment);
        fragmentList.add(waiting2PayFragment);
        fragmentList.add(waitingDeliverFragment);
        fragmentList.add(doneDeliverFragment);
        fragmentList.add(receiptedFragment);

        BuyerPageAdapter buyerPageAdapter = new BuyerPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(buyerPageAdapter);
        mTabIndicator.setViewPager(mViewPager);

        initCurrentIndex();
    }

    private void initCurrentIndex() {
        currentTag = getIntent().getStringExtra("TAG");
        Log.e("initCurrentIndex", "==" + currentTag);
        if (!TextUtils.isEmpty(currentTag)) {
            if (WaitingConfirmFragment.TAG.equals(currentTag)) {
                currentIndex = WAITING_CONFIRM;
            } else if (WaitingDeliverFragment.TAG.equals(currentTag)) {
                currentIndex = WAITING_DELIVER;
            } else if (DeliverDoneFragment.TAG.equals(currentTag)) {
                currentIndex = DELIVER_DONE;
            } else if (Waiting2PayFragment.TAG.equals(currentTag)) {
                currentIndex = WAITING_2PAY;
            } else if (ReceiptedFragment.TAG.equals(currentTag)) {
                currentIndex = RECEIPTED;
            }
        } else {
            currentIndex = 0;
        }
        mViewPager.setCurrentItem(currentIndex);
    }

    private void setListeners() {
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == mTabWaitingConfirm) {
                    currentIndex = WAITING_CONFIRM;
                } else if (v == mTabWaiting2Pay) {
                    currentIndex = WAITING_2PAY;
                } else if (v == mTabWaitingDeliver) {
                    currentIndex = WAITING_DELIVER;
                } else if (v == mTabDeliverDone) {
                    currentIndex = DELIVER_DONE;
                } else if (v == mTabReceipted) {
                    currentIndex = RECEIPTED;
                }

                mViewPager.setCurrentItem(currentIndex);
                updateTabView();
            }
        };

        mTabWaitingConfirm.setOnClickListener(listener);
        mTabWaiting2Pay.setOnClickListener(listener);
        mTabWaitingDeliver.setOnClickListener(listener);
        mTabDeliverDone.setOnClickListener(listener);
        mTabReceipted.setOnClickListener(listener);

        mViewPager.setViewPagerListener(new FixedViewPagerLintener() {

            @Override
            public void onRestoreInstanceState(Parcelable state) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                updateTabView();
                for (int i = 0; i < fragmentList.size(); i++) {
                    BaseFragment fragment = null;
                    if (currentIndex == i) {
                        fragment = (BaseFragment) fragmentList.get(i);
                        fragment.showFragmentPurchaseOrderActivity();
                        fragment.showOrderTatol(currentTag);
                    }
                }

                setLeftBtn(R.drawable.header_ic_back, new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

//                setRightIcon(R.drawable.ic_settings_selector,
//                        new OnClickListener() {
//
//                            @Override
//                            public void onClick(View v) {
//                                Intent intent = new Intent(PurchaseOrderActivity.this, OrderSearchActiivty.class);
//                                startActivity(intent);
//                            }
//                        });
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
//        setRightIcon(R.drawable.ic_settings_selector,
//                new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(PurchaseOrderActivity.this, OrderSearchActiivty.class);
//                        startActivity(intent);
//                    }
//                });
    }

    private void updateTabView() {
        mTabWaitingConfirm.setSelected(false);
        mTabWaiting2Pay.setSelected(false);
        mTabWaitingDeliver.setSelected(false);
        mTabDeliverDone.setSelected(false);
        mTabReceipted.setSelected(false);

        if (currentIndex == WAITING_CONFIRM) {
            mTabWaitingConfirm.setSelected(true);
            currentTag = WaitingConfirmFragment.TAG;
        } else if (currentIndex == WAITING_2PAY) {
            mTabWaiting2Pay.setSelected(true);
            currentTag = Waiting2PayFragment.TAG;
        } else if (currentIndex == WAITING_DELIVER) {
            mTabWaitingDeliver.setSelected(true);
            currentTag = WaitingDeliverFragment.TAG;
        } else if (currentIndex == DELIVER_DONE) {
            mTabDeliverDone.setSelected(true);
            currentTag = DeliverDoneFragment.TAG;
        } else if (currentIndex == RECEIPTED) {
            mTabReceipted.setSelected(true);
            currentTag = ReceiptedFragment.TAG;
        }
    }

    public boolean isCurentShow(String tag) {
        if (currentTag == null)
            return false;
        return currentTag.equals(tag);
    }

    public String getUserId() {
        return !TextUtils.isEmpty(lowerLevelUserId) ? lowerLevelUserId : null;
    }

    private class BuyerPageAdapter extends FragmentPagerAdapter {
        FragmentManager fmTmp;

        public BuyerPageAdapter(FragmentManager fm) {
            super(fm);
            fmTmp = fm;
        }

        @Override
        public Fragment getItem(int position) {

            return fragmentList.get(position);
        }


        @Override
        public int getCount() {
            return fragmentList != null ? fragmentList.size() : 0;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.fragmentList.clear();
        currentTag = null;
        Log.e("PurchaseOrderActivity", "==onDestroy");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

