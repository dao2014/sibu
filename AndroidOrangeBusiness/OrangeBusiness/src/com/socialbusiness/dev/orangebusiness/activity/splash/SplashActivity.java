package com.socialbusiness.dev.orangebusiness.activity.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.login.LoginActivity;
import com.socialbusiness.dev.orangebusiness.api.RequestAction;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.receiver.RongCloudProvidersListener;
import com.socialbusiness.dev.orangebusiness.util.FileUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class SplashActivity extends BaseActivity {
//    private ImageView mLogo;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = null;
            if (!SettingsManager.isUserLogin()) {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
                Log.e("===", "LoginActivity");
            } else {
                intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                UserGroupUtil.getFriends(SplashActivity.this);
            }
            startActivity(intent);
            finish();
        }
    };

    public void reLogin() {
        FileUtil.deleteSerializableFileForDefaultPath("User.cache");
        RequestAction.setSessionId(null);
        SettingsManager.saveLoginSession();
        handler.sendEmptyMessageDelayed(1, 1000);
        Log.e("===", "relogin");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        isHeaderTitleBarVisible(false);

    }


    @Override
    protected void onResume() {
        super.onResume();

        checkConection();
    }

    private void checkConection() {

        if (RongIM.getInstance() == null) {
            getMyApplication().intRongCloud();
            User user = SettingsManager.getLoginUser();
            Log.e("getInstance", "==" + RongIM.getInstance() + "==" + user);
            if (user != null && user.rongcloudToken != null) {
                try {
                    RongIM.connect(user.rongcloudToken, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onSuccess(String s) {
                            RongCloudProvidersListener.getInstance().setOtherListener();
                            handler.sendEmptyMessageDelayed(1, 1000);
                        }

                        @Override
                        public void onError(ErrorCode errorCode) {
                            reLogin();
                        }
                    });

                } catch (Exception e) {
                    reLogin();
                    e.printStackTrace();
                }

            } else {
                reLogin();
            }
        } else {

            if (RongIM.getInstance().getCurrentConnectionStatus() == RongIM.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                reLogin();
            } else {
                RongCloudProvidersListener.getInstance().setOtherListener();
                handler.sendEmptyMessageDelayed(1, 1000);

            }
        }


    }


}
