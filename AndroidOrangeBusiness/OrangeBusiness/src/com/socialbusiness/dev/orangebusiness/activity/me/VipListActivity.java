package com.socialbusiness.dev.orangebusiness.activity.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.base.WebActivity;
import com.socialbusiness.dev.orangebusiness.adapter.VipAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.fragment.order.MySalesFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by enwey on 2014/11/3.
 */
public class VipListActivity extends BaseActivity {
    private VipAdapter mAdapter;
    private PullToRefreshListView mListView;
    private List<User> mUserList;
    private int mCurPage;
    private int mTotalPage;
    private String mLevelIndex;
    private int fromActivity;
    private int isSearch;
    private final static int SEARCH_TAG = 1000;
    private final static int DEFULT_TAG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_user_list);
        fromActivity = getIntent().getIntExtra(Constance.COMEFROM, -1);
        setUp();
        findView();
        registerListener();
        callAPIListUserByLevel();
    }

    private InputMethodManager imm;

    private void setUp() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (fromActivity) {
            case MySalesFragment.TAG_INT:
                setTitle("我的下级");

                break;
            default:
                setTitle(R.string.level_title);
                findViewById(R.id.fragment_product_search_bg_layout).setVisibility(View.GONE);
                break;
        }

        mCurPage = 1;
        mLevelIndex = getIntent().getStringExtra("index");
        String mLevelName = getIntent().getStringExtra("levelName");
        mAdapter = new VipAdapter(this);
        if (mLevelName != null) {
            mAdapter.setLevelName(mLevelName);
        }
    }

    private TextView mSearchBtn;
    private EditText mInput;
    private String keyWord;
    View search_close_clear;


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void findView() {

        mInput = (EditText) findViewById(R.id.fragment_product_input);
        mSearchBtn = (TextView) findViewById(R.id.fragment_product_searchTxt);
        mListView = (PullToRefreshListView) this.findViewById(R.id.activity_vip_user_list);
        mListView.setAdapter(mAdapter);

        search_close_clear=findViewById(R.id.search_close_clear);
    }

    private void registerListener() {
        AdapterView.OnItemClickListener register = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View convertView,
                                    int position, long id) {
                Intent intent = null;

                switch (fromActivity) {

                    case MySalesFragment.TAG_INT:

                        intent = new Intent(VipListActivity.this, WebActivity.class);
                        intent.putExtra(Constance.WEB_ACTIVITY_TITLE, "销售对账单");
                        User user = SettingsManager.getLoginUser();
                        User downUser = ((User) parent.getAdapter().getItem(position));
                        intent.putExtra(Constance.WEB_DETAIL, String.format(APIManager.STATE_ACOUNT, user.companyId, downUser.id, user.id));
                        intent.putExtra(Constant.EXTRA_KEY_USER_ID, downUser.id);
                        startActivity(intent);
                        break;
                    default:
                        intent = new Intent(VipListActivity.this, UserInfoActivity.class);
                        intent.putExtra(Constant.EXTRA_KEY_USER_ID, ((User) parent.getAdapter().getItem(position)).id);
                        startActivity(intent);
                        break;
                }
                ;

            }
        };
        mListView.setOnItemClickListener(register);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mCurPage = 1;
                switch (isSearch) {
                    case SEARCH_TAG:
                        doSearch();
                        break;
                    default:
                        callAPIListUserByLevel();
                        break;
                }

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (mCurPage < mTotalPage && mUserList != null) {
                    mCurPage++;
                    switch (isSearch) {
                        case SEARCH_TAG:
                            doSearch();
                            break;
                        default:
                            callAPIListUserByLevel();
                            break;
                    }
                }
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                keyWord = mInput.getText().toString().trim();
                if (TextUtils.isEmpty(keyWord)) {
                    ToastUtil.show(getApplicationContext(), "搜索内容不能为空");
                } else {
                    isSearch = SEARCH_TAG;
                    mCurPage = 1;
                    doSearch();
                }
            }
        });

        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (TextUtils.isEmpty(s.toString())) {
                    search_close_clear.setVisibility(View.GONE);
                    isSearch = DEFULT_TAG;
                    mCurPage = 1;
                    callAPIListUserByLevel();
                }else{
                    search_close_clear.setVisibility(View.VISIBLE);
                }
            }
        });
        search_close_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_close_clear.setVisibility(View.GONE);
                mInput.setText("");
            }
        });
    }

    public void doSearch() {

        showLoading();
        Hashtable<String, String> params = new Hashtable();
        params.put("keyword", keyWord);
        params.put("page", mCurPage + "");
        params.put(APIManager.KEY_PAGE_SIZE, "1000");
        APIManager.getInstance(this).userDownSearch(params, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                hideNoData();
                showException(error);
                mListView.onRefreshComplete();
            }
        }, new Response.Listener<RequestListResult<User>>() {
            @Override
            public void onResponse(RequestListResult<User> response) {
                hideLoading();
                hideNoData();
                if (hasError(response)) {
                    return;
                }
                if (response.data != null ) {
                    if (mCurPage == 1) {
                        mUserList = response.data;
                        mAdapter.setVipList(mUserList);
                    } else if (mCurPage > 1 && mUserList != null) {
                        mUserList.addAll(response.data);
                    }
                    mCurPage = response.page;
                    mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
                    mAdapter.notifyDataSetChanged();
                }else{
                    ToastUtil.show(getApplicationContext(),"没有搜索到数据");
                    showNoDataTips();
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

    private void callAPIListUserByLevel() {
        showLoading();

        switch (fromActivity) {
            case MySalesFragment.TAG_INT:
                mListView.setPullToRefreshOverScrollEnabled(false);
                APIManager.getInstance(this).listUsersByChat(null, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        hideNoData();
                        showException(error);
                        mListView.onRefreshComplete();
                    }
                }, new Response.Listener<RequestResult<User.LeveledUsers>>() {
                    @Override
                    public void onResponse(RequestResult<User.LeveledUsers> response) {
                        hideLoading();
                        hideNoData();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null && response.data.down != null && response.data.down.size() > 0) {
                            mUserList = response.data.down;
                            mAdapter.setVipList(mUserList);
                            mAdapter.notifyDataSetChanged();
                        }

                        if (mAdapter.getCount() == 0) {
                            showNoDataTips();
                        }
                        mListView.onRefreshComplete();
                    }
                });
                break;
            default:
                Hashtable<String, String> parameters = new Hashtable<String, String>();
                parameters.put("page", mCurPage + "");
                parameters.put("levelIndex", mLevelIndex);
                APIManager.getInstance(this).listUsersByLevel(parameters, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        hideNoData();
                        showException(error);
                        mListView.onRefreshComplete();
                    }
                }, new Response.Listener<RequestListResult<User>>() {
                    @Override
                    public void onResponse(RequestListResult<User> response) {
                        hideLoading();
                        hideNoData();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null && response.data.size() > 0) {
                            if (mCurPage == 1) {
                                mUserList = response.data;
                                mAdapter.setVipList(mUserList);
                            } else if (mCurPage > 1 && mUserList != null) {
                                mUserList.addAll(response.data);
                            }
                            mCurPage = response.page;
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
                break;
        }

    }

    @Override
    public void showLoading() {
        if (mAdapter.getCount() == 0) {
            super.showLoading();
        }
    }
}
