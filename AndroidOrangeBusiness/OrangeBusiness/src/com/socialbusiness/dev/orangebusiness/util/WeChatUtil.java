package com.socialbusiness.dev.orangebusiness.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.R;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;

import java.io.ByteArrayOutputStream;

/**
 * 微信分享工具
 * Created by liangyaotian on 1/12/15.
 */
public class WeChatUtil {

    public static void shareToCircle(String title,
                                     String message,
                                     Bitmap image,
                                     String urlString,
                                     MyApplication application) {
        int scene = SendMessageToWX.Req.WXSceneTimeline;
        share(title, message, image, urlString, application, scene);
    }

    public static void shareToFriend(String title,
                                     String message,
                                     Bitmap image,
                                     String urlString,
                                     MyApplication application) {
        int scene = SendMessageToWX.Req.WXSceneSession;
        share(title, message, image, urlString, application, scene);
    }
    /**
     * 分享到微信
     * @param title 标题
     * @param message 描述
     * @param image 图片
     * @param urlString 点击链接
     * @param application application
     *                    @param scene SendMessageToWX.Req.WXSceneTimeline or SendMessageToWX.Req.WXSceneSession
     */
    private static void share(String title,
                                String message,
                                Bitmap image,
                                String urlString,
                                MyApplication application,
                                int scene){

        IWXAPI weixinAPI = application.weixinAPI;

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = urlString;

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = message;
        if (image == null || image.isRecycled()){
            image = BitmapFactory.decodeResource(application.getResources(), R.drawable.app_icon);
        }
        msg.thumbData = bmpToByteArray(image, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = scene;

        int wxSdkVersion = weixinAPI.getWXAppSupportAPI();
        if (wxSdkVersion < 0x21020001) {
            if (scene == SendMessageToWX.Req.WXSceneTimeline) {
                ToastUtil.show(application, "你的微信版本不支持分享到朋友圈");
            }
        }
        weixinAPI.sendReq(req);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
