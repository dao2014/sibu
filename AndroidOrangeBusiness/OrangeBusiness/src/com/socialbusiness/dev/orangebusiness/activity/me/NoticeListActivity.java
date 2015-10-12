package com.socialbusiness.dev.orangebusiness.activity.me;

import java.util.Hashtable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.NoticeAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Notice;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;

public class NoticeListActivity extends BaseActivity {
	
	private int mCurrPage;
	private int mTotalPage;
	public NoticeAdapter mAdapter;
	public List<Notice> mNoticeList;
	public PullToRefreshListView mListView;
	private User mLoginUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice_list);
		setUP();
		findView();
		callAPIGetNotice();
		registerListener();
		
		mLoginUser = SettingsManager.getLoginUser();
		if(mLoginUser == null) {
			Intent intent = new Intent(Constant.RECEIVER_NO_LOGIN);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
			finish();
		}
	}

	public void setUP() {
		setTitle(R.string.notice_item);
		mAdapter = new NoticeAdapter(this);
		mCurrPage = 1;
	}

	public void findView() {
		mListView = (PullToRefreshListView) this
				.findViewById(R.id.activity_notice_list);
		mListView.setAdapter(mAdapter);
	}

	public void callAPIGetNotice() {
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<>();
		parameters.put("page", mCurrPage + "");
		APIManager.getInstance(this).listNotice(parameters,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						hideNoData();
						showException(error);
						mListView.onRefreshComplete();
					}
				}, new Response.Listener<RequestListResult<Notice>>() {
					
					@Override
					public void onResponse(RequestListResult<Notice> response) {
						hideLoading();
						hideNoData();
						if (hasError(response)) {
							return;
						}
						if (response.data != null && response.data.size() > 0) {
							if (mCurrPage == 1) {
								mNoticeList = response.data;
								mAdapter.setNoticeList(mNoticeList);
							} else if (mCurrPage > 1 && mNoticeList != null) {
								mNoticeList.addAll(response.data);
							}

							mCurrPage = response.page;
							mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
							mAdapter.notifyDataSetChanged();
						}

						if (mAdapter.getCount() == 0) {
							showNoDataTips();
						} else if (mCurrPage >= mTotalPage) {
							mListView.setMode(Mode.PULL_FROM_START);
						} else {
							mListView.setMode(Mode.BOTH);
						}
						mListView.onRefreshComplete();
					}
		});
	}

	public void registerListener() {
		OnItemClickListener onClickRegister = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(NoticeListActivity.this,
						NoticeDetailActivity.class);
				intent.putExtra("id", ((Notice) parent.getAdapter().getItem(position)).id);
				intent.putExtra("companyId", mLoginUser.companyId);
				startActivity(intent);
			}
		};

		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						mCurrPage = 1;
						callAPIGetNotice();
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						if (mCurrPage < mTotalPage && mNoticeList != null) {
							mCurrPage++;
							callAPIGetNotice();
						}
					}
				});

		mListView.setOnItemClickListener(onClickRegister);
	}
	
	@Override
	public void showLoading() {
		if (mAdapter.getCount() == 0) {
			super.showLoading();
		}
	}
}
