package com.socialbusiness.dev.orangebusiness.fragment.product;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.product.ProductListActivity;
import com.socialbusiness.dev.orangebusiness.adapter.CategoryAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.model.Category;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;
import java.util.List;

public class ProductFragment extends BaseFragment {

	private EditText mInput;
	private TextView mSearchBtn;

	private PullToRefreshListView mListView;
	private CategoryAdapter mAdapter;

	private List<Category> mCategoryList;
	private int mCurPage = 1;
	private int mTotalPage;

	private InputMethodManager imm;
	private String keyWord;

	@Override
	public void setContentView(LayoutInflater inflater,
			ViewGroup mLayerContextView) {
		inflater.inflate(R.layout.fragment_product, mLayerContextView);
		setUp();
		findView(mLayerContextView);
		registerListener();
		callAPIListProductCategory();
	}

	private void setUp() {
		mAdapter = new CategoryAdapter(getActivity());
		imm = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
	}

	private void findView(View view) {
		mInput = (EditText) view.findViewById(R.id.fragment_product_input);
		mSearchBtn = (TextView) view.findViewById(R.id.fragment_product_searchTxt);
		mListView = (PullToRefreshListView) view.findViewById(R.id.fragment_product_listview);
		mListView.setAdapter(mAdapter);
		mListView.clearDivider();
		
		showFragment();
	}

	private void registerListener() {
		mSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
				keyWord = mInput.getText().toString().trim();
				if (TextUtils.isEmpty(keyWord)) {
					ToastUtil.show(getActivity(), "搜索内容不能为空");
				} else {
					Intent intent = new Intent(getActivity(),
							ProductListActivity.class);
					intent.putExtra("categoryId", "0");
					intent.putExtra("isSearch", true);
					intent.putExtra("keyword", keyWord);
                    // 将搜索框内容清空
                    mInput.setText("");
					startActivity(intent);
				}
			}
		});

		mListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				mCurPage = 1;
				callAPIListProductCategory();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				if (mCurPage < mTotalPage && mCategoryList != null) {
					mCurPage++;
					callAPIListProductCategory();
				}
			}
		});

	}

	private void callAPIListProductCategory() {
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("page", mCurPage + "");
		APIManager.getInstance(getActivity()).listProductCategories(parameters,
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						showException(error);
						mListView.onRefreshComplete();
					}
				}, new Response.Listener<RequestListResult<Category>>() {

					@Override
					public void onResponse(RequestListResult<Category> response) {
						hideLoading();
						hideTips();
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

							mCurPage = response.page;
							mTotalPage = AndroidUtil.getTotalPage(
									response.totalCount, response.pageSize);
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

	@Override
	public void showLoading() {
		if (mAdapter.getCount() == 0) {
			super.showLoading();
		}
	}
}
