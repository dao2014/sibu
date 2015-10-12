package com.socialbusiness.dev.orangebusiness;

import android.app.Application;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.kbeanie.imagechooser.api.FileUtils;
import com.kollway.android.imagecachelib.ImageCacheManager;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.receiver.RongCloudProvidersListener;
import com.socialbusiness.dev.orangebusiness.util.FileUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jpush.android.api.JPushInterface;
import io.rong.imkit.RongIM;
import io.rong.imkit.demo.DemoContext;
import io.rong.imkit.demo.message.GroupInvitationNotification;
import io.rong.imlib.AnnotationNotFoundException;

public class MyApplication extends Application {
	
	private static final String TAG = "MyApplication";
    public IWXAPI weixinAPI;
	
    public static final Handler uiHandler = new Handler(Looper.getMainLooper());
    public static final ExecutorService bgExecutor = Executors.newCachedThreadPool();

	@Override
	public void onCreate() {
		super.onCreate();
        Log.e("MyApplication","===oncreate");
        Constant.init(this);
        SettingsManager.init(this);
        APIManager.getInstance(this);
       
        initImageCache();
        mkdirs();
        initUmeng();
        //Jpush
        JPushInterface.init(getApplicationContext());
        //WeChat
        weixinAPI = WXAPIFactory.createWXAPI(this, Constant.WeChat_APP_ID, !Constant.DEBUG_MODE);
        weixinAPI.registerApp(Constant.WeChat_APP_ID);
		//RongCloud
        intRongCloud();

	}

    public void intRongCloud() {
        RongIM.init(this);
        RongCloudProvidersListener.init(this);
//        RongCloudEvent.init(this);
        DemoContext.init(this);
        try {
            //注册自定义消息类型
            RongIM.registerMessageType(GroupInvitationNotification.class);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initImageCache() {
		ImageCacheManager.init(getApplicationContext());
	}
	
	private void mkdirs() {
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			FileUtil.CAHCHEDIR = Environment.getExternalStorageDirectory() + "/android/data/" + getApplicationContext().getPackageName()+File.separator+"files";
		    FileUtils.DIRECTORY_CACHE = FileUtil.CAHCHEDIR;
		}else{
			FileUtil.CAHCHEDIR = getApplicationContext().getCacheDir().getPath();//用机身的
		    FileUtils.DIRECTORY_CACHE = FileUtil.CAHCHEDIR;
		    
		}
		File file = new File(FileUtil.CAHCHEDIR);
		if(!file.exists()) {
			file.mkdirs();
		}
	}
	

	private void initUmeng() {
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setUpdateAutoPopup(false);
		MobclickAgent.updateOnlineConfig(getApplicationContext());
	}
}
