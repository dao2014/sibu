package com.socialbusiness.dev.orangebusiness.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * User: Yaotian Leung Date: 2014-01-16 Time: 13:08
 */
public class AndroidUtil {

	public static boolean isIntentAvailable(Context context, Intent intent) {
		if (context == null || intent == null) {
			return false;
		}
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static Intent getURLIntent(String url) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		String prefix = "http://";
		if (url != null && !url.startsWith(prefix)) {
			url = prefix + url;
			intent.setData(Uri.parse(url));
		}
		return intent;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int windowHeight(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	public static Bitmap zoomImage(Bitmap bitmap, double newWidth,
			double newHeight) {
		// 获取这个图片的宽和高
		float width = bitmap.getWidth();
		float height = bitmap.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) width,
				(int) height, matrix, true);
		return newBitmap;
	}

	public static boolean isImageFile(String filePath) {
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts);
			return isBitmapAvailable(bitmap);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean isBitmapAvailable(Bitmap bitmap) {
		if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
			return true;
		}
		return false;
	}


    /**
     * 等比例缩放图片
     * @param input
     * @param scale
     * @return
     */
    public static Bitmap scale(Bitmap input, float scale){
        return scale(input, scale, scale);
    }

    /**
     * 等比例缩放图片
     * @param input
     * @param widthScale
     * @param heightScale
     * @return
     */
    public static Bitmap scale(Bitmap input, float widthScale, float heightScale){
        int dstWidth = (int) (input.getWidth() * widthScale);
        int dstHeight = (int) (input.getHeight() * heightScale);
        return Bitmap.createScaledBitmap(input, dstWidth, dstHeight, false);
    }

	public static Set<String> getContactPhones(Context context) {
		Set<String> contactPhones = new HashSet<String>();
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String contactId = cursor.getString(cursor
						.getColumnIndex(ContactsContract.Contacts._ID));
				int phoneCount = cursor
						.getInt(cursor
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				if (phoneCount > 0) {
					Cursor phones = context.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + contactId, null, null);
					if (phones != null) {
						while (phones.moveToNext()) {
							String phoneNumber = phones
									.getString(phones
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							contactPhones.add(phoneNumber);
						}
						phones.close();
					}
				}
			}
			cursor.close();
		}
		return contactPhones;
	}

	public static boolean toAppStoreQuery(Context context, String keyword) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		try {
			intent.setData(Uri.parse("market://search?q=" + keyword));
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean toAppStoreDetail(Context context, String packageName) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		try {
			intent.setData(Uri.parse("market://details?id=" + packageName));
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		// 获取手机所有连接管理对象（包括对wifi,net等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}
	
	public static boolean isPad(Context context) {  
	    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    Display display = wm.getDefaultDisplay();
	    DisplayMetrics dm = new DisplayMetrics();
	    display.getMetrics(dm);  
	    double x = Math.pow(dm.widthPixels / dm.xdpi, 2);  
	    double y = Math.pow(dm.heightPixels / dm.ydpi, 2);  
	    // 屏幕尺寸  
	    double screenInches = Math.sqrt(x + y);  
	    // 大于6尺寸则为Pad  
	    if (screenInches >= 6.0) {  
	        return true;  
	    }  
	    return false;  
	}
	
	public static void setError(EditText whichFiled, int errorMsgRes) {
    	if(errorMsgRes > 0) {
    		String errorMsg = whichFiled.getResources().getString(errorMsgRes);
    		setError(whichFiled, errorMsg);
    	}
    }
    
    public static void setError(EditText whichFiled, String errorMsg) {
        if(whichFiled != null && !TextUtils.isEmpty(errorMsg)) {
            ForegroundColorSpan span = new ForegroundColorSpan(Color.RED);
            SpannableStringBuilder ssbuilder = new SpannableStringBuilder(errorMsg);
            ssbuilder.setSpan(span, 0, errorMsg.length(), 0);
            whichFiled.setError(ssbuilder);
        }
    }
    
    /**
     * 根据“item总数”、“每页的item数”计算总页数
     * @param totalCount listView中的item总数
     * @param pageSize 每页的item数
     * @return 总页数
     */
    public static int getTotalPage(int totalCount, int pageSize) {
    	if (pageSize == 0) {
    		return 0;
    	} else {
    		return (totalCount % pageSize == 0) ? (totalCount / pageSize) : (totalCount / pageSize + 1);
    	}
    }
    
    public static String nullToEmptyString(String str) {
    	if(TextUtils.isEmpty(str)) {
    		return "";
    	}
    	return str;
    }
    
    /**
     * 通过点击区域，判断是否隐藏EditText
     * @param v
     * @param event
     * @return
     */
    public static boolean isShouldHideInput(View v, MotionEvent event) {
    	if (v != null && (v instanceof EditText)) {
    		int[] leftTop = {0, 0};
    		// 获取输入框当前的location位置
    		v.getLocationInWindow(leftTop);
    		int left = leftTop[0];
    		int top = leftTop[1];
    		int bottom = top + v.getHeight();
    		int right = left + v.getWidth();
    		if (event.getX() > left && event.getX() < right
    				&& event.getY() > top && event.getY() < bottom) {
    			// 点击的是输入框区域，保留点击EditText的事件
    			return false;
    		} else {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static void mockClickFromLauncher(Context context, Class<?> activityClass) {
    	Intent intent = getMockClickFromLauncherIntent(context, activityClass);
		context.startActivity(intent);
    }
    
    public static Intent getMockClickFromLauncherIntent(Context context, Class<?> activityClass) {
    	Intent intent = new Intent(context, activityClass);
		intent.setFlags(0x10200000);
		return intent;
    }
    
    public static boolean isAppRunning(Context context) {  
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);  
        List<RunningTaskInfo> tasks = am.getRunningTasks(Integer.MAX_VALUE);
        for (RunningTaskInfo task : tasks) {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
            	return true;
            }
        }
        return false ;  
    }  
    
    public static boolean isLauncherRunnig(Context context) {  
        boolean result = false ;  
        List<String> names = getAllTheLauncher(context);  
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE) ;  
        List<ActivityManager.RunningAppProcessInfo> appList = mActivityManager.getRunningAppProcesses() ;  
        for (RunningAppProcessInfo running : appList) {  
                if (running.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {  
                        for (int i = 0; i < names.size(); i++) {  
                                if (names.get(i).equals(running.processName)) {  
                                        result = true ;  
                                        break;  
                                }  
                        }  
                }  
        }  
        return result ;  
    }  
    
    private static List<String> getAllTheLauncher(Context context){  
        List<String> names = null;  
        PackageManager pkgMgt = context.getPackageManager();  
        Intent it = new Intent(Intent.ACTION_MAIN);     
        it.addCategory(Intent.CATEGORY_HOME);   
        List<ResolveInfo> ra =pkgMgt.queryIntentActivities(it,0);   
        if(ra.size() != 0){  
            names = new ArrayList<String>();  
        }  
        for(int i=0;i< ra.size();i++)      
        {  
        String packageName =  ra.get(i).activityInfo.packageName;  
        names.add(packageName);  
        }    
        return names;  
    }  
}
