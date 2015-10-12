package com.socialbusiness.dev.orangebusiness.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.fragment.mall.MallFrament;
import com.socialbusiness.dev.orangebusiness.fragment.me.MeFragment;
import com.socialbusiness.dev.orangebusiness.fragment.message.TabConversationListFrament;
import com.socialbusiness.dev.orangebusiness.fragment.order.MyPurchaseFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.MySalesFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.OrderFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.receiver.RongCloudProvidersListener;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.UserGroupUtil;
import com.umeng.update.UmengUpdateAgent;

import java.util.Hashtable;
import java.util.List;

import io.rong.imkit.RongIM;

public class MainActivity extends BaseActivity {

    private FrameLayout mContentLayout;
    private View mMessageTab;
    private View mProductTab;
    private View mContactTab;
    private View mOrderTab;
    private View mMeTab;
    private View mNewMessageIndicator;

    private long lastPressedBackKeyTime;
    private Toast exitToast;

    private InputMethodManager imm;

    //contact:我的销售，order:我的采购
    public static enum TabPosition {
        Message, Contact, Order, Product, Me
    }

    private TabPosition mCurrentTab;
    private User mUser;
    private String orderStr;

    private NotifyRefreshReceiver mNotifyRefreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isHeaderTitleBarVisible(false);
        checkUmengUdate();
        findView();
        registerListener();
        if (savedInstanceState == null) {
            showTab(TabPosition.Product);
        }
        initTabView();
        showBackBtn(false);

        fromMyJPushReceiver(getIntent());

