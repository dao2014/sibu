package com.socialbusiness.dev.orangebusiness.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import cn.jpush.android.api.JPushInterface;

import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.api.RequestAction;
import com.socialbusiness.dev.orangebusiness.model.Push;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.NotificationUtil;

public class MyJPushReceiver extends BroadcastReceiver {

	public static final String TAG = "MyJPushReceiver";
	
	private NotificationUtil notificationUtil;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null) {
			return;
		}
		
		Bundle bundle = intent.getExtras();
		String action = intent.getAction();
		
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(action)) {
			
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(action)) {//自定义消息不会展示在通知栏，完全要开发者写代码去处理
			
		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(action)) {//收到了通知
			Intent localIntent = new Intent(Constant.BROADCAST_RECEIVED_JPUSH);
			
			String extrasJson = bundle.getString(JPushInterface.EXTRA_EXTRA);
			Push pushInfo = RequestAction.GSON.fromJson(extrasJson, Push.class);
			pushInfo.title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
			pushInfo.summary = bundle.getString(JPushInterface.EXTRA_ALERT);
			
			localIntent.putExtra(Constant.EXTRA_KEY_JPUSH, pushInfo);
			LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action)) { //在这里可以写代码去定义用户点击后的行为
			showMyMessage(context, bundle);
		}
	}
	
	private void showMyMessage(Context context, Bundle bundle) {
		if (bundle == null || context == null) {
			return;
		}
		
		if (notificationUtil == null) {
			notificationUtil = NotificationUtil.getInstance(context);
		}
		
		String extrasJson = bundle.getString(JPushInterface.EXTRA_EXTRA);
		Push pushInfo = RequestAction.GSON.fromJson(extrasJson, Push.class);
		pushInfo.title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
		pushInfo.summary = bundle.getString(JPushInterface.EXTRA_ALERT);
		
		Log.d(TAG, "pushInfo=" + pushInfo);
		
		Intent intent = pushInfo.getStartupIntent(context);
		if(intent != null && intent.getComponent() != null) {
			context.startActivity(intent);
		}
	}

}
