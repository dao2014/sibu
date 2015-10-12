package com.socialbusiness.dev.orangebusiness.fragment.order;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.order.OrderSearchActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.FixedViewPager;
import com.socialbusiness.dev.orangebusiness.component.FixedViewPager.FixedViewPagerLintener;
import com.socialbusiness.dev.orangebusiness.component.TabIndicator;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;

import java.util.Hashtable;
import java.util.List;

public class OrderFragment extends BaseFragment {

    public static final String TAG = "OrderFragment";
    private static final int MY_PURCHASE = 0;
    private static final int MY_SALES = 1;

    private TextView mMyPurchase;
    private TextView mMySales;

    private FixedViewPager mViewPager;
    private TabIndicator mTabIndicator;

    private int currentIndex;
    private String currentTag;

    private ShopCartDbHelper mHelper;

    @Override
    public void setContentView(LayoutInflater inflater,
                               ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.fragment_order, mLayerContextView);
        setUp();
        findView(mLayerContextView);
        initViewPager();
        updateTabView();
        setListeners();
    }

    private void setUp() {
        mHelper = new ShopCartDbHelper(getActivity());
    }

    private void findView(View view) {
        mMyPurchase = (TextView) view.findViewById(R.id.fragment_order_my_purchase);
        mMySales = (TextView) view.findViewById(R.id.fragment_order_my_sales);
        mViewPager = (FixedViewPager) view.findViewById(R.id.fragment_order_mViewPager);
        mTabIndicator = (TabIndicator) view.findViewById(R.id.fragment_order_mTabIndicator);

        mViewPager.setOffscreenPageLimit(2);
        showFragment();
    }

    private void initViewPager() {

        // 此处注意getChildFragmentManager()与getFragmentManager()的不同
        OrderPagerAdapter orderPagerAdapter = new OrderPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(orderPagerAdapter);
        mTabIndicator.setViewPager(mViewPager);

        initCurrentIndex();
    }

    private void initCurrentIndex() {
        currentTag = getActivity().getIntent().getStringExtra("TAG");
        if ((!TextUtils.isEmpty(currentTag) && MySalesFragment.TAG.equals(currentTag))
                || currentIndex == MY_SALES) {
            currentIndex = MY_SALES;
        } else {
            currentIndex = MY_PURCHASE;
        }

        if (mViewPager != null) {
            mViewPager.setCurrentItem(currentIndex);
        }
    }

    private void setListeners() {
        OnClickListener listeners = new OnClickListener() {

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

        mViewPager.setViewPagerListener(new FixedViewPagerLintener() {

            @Override
            public void onRestoreInstanceState(Parcelable state) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                updateTabView();
                List<Fragment> fragments = getChildFragmentManager().getFragments();
                if (fragments == null) {
                    return;
                }
                for (int i = 0; i < fragments.size(); i++) {
                    BaseFragment fragment = null;
                    if (currentIndex == i) {
                        fragment = (BaseFragment) fragments.get(i);
                        fragment.showFragment();
                    }
                }
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

    public void setOrderData(User user) {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments == null) {
            return;
        }
        for (Fragment currentFragment : fragments) {
            if (currentFragment instanceof MyPurchaseFragment) {
                if (mHelper != null && mHelper.getShopCartItemsByUserId(user.id) != null) {
                    ((MyPurchaseFragment) currentFragment).setProductNums(mHelper.getShopCartItemsByUserId(user.id).size());
                }
                ((MyPurchaseFragment) currentFragment).setWaitingConfirmOrderNums(user.buyerWaitingOrderCount);
                ((MyPurchaseFragment) currentFragment).setWaiting2PayOrderNums(user.buyerWaitingPayOrderCount);
                ((MyPurchaseFragment) currentFragment).setWaitingDeliverOrderNums(user.buyerShopOrderCount);
                ((MyPurchaseFragment) currentFragment).setDeliveredOrderNums(user.buyerShipOrderCount);
                ((MyPurchaseFragment) currentFragment).setReceiptedNums(user.buyerClosedOrderCount);

            } else if (currentFragment instanceof MySalesFragment) {
                ((MySalesFragment) currentFragment).setWaitingConfirmOrderNum(user.sellerWaitingOrderCount);
                ((MySalesFragment) currentFragment).setWaiting2PayOrderNum(user.sellerWaitingPayOrderCount);
                ((MySalesFragment) currentFragment).setWaitingDeliverOrderNum(user.sellerShopOrderCount);
                ((MySalesFragment) currentFragment).setDeliveredOrderNum(user.sellerShipOrderCount);

                ((MySalesFragment) currentFragment).setMonthDeliverNum(user.monthlyOrderMoney);
                ((MySalesFragment) currentFragment).setMonthDeliverSum(user.monthlyShipMoney);
                ((MySalesFragment) currentFragment).setMonthProfitSum(user.totalOrderMoney);

                ((MySalesFragment) currentFragment).setReceiptedNums(user.sellerClosedOrderCount);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        callAPIUserRefresh();
    }

    @Override
    public void showFragment() {
        super.showFragment();
        showRightBtn();
    }

    private void showRightBtn() {
        getMainActivity().setRightIcon(R.drawable.ic_search_api_holo_dark, seachOnclick);
    }

    OnClickListener seachOnclick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getMainActivity(),
                    OrderSearchActivity.class);
            intent.putExtra(Constance.ISSELLER,currentIndex);
            startActivityForResult(intent, 0);
        }
    };

    private void callAPIUserRefresh() {
        Hashtable<String, String> parameters = new Hashtable<>();
        APIManager.getInstance(getActivity()).userRefresh(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<User>>() {

                    @Override
                    public void onResponse(RequestResult<User> response) {
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            SettingsManager.saveLoginUserWithoutSetJPush(response.data);
                            setOrderData(response.data);
                        }
                    }
                });
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
                    return new MyPurchaseFragment();
                }
                case 1: {
                    return new MySalesFragment();
                }
            }
            return null;
        }
    }


}
