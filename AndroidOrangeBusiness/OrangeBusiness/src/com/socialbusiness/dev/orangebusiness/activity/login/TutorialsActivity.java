package com.socialbusiness.dev.orangebusiness.activity.login;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;

public class TutorialsActivity extends BaseActivity {
	
	private WebView mWebView;
//	private ProgressDialog mLoadingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorials);
		setUp();
		findView();
	}
	
	private void setUp() {
		setTitle(R.string.use_help);
	}
	
	private void findView() {
		mWebView = (WebView) findViewById(R.id.activity_tutorials_webview);
		
		mWebView.setWebViewClient(new MyWebViewClient());
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		mWebView.setBackgroundColor(0);
		Drawable background = mWebView.getBackground();
		if (background != null) {
			background.setAlpha(0);
		}
		
		mWebView.loadUrl(APIManager.toAbsoluteUrl("/First/Index"));
	}
	
	class MyWebViewClient extends WebViewClient {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return super.shouldOverrideUrlLoading(view, url);
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
		}
	}
}
