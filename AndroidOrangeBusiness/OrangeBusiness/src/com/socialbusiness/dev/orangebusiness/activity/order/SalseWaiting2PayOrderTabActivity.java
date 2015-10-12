package com.socialbusiness.dev.orangebusiness.activity.order;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.component.FixedViewPager;
import com.socialbusiness.dev.orangebusiness.component.TabIndicator;
import com.socialbusiness.dev.orangebusiness.fragment.order.SalesUpLoadPayOrderFament;
import com.socialbusiness.dev.orangebusiness.fragment.order.SalesWaitingPayOrderFrament;

/**
 * Created by zkboos on 2015/4/8.
 */
public class  SalseWaiting2PayOrderTabActivity extends BaseActivity {

    public static final String TAG = "SalesWaiting2PayOrderActivity";
    private static final int MY_PURCHASE = 0;
    private static final int MY_SALES = 1;

    private TextView mMyPurchase;
    private TextView mMySales;

    private FixedViewPager mViewPager;
    private TabIndicator mTabIndicator;

    private int currentIndex;
    private String currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salse_waiting_pay_order_tab);
        findView();
        initViewPager();
        updateTabView();
        setListeners();
        setUp();
    }

    private void setUp() {
        setTitle(R.string.sales_waiting_deliver_order);
    }


    private void findView() {
        mMyPurchase = (TextView) findViewById(R.id.fragment_order_my_purchase);
        mMySales = (TextView) findViewById(R.id.fragment_order_my_sales);
        mViewPager = (FixedViewPager) findViewById(R.id.fragment_order_mViewPager);
        mTabIndicator = (TabIndicator) findViewById(R.id.fragment_order_mTabIndicator);
        mTabIndicator.setTextView(mMyPurchase);
        mViewPager.setOffscreenPageLimit(2);
    }

    private void initViewPager() {

        // 此处注意getChildFragmentManager()与getFragmentManager()的不同
        OrderPagerAdapter orderPagerAdapter = new OrderPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(orderPagerAdapter);
        mTabIndicator.setViewPager(mViewPager);

    }


    private void setListeners() {
        View.OnClickListener listeners = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == mMyPurchase) {
                    currentIndex = MY_PURCHASE;
                } else if (v == mMySales) {
                    currentIndex = MY_SALES;
                }

                mViewPager.setCurrentItem(currentIndex);
                updateTabView();
            }
        };
        mMyPurchase.setOnClickListener(listeners);
        mMySales.setOnClickListener(listeners);

        mViewPager.setViewPagerListener(new FixedViewPager.FixedViewPagerLintener() {

            @Override
            public void onRestoreInstanceState(Parcelable state) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                updateTabView();
//                List<Fragment> fragments = getFragmentManager().;
//                if (fragments == null){
//                    return;
//                }
//                for (int i = 0; i < fragments.size(); i++) {
//                    BaseFragment fragment = null;
//                    if (currentIndex == i) {
//                        fragment = (BaseFragment) fragments.get(i);
//                        fragment.showFragment();
//                    }
//                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateTabView() {
        mMyPurchase.setSelected(false);
        mMySales.setSelected(false);

        if (currentIndex == MY_PURCHASE) {
            mMyPurchase.setSelected(true);
        } else if (currentIndex == MY_SALES) {
            mMySales.setSelected(true);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    private class OrderPagerAdapter extends FragmentPagerAdapter {

        public OrderPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return new SalesUpLoadPayOrderFament();
                }
                case 1: {
                    SalesWaitingPayOrderFrament temp = new SalesWaitingPayOrderFrament();
                    return temp;
                }
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
//            refresh();
            finish();
        }
    }
}
