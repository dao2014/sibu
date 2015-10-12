package com.socialbusiness.dev.orangebusiness.activity.base;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.config.Constance;

public class WebActivity extends BaseActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_web_layout);
		initView();
		initData();
	}

	private void initData() {
		String url = getIntent().getStringExtra(Constance.WEB_DETAIL);
        WebSettings seting=web_view.getSettings();
        seting.setJavaScriptEnabled(true);
        seting.setJavaScriptCanOpenWindowsAutomatically(true);


		web_view.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
                showProgressDialog();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
                hideProgressDialog();
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

			}

		});

        web_view.loadUrl(url);
	}

	WebView web_view;

	private void initView() {

		web_view = (WebView) findViewById(R.id.web_view);

        setOnClickBackBtnListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
	setTitle(getIntent().getStringExtra(Constance.WEB_ACTIVITY_TITLE));
    }

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.view_header_mLyerLeft:
			finish();
			break;

		default:
			break;
		}
	}
}
