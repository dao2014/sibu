package com.socialbusiness.dev.orangebusiness.activity.login;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.receiver.RongCloudProvidersListener;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.MD5;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.socialbusiness.dev.orangebusiness.util.Validator;

import java.util.Hashtable;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class LoginActivity extends BaseActivity {

    private EditText mPhone;
    private EditText mPassword;
    private TextView mRemember;
    private TextView mLogin;
    private TextView mRegister;
    private TextView mFindPassword;
    private TextView mSeeTutorials;

    private boolean isSelect = false;
    private long lastPressedBackKeyTime;
    private Toast exitToast;
    private User mLoginUser;
    private String mAccountStr;
    private String mPasswordStr;
    private String registerPhone;
    private String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUp();
        findView();
        registerListener();
        autoFillForm();
    }

    private void setUp() {
        setTitle(R.string.login);
        showBackBtn(false);
        from = getIntent().getStringExtra(Constant.EXTRA_KEY_FROM);
        registerPhone = getIntent().getStringExtra("registerPhone");
    }

    private void findView() {
        mPhone = (EditText) findViewById(R.id.activity_login_account);
        mPassword = (EditText) findViewById(R.id.activity_login_password);
        mRemember = (TextView) findViewById(R.id.activity_login_remember);
        mLogin = (TextView) findViewById(R.id.activity_login_login);
        mRegister = (TextView) findViewById(R.id.activity_login_register);
        mFindPassword = (TextView) findViewById(R.id.activity_login_find_password);
        mSeeTutorials = (TextView) findViewById(R.id.activity_login_see_tutorials);

        mPhone.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        setTextsColor();

        TextView mVersionName = (TextView) findViewById(R.id.activity_settings_version_name);
        mVersionName.setText("v" + getVersion());
    }

    private void setTextsColor() {
        ColorStateList csl = (ColorStateList) getResources()
                .getColorStateList(R.color.sl_register_txt_color);
        if (csl != null) {
            mRegister.setTextColor(csl);
            mFindPassword.setTextColor(csl);
        }
    }

    private void registerListener() {
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.activity_login_remember:
                        isSelect = !isSelect;
                        doRemember(isSelect);
                        break;

                    case R.id.activity_login_login:
                        doLogin();
                        break;

                    case R.id.activity_login_register:
                        doRegister();
                        break;

                    case R.id.activity_login_find_password:
                        doFindPassword();
                        break;

                    case R.id.activity_login_see_tutorials:
                        doSeeTutorials();
                        break;

                    case R.id.activity_login_account:
                        mPassword.setText("");
                        break;

                    default:
                        break;
                }
            }
        };

        mRemember.setOnClickListener(listener);
        mLogin.setOnClickListener(listener);
        mRegister.setOnClickListener(listener);
        mFindPassword.setOnClickListener(listener);
        mSeeTutorials.setOnClickListener(listener);
        mPhone.setOnClickListener(listener);
    }

    private void autoFillForm() {
        if (!TextUtils.isEmpty(registerPhone)) {
            mPhone.setText(registerPhone);
            mPassword.setText("");
            return;
        }
        String phone = SettingsManager.getPhone();
        if (!TextUtils.isEmpty(phone)) {
            mPhone.setText(phone);
            isSelect = SettingsManager.isRememberPwd();
            if (isSelect) {
                String pwd = SettingsManager.getRememberPwd();
                mPassword.setText(pwd);
            }
            doRemember(isSelect);
        }
    }

    /**
     * 记住密码
     */
    private void doRemember(boolean select) {
        if (select) {
            mRemember.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_change_true, 0, 0, 0);
        } else {
            mRemember.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_change_false, 0, 0, 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (exitToast == null) {
                exitToast = Toast.makeText(LoginActivity.this, R.string.info_back_key_exit, Toast.LENGTH_LONG);
            }

            long delay = (exitToast.getDuration() == Toast.LENGTH_LONG ? 3500 : 2000);
            if (System.currentTimeMillis() - lastPressedBackKeyTime < delay) {
                exitToast.cancel();
                LoginActivity.this.finish();
            } else {
                exitToast.show();
            }

            lastPressedBackKeyTime = System.currentTimeMillis();
        }
        return false;
    }

    /**
     * 登录
     */
    private void doLogin() {
        mAccountStr = mPhone.getText().toString().trim();
        mPasswordStr = mPassword.getText().toString().trim();

        if ("".equals(mAccountStr)) {
            AndroidUtil.setError(mPhone, "请输入手机号码");
            return;
        }
        if ("".equals(mPasswordStr)) {
            AndroidUtil.setError(mPassword, "请输入密码");
            return;
        }
        if (!Validator.isPhone(mAccountStr)) {
            AndroidUtil.setError(mPhone, "请输入正确的手机号码");
            return;
        }

        showProgressDialog("正在登录").setCancelable(false);
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("account", mAccountStr);
        parameters.put("password", MD5.encode(mPasswordStr));
        APIManager.getInstance(this).userLogin(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<User>>() {

                    @Override
                    public void onResponse(RequestResult<User> response) {
                        if (hasError(response)) {
                            hideProgressDialog();
                            return;
                        }
                        if (response.code == 0 && response.data != null) {
                            mLoginUser = response.data;
                            SettingsManager.saveLoginSession();


                            // 登录成功调转到主页面
//                            SettingsManager.saveLoginUser(mLoginUser);
//                            SettingsManager.setPhone(mLoginUser.phone);
//                            SettingsManager.setRememberPwd(isSelect, mPasswordStr);
//                            goToMainActivity();

                            //连接到融云
                            conectionRongYun();
                        }
                    }
                });
    }

    private void conectionRongYun() {

//        APIManager.getInstance(this).getRongyunToken(new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                hideProgressDialog();
//                showException(error);
//            }
//        }, new Response.Listener<RequestResult<?>>() {
//            @Override
//            public void onResponse(RequestResult<?> response) {
//                if (response.code==RequestResult.RESULT_SUCESS) {
        Log.e("TOKEN", "==" + mLoginUser.rongcloudToken);
        try {
//            mLoginUser.rongcloudToken="FHGRf2wRDyokcR1o/HiLdM2yq+hfEluLjZ78E1qo4hGoJtlXfHETWOBzWjJmmabX2UDBRYDylpE0F1hw19xVFA==";
            RongIM.connect(mLoginUser.rongcloudToken, new RongIMClient.ConnectCallback() {

                @Override
                public void onSuccess(String s) {
                    // 此处处理连接成功。
                    Log.e("Connect:", "Login successfully." + s);
//                    hideProgressDialog();
                    SettingsManager.saveLoginUser(mLoginUser);
                    SettingsManager.setPhone(mLoginUser.phone);
                    SettingsManager.setRememberPwd(isSelect, mPasswordStr);
                    goToMainActivity();
                    new Thread() {
                        @Override
                        public void run() {
                            RongCloudProvidersListener.getInstance().setOtherListener();
//                            getFriends();

                        }
                    }.start();


                }

                @Override
                public void onError(ErrorCode errorCode) {
                    hideProgressDialog();
//                    ToastUtil.show(getMyApplication(), "融云服务器连接失败");
//                                showException(error);
                    // 此处处理连接错误。
                    Log.e("Connect:", "Login failed.==" + errorCode);
                    SettingsManager.saveLoginUser(mLoginUser);
                    SettingsManager.setPhone(mLoginUser.phone);
                    SettingsManager.setRememberPwd(isSelect, mPasswordStr);
                    goToMainActivity();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//                }
    }



    private void goToMainActivity() {
        hideProgressDialog();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 注册
     */
    private void doRegister() {
//        Intent intent = new Intent(LoginActivity.this, RegisterStep1Activity.class);
        Intent intent = new Intent(LoginActivity.this, RegisterStep1TabActivity.class);
        startActivity(intent);
    }

    /**
     * 找回密码
     */
    private void doFindPassword() {
        Intent intent = new Intent(LoginActivity.this, FindPasswordStep1Activity.class);
        startActivity(intent);
    }

    /**
     * 查看教程
     */
    private void doSeeTutorials() {
        Intent intent = new Intent(LoginActivity.this, TutorialsActivity.class);
        startActivity(intent);
    }

    /**
     * 登录环信聊天服务器
     */
//    private void loginEasemod() {
//        final String emUserName = MD5.encode(mLoginUser.id);
//        final String emPassword = MD5.encode(mPasswordStr);
//        final User loginUser = mLoginUser;
//        final String passwordStr = mPasswordStr;
//
//        EMChatManager.getInstance().login(emUserName, emPassword, new EMCallBack() {
//
//            @Override
//            public void onSuccess() {
//            	EMChat.getInstance().setAutoLogin(true);
////                EMGroupManager.getInstance().loadAllGroups();
//            	try {
//					EMGroupManager.getInstance().getGroupsFromServer();
//				} catch (EaseMobException e) {
//					e.printStackTrace();
//				}
//
//            	EMChatManager.getInstance().loadAllConversations();
//
//                EMGroupManager.getInstance().loadAllGroups(new EMCallBack() {
//
//					@Override
//					public void onSuccess() {
//						Intent intent = new Intent();
//						intent.putExtra(EMConstant.EXTRA_LOAD_CONVERSATION, true);
//						intent.setAction(EasemodManager.BROADCAST_NOTIFY_CONVERSATION_CHANGED);
//						sendBroadcast(intent);
//					}
//
//					@Override
//					public void onProgress(int progress, String status) {
//
//					}
//
//					@Override
//					public void onError(int code, final String message) {
//
//					}
//				});
//
//                if (!TextUtils.isEmpty(loginUser.nickName)) {
//                    EMChatManager.getInstance().updateCurrentUserNick(loginUser.nickName);
//                }
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        hideProgressDialog();
//                        EasemodManager.getInstance().setLoginUserId(loginUser.id);
//                        EasemodManager.getInstance().setLoginUserName(emUserName);
//                        EasemodManager.getInstance().setLoginUserAvatar(APIManager.toAbsoluteUrl(loginUser.head));
//                        EasemodManager.getInstance().setLoginUserNickName(loginUser.nickName);
//                        EasemodManager.getInstance().notifyInitFinish();
//
//                        EasemodManager.getInstance().saveUserEntity(loginUser.id,
//                                emUserName, loginUser.nickName, loginUser.head);
//                        SettingsManager.saveEasemodPwd(emPassword);
//                        SettingsManager.saveLoginUser(loginUser);
//                        SettingsManager.setPhone(loginUser.phone);
//                        SettingsManager.setRememberPwd(isSelect, passwordStr);
//                        goToMainActivity();
//                        finish();
//                    }
//                });
//            }
//
//            @Override
//            public void onProgress(int progress, String status) {
//
//            }
//
//            @Override
//            public void onError(final int code, final String message) {
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        hideProgressDialog();
//                        ToastUtil.show(getApplicationContext(), message);
//                    }
//                });
//            }
//        });
//
//    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        registerPhone = intent.getStringExtra("registerPhone");
        autoFillForm();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
