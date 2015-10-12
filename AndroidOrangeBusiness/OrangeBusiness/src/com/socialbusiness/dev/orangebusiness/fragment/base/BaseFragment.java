package com.socialbusiness.dev.orangebusiness.fragment.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.PurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

public abstract class BaseFragment extends Fragment {

    private View mRootView;
    private FrameLayout mLayerContextView;
    private View mRelativeLayout;
    private View mNoDataLayout;
    private ImageView mNoDataImage;
    private TextView mNoDataTxt;

    private View.OnClickListener mOnClickReloadListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_base, container, false);
        findViews();
        setContentView(LayoutInflater.from(getActivity()), mLayerContextView);
        super.onCreateView(inflater, container, savedInstanceState);
        return mRootView;
    }

    private void findViews() {
        mLayerContextView = (FrameLayout) mRootView.findViewById(R.id.fragment_base_mLayerContextView);
        mRelativeLayout = mRootView.findViewById(R.id.fragment_base_loading_layout);
        mNoDataLayout = mRootView.findViewById(R.id.fragment_base_nodata_layout);
        mNoDataImage = (ImageView) mRootView.findViewById(R.id.fragment_base_nodata_image);
        mNoDataTxt = (TextView) mRootView.findViewById(R.id.fragment_base_nodata_text);

        mNoDataLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnClickReloadListener != null) {
                    if (mRelativeLayout != null && mRelativeLayout.getVisibility() != View.VISIBLE) {
                        hideTips();
                        mOnClickReloadListener.onClick(v);
                    }
                }
            }
        });
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    protected MyApplication getMyApplication() {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        return (MyApplication) activity.getApplication();
    }

    public void showFragment() {
        if (getMainActivity() != null) {
            getMainActivity().showBackBtn(false);
            getMainActivity().showRightBtn(false);
        }
    }

    public void showFragmentPurchaseOrderActivity() {
        PurchaseOrderActivity activity = (PurchaseOrderActivity) getActivity();
        if (activity != null) {
            activity.showBackBtn(false);
            activity.showRightBtn(false);
        }
    }

    public void showPurchaseOrderActivityTotalData(String title, String value, String tag) {
//        PurchaseOrderActivity activity = (PurchaseOrderActivity) getActivity();
//        if (activity != null && activity.isCurentShow(tag)) {
//            activity.setTotalData(title, value);
//        }
        if (purchaseOrderActivity != null && purchaseOrderActivity.isCurentShow(tag))
            showPurchaseOrderActivityTotalData(title, value);
    }

    public void showPurchaseOrderActivityTotalData(String title, String value) {
//        PurchaseOrderActivity activity = (PurchaseOrderActivity) getActivity();
//        Log.e("==", "==" + activity);
//        if (activity != null) {
//            activity.setTotalData(title, value);
//        }
        purchaseOrderActivity.setTotalData(title, value);
    }

    protected String getTitle() {
        return "";
    }


    protected String getValue() {
        return "";
    }


    public void showOrderTatol(String tag) {
        showPurchaseOrderActivityTotalData(getTitle(), getValue(), tag);
    }


    public void showOrderTatol() {
        showPurchaseOrderActivityTotalData(getTitle(), getValue());
    }

    PurchaseOrderActivity purchaseOrderActivity;

    public void setPurchaseOrderActivity(PurchaseOrderActivity purchaseOrderActivity) {
        this.purchaseOrderActivity = purchaseOrderActivity;
    }


    public void hideFragment() {
        if (getMainActivity() != null) {
            getMainActivity().showBackBtn(false);
            getMainActivity().showRightBtn(false);
        }
    }

    public void showNetworkDisconnet() {
        showTips(0, "当前网络连接出错");
    }

    public void showNoDataTips() {
        showTips(0, "当前没有数据");
    }

    public void showException(Exception e) {
        this.showException(e, null);
    }

    public void showException(Exception e, String message) {
        if (e instanceof NetworkError) {
            ToastUtil.show(getActivity(), "网络连接异常");
            return;
        }
        if (!TextUtils.isEmpty(message)) {
            ToastUtil.show(getActivity(), message);
            return;
        }
        if (e == null) {
            return;
        }
        if (e instanceof VolleyError) {
            if (TextUtils.isEmpty(e.getMessage())) {
                ToastUtil.show(getActivity(), "请求数据失败，请重试");
                return;
            }
        }
        ToastUtil.show(getActivity(), e.getMessage());
    }

    public boolean hasError(RequestResult<?> requestResult) {
        if (requestResult != null && requestResult.code == 0) {
            return false;
        }
        String message = requestResult != null ? requestResult.message : null;
        if (TextUtils.isEmpty(message)) {
            message = "请求数据失败，请重试";
        }
        ToastUtil.show(getActivity(), message);
        return true;
    }

    public boolean hasError(RequestListResult<?> requestListResult) {
        if (requestListResult != null && requestListResult.code == 0) {
            return false;
        }
        String message = requestListResult != null ? requestListResult.message : null;
        if (TextUtils.isEmpty(message)) {
            message = "请求数据失败，请重试";
        }
        ToastUtil.show(getActivity(), message);
        return true;
    }

    protected void showMessageByDialog(String message) {
        Dialog showDialog = new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.alertDialog_positive)
                        , new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        showDialog.show();
    }

    public void showTips(int image, String message) {
        if (mNoDataLayout != null) {
            mNoDataImage.setImageResource(image);
            mNoDataTxt.setText(message);
            mNoDataLayout.setVisibility(View.VISIBLE);
        }

    }

    public void hideTips() {
        if (mNoDataLayout != null) {
            mNoDataLayout.setVisibility(View.GONE);
        }
    }

    public void showLoading() {
        mRelativeLayout.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        mRelativeLayout.setVisibility(View.GONE);
    }

    protected void setOnClickReloadListener(View.OnClickListener l) {
        mOnClickReloadListener = l;
    }

    public abstract void setContentView(LayoutInflater inflater,
                                        ViewGroup mLayerContextView);

}
