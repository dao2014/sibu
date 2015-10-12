package com.socialbusiness.dev.orangebusiness.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesUtil {
	private static SharedPreferences sharedPreferences;
	private static SharedPreferences getSharedPreferences(Context context){
		if (sharedPreferences==null) {
			sharedPreferences=context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		}
		return sharedPreferences;
	}
	
	public static void remove(Context context,String key){
		Editor editor = getSharedPreferences(context).edit();//获取编辑器
		editor.remove(key);
		editor.commit();//提交修改
//		Log.i("my", "remove:"+key);
	}

	/*********************String******************************/
	public static void putString(Context context,String key,String value){
		Editor editor = getSharedPreferences(context).edit();//获取编辑器
		editor.putString(key, value);
		editor.commit();//提交修改
//		Log.i("my", "putString:"+key+"->"+value);
	}
	public static String getString(Context context,String key){
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		String value=sharedPreferences.getString(key, null);
//		Log.i("my", "getString:"+key+"->"+value);
		return value;
	}
	
	/************************Int***************************/
	public static void putInt(Context context,String key,int value){
		Editor editor = getSharedPreferences(context).edit();//获取编辑器
		editor.putInt(key, value);
		editor.commit();//提交修改
//		Log.i("my", "putInt:"+key+"->"+value);
	}
	public static int getInt(Context context,String key){
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		int value=sharedPreferences.getInt(key, 0);
//		Log.i("my", "getInt:"+key+"->"+value);
		return value;
	}
	
	/************************Boolean***************************/
	public static void putBoolean(Context context, String key, Boolean value){
		Editor editor = getSharedPreferences(context).edit();//获取编辑器
		editor.putBoolean(key, value);
		editor.commit();//提交修改
//		Log.i("my", "putBoolean:"+key+"->"+value);
	}
	public static boolean getBoolean(Context context, String key){
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		boolean value=sharedPreferences.getBoolean(key, true);
//		Log.i("my", "getBoolean:"+key+"->"+value);
		return value;
	}
	public static boolean getBoolean(Context context, String key, boolean def){
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		boolean value=sharedPreferences.getBoolean(key, def);
//		Log.i("my", "getBoolean:"+key+"->"+value);
		return value;
	}
	
	
	
}