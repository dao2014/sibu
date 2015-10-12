package com.socialbusiness.dev.orangebusiness.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.api.RequestAction;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.FileUtil;
import com.socialbusiness.dev.orangebusiness.util.MD5;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import io.rong.imkit.RongIM;
import it.sauronsoftware.base64.Base64;

public final class SettingsManager {
	
	private static Context mContext;
	private static SharedPreferences sharedPreferences;
	private static SettingsManager instance;
    public static final String IS_DESTROY_LOGOUT="IS_DESTROY_LOGOUT";

	private SettingsManager(Context context) {
		mContext = context.getApplicationContext();
		sharedPreferences = mContext.getSharedPreferences("Settings.sp", Context.MODE_PRIVATE);
	}	
	
	public static SettingsManager init(Context context) {
		if(instance == null || instance.mContext == null) {
			SettingsManager.destroy();
			instance = new SettingsManager(context);
		}
        return instance;
	}
	
	public static void destroy() {
		if(instance != null) {
			instance.mContext = null;
			instance.sharedPreferences = null;
			instance = null;
		}
	}
	
	public static SettingsManager getInstance() {
		return instance;
	}
	
	public static void saveLoginUser(User user) {
		if(user != null) {
//			EasemodManager.getInstance().setLoginUserAvatar(APIManager.toAbsoluteUrl(user.head));
//			EasemodManager.getInstance().setLoginUserNickName(user.nickName);
		}
        JPushInterface.resumePush(mContext);
		refreshJpushStatus(user, JpushStatus.FRONTGROUND);
		FileUtil.writeSerializableForDefaultPath("User.cache", user);
	}
	
	public static void saveLoginUserWithoutSetJPush(User user) {
		if(user != null) {
//			EasemodManager.getInstance().setLoginUserAvatar(APIManager.toAbsoluteUrl(user.head));
//			EasemodManager.getInstance().setLoginUserNickName(user.nickName);
		}
		FileUtil.writeSerializableForDefaultPath("User.cache", user);
	}

	public static User getLoginUser() {
		return (User) FileUtil.readSerializableFromDefaultPath("User.cache");
	}
	
	public static void saveLoginSession() {
		sharedPreferences.edit().putString("Session", RequestAction.getSessionId()).commit();
	}
	
	public static String loadLoginSession() {
		String session = sharedPreferences.getString("Session", null);
		RequestAction.setSessionId(session);
		return session;
	}
	
	public static void setPhone(String phone) {
		sharedPreferences.edit().putString(Constant.SP_KEY_PHONE, phone).commit();
	}
	
	public static String getPhone() {
		return sharedPreferences.getString(Constant.SP_KEY_PHONE, "");
	}

    public  void setLogDestroyFlag(boolean isLogout) {
        sharedPreferences.edit().putBoolean(IS_DESTROY_LOGOUT, isLogout).commit();
    }

    public  boolean getLogDestroyFlag() {
        return sharedPreferences.getBoolean(IS_DESTROY_LOGOUT, true);
    }
	
	public static void setRememberPwd(boolean remember, String pwd) {
		Editor editor = sharedPreferences.edit();
		if(remember) {
			editor.putString(Constant.SP_KEY_RM_TOKEN, constructSecretaryString(Base64.encode(pwd)));
		}
		else {
			editor.putString(Constant.SP_KEY_RM_TOKEN, "");
		}
		editor.putBoolean(Constant.SP_KEY_REMEMBER_PWD, remember);
		editor.commit();
	}
	
	public static boolean isRememberPwd() {
		return sharedPreferences.getBoolean(Constant.SP_KEY_REMEMBER_PWD, false);
	}
	
	public static String getRememberPwd() {
		boolean isRememberPwd = isRememberPwd();
		String token = sharedPreferences.getString(Constant.SP_KEY_RM_TOKEN, null);
		if(!isRememberPwd || TextUtils.isEmpty(token) || token.length() < 10) {
			return "";
		}
		token = token.substring(5, token.length() - 5);
		return Base64.decode(token);
	}
	
