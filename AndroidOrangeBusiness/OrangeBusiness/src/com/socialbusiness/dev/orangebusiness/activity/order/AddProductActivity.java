package com.socialbusiness.dev.orangebusiness.activity.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.AddProductAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

public class AddProductActivity extends BaseActivity {

	private EditText mKeyword;
	private TextView mSearchBtn;
	private PullToRefreshListView mListView;
	private AddProductAdapter mAdapter;
	
	private List<Product> mProductList;
	private int mCurPage = 1;
	private int mTotalPage;

	private List<Product> mSearchProductList;
	private int mSearchCurPage = 1;
	private int mSearchTotalPage; 
	
	private String keyword;
	private boolean isRefresh = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_product);
		findViews();
		setUp();
		setListeners();
		callAPIListProduct();
	}
	
	private void findViews() {
		mKeyword = (EditText) findViewById(R.id.activity_add_product_input);
		mSearchBtn = (TextView) findViewById(R.id.activity_add_product_searchTxt);
		mListView = (PullToRefreshListView) findViewById(R.id.activity_add_product_listview);
	}
	
	private void setUp() {
		setTitle(R.string.add_product);
		mAdapter = new AddProductAdapter(this);
		mListView.setAdapter(mAdapter);
		mSearchBtn.setHint(getResources().getString(R.string.input_product_name_to_search));
	}
	
	private void setListeners() {
		mListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			
			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				keyword = mKeyword.getText().toString().trim();
				isRefresh = true;
				if (!TextUtils.isEmpty(keyword)) {
					mSearchCurPage = 1;
					callAPIListProductSearch();
				} else {
					mCurPage = 1;
					callAPIListProduct();
				}
			}
			
			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				keyword = mKeyword.getText().toString().trim();
				if (!TextUtils.isEmpty(keyword)) {
					if (mSearchCurPage < mSearchTotalPage && mSearchProductList != null) {
						mSearchCurPage++;
						callAPIListProductSearch();
					}
				} else {
					if (mCurPage < mTotalPage && mProductList != null) {
						mCurPage++;
						callAPIListProduct();
					}
				}
			}
		});
		
		mSearchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				keyword = mKeyword.getText().toString().trim();
				if (TextUtils.isEmpty(keyword)) {
					ToastUtil.show(AddProductActivity.this, "搜索内容不能为空");
					return;
				}
				callAPIListProductSearch();
			}
		});
		
		setLeftBtn(R.drawable.ic_back_selector, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doClickBack();
			}
		});
	}
	
	private void doClickBack() {
		HashMap<String, Boolean> isAddMap = mAdapter.getIsAddMap();
		if (mProductList != null 
				&& isAddMap != null 
				&& isAddMap.containsValue(true)) {

			ArrayList<Product> productList = new ArrayList<Product>();
			for (int i = 0; i < mProductList.size(); i++) {
				if (isAddMap.containsKey(mProductList.get(i).id) 
						&& isAddMap.get(mProductList.get(i).id)) {
					productList.add(mProductList.get(i));
				}
			}
			if (productList.size() != 0) {
				Intent intent = new Intent();
				intent.putExtra(Constant.EXTRA_KEY_PRODUCT_LIST, productList);
				setResult(RESULT_OK, intent);
			}
		}
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			doClickBack();
		}
		return true;
	}
	
	private void callAPIListProduct() {
		showLoading();
		hideNoData();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("page", mCurPage + "");
		APIManager.getInstance(this).listProduct(parameters,
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						showException(error);
						mListView.onRefreshComplete();
					}
				}, new Response.Listener<RequestListResult<Product>>() {

					@Override
					public void onResponse(RequestListResult<Product> response) {
						hideLoading();
						hideNoData();
						if (hasError(response)) {
							return;
						}
						if (response.data != null) {
							if (mCurPage == 1) {
								mProductList = response.data;
								if (isRefresh) {
									isRefresh = false;
									mAdapter.updateProductList(mProductList);
								} else {
									mAdapter.setProductList(mProductList);
								}
							} else if (mCurPage > 1 && mProductList != null) {
								mProductList.addAll(response.data);
								mAdapter.addProductList(response.data);
							}

							mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
							mAdapter.notifyDataSetChanged();
						}
						if (mAdapter.getCount() == 0) {
							showNoDataTips();
						} else if (mCurPage >= mTotalPage) {
							mListView.setMode(Mode.PULL_FROM_START);
						} else {
							mListView.setMode(Mode.BOTH);
						}
						mListView.onRefreshComplete();
					}
				});
	}

	private void callAPIListProductSearch() {
		showLoading();
		hideNoData();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("page", mSearchCurPage + "");
		parameters.put("keyword", keyword);
		APIManager.getInstance(this).listProductSearch(parameters,
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						showException(error);
						mListView.onRefreshComplete();
					}
				}, new Response.Listener<RequestListResult<Product>>() {

					@Override
					public void onResponse(RequestListResult<Product> response) {
						hideLoading();
						hideNoData();
						if (hasError(response)) {
							return;
						}
						if (response.data != null) {
							if (mSearchCurPage == 1) {
								mSearchProductList = response.data;
								if (isRefresh) {
									isRefresh = false;
									mAdapter.updateProductList(mSearchProductList);
								} else {
									mAdapter.setProductList(mSearchProductList);
								}
							} else if (mSearchCurPage > 1 && mSearchProductList != null) {
								mSearchProductList.addAll(response.data);
								mAdapter.addProductList(response.data);
							}

							mSearchTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
							mAdapter.notifyDataSetChanged();
						}

						if (mAdapter.getCount() == 0) {
							showNoDataTips();
						} else if (mSearchCurPage >= mSearchTotalPage) {
							mListView.setMode(Mode.PULL_FROM_START);
						} else {
							mListView.setMode(Mode.BOTH);
						}
						mListView.onRefreshComplete();
					}
				});
	}
}
