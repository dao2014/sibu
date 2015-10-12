package com.socialbusiness.dev.orangebusiness.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.NoticeAdapter;
import com.socialbusiness.dev.orangebusiness.model.Notice;

public class NoticeListAcitivity extends BaseActivity {
	public NoticeAdapter mAdapter;
	public PullToRefreshListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice_list);
		setUP();
		findView();
		registerListener();
	}

	public void setUP() {
		setTitle(R.string.notice_item);
		mAdapter = new NoticeAdapter(this);
	}

	public void findView() {
		mListView = (PullToRefreshListView) this
				.findViewById(R.id.activity_notice_list);
		mListView.setAdapter(mAdapter);
	}

	public void registerListener() {
		OnItemClickListener register = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Notice notice = (Notice) parent.getAdapter().getItem(position);
				Intent intent = new Intent(NoticeListAcitivity.this,
						NoticeDetailActivity.class);
				intent.putExtra("id", notice.id);
				startActivity(intent);
			}
		};
		mListView.setOnItemClickListener(register);
	}
}
