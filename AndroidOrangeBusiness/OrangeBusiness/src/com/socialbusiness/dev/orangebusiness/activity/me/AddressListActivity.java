package com.socialbusiness.dev.orangebusiness.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.PlaceAnOrderActivity;
import com.socialbusiness.dev.orangebusiness.adapter.AddressAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.AddressAdapter.OnAddressCallbackListener;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.AddressItem;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.model.Address;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;
import java.util.List;

public class AddressListActivity extends BaseActivity {

    public static final int REQUEST_CODE_UPDATE_ADDRESS = 111;

    private AddressAdapter mAdapter = null;
    private PullToRefreshListView mListView = null;

    private List<Address> mAddressList = null;
    private int mCurPage = 1;
    private int mTotalPage;

    private String from;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);
        setUP();
        findView();
        callAPIAddressList();
        registerListener();
    }

    private void setUP() {
        from = getIntent().getStringExtra("from");//如果有值说明是从 下单入口进的 否则就是从地址管理进的

        mAdapter = new AddressAdapter(this);

        //设置顶部title栏颜色
//        setLeftIcon(R.drawable.ic_back_orange_selector);
//        setHeaderTitleBarBg(R.color.white);
//        setTitleTextColor(R.color.black);
    }

    private void findView() {
        if (!TextUtils.isEmpty(from) && from.equals(PlaceAnOrderActivity.TAG)) {
            setTitle(R.string.address_list);
        }else{
            setTitle(R.string.address_list_title);
        }
        mListView = (PullToRefreshListView) findViewById(R.id.activity_address_list);
        mListView.setAdapter(mAdapter);
    }

    private void registerListener() {
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                mCurPage = 1;
                callAPIAddressList();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                if (mCurPage < mTotalPage && mAddressList != null) {
                    mCurPage++;
                    callAPIAddressList();
                }
            }
        });

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (!TextUtils.isEmpty(from) && from.equals(PlaceAnOrderActivity.TAG)) {
                    Intent intent = new Intent();
                    intent.putExtra("address", (Address) parent.getAdapter().getItem(position));
                    setResult(RESULT_OK, intent);
                    finish();
                }else{
                    Intent intent = new Intent();
                    intent.putExtra("title", "修改收货地址");
                    intent.putExtra("address", (Address) parent.getAdapter().getItem(position));
                    intent.setClass(AddressListActivity.this, AddAddressActivity.class);
                    startActivityForResult(intent, AddressListActivity.REQUEST_CODE_UPDATE_ADDRESS);
                }
            }
        });

        mListView.getRefreshableView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(view instanceof AddressItem){
                    if (!TextUtils.isEmpty(from) && from.equals(PlaceAnOrderActivity.TAG)) {
                       // setTitle(R.string.address_list);
                    }else{
                        ((AddressItem) view).deleteDialog();
                    }

                }
                return false;
            }
        });

        mAdapter.setOnAddressCallbackListener(new OnAddressCallbackListener() {

            @Override
            public void setDefault(int position) {
                Address addr = getDefaultAddress();
                if (addr != null) {
                    addr.isDefault = false;
                    callAPIUpdateAddress(addr);
                }
                if (position >= 0 && position < mAddressList.size()) {
                    addr = mAddressList.get(position);
                    if (addr == null) {
                        return;
                    }
                    addr.isDefault = true;
                    callAPIUpdateAddress(addr);
                }
            }

            @Override
            public void onDelete(int position) {
                if (position >= 0 && position < mAddressList.size()) {
                    callAPIDeleteAddress(mAddressList.get(position).id);
                }
            }
        });
    }

    private Address getDefaultAddress() {
        if (mAddressList == null) {
            return null;
        }

        for (Address address : mAddressList) {
            if (address.isDefault) {
                return address;
            }
        }
        return null;
    }

    // 从api中获取address数据
    private void callAPIAddressList() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("page", mCurPage + "");
        APIManager.getInstance(this).listAddress(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        hideNoData();
                        showException(error);
                        mListView.onRefreshComplete();
                    }
                }, new Response.Listener<RequestListResult<Address>>() {

                    @Override
                    public void onResponse(RequestListResult<Address> response) {
                        hideLoading();
                        hideNoData();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null && response.data.size() > 0) {
                            if (mCurPage == 1) {
                                mAddressList = response.data;
                                mAdapter.setAddressList(mAddressList);
                            } else if (mCurPage > 1 && mAddressList != null) {
                                mAddressList.addAll(response.data);
                            }

                            mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
                            mAdapter.notifyDataSetChanged();
                        }
                        if (mAdapter.getCount() == 0) {
                            showNoDataTips();
                        } else if (mCurPage >= mTotalPage) {
                            mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        } else {
                            mListView.setMode(PullToRefreshBase.Mode.BOTH);
                        }
                        mListView.onRefreshComplete();
                    }
                });
    }

    private void callAPIUpdateAddress(Address addr) {
        APIManager.getInstance(this).updateAddress(addr,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<?>>() {

                    @Override
                    public void onResponse(RequestResult<?> response) {
                        if (hasError(response)) {
                            return;
                        }
                        sendBroadcast(new Intent(Constance.INTENT_ONADDRESSREFRESH_SECCUS));
						mAdapter.notifyDataSetChanged();
//                        callAPIAddressList();
                    }
                });

    }

    private void callAPIDeleteAddress(final String id) {
        if (TextUtils.isEmpty(id)) {
            return;
        }
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("id", id);
        APIManager.getInstance(this).deleteAddress(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<?>>() {

                    @Override
                    public void onResponse(RequestResult<?> response) {
                        if (hasError(response)) {
                            return;
                        }
                        for (Address address : mAddressList) {
                            if (address.id.equals(id)) {
                                mAddressList.remove(address);
                                mAdapter.notifyDataSetChanged();
                                ToastUtil.show(AddressListActivity.this, response.message);
                                return;
                            }
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRightIcon(R.drawable.ic_add_address_selector, new OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(AddressListActivity.this,
                        AddAddressActivity.class);
                startActivityForResult(intent, REQUEST_CODE_UPDATE_ADDRESS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE_ADDRESS
                && resultCode == RESULT_OK) {
            callAPIAddressList();
        }
    }

    @Override
    public void showLoading() {
        if (mAdapter.getCount() == 0) {
            super.showLoading();
        }
    }
}
