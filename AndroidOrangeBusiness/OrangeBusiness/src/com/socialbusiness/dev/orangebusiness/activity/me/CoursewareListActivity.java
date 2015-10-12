package com.socialbusiness.dev.orangebusiness.activity.me;

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
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.CoursewareAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Course;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;

import java.util.Hashtable;
import java.util.List;

public class CoursewareListActivity extends BaseActivity {
	private PullToRefreshListView mListView;
	private CoursewareAdapter mAdapter;
    private List<Course> mCourseList;
    private String categoryId;
    private int mTotalPage;
    private int mCurPage;

    private int mSearchCurPage = 1;
    private int mSearchTotalPage;
    private List<Course> mSearchCourseList;

    private String keyword;
    private boolean isSearch;
    
    private User mLoginUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_courseware_list);
		setUp();
		findView();
        if (isSearch) {
            callAPIListCourseSearch();
        } else {
            callAPIListCourse();
        }
		registerListener();
		
		mLoginUser = SettingsManager.getLoginUser();
		if(mLoginUser == null) {
			Intent intent = new Intent(Constant.RECEIVER_NO_LOGIN);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
			finish();
		}
	}

	private void setUp() {
		setTitle(R.string.courseware_title);
        mCurPage = 1;
        categoryId = getIntent().getStringExtra("categoryId");
        isSearch = getIntent().getBooleanExtra("isSearch", false);
        keyword = getIntent().getStringExtra("keyword");
		mAdapter = new CoursewareAdapter(this);
	}

	private void findView() {
		mListView = (PullToRefreshListView) this.findViewById(R.id.activity_courseware_list);
		mListView.setAdapter(mAdapter);
	}

    private void callAPIListCourse() {
        showLoading();
        hideNoData();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("page", mCurPage + "");
        parameters.put("categoryId", categoryId);
        APIManager.getInstance(this).listCourse(parameters, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                showException(error);
                mListView.onRefreshComplete();
            }
        }, new Response.Listener<RequestListResult<Course>>() {

            @Override
            public void onResponse(RequestListResult<Course> response) {
                hideLoading();
                if (hasError(response)) {
                    return;
                }

                if (response.data != null && response.data.size() > 0) {
                    if (mCurPage == 1) {
                        mCourseList = response.data;
                        mAdapter.setCoursewareList(mCourseList);
                    }else if (mCurPage > 1 && mCourseList != null) {
                        mCourseList.addAll(response.data);
                    }
                    mCurPage = response.page;
                    mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
                    mAdapter.notifyDataSetChanged();
                    mListView.onRefreshComplete();
                }

                if (mAdapter.getCount() == 0) {
                    showNoDataTips();
                }else if (mCurPage >= mTotalPage) {
                    mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                }else {
                    mListView.setMode(PullToRefreshBase.Mode.BOTH);
                }
                mListView.onRefreshComplete();
            }
        });
    }

    private void callAPIListCourseSearch() {
        showLoading();
        hideNoData();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("page", mSearchCurPage + "");
        parameters.put("keyword", keyword);
        APIManager.getInstance(this).listCourseSearch(parameters, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                showException(error);
                mListView.onRefreshComplete();
            }
        }, new Response.Listener<RequestListResult<Course>>() {

            @Override
            public void onResponse(RequestListResult<Course> response) {
                hideLoading();
                if (hasError(response)) {
                    return;
                }
                if (response.data != null && response.data.size() > 0) {
                    if (mSearchCurPage == 1) {
                        mSearchCourseList = response.data;
                        mAdapter.setCoursewareList(mSearchCourseList);
                    }else if (mSearchCurPage > 1 && mSearchCourseList != null) {
                        mSearchCourseList.addAll(response.data);
                    }
                    mSearchCurPage = response.page;
                    mSearchTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
                    mAdapter.notifyDataSetChanged();
                    mListView.onRefreshComplete();
                }

                if (mAdapter.getCount() == 0) {
                    showNoDataTips();
                }else if (mSearchCurPage >= mSearchTotalPage) {
                    mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                }else {
                    mListView.setMode(PullToRefreshBase.Mode.BOTH);
                }
                mListView.onRefreshComplete();
            }
        });
    }

    @Override
    public void showLoading() {
    	if(mAdapter.getCount() == 0){
    		super.showLoading();
    	}
    }
	private void registerListener() {
		OnItemClickListener register = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View convertView,
					int position, long id) {
                Course course = (Course) parent.getAdapter().getItem(position);
                if (course != null) {
                    Intent intent = new Intent(CoursewareListActivity.this, NoticeDetailActivity.class);
                    intent.putExtra("course", course);
                    startActivity(intent);
                }
			}
		};
		mListView.setOnItemClickListener(register);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (isSearch) {
                    mSearchCurPage = 1;
                    callAPIListCourseSearch();
                } else {
                    mCurPage = 1;
                    callAPIListCourse();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (isSearch) {
                    if (mSearchCurPage < mSearchTotalPage && mSearchCourseList != null) {
                        mSearchCurPage++;
                        callAPIListCourseSearch();
                    }
                } else {
                    if (mCurPage < mTotalPage && mCourseList != null) {
                        mCurPage++;
                        callAPIListCourse();
                    }
                }
            }
        });
	}
}
