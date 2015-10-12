package com.socialbusiness.dev.orangebusiness.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.PurchaseOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesDeliveredOrderActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.CircleImageView;
import com.socialbusiness.dev.orangebusiness.fragment.order.DeliverDoneFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingDeliverFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingConfirmFragment;
import com.socialbusiness.dev.orangebusiness.model.User;

import java.util.Hashtable;

/**
 * Created by enwey on 2014/11/3.
 */
public class VipDetailActivity extends BaseActivity {
    private LinearLayout mUserDetail;
    private CircleImageView mUserInfo;
    private TextView mUserName;
    private TextView mAccumulation;
    private TextView mLevelName;
    private TextView mTotalSum;
    private TextView mTotalNum;
    private TextView mWaitingHandleOrderNums;
    private TextView mMonthShipmentsSum;
    private TextView mTotalShipmentsSum;
    private RelativeLayout mAlreadyOut;
    private RelativeLayout mWaitHandle;
    private RelativeLayout mWaitSend;
    private RelativeLayout mAlreadySend;
    private RelativeLayout mUserLevel;
    private String mUserId;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_detail);
        setUp();
        findView();
        registerListener();
        callAPIUserRefresh();
    }

    private void setUp() {
        setTitle(R.string.vip_detail_title);
        mUserId = getIntent().getStringExtra("userId");
    }

    private void findView() {
        mUserDetail = (LinearLayout) this.findViewById(R.id.activity_vip_detail_layout_user);
        mUserName = (TextView) this.findViewById(R.id.activity_vip_detail_name);
        mUserInfo = (CircleImageView) this.findViewById(R.id.view_vip_detail_image);
        mAccumulation = (TextView) this.findViewById(R.id.activity_vip_detail_accumulation);
        mLevelName = (TextView) this.findViewById(R.id.activity_vip_detail_level_name);
        mTotalSum = (TextView) this.findViewById(R.id.activity_vip_detail_total_sum);
        mTotalNum = (TextView) this.findViewById(R.id.activity_vip_detail_total_num);
        mWaitingHandleOrderNums = (TextView) findViewById(R.id.activity_vip_detail_waiting_handle_order_nums);
        mMonthShipmentsSum = (TextView) this.findViewById(R.id.activity_vip_detail_month_shipments_sum);
        mTotalShipmentsSum = (TextView) this.findViewById(R.id.activity_vip_detail_total_shipments_sum);
        mAlreadyOut = (RelativeLayout) this.findViewById(R.id.activity_vip_detail_layout_already_out);
        mWaitHandle = (RelativeLayout) this.findViewById(R.id.activity_vip_detail_layout_wait_handle);
        mWaitSend = (RelativeLayout) this.findViewById(R.id.activity_vip_detail_layout_wait_send);
        mAlreadySend = (RelativeLayout) this.findViewById(R.id.activity_vip_detail_layout_already_send);
        mUserLevel = (RelativeLayout) this.findViewById(R.id.activity_vip_detail_layout_layout_level);

        mUserInfo.setDefaultImageResId(R.drawable.image_head_userinfo);
        mUserInfo.setErrorImageResId(R.drawable.image_head_userinfo);
        mWaitingHandleOrderNums.setVisibility(View.GONE);
    }

    private void callAPIUserRefresh() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("userId", mUserId);
        APIManager.getInstance(this).userRefresh(parameters, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                hideNoData();
                showException(error);
            }
        }, new Response.Listener<RequestResult<User>>() {

            @Override
            public void onResponse(RequestResult<User> response) {
                hideLoading();
                hideNoData();
                if (hasError(response)) {
                    return;
                }
                if (response.data != null) {
                    mUser = response.data;
                    setUserData(mUser);
                }
            }
        });
    }

    private void setUserData(User user) {
        mUserInfo.setImageUrl(APIManager.toAbsoluteUrl(user.head),
                APIManager.getInstance(this).getImageLoader());
        mUserName.setText(user.nickName + "");
        mAccumulation.setText(user.credit + "");
        if (user.type != null) {
            mLevelName.setText(user.type.name);
        } else {
            mLevelName.setText("");
        }
        // 订单总金额
        mTotalSum.setText(user.totalOrderMoney + "");
        // 总订单数
        mTotalNum.setText(user.monthlyOrderCount + "");
        mMonthShipmentsSum.setText(user.monthlyOrderMoney + "");
        mTotalShipmentsSum.setText(user.totalOrderMoney + "");

        if (mUser.buyerWaitingOrderCount > 0) {
            mWaitingHandleOrderNums.setText(mUser.buyerWaitingOrderCount + "");
            mWaitingHandleOrderNums.setVisibility(View.VISIBLE);
        }
    }

    private void registerListener() {
        View.OnClickListener register = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                switch (view.getId()) {
                    case R.id.activity_vip_detail_layout_user:
                        if (mUser != null) {
                            intent.setClass(VipDetailActivity.this, UserInfoActivity.class);
                            intent.putExtra(Constant.EXTRA_KEY_USER_ID, mUserId);
                        }
                        break;

                    case R.id.activity_vip_detail_layout_already_out:
                        intent.setClass(VipDetailActivity.this, SalesDeliveredOrderActivity.class);
                        intent.putExtra("userId", mUserId);
                        break;

                    case R.id.activity_vip_detail_layout_wait_handle:
                        intent.setClass(VipDetailActivity.this, PurchaseOrderActivity.class);
                        intent.putExtra("userId", mUserId);
                        intent.putExtra("TAG", WaitingConfirmFragment.TAG);
                        break;

                    case R.id.activity_vip_detail_layout_wait_send:
                        intent.setClass(VipDetailActivity.this, PurchaseOrderActivity.class);
                        intent.putExtra("userId", mUserId);
                        intent.putExtra("TAG", WaitingDeliverFragment.TAG);
                        break;

                    case R.id.activity_vip_detail_layout_already_send:
                        intent.setClass(VipDetailActivity.this, PurchaseOrderActivity.class);
                        intent.putExtra("userId", mUserId);
                        intent.putExtra("TAG", DeliverDoneFragment.TAG);
                        break;

                    case R.id.activity_vip_detail_layout_layout_level:
                        intent.setClass(VipDetailActivity.this, LevelListActivity.class);
                        intent.putExtra("userId", mUserId);
                        break;
                }
                startActivity(intent);
            }
        };
        mUserDetail.setOnClickListener(register);
        mAlreadyOut.setOnClickListener(register);
        mWaitHandle.setOnClickListener(register);
        mWaitSend.setOnClickListener(register);
        mAlreadySend.setOnClickListener(register);
        mUserLevel.setOnClickListener(register);
    }
}
