package com.socialbusiness.dev.orangebusiness.activity.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.login.LoginActivity;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import java.lang.ref.WeakReference;

import cn.jpush.android.api.JPushInterface;

public class BaseActivity extends FragmentActivity {

	private RelativeLayout mHeaderLayout;
	private LinearLayout mLyerLeft;
	private ImageView mBtnLeft;
	private TextView mTVLeftLabel;
	private TextView mTxtTitle;
	private ImageView mTitleImage;
	private ImageView mBtnRight;
	private TextView mTxtRight;
	private FrameLayout mContentLayout;
	private View mLoadingLayout;
	private View mNoDataLayout;
	private ImageView mNoDataImage;
	private TextView mNoDataTxt;
	private ProgressDialog mProgressDialog;
	private AlertDialog mUmengUpdateDialog;
	
	private IntentFilter intentFilter;
	private BroadcastReceiverHelper mReceiver;

	private OnClickListener mLyerLeftClickListener;
	private boolean isDestroy;

	private InputMethodManager imm;
    protected WeakReference<? extends Activity> weakThis = new WeakReference<Activity>(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.RECEIVER_NO_LOGIN);
		intentFilter.addAction(Constant.RECEIVER_SHOW_LOGIN);
		
		mReceiver = new BroadcastReceiverHelper();
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(R.layout.activity_base);
		findView();
		LayoutInflater layoutInflater = getLayoutInflater();
		layoutInflater.inflate(layoutResID, mContentLayout,true);
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(R.layout.activity_base);
		findView();
		mContentLayout.addView(view);
	}

	private void findView(){
		mHeaderLayout = (RelativeLayout) findViewById(R.id.view_header_layoutMain);
		mLyerLeft = (LinearLayout) findViewById(R.id.view_header_mLyerLeft);
		mBtnLeft = (ImageView) findViewById(R.id.view_header_mBtnLeft);
		mBtnRight = (ImageView) findViewById(R.id.view_header_mBtnRight);
		mTVLeftLabel = (TextView) findViewById(R.id.view_header_mTVLeftLabel);
		mTxtRight = (TextView) findViewById(R.id.view_header_mTxtRight);
		mTxtTitle = (TextView) findViewById(R.id.view_header_mTxtTitle);
		mTitleImage = (ImageView) findViewById(R.id.view_header_mBtnTitle);
		mContentLayout = (FrameLayout) findViewById(R.id.ActivityBase_contentLayout);
		mLoadingLayout = findViewById(R.id.activity_base_loading_layout);
		mNoDataLayout = findViewById(R.id.activity_base_nodata_layout);
		mNoDataImage = (ImageView) findViewById(R.id.activity_base_nodata_image);
		mNoDataTxt = (TextView) findViewById(R.id.activity_base_nodata_text);
		setLeftLabel("");
		
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	}

	public MyApplication getMyApplication(){
		return (MyApplication) getApplication();
	}

	public void setHeaderTitleBarBg(int redId){
		mHeaderLayout.setBackgroundResource(redId);
	}

	public void isHeaderTitleBarVisible(boolean isVisible){
		if(isVisible){
			mHeaderLayout.setVisibility(View.VISIBLE);
		}else{
			mHeaderLayout.setVisibility(View.GONE);
		}
		
	}
	
	public void showBackBtn(boolean isShow){
    	if(isShow){
    		mLyerLeft.setVisibility(View.VISIBLE);
    	}else{
    		mLyerLeft.setVisibility(View.INVISIBLE);
    	}
    }

	public void setTitleBg(int bg){
		mTxtTitle.setVisibility(View.GONE);
		mTitleImage.setVisibility(View.VISIBLE);
		mTitleImage.setImageResource(bg);
	}
	
	public void showRightBtn(boolean isShow) {
		if(isShow){
			mBtnRight.setVisibility(View.VISIBLE);
    	}else{
    		mBtnRight.setVisibility(View.INVISIBLE);
    	}
	}

	/**
	 * 只有一个确定按钮的提示信息框
	 * @param message 显示的提示信息
	 */
	protected void showMessageByDialogOP(String message) {
		Dialog showDialog = new AlertDialog.Builder(BaseActivity.this)
				.setMessage(message)
				.setPositiveButton(getResources().getString(R.string.alertDialog_positive)
						, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}
						}).create();
		showDialog.show();
	}
	
	public void showRightTxt(boolean isShow) {
		if (isShow) {
			mTxtRight.setVisibility(View.VISIBLE);
		} else {
			mTxtRight.setVisibility(View.INVISIBLE);
		}
	}

	public void setRightTxt(String txt, int drawableId, int txtColor) {
		mTxtRight.setText(txt);
		mTxtRight.setTextColor(txtColor);
		mTxtRight.setPadding(0, 0, 0, 0);
		mTxtRight.setBackgroundResource(drawableId);
	}
	
	public void setRightTxt(String txt, int bgDrawableId, ColorStateList csl) {
		mTxtRight.setText(txt);
		mTxtRight.setBackgroundResource(bgDrawableId);
		if (csl != null) {
			mTxtRight.setTextColor(csl);
		}
	}
	
	public void setRightTxtOnClickListener(OnClickListener listener) {
		if (listener != null) {
			mTxtRight.setOnClickListener(listener);
		}
	}

	public void setLeftBtn(int resId,OnClickListener clickListener){
		if(mBtnLeft == null){
			return;
		}
		mBtnLeft.setImageResource(resId);
		if(clickListener != null){
			mBtnLeft.setOnClickListener(clickListener);
		}
		showBackBtn(true);
	}

	public void setLeftLabel(String label){
		mTVLeftLabel.setText(label);
		showBackBtn(true);

		setOnClickBackBtnListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void setOnClickBackBtnListener(View.OnClickListener l) {
		if(mLyerLeft != null) {
			mLyerLeft.setOnClickListener(l);
			mLyerLeftClickListener = l;
		}
	}
    
	public void setTitle(String title){
		mTitleImage.setVisibility(View.GONE);
		mTxtTitle.setVisibility(View.VISIBLE);
    	this.mTxtTitle.setText(title);
    }

    public void setTitle(int titleId){
    	mTitleImage.setVisibility(View.GONE);
		mTxtTitle.setVisibility(View.VISIBLE);
    	this.mTxtTitle.setText(titleId);
    }
    public void setTitleTextColor(int colorId){

        this.mTxtTitle.setTextColor(getResources().getColor(colorId));
    }

    public void setRightIcon(int resId){
    	mBtnRight.setImageResource(resId);
    	showRightBtn(true);
    }

    public void setLeftIcon(int resId){
    	mBtnLeft.setImageResource(resId);
    	showBackBtn(true);
    }

    public void setRightIconListener(OnClickListener listener){
    	if(mBtnRight != null){
    		mBtnRight.setOnClickListener(listener);
    	}
    }

    public void hideHeaderView(){
    	if(mHeaderLayout != null){
    		mHeaderLayout.setVisibility(View.GONE);
    	}
    }

    public void setRightIcon(int resId, OnClickListener listener){
    	mBtnRight.setImageResource(resId);
    	mBtnRight.setOnClickListener(listener);
    	showRightBtn(true);
    }
    
    public void setRightIconTxt(int resId){
    	
    }
	
    public void showLoading() {
		if(mLoadingLayout != null) {
			mLoadingLayout.setVisibility(View.VISIBLE);
		}
	}
    
    public  ProgressDialog showProgressDialog() {
    	return showProgressDialog(null);
    }
    
    public ProgressDialog showProgressDialog(String msg) {
    	if(mProgressDialog != null) {
    		mProgressDialog.dismiss();
    		mProgressDialog = null;
    	}
    	
    	mProgressDialog = new ProgressDialog(this);
    	if(!TextUtils.isEmpty(msg)) {
    		mProgressDialog.setMessage(msg);
    	}
    	mProgressDialog.show();
    	return mProgressDialog;
    }
    
    public void hideProgressDialog() {
    	if(mProgressDialog != null) {
    		mProgressDialog.dismiss();
    	}
    }
    
    public void showNetworkDisconnet(){
    	showNoData(0, "当前网络连接出错", true);
    }
    
    public void showNoDataTips(){
    	showNoData(0, "当前没有数据", false);
    }
    
    public void showNoData(int image,String message,boolean reload){
    	if(mNoDataLayout != null){
    		mNoDataImage.setImageResource(image);
        	mNoDataTxt.setText(message);
        	mNoDataLayout.setVisibility(View.VISIBLE);
    	}
    	
    }
    
    public void hideNoData(){
    	if(mNoDataLayout != null){
    		mNoDataLayout.setVisibility(View.GONE);
    	}
    }

	public void hideLoading() {
		if(mLoadingLayout != null) {
			mLoadingLayout.setVisibility(View.GONE);
		}
	}

    public void showException(Exception e){
        this.showException(e, null);
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
    
    public void showException(Exception e, String message){
    	if (e instanceof NetworkError) {
    		ToastUtil.show(this, "网络连接异常");
    		return;
    	}
        if(!TextUtils.isEmpty(message)){
            ToastUtil.show(this, message);
            return;
        }
        if(e == null){
            return;
        }
        if(e instanceof VolleyError){
        	if (TextUtils.isEmpty(e.getMessage())) {
        		ToastUtil.show(this, "请求数据失败，请重试");
        		return;
        	}
        }
        ToastUtil.show(this, e.getMessage());
    }


    public boolean hasError(RequestResult<?> requestResult){
        if(requestResult != null && requestResult.code == 0){
            return false;
        }
        if(requestResult != null && requestResult.code == 1){
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constant.RECEIVER_NO_LOGIN));
            return false;
        }
        String message = requestResult != null ? requestResult.message : null;
        if (TextUtils.isEmpty(message)){
            message = "请求数据失败，请重试";
        }
        ToastUtil.show(this, message);
        return true;
    }

    public boolean hasError(RequestListResult<?> requestListResult){
        if(requestListResult != null && requestListResult.code == 0){
            return false;
        }
        if(requestListResult != null && requestListResult.code == 1){
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constant.RECEIVER_NO_LOGIN));
            return false;
        }
        String message = requestListResult != null ? requestListResult.message : null;
        if (TextUtils.isEmpty(message)){
            message = "请求数据失败，请重试";
        }
        ToastUtil.show(this, message);
        return true;
    }

    
    public String getNavTitle() {
		if(mTxtTitle != null) {
			return mTxtTitle.getText().toString();
		}
		return null;
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
		String title = getNavTitle();
		if(title != null){
			MobclickAgent.onPageStart(title);
		}
		MobclickAgent.onResume(this);
		JPushInterface.onResume(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
		String title = getNavTitle();
		if(title != null){
			MobclickAgent.onPageEnd(title);
		}
		MobclickAgent.onPause(this);
		JPushInterface.onPause(this);

    }

    public String getVersion() {
    	     try {
    	         PackageManager manager = this.getPackageManager();
    	         PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
    	         String version = info.versionName;
    	        return  version;
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        return "找不到当前版本名";
    	    }
    	}
    
    public UmengUpdateListener getUpdateListener() {
    	return getUpdateListener(false);
    }
    
	public UmengUpdateListener getUpdateListener(final boolean toastIfHasNoUpdate) {
		final WeakReference<BaseActivity> weakReference = new WeakReference<BaseActivity>(this);
		return new UmengUpdateListener() {
			
			@Override
			public void onUpdateReturned(int updateStatus, final UpdateResponse updateInfo) {
				final BaseActivity baseActivity = weakReference.get();
				if(baseActivity == null){
					return ;
				}
				switch (updateStatus) {
				case UpdateStatus.Yes:
					String title = "发现更新,马上下载安装?";
		            String message = updateInfo.updateLog;
		            String yesText = "确定";
		            String noText = "取消";
		            
		            if(mUmengUpdateDialog != null) {
		            	mUmengUpdateDialog.dismiss();
		            	mUmengUpdateDialog = null;
		            }
		            
		    		AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
		    		builder.setTitle(title);
		    		builder.setMessage(message);
		    		builder.setPositiveButton(yesText, new DialogInterface.OnClickListener() {
		    			
		    			@Override
		    			public void onClick(DialogInterface dialog, int which) {
		    				dialog.dismiss();
		    				UmengUpdateAgent.startDownload(baseActivity, updateInfo);
		    			}
		    		});
		    		builder.setNegativeButton(noText, null);
		    		
		    		mUmengUpdateDialog = builder.create();
		    		mUmengUpdateDialog.show();
					break;
				case UpdateStatus.NoneWifi:
//					ToastUtil.show(BaseActivity.this, "没有wifi连接， 只在wifi下更新");
					break;
				case UpdateStatus.No:
					if(toastIfHasNoUpdate) {
						ToastUtil.show(baseActivity, "当前已是最新版本");
					}
					break;
				case UpdateStatus.Timeout:
					ToastUtil.show(baseActivity, "请求超时");
					break;
				default:
					break;
				}
			}
		};
	}
	
	private void showLoginView(){
		Intent intent = new Intent();
		intent.setClass(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		BaseActivity.this.startActivity(intent);
	}
	
	protected boolean isDestroy() {
		return isDestroy;
	}
	
	@Override
	protected void onDestroy() {
		isDestroy = true;
		if(mUmengUpdateDialog != null) {
			mUmengUpdateDialog.dismiss();
			mUmengUpdateDialog = null;
		}
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
		super.onDestroy();
	}

	class  BroadcastReceiverHelper extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(Constant.IGNORE_KICK_USER) {
				return ;
			}
			
			String actionString = intent.getAction();
			if (actionString != null) {
				if (actionString.equals(Constant.RECEIVER_NO_LOGIN)) {
//					if (!(BaseActivity.this instanceof SplashActivity)) {
						BaseActivity.this.finish();
						Intent showLoginIntent = new Intent(Constant.RECEIVER_SHOW_LOGIN);
		                LocalBroadcastManager.getInstance(context).sendBroadcast(showLoginIntent);
//					}
				}else if (actionString.equals(Constant.RECEIVER_SHOW_LOGIN)) {
					showLoginView();
				}
			}
		}
		
	}
	
}
