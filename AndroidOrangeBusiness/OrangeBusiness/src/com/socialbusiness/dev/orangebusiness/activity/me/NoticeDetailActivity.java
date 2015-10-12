package com.socialbusiness.dev.orangebusiness.activity.me;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Course;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.socialbusiness.dev.orangebusiness.util.WeChatUtil;

public class NoticeDetailActivity extends BaseActivity {

	private WebView mWebView;
	private ImageView mGoBack;
	private ImageView mGoForward;
	private ImageView mReload;
	private RelativeLayout mBottomLayout;

	private String id;
	private String companyId;
	private String site;
	private String title;
	private String pushId;
	private String splash = "";
	private String video;
	private boolean isFromJPush;
    private Course mCourse;
    private User loginUser;
    private Bitmap shareImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice_detail);
		setUp();
		findView();
		registerListener();
	}

	private void setUp() {
        loginUser = SettingsManager.getLoginUser();
        if (loginUser == null){
            finish();
            return;
        }
		Intent intent =getIntent();
		id = intent.getStringExtra("id");
		companyId = intent.getStringExtra("companyId");
		video = intent.getStringExtra("video");
		site = intent.getStringExtra("site");
		title =intent.getStringExtra("title");
        mCourse = (Course) intent.getSerializableExtra("course");
		splash = intent.getStringExtra("splash");
		pushId = intent.getStringExtra("pushId");
		isFromJPush = intent.getBooleanExtra(Constant.EXTRA_KEY_JPUSH, false);
	}

	private void findView() {
		mWebView = (WebView) findViewById(R.id.activity_notice_detail_webview);
		mGoBack = (ImageView) findViewById(R.id.activity_notice_detail_webview_backward);
		mGoForward = (ImageView) findViewById(R.id.activity_notice_detail_webview_forward);
		mReload = (ImageView) findViewById(R.id.activity_notice_detail_webview_reload);
		mBottomLayout = (RelativeLayout) findViewById(R.id.activity_notice_detail_webview_bottomlayout);
		
		mWebView.setWebViewClient(new MyWebViewClient());
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		mWebView.setBackgroundColor(0);
		Drawable background = mWebView.getBackground();
		if (background != null) {
			background.setAlpha(0);
		}


        if (mCourse != null && mCourse.isValidate()) {
            
            String url = APIManager.HTTP_HOST + "/course/detail?id=" + mCourse.id + "&companyId=" + loginUser.companyId;
            setTitle("课件详情");
            showRightTxt(false);
            mWebView.loadUrl(url);
            mBottomLayout.setVisibility(View.GONE);

            setRightIcon(R.drawable.bg_selector_share, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCourse == null){
                        return;
                    }

                    boolean hasShareImage = shareImage != null && !shareImage.isRecycled();
                    if (TextUtils.isEmpty(mCourse.image) || hasShareImage){
                        shareToWeChat();
                    }else{
                        int width = AndroidUtil.dip2px(NoticeDetailActivity.this, 160);
                        int height = AndroidUtil.dip2px(NoticeDetailActivity.this, 100);
                        String requestUrl = APIManager.toAbsoluteUrl(mCourse.image);

                        showLoading();
                        ImageLoader imageLoader = APIManager.getInstance(NoticeDetailActivity.this).getImageLoader();
                        imageLoader.get(requestUrl, new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                NoticeDetailActivity activity = (NoticeDetailActivity)weakThis.get();
                                activity.hideLoading();

                                Bitmap result = response.getBitmap();
                                if (result != null){
                                    int w = result.getWidth();
                                    float scale = 100.0f/w;
                                    activity.shareImage = AndroidUtil.scale(result, scale);
                                }
                                activity.shareToWeChat();
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                BaseActivity activity = (BaseActivity)weakThis.get();
                                activity.hideLoading();
                            }
                        }, width, height);
                    }
                }
            });
        } else if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(companyId)) {
			setTitle("公告详情");
			showRightTxt(false);
			mWebView.loadUrl(APIManager.HTTP_HOST + "/notice/detail?id=" + id + "&companyId=" + companyId);
			mBottomLayout.setVisibility(View.GONE);
		} else if (!TextUtils.isEmpty(site)){
			mWebView.loadUrl(site);
			setTitle(title);
			if(splash != null){
				if(splash.equals("splash")){
					showRightTxt(false);
				}
			}
			
		} else if (!TextUtils.isEmpty(pushId)) {
			setTitle("公告详情");
			showRightTxt(false);
			mWebView.loadUrl(APIManager.HTTP_HOST + "/notice/detail?id=" + pushId);
			mBottomLayout.setVisibility(View.GONE);
		} else {
			setTitle(title);
			mBottomLayout.setVisibility(View.GONE);
			mWebView.postDelayed(new Runnable() {

                @Override
				public void run() {
					if (site == null || site.equals("") ) {
						NoticeDetailActivity.this.finish();
						ToastUtil.show(NoticeDetailActivity.this, "没有相关数据");
						return;
					}
				}
			}, 500);
		}
	}

    private void shareToWeChat() {
        if (mCourse == null || !mCourse.isValidate()){
            return;
        }

        final MyApplication application = (MyApplication) weakThis.get().getApplication();
        CharSequence [] items = {"微信朋友圈", "微信好友"};
        final String title = mCourse.title;
        final String message = "";

        final String urlString = mWebView.getUrl();

        new AlertDialog.Builder(this).setTitle("分享到").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        WeChatUtil.shareToCircle(title, message, shareImage, urlString, application);
                        break;
                    }
                    case 1: {
                        WeChatUtil.shareToFriend(title, message, shareImage, urlString, application);
                        break;
                    }
                    default:
                        break;
                }
            }
        }).show();
    }

	private void registerListener() {
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.activity_notice_detail_webview_backward:
					mWebView.goBack();
					break;

				case R.id.activity_notice_detail_webview_forward:
					mWebView.goForward();
					break;

				case R.id.activity_notice_detail_webview_reload:
					mWebView.reload();
					break;

				default:
					break;
				}
			}
		};

		mGoBack.setOnClickListener(listener);
		mGoForward.setOnClickListener(listener);
		mReload.setOnClickListener(listener);
		
		setOnClickBackBtnListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
//		if(isFromJPush) {
//			AndroidUtil.mockClickFromLauncher(NoticeDetailActivity.this, SplashActivity.class);
//		}
		finish();
	}

  private class MyWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
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