	public boolean isFirstLaunch() {
		return sharedPreferences.getBoolean(Constant.SP_KEY_FIRST_LAUNCH, true);
	}
	
	public void setFirstLaunchDone() {
		sharedPreferences.edit().putBoolean(Constant.SP_KEY_FIRST_LAUNCH, false).commit();
	}
	
	public int getVersionCode(){
		return sharedPreferences.getInt(Constant.SP_KEY_VERSON_CODE, 0);
	}
	
	public void setVersionCode(int versionCode){
		sharedPreferences.edit().putInt(Constant.SP_KEY_VERSON_CODE, versionCode).commit();
	}
	
	public static void destroyLogin() {
		if(Constant.IGNORE_KICK_USER) {
			return ;
		}
        User user = getLoginUser();
		FileUtil.deleteSerializableFileForDefaultPath("User.cache");
		RequestAction.setSessionId(null);
		saveLoginSession();
        unRegisterJpushAlias();
        JPushInterface.stopPush(mContext);
//        EasemodManager.getInstance().logout();
        RongIM.getInstance().disconnect();
	}
	
	public static boolean isUserLogin() {
        User user = getLoginUser();
        String sessionStr = loadLoginSession();
        return !TextUtils.isEmpty(sessionStr) && user != null;
	}

	public static void saveEasemodPwd(String easemodPwd) {
		sharedPreferences.edit().putString("secre_data", constructSecretaryString(easemodPwd)).commit();
	}
	
	public static String getEasemodPwd() {
		String secreData = sharedPreferences.getString("secre_data", null);
		if(TextUtils.isEmpty(secreData) || secreData.length() < 10) {
			return null;
		}
		return secreData.substring(5, secreData.length() - 5);
	}
	
	private static String constructSecretaryString(String src) {
		String subfix = StringUtil.randomChar(5);
		String prefix =StringUtil.randomChar(5);
		return subfix + src + prefix;
	}
	
	public static void refreshJpushStatus(User user, JpushStatus status){
        if(user == null || TextUtils.isEmpty(user.phone)){
            status = JpushStatus.DISABLE;
            return;
        }

        HashSet<String> jpushTag = new HashSet<String>();
        if(status != JpushStatus.DISABLE && user != null) {
        	// 将企业ID作为tag
//        	String tag = user.companyId.replaceAll("-", "_");
        	String tag = MD5.encode(user.companyId);
        	jpushTag.add(tag);
        }
        switch (status){
            case FRONTGROUND:
                jpushTag.add("FRONTGROUND");
                registerJpushAlias(user, jpushTag);
                break;
            case BACKGROUND:
                jpushTag.add("BACKGROUND");
                registerJpushAlias(user, jpushTag);
                break;
            case DISABLE:
                unRegisterJpushAlias();
                break;
        }
    }
	
	private static void registerJpushAlias(final User user, final HashSet<String> jpushTag){
//		String alias = user.id.replaceAll("-", "_");
		String alias = MD5.encode(user.id);
        JPushInterface.setAliasAndTags(mContext, alias, jpushTag, new TagAliasCallback() {
            @Override
            public void gotResult(int code, String alias, Set<String> tags) {
                if(code == 6002){//如果返回 6002 （超时）则建议重试
                    registerJpushAlias(user, jpushTag);
                }
            }
        });
    }
	
	private static void unRegisterJpushAlias(){
        JPushInterface.setAliasAndTags(mContext, "", new HashSet<String>(), new TagAliasCallback() {
            @Override
            public void gotResult(int code, String alias, Set<String> tags) {
                if(code == 6002){//如果返回 6002 （超时）则建议重试
                    unRegisterJpushAlias();
                }
            }
        });
    }
	
	public static enum JpushStatus{
        FRONTGROUND,
        BACKGROUND,
        DISABLE,
    }
}
