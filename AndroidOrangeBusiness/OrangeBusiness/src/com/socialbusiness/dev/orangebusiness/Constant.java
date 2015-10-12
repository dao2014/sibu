package com.socialbusiness.dev.orangebusiness;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import java.io.File;

public class Constant {
    public static boolean DEBUG_MODE = true;

    public static boolean IGNORE_KICK_USER = DEBUG_MODE && false;

    public static final String UPEST_DEFAULT_USERID = "00000000-0000-0000-0000-000000000000";

    public static int MESSAGE_SHOW_DATEPICKER = 20141219;
    public static int DATE_DIALOG_ID = 20141220;
    
    public static final int REQUESTCODE_ADD_PRODUCT = 1220;
    public static final String EXTRA_KEY_ORDER_SALES_ADD_OBJ = "EXTRA_KEY_ORDER_SALES_ADD_OBJ";
    public static final String EXTRA_KEY_PRODUCT_ITEM_LIST = "EXTRA_KEY_PRODUCT_ITEM_LIST";
    public static final String EXTRA_KEY_TABPOSITION = "EXTRA_KEY_TABPOSITION";
    public static final String EXTRA_KEY_LOGOUT = "EXTRA_KEY_LOGOUT";
    public static final String EXTRA_KEY_JPUSH = "EXTRA_KEY_JPUSH";
    public static final String EXTRA_KEY_USER_ID = "EXTRA_KEY_USER_ID";
    public static final String EXTRA_KEY_ORDER_ID = "EXTRA_KEY_ORDER_ID";
    public static final String EXTRA_KEY_ORDER_OBJ = "EXTRA_KEY_ORDER_OBJ";
    public static final String EXTRA_KEY_ORDER_DELIVER_OBJ = "EXTRA_KEY_ORDER_DELIVER_OBJ";
    public static final String EXTRA_KEY_PRODUCT_LIST = "EXTRA_KEY_PRODUCT_LIST";
    public static final String EXTRA_KEY_SCAN_RESULT_CODES = "EXTRA_KEY_SCAN_RESULT_CODES";
    public static final String EXTRA_KEY_SCAN_RESULT_ITEMS = "EXTRA_KEY_SCAN_RESULT_ITEMS";
    public static final String EXTRA_KEY_SCAN_RESULT_STANDERS = "EXTRA_KEY_SCAN_RESULT_STANDERS";
    public static final String EXTRA_KEY_FROM = "EXTRA_KEY_FROM";
    public static final String EXTRA_VALUE_PRODUCT = "EXTRA_VALUE_PRODUCT";

    public static final String EXTRA_KEY_PRODUCT_TYPE = "EXTRA_KEY_PRODUCT_TYPE";
    public static final int DEFAULT_ILLEGAL_PRODUCT_TYPE = -27;

    public static final String EXTRA_ORDER_FROM_BUYER = "EXTRA_ORDER_FROM_BUYER";
    public static final String EXTRA_ORDER_FROM_SELLER = "EXTRA_ORDER_FROM_SELLER";
    public static final String EXTRA_ORDER_FROM = "EXTRA_ORDER_FROM";

    public static final String EXTRA_KEY_TRADETYPE = "EXTRA_KEY_TRADETYPE";

    public static final int REQUESTCODE_DELIVERED = 17415;
    public static final int RESULTCODE_OK = 1417;

    public static final String SETTINGS_SP_NAME = "settings.sp";
    
    public static final String APP_ID = "wxd930ea5d5a258f4f";//要申请一个appid
    public static final String RECEIVER_NO_LOGIN = "com.kollway.android.storesecretary.RECEIVER_NO_LOGIN";
    public static final String RECEIVER_SHOW_LOGIN = "com.kollway.android.storesecretary.RECEIVER_SHOW_LOGIN";
    public static final String BROADCAST_RECEIVED_JPUSH = R.class.getPackage().getName() + ".BROADCAST_RECEIVED_JPUSH";
    public static final String SP_KEY_PHONE = "Phone";
    public static final String SP_KEY_REMEMBER_PWD = "RememberFlag";
    public static final String SP_KEY_RM_TOKEN = "UserToken";
    public static final String SP_KEY_FIRST_LAUNCH = "FirstLaunch";
    public static final String SP_KEY_VERSON_CODE = "VersionCode";
    
    public static String PATH_SD_DIR;
    public static String PATH_IMAGE_TEMP_FOLDER = "/files/OrangeBusiness/temp";	//此处不要使用绝对路径
    public static String PATH_DOWNLOADED_COURSE_DIR;
	public static String PATH_DOWNLOADED_UNZIP_DIR;
    public static final String WeChat_APP_ID = "wx39f30f1b6f337b0b";
    
    public static void init(Application application){
        DEBUG_MODE = (0 != (application.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        File filesDir = application.getExternalFilesDir(null);
        if(filesDir == null){
            filesDir = application.getFilesDir();
        }
        initFilesDir(filesDir);
    }

    public static void initFilesDir(File filesDir) {
    	PATH_SD_DIR = filesDir.getAbsolutePath();
        PATH_DOWNLOADED_COURSE_DIR = PATH_SD_DIR+"/downLoaded";
        PATH_DOWNLOADED_UNZIP_DIR = PATH_SD_DIR + "/unzip";
    }
    
    
}
