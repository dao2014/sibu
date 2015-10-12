package com.socialbusiness.dev.orangebusiness.fragment.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.login.EnterpriseRegisterStap2Activity;
import com.socialbusiness.dev.orangebusiness.activity.login.RegisterStep2Activity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.KWTimer;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.socialbusiness.dev.orangebusiness.util.Validator;

import java.util.Hashtable;

/**
 * Created by zkboos on 2015/4/8.
 */
public class UserRegisterFrament extends BaseFragment {
    private EditText mPhone;
    private EditText mVerifyCode;
    private TextView mGetVerifyCode;
    private EditText mReFerrerPhone;
    private TextView mNext;
    private View enterpriseLayout;

    private ProgressDialog mLoadingDialog;
    private boolean isLoading;
    private KWTimer mKWTimer;

    private String mReFerrerPhoneStr;
    private String mReferrerNicknameStr;

    private InputMethodManager imm;

    private final static int UASER_REGISTER_TYPE = 0;
    private final static int ENTERPRISE_REGISTER_TYPE = 1;

    private int curentType = UASER_REGISTER_TYPE;

    public void hideSuperLevel() {
        curentType = ENTERPRISE_REGISTER_TYPE;

    }

    @Override
    public void setContentView(LayoutInflater inflater, ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.activity_register_step1, mLayerContextView);
        setUp();
        findView(mLayerContextView);
        registerListener();
    }


    private void setUp() {
        imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
    }

    private void findView(View view) {
        enterpriseLayout = view.findViewById(R.id.enterprise_layout);
        mPhone = (EditText) view.findViewById(R.id.activity_register_step1_input_phone);
        mVerifyCode = (EditText) view.findViewById(R.id.activity_register_step1_input_verifycode);
        mGetVerifyCode = (TextView) view.findViewById(R.id.activity_register_step1_get_verifycode);
        mReFerrerPhone = (EditText) view.findViewById(R.id.activity_register_step1_input_referrer_phone);
        mNext = (TextView) view.findViewById(R.id.activity_register_step1_next);

        mPhone.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        mReFerrerPhone.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        mLoadingDialog = new ProgressDialog(getActivity());
        mKWTimer = new KWTimer(60);

        if(curentType==ENTERPRISE_REGISTER_TYPE){
            enterpriseLayout.setVisibility(View.GONE);
        }
    }

    private void registerListener() {
        mKWTimer.setTimerListener(new KWTimer.TimerListener() {

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
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.activity_register_step1_get_verifycode) {
                    if (mGetVerifyCode.isEnabled()) {
                        requestVerifyCode();
                    }
                } else if (v.getId() == R.id.activity_register_step1_next) {
//                    toNextStep();
//                    return;
                    switch (curentType) {
                        case UASER_REGISTER_TYPE:
                            goNext();
                            break;
                        case ENTERPRISE_REGISTER_TYPE:
                            goEnterpriseNext();
                            break;
                    }

                }
            }
        };

        mGetVerifyCode.setOnClickListener(listener);
        mNext.setOnClickListener(listener);
    }

    private void goEnterpriseNext() {
        final String phone = mPhone.getText().toString().trim();
        final String verifyCode = mVerifyCode.getText().toString().trim();

        if (!Validator.isPhone(phone)) {
            AndroidUtil.setError(mPhone, "请输入正确的手机号码");
            return;
        }
        if (TextUtils.isEmpty(verifyCode)) {
            AndroidUtil.setError(mVerifyCode, "请输入手机收到的验证码");
            return;
        }
        showLoading();
        Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("phone", phone);
        params.put("smsVerify", verifyCode);
        APIManager.getInstance(getActivity().getApplicationContext()).checkCompanyPhone(
                params,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);

                    }
                },
                new Response.Listener<RequestResult<String>>() {

                    @Override
                    public void onResponse(RequestResult<String> response) {
                        hideLoading();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.code == 0) {
                            if (!TextUtils.isEmpty(response.data)) {
                                mReferrerNicknameStr = response.data;
                            } else {
                                mReferrerNicknameStr = null;
                            }
                            toNextStep();
                        }
                    }
                });
    }

    private void requestVerifyCode() {
        if (isLoading) {
            return;
        }

        if (!Validator.isPhone(mPhone.getText().toString().trim())) {
            AndroidUtil.setError(mPhone, "请输入正确的手机号码");
            return;
        }

        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("phone", mPhone.getText().toString().trim());
        APIManager.getInstance(getActivity()).userRegisterSms(parameters,
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
                            ToastUtil.show(getActivity(), response.message);
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
        final String referrerPhone = mReFerrerPhone.getText().toString().trim();

        if (!Validator.isPhone(phone)) {
            AndroidUtil.setError(mPhone, "请输入正确的手机号码");
            return;
        }
        if ("".equals(verifyCode)) {
            AndroidUtil.setError(mVerifyCode, "请输入手机收到的验证码");
            return;
        }
        if (TextUtils.isEmpty(referrerPhone)) {
            AndroidUtil.setError(mReFerrerPhone, "请输入推荐人手机号");
            return;
        }
        if (!Validator.isPhone(referrerPhone)) {
            AndroidUtil.setError(mReFerrerPhone, "请输入正确的推荐人手机号码");
            return;
        }

        mReFerrerPhoneStr = referrerPhone;

//        String msg = TextUtils.isEmpty(mReFerrerPhoneStr) ?
//                "推荐人为空，是否继续注册为【公司】？"
//                :
//                String.format("您是否注册为%s的客户（下级）？", referrerPhone);

//        new AlertDialog.Builder(getActivity())
//                .setCancelable(false)
//                .setMessage(msg)
//                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
        requestCheckInput(phone, verifyCode, referrerPhone);
//                    }
//                })
//                .setNegativeButton("取消", null)
//                .create()
//                .show();


//		else {
//			mReFerrerPhoneStr = null;
//			mReferrerNicknameStr = null;
//			toNextStep();
//		}

    }

    private void requestCheckInput(final String phone, final String verifyCode, final String referrerPhone) {
        showLoading();
        Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("phone", phone);
        params.put("smsVerify", verifyCode);
        params.put("recommendPhone", referrerPhone);
        APIManager.getInstance(getActivity().getApplicationContext()).userCheckPhone(
                params,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);

                    }
                },
                new Response.Listener<RequestResult<String>>() {

                    @Override
                    public void onResponse(RequestResult<String> response) {
                        hideLoading();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.code == 0) {
                            if (!TextUtils.isEmpty(response.data)) {
                                mReferrerNicknameStr = response.data;
                            } else {
                                mReferrerNicknameStr = null;
                            }
                            toNextStep();
                        }
                    }
                });
    }

    private void toNextStep() {
        Intent intent = null;

        switch (curentType) {
            case UASER_REGISTER_TYPE:
                intent = new Intent(getActivity(),
                        RegisterStep2Activity.class);
                break;
            case ENTERPRISE_REGISTER_TYPE:
                intent = new Intent(getActivity(),
                        EnterpriseRegisterStap2Activity.class);
                break;
        }
        intent.putExtra("phone", mPhone.getText().toString().trim());
        intent.putExtra("smsVerify", mVerifyCode.getText().toString().trim());
        if (!TextUtils.isEmpty(mReFerrerPhoneStr)) {
            intent.putExtra("recommendPhone", mReFerrerPhoneStr);
        }
        if (!TextUtils.isEmpty(mReferrerNicknameStr)) {
            intent.putExtra("recommendName", mReferrerNicknameStr);
        }
        startActivity(intent);
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN
//                || ev.getAction() == MotionEvent.ACTION_MOVE
//                || ev.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
//            View v = getActivity().getCurrentFocus();
//            if (AndroidUtil.isShouldHideInput(v, ev) && imm != null) {
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//            }
//            return super.dispatchTouchEvent(ev);
//        }
//
//        // 必不可少，否则所有的组件都不会有TouchEvent了
//        if (getWindow().superDispatchTouchEvent(ev)) {
//            return true;
//        }
//        return onTouchEvent(ev);
//    }


    @Override
    public void onDestroy() {
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