        mNotifyRefreshReceiver = new NotifyRefreshReceiver();
        IntentFilter refreshFilter = new IntentFilter();
        refreshFilter.addAction(NotifyRefreshReceiver.BROADCAST_NOTIFY_REFRESH_VIEW);
        refreshFilter.addAction(NotifyRefreshReceiver.BROADCAST_NOTIFY_CONVERSATION_CHANGED);
        registerReceiver(mNotifyRefreshReceiver, refreshFilter);

//        APIManager.getInstance(this).loginflag(null,new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        },new Response.Listener<RequestListResult<?>>() {
//            @Override
//            public void onResponse(RequestListResult<?> response) {
//                hasError(response);
//            }
//        });

    }

    /**
     * 检查是否有更新
     */
    private void checkUmengUdate() {
        UmengUpdateAgent.update(this);
    }


    private void findView() {
        mContentLayout = (FrameLayout) findViewById(R.id.activity_main_contentLayout);
        mMessageTab = findViewById(R.id.activity_main_messageTab);
        mContactTab = findViewById(R.id.activity_main_contactTab);
        mOrderTab = findViewById(R.id.activity_main_orderTab);
        mProductTab = findViewById(R.id.activity_main_productTab);
        mMeTab = findViewById(R.id.activity_main_meTab);
        mNewMessageIndicator = findViewById(R.id.activity_main_newMsgIndicator);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    private void registerListener() {
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.activity_main_messageTab:
                        showTab(TabPosition.Message);
                        initTabView();
                        break;

                    case R.id.activity_main_contactTab:
                        showTab(TabPosition.Contact);
                        initTabView();
                        break;

                    case R.id.activity_main_orderTab:
                        showTab(TabPosition.Order);
                        initTabView();
                        break;

                    case R.id.activity_main_productTab:
                        showTab(TabPosition.Product);
                        initTabView();
                        break;

                    case R.id.activity_main_meTab:
                        showTab(TabPosition.Me);
                        initTabView();
                        break;

                    default:
                        break;
                }
            }
        };

        mMessageTab.setOnClickListener(listener);
        mProductTab.setOnClickListener(listener);
        mContactTab.setOnClickListener(listener);
        mOrderTab.setOnClickListener(listener);
        mMeTab.setOnClickListener(listener);
    }

    private void initTabView() {
        mMessageTab.setSelected(false);
        mProductTab.setSelected(false);
        mContactTab.setSelected(false);
        mOrderTab.setSelected(false);
        mMeTab.setSelected(false);
        if (mCurrentTab == TabPosition.Message) {
            mMessageTab.setSelected(true);
            setTitle(R.string.main_tab_message);
        } else if (mCurrentTab == TabPosition.Contact) {
            mContactTab.setSelected(true);
            setTitle(R.string.seller_function);
        } else if (mCurrentTab == TabPosition.Order) {
            mOrderTab.setSelected(true);
            setTitle(R.string.buyer_function);
//			callAPIUserRefresh(TabPosition.Order);
        } else if (mCurrentTab == TabPosition.Product) {
            mProductTab.setSelected(true);
//            setTitle(R.string.main_tab_product);
            setTitle("商城");
        } else {

            mMeTab.setSelected(true);
            setTitle(R.string.main_tab_me);
            callAPIUserRefresh(TabPosition.Me);
        }
    }

    private void callAPIUserRefresh(final TabPosition clickPosition) {

        Hashtable<String, String> parameters = new Hashtable<>();
        APIManager.getInstance(this).userRefresh(parameters, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                MainActivity weakActivity = (MainActivity) weakThis.get();
                if (weakActivity == null) {
                    return;
                }
                weakActivity.showException(error);
            }
        }, new Response.Listener<RequestResult<User>>() {

            @Override
            public void onResponse(RequestResult<User> response) {
                MainActivity weakActivity = (MainActivity) weakThis.get();
                if (weakActivity == null) {
                    return;
                }
                if (weakActivity.hasError(response)) {
                    return;
                }

                if (response.data != null) {
                    weakActivity.mUser = response.data;
                    SettingsManager.saveLoginUserWithoutSetJPush(weakActivity.mUser);
                    Fragment fragment = getFragmentByPosition(clickPosition);
                    if (fragment == null) {
                        return;
                    }
                    if (clickPosition == TabPosition.Order) {
                        ((OrderFragment) fragment).setOrderData(weakActivity.mUser);
                    } else if (clickPosition == TabPosition.Me) {
                        ((MeFragment) fragment).setMeData(weakActivity.mUser);
                    }
                }
            }
        });
    }


    /**
     * 显示 菜单的界面
     * @param clickPosition
     */
    private void showTab(TabPosition clickPosition) {
        if (clickPosition == mCurrentTab) {
            return;
        }

        Fragment fragment = getFragmentByPosition(clickPosition);
        Log.i("tag_fragment", fragment + "");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        List<Fragment> fragmentList = fragmentManager.getFragments();
        if (fragmentList != null) {
            for (Fragment f : fragmentList) {
                transaction.hide(f);
                if (f instanceof BaseFragment) {
                    ((BaseFragment) f).hideFragment();
                } else {
                    showBackBtn(false);
                    showRightBtn(false);
                }
            }
        }

        if (fragment == null) {
            if (clickPosition == TabPosition.Message) {

//                fragment= new CustomConversationListFragment();
                fragment=new TabConversationListFrament();
            } else if (clickPosition == TabPosition.Contact) {
//                fragment = new ContactFragment();
                fragment = new MySalesFragment(); //销售
            } else if (clickPosition == TabPosition.Order) {//采购
                //fragment = new OrderFragment();
                fragment = new MyPurchaseFragment();
            } else if (clickPosition == TabPosition.Product) {
//                fragment = new ProductFragment();
                fragment = new MallFrament();  //商城
            } else if (clickPosition == TabPosition.Me) {
                fragment = new MeFragment();
            }
            transaction.add(R.id.activity_main_contentLayout, fragment, clickPosition.toString());
        } else {
            transaction.show(fragment);
            if (fragment instanceof BaseFragment)
                ((BaseFragment) fragment).showFragment();
            else {
                showBackBtn(false);
                showRightBtn(false);
            }

        }
        transaction.commit();
        mCurrentTab = clickPosition;

    }

    public User getUser() {
        return mUser != null ? mUser : SettingsManager.getLoginUser();
    }

    private Fragment getFragmentByPosition(TabPosition position) {
        if (position != null) {
            Fragment fragment = null;
            FragmentManager fragmentManager = getSupportFragmentManager();
            String tag = position.toString();
            fragment = fragmentManager.findFragmentByTag(tag);
            return fragment;
        }
        return null;
    }

    private void fromMyJPushReceiver(Intent newIntent) {
        Intent intent = newIntent.getParcelableExtra("intent");
        if (intent != null) {
            this.startActivity(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String currentTab = intent.getStringExtra(Constant.EXTRA_KEY_TABPOSITION);
        if (currentTab != null) {
            showTab(TabPosition.valueOf(currentTab));
            mCurrentTab = TabPosition.valueOf(currentTab);
            initTabView();
            return;
        }
        fromMyJPushReceiver(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("currentTab", mCurrentTab);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 读取Tab选择位置
        final TabPosition currentPosition = (TabPosition) savedInstanceState
                .getSerializable("currentTab");
        showTab(currentPosition);
        initTabView();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (exitToast == null) {
                exitToast = Toast.makeText(MainActivity.this, R.string.info_back_key_exit, Toast.LENGTH_LONG);
            }

            long delay = (exitToast.getDuration() == Toast.LENGTH_LONG ? 3500 : 2000);
            if (System.currentTimeMillis() - lastPressedBackKeyTime < delay) {
                exitToast.cancel();
                MainActivity.this.finish();
            } else {
                exitToast.show();
            }

            lastPressedBackKeyTime = System.currentTimeMillis();
        }
        return false;
    }

    private void updateNewMessageIndicator(Intent intent) {
        if (mNewMessageIndicator != null) {
            int newMessageCount = intent.getIntExtra(RongCloudProvidersListener.UNREAD_COUNT_KEY, 0);
            mNewMessageIndicator.setVisibility(newMessageCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void updateNewMessageIndicator() {
        if (mNewMessageIndicator != null) {
//            Log.e("updateNewMessageIndicator",""+ RongIM.getInstance());
            int newMessageCount = RongIM.getInstance()==null?0:RongIM.getInstance().getTotalUnreadCount();
            mNewMessageIndicator.setVisibility(newMessageCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        User loginUser = SettingsManager.getLoginUser();
        String session = SettingsManager.loadLoginSession();
        if (loginUser == null || TextUtils.isEmpty(session)) {
//            RongIM.getInstance().disconnect();
            return;
        }

        updateNewMessageIndicator();

        UmengUpdateAgent.setUpdateListener(getUpdateListener());

//		Intent intent = new Intent(EasemodManager.BROADCAST_NOTIFY_REFRESH_VIEW);
//		sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        UmengUpdateAgent.setUpdateListener(null);
        if (mNotifyRefreshReceiver != null) {
            unregisterReceiver(mNotifyRefreshReceiver);
            mNotifyRefreshReceiver = null;
        }
        super.onDestroy();
    }


    public class NotifyRefreshReceiver extends BroadcastReceiver {

        public static final String BROADCAST_NOTIFY_REFRESH_VIEW = "BROADCAST_NOTIFY_REFRESH_VIEW";

        public static final String BROADCAST_NOTIFY_CONVERSATION_CHANGED = "BROADCAST_NOTIFY_CONVERSATION_CHANGED";

        public static final String ISGROUP = "isgroup";

        public static final String ISUSER = "isuser";

        public static final String TARGETID = "TARGETID";

        @Override
        public void onReceive(Context context, Intent intent) {

            updateNewMessageIndicator(intent);
            if (intent.getBooleanExtra(ISGROUP, false)) {
                Log.e("=====", "刷新");
                UserGroupUtil.customConversationListFragment = null;
                UserGroupUtil.getGroups(context);
            }
            if (intent.getBooleanExtra(ISUSER, false)) {
                UserGroupUtil.getFriends(context, null);
            }
        }

    }
}
