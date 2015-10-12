package com.socialbusiness.dev.orangebusiness.activity.login;

import java.util.Hashtable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.MD5;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

public class FindPasswordStep2Activity extends BaseActivity {

	private EditText mPassword;
	private EditText mPasswordAgain;
	private TextView mConfirm;
	
	private ProgressDialog mLoadingDialog;
	private boolean isLoading;
	
	private String phone;
	private String smsVerify;
	
	private InputMethodManager imm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_password_step2);
		setUp();
		findView();
		registerListener();
	}
	
	private void setUp() {
		setTitle(R.string.reset_password);
		phone = getIntent().getStringExtra("phone");
		smsVerify = getIntent().getStringExtra("smsVerify");
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	}
	
	private void findView() {
		mPassword = (EditText) findViewById(R.id.activity_find_password_step2_input_new_password);
		mPasswordAgain = (EditText) findViewById(R.id.activity_find_password_step2_input_new_password_again);
		mConfirm = (TextView) findViewById(R.id.activity_find_password_step2_confirm);
		
		mLoadingDialog = new ProgressDialog(this);
	}
	
	private void registerListener() {
		mConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doResetPassword();
			}
		});
	}
	
	private void doResetPassword() {
		if (isLoading) {
			return;
		}
		if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(smsVerify)) {
			ToastUtil.show(FindPasswordStep2Activity.this, "必要信息不完整，请返回上一步重新填写");
			return;
		}
		
		final String newPassword = mPassword.getText().toString();
		final String newPasswordAgain = mPasswordAgain.getText().toString();
		
		if (TextUtils.isEmpty(newPassword)) {
			AndroidUtil.setError(mPassword, "密码不能为空");
			return ;
		}
		if (TextUtils.isEmpty(newPasswordAgain)) {
			AndroidUtil.setError(mPasswordAgain, "密码不能为空");
			return ;
		}
		if (!newPassword.equals(newPasswordAgain)) {
			ToastUtil.show(FindPasswordStep2Activity.this, "两次输入的密码不一致，请校对");
			return;
		}
		
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("phone", phone);
		parameters.put("smsVerify", smsVerify);
		parameters.put("password", MD5.encode(newPassword));
		APIManager.getInstance(this).userResetPassword(parameters, 
				new Response.ErrorListener() {
			
					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
						showException(error);
					}
				}, new Response.Listener<RequestResult<?>>() {
					
					@Override
					public void onResponse(RequestResult<?> response) {
						hideLoading();
						if (hasError(response)) {
							return;
						}
						if (response.code == 0) {
							ToastUtil.show(FindPasswordStep2Activity.this, "重设密码成功，请记住新密码");
							Intent intent = new Intent(FindPasswordStep2Activity.this, LoginActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}
					}
				});
	}
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if (ev.getAction() == MotionEvent.ACTION_DOWN
    			|| ev.getAction() == MotionEvent.ACTION_MOVE
    			|| ev.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
    		View v = getCurrentFocus();
    		if (AndroidUtil.isShouldHideInput(v, ev) && imm != null) {
    			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    		}
    		return super.dispatchTouchEvent(ev);
    	}
    	
    	// 必不可少，否则所有的组件都不会有TouchEvent了
    	if (getWindow().superDispatchTouchEvent(ev)) {
    		return true;
    	}
    	return onTouchEvent(ev);
    }
	
	@Override
	public void showLoading() {
		isLoading = true;
		mLoadingDialog.show();
	}
	
	@Override
	public void hideLoading() {
		isLoading = false;
		mLoadingDialog.dismiss();
	}
	
}
