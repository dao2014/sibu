package com.socialbusiness.dev.orangebusiness.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.socialbusiness.dev.orangebusiness.activity.product.ProductListActivity;
import com.socialbusiness.dev.orangebusiness.adapter.CategoryAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.model.Category;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;
import java.util.List;

public class CourseClassifyListActivity extends BaseActivity {

    private TextView mSearah;
    private EditText mKeyword;
	private CategoryAdapter mAdapter;
	private PullToRefreshListView mListView;
    private List<Category> mCategoryList;
    private int mCurPage;
    private int mTotalPage;
    private int mProductTypeInt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_courseclassify);
		setUp();
		findView();
        registerListener();
        setDifference();
	}

	private void setUp() {
        mCurPage = 1;
		mAdapter = new CategoryAdapter(this);
        mProductTypeInt = getIntent().getIntExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, Constant.DEFAULT_ILLEGAL_PRODUCT_TYPE);
        mAdapter.setProductType(mProductTypeInt);
	}

	private void findView() {
		mListView = (PullToRefreshListView) this.findViewById(R.id.activity_courseware_listview);
        mSearah = (TextView) findViewById(R.id.activity_courseware_searchTxt);
        mKeyword = (EditText) findViewById(R.id.activity_courseware_input);
		mListView.setAdapter(mAdapter);
	}

    private void setDifference() {
        if (mProductTypeInt == Product.ProductType.KWMProductTypeStock.getIntValue()) {
            mAdapter.isCourseCategory = false;
            setTitle(getApplication().getString(R.string.main_tab_product));
            mKeyword.setHint("请输入产品名称进行搜索");
            callAPIListProductCategory();
        } else {
            mAdapter.isCourseCategory = true;
            setTitle(getApplication().getString(R.string.courseware));
            mKeyword.setHint("请输入课件名称进行搜索");
            callAPIListCourseCategories();
        }
    }

    private void callAPIListCourseCategories() {
        showLoading();
        hideNoData();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("page", mCurPage+ "");
        APIManager.getInstance(this).listCourseCategories(parameters, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                errorCallBack(error);
            }
        }, new Response.Listener<RequestListResult<Category>>() {

            @Override
            public void onResponse(RequestListResult<Category> response) {
                successCallBack(response);
            }
        });
    }

    private void callAPIListProductCategory() {
        showLoading();
        hideNoData();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("page", mCurPage + "");
        APIManager.getInstance(this).listProductCategories(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorCallBack(error);
                    }
                }, new Response.Listener<RequestListResult<Category>>() {

                    @Override
                    public void onResponse(RequestListResult<Category> response) {
                        successCallBack(response);
                    }
                });
    }

    private void successCallBack(RequestListResult<Category> response) {
        hideLoading();
        if (hasError(response)) {
            return;
        }
        if (response.data != null && response.data.size() > 0) {
            if (mCurPage == 1) {
                mCategoryList = response.data;
                mAdapter.setCategoryList(mCategoryList);
            } else if (mCurPage > 1 && mCategoryList != null) {
                mCategoryList.addAll(response.data);
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

    private void errorCallBack(VolleyError error) {
        hideLoading();
        showException(error);
        mListView.onRefreshComplete();
    }

    @Override
    public void showLoading() {
    	if(mAdapter.getCount() == 0){
    		super.showLoading();
    	}
    }

    private void registerListener() {
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mCurPage = 1;
                if (mProductTypeInt == Product.ProductType.KWMProductTypeStock.getIntValue()) {
                    callAPIListProductCategory();
                    return;
                }
                callAPIListCourseCategories();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (mCurPage < mTotalPage && mCategoryList != null) {
                    mCurPage ++;
                    if (mProductTypeInt == Product.ProductType.KWMProductTypeStock.getIntValue()) {
                        callAPIListProductCategory();
                        return;
                    }
                    callAPIListCourseCategories();
                }
            }
        });

        mSearah.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String keyword = mKeyword.getText().toString().trim();
                if (TextUtils.isEmpty(keyword)) {
                    ToastUtil.show(CourseClassifyListActivity.this, "搜索内容不能为空");
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("isSearch", true);
                    intent.putExtra("keyword", keyword);
                    mKeyword.setText("");
                    if (mProductTypeInt == Product.ProductType.KWMProductTypeStock.getIntValue()) {
                        intent.setClass(CourseClassifyListActivity.this, ProductListActivity.class);
                        intent.putExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, mProductTypeInt);
                    } else {
                        intent.setClass(CourseClassifyListActivity.this, CoursewareListActivity.class);
                    }
                    startActivity(intent);
                }
            }
        });
    }
}
