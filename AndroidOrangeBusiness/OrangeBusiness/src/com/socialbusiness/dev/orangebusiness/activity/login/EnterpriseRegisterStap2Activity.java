package com.socialbusiness.dev.orangebusiness.activity.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.MD5;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;

/**
 * Created by zkboos on 2015/4/8.
 */
public class EnterpriseRegisterStap2Activity extends BaseActivity{

    private EditText mNickname;
    private EditText mPassword;
    private EditText mWeixin;
    private TextView mRegister;
    private View mReferrerLayout;
    private EditText mReferrerNameTV;
    private EditText registerAddress;

    private ProgressDialog mLoadingDialog;
    private boolean isLoading;

    private String phone;
    private String smsVerify;
    private String recommendPhone;
    private String recommendName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step2_enterprise);
        setUp();
        findView();
        registerListener();
    }

    private void setUp() {
        setTitle(R.string.register);
        phone = getIntent().getStringExtra("phone");
        smsVerify = getIntent().getStringExtra("smsVerify");
        recommendPhone = getIntent().getStringExtra("recommendPhone");
        recommendName = getIntent().getStringExtra("recommendName");
    }

    private void findView() {
        mNickname = (EditText) findViewById(R.id.activity_register_step2_input_nickname);
        mPassword = (EditText) findViewById(R.id.activity_register_step2_input_password);
        mWeixin = (EditText) findViewById(R.id.activity_register_step2_input_weixin);
        mRegister = (TextView) findViewById(R.id.activity_register_step2_register);
        mReferrerLayout = findViewById(R.id.activity_register_step2_referrerLayout);
        mReferrerNameTV = (EditText) findViewById(R.id.activity_register_step2_referrer_nickname);
        registerAddress =(EditText)findViewById(R.id.activity_register_step2_address);

        mLoadingDialog = new ProgressDialog(this);

//        if(!TextUtils.isEmpty(recommendName)) {
//            mReferrerNameTV.setText(recommendName);
//            mReferrerLayout.setVisibility(View.VISIBLE);
//        }
//        else {
//            mReferrerLayout.setVisibility(View.GONE);
//        }
    }

    private void registerListener() {
        mRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                doRegister();
            }
        });
    }

    private void doRegister() {
        if (isLoading) {
            return;
        }
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(smsVerify)) {
            ToastUtil.show(EnterpriseRegisterStap2Activity.this, "必要信息不完整，请返回上一步重新填写");
            return;
        }

        final String tel = mNickname.getText().toString().trim();
        final String password = mPassword.getText().toString();
        final String weixin = mWeixin.getText().toString().trim();
        final String nickname = mReferrerNameTV.getText().toString().trim();
        final String add = registerAddress.getText().toString().trim();
        if (TextUtils.isEmpty(nickname)) {
            AndroidUtil.setError(mReferrerNameTV, "名称不能为空");
            return;
        }
        if (TextUtils.isEmpty(tel)) {
            AndroidUtil.setError(mNickname, "号码不能为空");
            return;
        }

//        if(!Validator.isTel(tel)){
//            AndroidUtil.setError(mNickname, "号码不正确");
//            return;
//        }

        if (TextUtils.isEmpty(weixin)) {
            AndroidUtil.setError(mWeixin, "营业执照不能为空");
            return;
        }
        if (TextUtils.isEmpty(add)) {
            AndroidUtil.setError(registerAddress, "企业地址不能为空");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            AndroidUtil.setError(mPassword, "密码不能为空");
            return;
        }

        final Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("name", nickname);
        parameters.put("password", MD5.encode(password));
        parameters.put("phone", phone);
        parameters.put("license", weixin);
        parameters.put("address", add);
        parameters.put("tel", tel);
        parameters.put("smsVerify", smsVerify);
//        if (!TextUtils.isEmpty(recommendPhone)) {
//            parameters.put("recommendPhone", recommendPhone);
//        }
//        if (!TextUtils.isEmpty(weixin)) {
//            parameters.put("wechat", weixin);
//        }

        submitForm(parameters);
    }

    private void submitForm(Hashtable<String, String> parameters) {
        showLoading();
        APIManager.getInstance(this).enterpriseUserRegister(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<User>>() {

                    @Override
                    public void onResponse(RequestResult<User> response) {
                        hideLoading();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            SettingsManager.saveLoginSession();
                            SettingsManager.saveLoginUser(response.data);
                            ToastUtil.show(EnterpriseRegisterStap2Activity.this, response.message);
                            goToLoginActivity();
                            finish();
                        }
                    }
                });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(EnterpriseRegisterStap2Activity.this, LoginActivity.class);
        intent.putExtra(Constant.EXTRA_KEY_FROM, "register");
        intent.putExtra("registerPhone", phone);
        startActivity(intent);
    }

    @Override
    public void showLoading() {
        isLoading = true;
        if (mLoadingDialog != null) {
            mLoadingDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        isLoading = false;
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }
}
