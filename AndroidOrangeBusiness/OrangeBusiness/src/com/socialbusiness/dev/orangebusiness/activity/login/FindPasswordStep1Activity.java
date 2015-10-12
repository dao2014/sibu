package com.socialbusiness.dev.orangebusiness.activity.login;

import java.util.Hashtable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.KWTimer;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.socialbusiness.dev.orangebusiness.util.KWTimer.TimerListener;
import com.socialbusiness.dev.orangebusiness.util.Validator;

public class FindPasswordStep1Activity extends BaseActivity {

	private EditText mPhone;
	private EditText mVerifyCode;
	private TextView mGetVerifyCode;
	private TextView mNext;
	
	private ProgressDialog mLoadingDialog;
	private boolean isLoading;
	private KWTimer mKWTimer;
	
	private InputMethodManager imm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_password_step1);
		setUp();
		findView();
		registerListener();
	}
	
	private void setUp() {
		setTitle(R.string.find_password);
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	}
	
	private void findView() {
		mPhone = (EditText) findViewById(R.id.activity_find_password_step1_input_phone);
		mVerifyCode = (EditText) findViewById(R.id.activity_find_password_step1_input_verifycode);
		mGetVerifyCode = (TextView) findViewById(R.id.activity_find_password_step1_get_verifycode);
		mNext = (TextView) findViewById(R.id.activity_find_password_step1_next);
		
		mPhone.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		mLoadingDialog = new ProgressDialog(this);
		mKWTimer = new KWTimer(60);
	}
	
	private void registerListener() {
		mKWTimer.setTimerListener(new TimerListener() {
			
			@Override
			public void onStop() {
				MyApplication.uiHandler.post(new Runnable() {
					
					@Override
					public void run() {
						if (mGetVerifyCode != null) {
							mGetVerifyCode.setEnabled(true);
						}
					}
				});
			}
			
			@Override
			public void onRunning(final int currentSeconds) {
				MyApplication.uiHandler.post(new Runnable() {
					
					@Override
					public void run() {
						if (mGetVerifyCode != null) {
							if (currentSeconds == 1) {
                                mGetVerifyCode.setText(String.format("获取%d秒", currentSeconds));
                                mPhone.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        mGetVerifyCode.setText("获取验证码");
                                    }
                                }, 1000);
							} else {
								mGetVerifyCode.setText(String.format("获取%d秒", currentSeconds));
							}
						}
					}
				});
			}
		});
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.activity_find_password_step1_get_verifycode) {
					if (mGetVerifyCode.isEnabled()) {
						requestVerifyCode();
					}
				} else if (v.getId() == R.id.activity_find_password_step1_next) {
					goNext();
				}
			}
		};
		
		mGetVerifyCode.setOnClickListener(listener);
		mNext.setOnClickListener(listener);
	}
	
	private void requestVerifyCode() {
		if (isLoading) {
			return;
		}

		if(!Validator.isPhone(mPhone.getText().toString().trim())) {
			AndroidUtil.setError(mPhone, "请输入正确的手机号码");
			return ;
		}
		
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<String, String>();
		parameters.put("phone", mPhone.getText().toString().trim());
		APIManager.getInstance(this).userResetPasswordSms(parameters, 
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
						if (mKWTimer != null) {
							if (mGetVerifyCode != null) {
								mGetVerifyCode.setEnabled(false);
							}
							mKWTimer.start();
							ToastUtil.show(FindPasswordStep1Activity.this, response.message);
						}
					}
				});
	}
	
	private void goNext() {
		if (isLoading) {
			return;
		}
		
		final String phone = mPhone.getText().toString().trim();
		final String verifyCode = mVerifyCode.getText().toString().trim();
		
		if (!Validator.isPhone(phone)) {
			AndroidUtil.setError(mPhone, "请输入正确的手机号码");
			return;
		}
		if ("".equals(verifyCode)) {
			AndroidUtil.setError(mVerifyCode, "请输入手机收到的验证码");
			return;
		}
		
		requestCheckInput(phone, verifyCode);
	}
	
	private void requestCheckInput(final String phone, final String verifyCode) {
		showProgressDialog().setCancelable(false);
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put("phone", phone);
		params.put("smsVerify", verifyCode);
		APIManager.getInstance(getApplicationContext()).userCheckResetPwdSms(
                params,
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
                        if (hasError(response)) {
                            return;
                        }
                        if (response.code == 0) {
                            Intent intent = new Intent(FindPasswordStep1Activity.this, FindPasswordStep2Activity.class);
                            intent.putExtra("phone", phone);
                            intent.putExtra("smsVerify", verifyCode);
                            startActivity(intent);
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
	protected void onDestroy() {
		if (mKWTimer != null) {
			mKWTimer.destroy();
			mKWTimer = null;
		}
		super.onDestroy();
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
