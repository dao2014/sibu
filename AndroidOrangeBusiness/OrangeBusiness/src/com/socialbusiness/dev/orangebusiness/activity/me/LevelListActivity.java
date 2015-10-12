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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.LevelAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.VipAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Level;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by enwey on 2014/11/3.
 */
public class LevelListActivity extends BaseActivity {
    private ListView mListView;
    private LevelAdapter mAdapter;
    private VipAdapter mVipAdapter;
    private List<Level> mLevelList;
    private String mUserId;
    private List<User> mUserList;
    private int mCurPage;
    private int mTotalPage;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_list);
        setUp();
        findView();
        registerListener();
        callAPIListLevel();
    }

    private void setUp() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mUserId = getIntent().getStringExtra("userId");
        if (!TextUtils.isEmpty(mUserId) && !mUserId.equals(SettingsManager.getLoginUser().id)) {
            setTitle(R.string.his_lower_level);
        } else {
            setTitle(R.string.level_title);
        }
        mCurPage = 1;
        mAdapter = new LevelAdapter(this);

        //设置顶部title栏颜色
//        setLeftIcon(R.drawable.ic_back_orange_selector);
//        setHeaderTitleBarBg(R.color.white);
//        setTitleTextColor(R.color.black);
    }

    private TextView mSearchBtn;
    private EditText mInput;
    private String keyWord;
    View search_close_clear;
    private ListView user_search_list;

    private void findView() {
        mListView = (ListView) findViewById(R.id.activity_level_list);
        mListView.setAdapter(mAdapter);

        mInput = (EditText) findViewById(R.id.fragment_product_input);
        mSearchBtn = (TextView) findViewById(R.id.fragment_product_searchTxt);
        user_search_list = (ListView) this.findViewById(R.id.user_search_list);
        mVipAdapter = new VipAdapter(this);
        user_search_list.setAdapter(mVipAdapter);

        search_close_clear = findViewById(R.id.search_close_clear);
    }

    private void registerListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(LevelListActivity.this, VipListActivity.class);
                intent.putExtra("index", ((Level) parent.getAdapter().getItem(position)).levelIndex + "");
                intent.putExtra("levelName", ((Level) parent.getAdapter().getItem(position)).name);
                intent.putExtra(Constance.COMEFROM, getIntent().getIntExtra(Constance.COMEFROM, -1));
                startActivity(intent);
            }
        });
        user_search_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(LevelListActivity.this, UserInfoActivity.class);
                intent.putExtra(Constant.EXTRA_KEY_USER_ID, ((User) parent.getAdapter().getItem(position)).id);
                startActivity(intent);
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
                    user_search_list.setVisibility(View.GONE);
                } else {
                    search_close_clear.setVisibility(View.VISIBLE);
                }
            }
        });
        search_close_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_search_list.setVisibility(View.GONE);
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
        APIManager.getInstance(this).userSearch(params, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                hideNoData();
                showException(error);

            }
        }, new Response.Listener<RequestListResult<User>>() {
            @Override
            public void onResponse(RequestListResult<User> response) {
                hideLoading();
                hideNoData();
                if (hasError(response)) {
                    return;
                }
                if (response.data != null) {
//                    if (mCurPage == 1) {
                    mUserList = response.data;
                    mVipAdapter.setVipList(mUserList);
//                    } else if (mCurPage > 1 && mUserList != null) {
//                        mUserList.addAll(response.data);
//                    }
//                    mCurPage = response.page;
//                    mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
                    mVipAdapter.notifyDataSetChanged();
                    user_search_list.setVisibility(View.VISIBLE);
                } else {
                    ToastUtil.show(getApplicationContext(), "没有搜索到数据");
//                    showNoDataTips();
                }

//                if (mVipAdapter.getCount() == 0) {
//                    showNoDataTips();
//                } else if (mCurPage >= mTotalPage) {
//                    mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//                } else {
//                    mListView.setMode(PullToRefreshBase.Mode.BOTH);
//                }
//                mListView.onRefreshComplete();
            }
        });
    }

    private void callAPIListLevel() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", mCurPage + "");
        if (mUserId != null) {
            parameters.put("userId", mUserId);
        }
        APIManager.getInstance(this).listLevel(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        hideNoData();
                        showException(error);
                    }
                }, new Response.Listener<RequestListResult<Level>>() {
                    @Override
                    public void onResponse(RequestListResult<Level> response) {
                        hideLoading();
                        hideNoData();
                        if (hasError(response)) {
                            return;
                        }

                        if (response.data.size() > 0) {
                            mLevelList = response.data;
                            mAdapter.setLevelList(mLevelList);
                            mAdapter.notifyDataSetChanged();
                        }

                        if (mAdapter.getCount() == 0) {
                            showNoDataTips();
                        }
                    }
                });
    }

}
