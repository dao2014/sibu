package com.socialbusiness.dev.orangebusiness.model;

import android.content.Context;
import android.content.Intent;

import com.google.gson.annotations.SerializedName;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.NoticeDetailActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.UserInfoActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.OrderDetailActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.ReceiptedActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesDeliveredOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesInputOrderDetailActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaiting2PayOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingConfirmOrderActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.SalesWaitingDeliverOrderActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.fragment.order.DeliverDoneFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.ReceiptedFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.Waiting2PayFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingConfirmFragment;
import com.socialbusiness.dev.orangebusiness.fragment.order.WaitingDeliverFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.util.Log;

/**
 * 推送信息
 * Created by liangyaotian on 2014-09-16.
 */
public class Push extends BaseModel {
    @SerializedName("title")
    public String title;
    @SerializedName("summary")
    public String summary;

    @SerializedName("MessageType")
    public String messageType;

    @SerializedName("IdentityGUID")
    public String identityGUID;


    public static final int TAB_ORDER = 3000;


    public static final int TAB_ME = 5000;
    public static final int PUBLIC_MESSAGE = 1;
    public static final int CLASS_INFO = 2;

    /**
     * 获取Intent
     */
    public Intent getStartupIntent(Context context) {
        int messageType = 0;
        try {
            messageType = Integer.parseInt(this.messageType);
            Log.e("===", "messageType==" + messageType+"=="+this.identityGUID);
        } catch (Exception e) {
            return null;
        }

        User loginUser = SettingsManager.getLoginUser();

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(Constant.EXTRA_KEY_JPUSH, true);

//        if (loginUser == null ) {
//            //当前应用已经启动但未登录，停留在登录界面
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.setClass(context, LoginActivity.class);
//            return intent;
//        }

        switch (messageType) {
            case PUBLIC_MESSAGE://公告
                intent.setClass(context, NoticeDetailActivity.class);
                intent.putExtra("companyId", loginUser.companyId);
                intent.putExtra("id", this.identityGUID);
                break;
            case CLASS_INFO://课件
                intent.setClass(context, NoticeDetailActivity.class);
                String courseUrl = APIManager.HTTP_HOST + "/course/detail?id=" + identityGUID + "&companyId=" + loginUser.companyId;
                intent.putExtra("url", courseUrl);
                break;
            case TAB_ORDER://程单
                intent.setClass(context, MainActivity.class);
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, MainActivity.TabPosition.Order.name());
                break;
            case TAB_ME://我
                intent.setClass(context, MainActivity.class);
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, MainActivity.TabPosition.Me.name());
                break;
            //我的采购
            case 3011://待确认
                intent.setClass(context, OrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", WaitingConfirmFragment.TAG);

                break;
            case 3012://待付款
                intent.setClass(context, OrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", Waiting2PayFragment.TAG);
                break;
            case 3013:// 待发货
                intent.setClass(context, OrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", WaitingDeliverFragment.TAG);
                break;
            case 3014:// 已发货
                intent.setClass(context, OrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", DeliverDoneFragment.TAG);
                break;
            case 3015://已收货
                intent.setClass(context, OrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", ReceiptedFragment.TAG);
                break;

            //我的销售

            case 3021:
                intent.setClass(context, SalesInputOrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", SalesWaitingConfirmOrderActivity.TAG);
                break;
            case 3022:
                intent.setClass(context, OrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", SalesWaiting2PayOrderActivity.TAG);
                break;
            case 3023:
                intent.setClass(context, SalesInputOrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", SalesWaitingDeliverOrderActivity.TAG);
                break;
            case 3024:
                intent.setClass(context, OrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", SalesDeliveredOrderActivity.TAG);
                break;
            case 3025:
                intent.setClass(context, OrderDetailActivity.class);
                intent.putExtra("id", this.identityGUID);
                intent.putExtra("tag", ReceiptedActivity.TAG);
                break;

            //我的客户
            case 5020:
                intent.setClass(context, UserInfoActivity.class);
                intent.putExtra(Constant.EXTRA_KEY_USER_ID, this.identityGUID);
            break;

        }

        return intent;
    }
}
