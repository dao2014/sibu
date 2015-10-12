package com.socialbusiness.dev.orangebusiness.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
    public static final String TAG = "WXEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }

    @Override
    public void onReq(BaseReq baseReq) {
        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.weixinAPI.handleIntent(getIntent(), this);

        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                WXMediaMessage wxMsg = ((ShowMessageFromWX.Req)baseReq).message;
                WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

                StringBuilder msg = new StringBuilder(); // 组织一个待显示的消息内容
                msg.append("description: ");
                msg.append(wxMsg.description);
                msg.append("\n");
                msg.append("extInfo: ");
                msg.append(obj.extInfo);
                msg.append("\n");
                msg.append("filePath: ");
                msg.append(obj.filePath);

                Log.d(TAG, "msg=" + msg.toString());
                ToastUtil.show(this, msg.toString());

                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp baseResp) {

        int resultResId;
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                resultResId = R.string.errcode_success;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                resultResId = R.string.errcode_cancel;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                resultResId = R.string.errcode_deny;
                break;
            default:
                resultResId = R.string.errcode_unknown;
                break;
        }

        ToastUtil.show(this, getString(resultResId));
    }
}