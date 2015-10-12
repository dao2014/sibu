package com.socialbusiness.dev.orangebusiness.activity.contact;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.model.UserType;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;
import java.util.List;

public class GroupCreateActivity extends BaseActivity {
	
	private EditText mGroupNameET;
	private EditText mGroupDescriptionET;
	private CheckBox mMemberInviterCB;
	private View mSelectLabelLayout;
	private TextView mLabelTV;
	private Button mCreateBtn;
	
	private List<UserType> mLabels;
	private UserType mSelectedLabel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_group);
		findViews();
		registerListeners();
	}

	private void findViews() {
		mGroupNameET = (EditText) findViewById(R.id.activity_new_group_nameET);
		mGroupDescriptionET = (EditText) findViewById(R.id.activity_new_group_descriptionET);
		mMemberInviterCB = (CheckBox) findViewById(R.id.activity_new_group_memberInviterCB);
		mSelectLabelLayout = findViewById(R.id.activity_new_group_selectLabelLayout);
		mLabelTV = (TextView) findViewById(R.id.activity_new_group_labelTV);
		mCreateBtn = (Button) findViewById(R.id.activity_new_group_createBtn);
		
		setTitle("创建群组");
	}

	private void registerListeners() {
		View.OnClickListener l = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.activity_new_group_selectLabelLayout) {
					if(mLabels != null && !mLabels.isEmpty()) {
						showLabels();
					}
					else {
						requestLabels();
					}
				}
				else if(v.getId() == R.id.activity_new_group_createBtn) {
					submit();
				}
			}
		};
		
		mSelectLabelLayout.setOnClickListener(l);
		mCreateBtn.setOnClickListener(l);
	}
	
	private void requestLabels() {
		showProgressDialog();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("page", "1");
		parameters.put("pageSize", "999");
		APIManager.getInstance(getApplicationContext()).listUserType(
				parameters, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideProgressDialog();
						showException(error, "获取标签列表失败，请重试");
					}
				}, 
				new Response.Listener<RequestListResult<UserType>>() {

					@Override
					public void onResponse(RequestListResult<UserType> response) {
						hideProgressDialog();
						if(response != null && response.data != null) {
							mLabels = response.data;
							showLabels();
						}
					}
				});
		
	}
	
	private void showLabels() {
		if(mLabels != null && !mLabels.isEmpty()) {
			final String[] items = new String[mLabels.size() + 1];
			items[0] = "不使用标签";
			for(int i = 1; i < items.length; i++) {
				items[i] = mLabels.get(i - 1).name;
			}
			new AlertDialog.Builder(this)
			.setTitle("请选择标签")
			.setItems(items, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which > 0) {
						mSelectedLabel = mLabels.get(which - 1);
					}
					else {
						mSelectedLabel = null;
					}
					mLabelTV.setText(items[which]);
					dialog.dismiss();
				}
			})
			.create()
			.show();
		}
	}
	
	private void submit() {
		if(!checkInput()) {
			return ;
		}
		//TODO 调用创建群API，需判断是否选择了label而调用不同的API
		if(mSelectedLabel == null) {
			//无标签创建群组
			submitWithoutLabel();
		}
		else {
			//使用标签创建群组
			submitWithLabel();
		}
	}
	
	private boolean checkInput() {
		if(TextUtils.isEmpty(mGroupNameET.getText().toString().trim())) {
			AndroidUtil.setError(mGroupNameET, "群组名称不能为空");
			return false;
		}
		if(TextUtils.isEmpty(mGroupDescriptionET.getText().toString().trim())) {
			AndroidUtil.setError(mGroupDescriptionET, "群组简介不能为空");
			return false;
		}
		return true;
	}
	
	private void submitWithoutLabel() {
		showProgressDialog();
		Hashtable<String, String> parameters = new Hashtable<>();
		parameters.put("name", mGroupNameET.getText().toString().trim());
		parameters.put("summary", mGroupDescriptionET.getText().toString().trim());
		parameters.put("isAllowInvite", mMemberInviterCB.isChecked() ? "1" : "0");
		APIManager.getInstance(getApplicationContext()).addGroup(
				parameters, 
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
						if(response.code == 0) {
							new AlertDialog.Builder(GroupCreateActivity.this)
							.setMessage("创建成功")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									setResult(RESULT_OK);
									finish();
								}
							})
							.create()
							.show();
						}
						else {
							ToastUtil.show(getApplicationContext(), response.message);
						}
					}
				});
	}
	
	private void submitWithLabel() {
		showProgressDialog();
		Hashtable<String, String> parameters = new Hashtable<>();
		parameters.put("name", mGroupNameET.getText().toString().trim());
		parameters.put("summary", mGroupDescriptionET.getText().toString().trim());
		parameters.put("isAllowInvite", mMemberInviterCB.isChecked() ? "1" : "0");
		parameters.put("userTypeId", mSelectedLabel.id);
		APIManager.getInstance(getApplicationContext()).addByUserType(
				parameters, 
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
						if(response.code == 0) {
							new AlertDialog.Builder(GroupCreateActivity.this)
							.setMessage("创建成功")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									setResult(RESULT_OK);
									finish();
								}
							})
							.create()
							.show();
						}
						else {
							ToastUtil.show(getApplicationContext(), response.message);
						}
					}
				});
	}

}
