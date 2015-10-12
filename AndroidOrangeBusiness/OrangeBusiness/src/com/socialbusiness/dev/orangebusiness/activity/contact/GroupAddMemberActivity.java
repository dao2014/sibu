package com.socialbusiness.dev.orangebusiness.activity.contact;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.adapter.ContactAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.headerlistview.HeaderListView;
import com.socialbusiness.dev.orangebusiness.model.User.LeveledUsers;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

public class GroupAddMemberActivity extends BaseActivity 
	{
	
	private HeaderListView mListView;
	private ContactAdapter mAdapter;
    String groupId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_add_members);
		findViews();
		registerListeners();

//		if(mGroup == null) {
//			finish();
//			return ;
//		}
        groupId=getIntent().getStringExtra(GroupDetailActivity.EXTRA_KEY_GROUP_ID);
		loadData();
		
	}

	private void findViews() {
		mListView = (HeaderListView) findViewById(R.id.activity_group_add_members_userLV);
		setTitle("添加群成员");
		setRightTxt("确定", 0, Color.WHITE);
		showRightTxt(true);
	}

	private void registerListeners() {
		setRightTxtOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submit();
			}
		});
	}
	
	private void submit() {
		if(mAdapter == null ) {
			return ;
		}
		if(mAdapter.getSelectedUserIds() == null || mAdapter.getSelectedUserIds().isEmpty()) {
			ToastUtil.show(getApplicationContext(), "尚未选择成员");
			return ;
		}
		showProgressDialog();
		APIManager.getInstance(getApplicationContext()).addMember(
				groupId,
				mAdapter.getSelectedUserIds(), 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideProgressDialog();
						showException(error);
					}
				}, 
				new Response.Listener<RequestResult<?>>() {

					@Override
					public void onResponse(RequestResult<?> response) {
						hideProgressDialog();
						if(response != null && response.code == 0) {
							new AlertDialog.Builder(GroupAddMemberActivity.this)
							.setMessage("添加群成员成功")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									setResult(RESULT_OK);
									finish();
								}
								
							})
							.create()
							.show();
						}
						else {
                            if(response==null)
							ToastUtil.show(getApplicationContext(), "添加群成员失败，请重试");
                            else ToastUtil.show(getApplicationContext(), response.message);
						}
					}
				});
	}
	
	private void loadData() {

		showLoading();
		APIManager.getInstance(getApplicationContext()).listUsersByGroupInvite(
				null, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						showException(error);
					}
				}, 
				new Response.Listener<RequestResult<LeveledUsers>>() {

					@Override
					public void onResponse(RequestResult<LeveledUsers> response) {
						hideLoading();
						if(response != null && response.data != null) {
							mAdapter = new ContactAdapter();
							mAdapter.setData(response.data.up, response.data.center, response.data.down);
							mAdapter.setDisplayGroup(false);
							mAdapter.setSelectable(true);
//							if(group != null) {
//								mAdapter.setGroupMembers(group.getMembers());
//							}
							mListView.setAdapter(mAdapter);
						}
						else {
							showNoDataTips();
						}
					}
				});
	}
	
	@Override
	protected void onDestroy() {
		hideProgressDialog();
		super.onDestroy();
	}



}
