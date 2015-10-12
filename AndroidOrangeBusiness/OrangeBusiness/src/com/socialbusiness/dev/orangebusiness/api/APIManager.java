package com.socialbusiness.dev.orangebusiness.api;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;
import com.kollway.android.imagecachelib.ImageCacheManager;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.model.Address;
import com.socialbusiness.dev.orangebusiness.model.Category;
import com.socialbusiness.dev.orangebusiness.model.Course;
import com.socialbusiness.dev.orangebusiness.model.Group;
import com.socialbusiness.dev.orangebusiness.model.GroupDetail;
import com.socialbusiness.dev.orangebusiness.model.Level;
import com.socialbusiness.dev.orangebusiness.model.MultiOrderUpload;
import com.socialbusiness.dev.orangebusiness.model.Notice;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.model.UserType;
import com.socialbusiness.dev.orangebusiness.util.BaseHttp;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.MD5;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * 约定1:listXXX:获取列表数据
 * 约定2:getXXX:获取单个对象
 * 约定3:addXXX:提交对象到服务端,返回RequestResult
 * 约定4:请求成功call back内,要做对象非空判断
 * 约定5:提交boolean参数都用数字代替true="1", false="0"
 * Created by liangyaotian on 2014-07-01.
 */
public final class APIManager {
    public static final String TAG = "APIManager";
    public static final String HOST = "apiv3.orangebusiness.com.cn";
    //    public static final String HOST = "api.orangebusiness.com.cn";
    public static final String HTTP_HOST = "http://apiv3.orangebusiness.com.cn";
    //    public static final String HTTP_HOST = "http://api.orangebusiness.com.cn";
    public static final String BASE_URL = "http://" + HOST + "/api";
    public static final int REQUEST_TIMEOUT_SECONDS = 15;
    public static final String KEY_PAGE_SIZE = "pageSize";
    public static final String KEY_PAGE = "page";
    public static final int PAGE_SIZE = 15;
    public static final String MAX_PAGE_SIZE = "1000";
    private static final int CACHE_SIZE = 20*1024*1024;

    public static final String STATE_ACOUNT = HTTP_HOST + "/Report/Detail/List?companyId=%s&from=%s&to=%s";
    public static final String PRODUCT_DETAIL_URL = HTTP_HOST + "/Product/Detail/Info/?companyId=%s&id=%s";
    public static final String KUAIDI_URL = HTTP_HOST + "/KuaiDi/Search?expressname=%s&expresscode=%s";


    private static APIManager instance;
    private static WeakReference<Context> weakAppContext;
    private final RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private APIManager(Context context) {
        Context appContext = context.getApplicationContext();
        requestQueue = Volley.newRequestQueue(appContext);
        weakAppContext = new WeakReference<>(appContext);

    }

    public static APIManager getInstance(Context context) {
        if (weakAppContext == null || weakAppContext.get() == null) {
            instance = null;
        }
        if (instance == null) {
            synchronized (APIManager.class) {
                if (instance == null) {
                    instance = new APIManager(context);
                }
            }
        }
        return instance;
    }

    public static String toAbsoluteUrl(String input) {
        if (TextUtils.isEmpty(input)) {
            return BASE_URL;
        }
        if (input.startsWith("http://")) {
            return input;
        }
        if (!input.startsWith("/")) {
            input = "/" + input;
        }
        return "http://" + HOST + input;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        if (imageLoader == null) {
            synchronized (this) {
                if (imageLoader == null && weakAppContext != null && weakAppContext.get() != null) {
//                    ImageCacheManager imageCacheManager = ImageCacheManager.getInstance();
//                    imageCacheManager.init(weakAppContext.get(), "images", CACHE_SIZE, Bitmap.CompressFormat.PNG, 80);
                    imageLoader = ImageCacheManager.getInstance().getImageLoader();
                }
            }
        }
        return imageLoader;
    }

    private String getParameterDescription(String... parameterKey) {
        StringBuilder stringBuilder = new StringBuilder("your parameters map must be look like this: ");
        stringBuilder.append("{ ");
        for (String item : parameterKey) {
            stringBuilder.append(item);
            stringBuilder.append("=? ");
        }
        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    /**
     * @param modelType       返回对象的Type
     * @param api             api名
     * @param httpMethod      http请求方法
     * @param parameters      请求参数
     * @param errorListener   失败callback
     * @param successListener 成功callback
     * @param <T>             返回的泛型
     * @return 该请求对象
     */
    private <T> RequestAction<T> buildRequestAction(Type modelType,
                                                    String api,
                                                    int httpMethod,
                                                    Hashtable<String, String> parameters,
                                                    Response.ErrorListener errorListener,
                                                    final Response.Listener<T> successListener) {

        return buildRequestAction(modelType, api, httpMethod, parameters, null, true, errorListener, successListener);
    }

    /**
     * @param modelType       返回对象的Type
     * @param api             api名
     * @param httpMethod      http请求方法
     * @param parameters      请求参数
     * @param body            请求体
     * @param runNow          是否马上执行
     * @param errorListener   失败callback
     * @param successListener 成功callback
     * @param <T>             返回的泛型
     * @return 该请求对象
     */
    private <T> RequestAction<T> buildRequestAction(Type modelType,
                                                    String api,
                                                    int httpMethod,
                                                    Hashtable<String, String> parameters,
                                                    RequestAction.RequestBody body,
                                                    boolean runNow,
                                                    Response.ErrorListener errorListener,
                                                    final Response.Listener<T> successListener) {

        Response.Listener<T> mySuccessListener = new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                successListener.onResponse(response);
                checkRequestResult(response);
            }
        };
        RequestAction<T> requestAction = new RequestAction<>(
                api, httpMethod, null, modelType, body, errorListener, mySuccessListener);
        if (parameters != null) {
            Set<String> keySet = parameters.keySet();
            for (String key : keySet) {
                String value = parameters.get(key);
                requestAction.addRequestParams(new RequestParam(key, value));
            }
        }
        if (runNow) {
            getRequestQueue().add(requestAction);
        }

        if (Constant.DEBUG_MODE) {
            Log.d(TAG, "" + requestAction);
        }

        return requestAction;
    }

    private <T> void checkRequestResult(T response) {
        Context context = weakAppContext.get();
        if (context == null) {
            return;
        }

        int responseCode;
        if (response instanceof RequestListResult) {
            responseCode = ((RequestListResult) response).code;

        } else if (response instanceof RequestResult) {
            responseCode = ((RequestResult) response).code;

        } else {
            return;
        }

        switch (responseCode) {
            case 1: {
                Intent intent = new Intent(Constant.RECEIVER_NO_LOGIN);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                break;
            }
            default:
                break;
        }
    }

    /**
     * 处理列表API的请求参数
     *
     * @param parameters
     */
    private void processListAPIParameters(Hashtable<String, String> parameters) {
        if (parameters == null
                || !parameters.containsKey("page")
                ) {
            throw new IllegalArgumentException(getParameterDescription("page"));
        }
        if (!parameters.containsKey(KEY_PAGE_SIZE)) {
            parameters.put(KEY_PAGE_SIZE, PAGE_SIZE + "");
        }
    }

    //=====================================API========================================

    /**
     * 登录
     *
     * @param parameters      {account=?,password=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<User>> userLogin(Hashtable<String, String> parameters,
                                                        Response.ErrorListener errorListener,
                                                        final Response.Listener<RequestResult<User>> successListener) {
        RequestAction.setSessionId("");

        String ACCOUNT = "account";
        String PASSWORD = "password";
        if (parameters == null
                || !parameters.containsKey(ACCOUNT)
                || !parameters.containsKey(PASSWORD)) {
            throw new IllegalArgumentException(getParameterDescription(ACCOUNT, PASSWORD));
        }

        Type modelType = new TypeToken<RequestResult<User>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/login", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 登出
     *
     * @param parameters      {}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> userLogout(Hashtable<String, String> parameters,
                                                      Response.ErrorListener errorListener,
                                                      final Response.Listener<RequestResult<?>> successListener) {

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/logout", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 刷新用户信息
     *
     * @param parameters      {}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<User>> userRefresh(Hashtable<String, String> parameters,
                                                          Response.ErrorListener errorListener,
                                                          final Response.Listener<RequestResult<User>> successListener) {

        Type modelType = new TypeToken<RequestResult<User>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/refresh", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 注册
     *
     * @param parameters      {phone=?,recommendPhone=?,nickName=?,password=?,smsVerify=?,wechat=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<User>> userRegister(Hashtable<String, String> parameters,
                                                           Response.ErrorListener errorListener,
                                                           final Response.Listener<RequestResult<User>> successListener) {
        String PHONE = "phone";
        String R_PHONE = "recommendPhone";
        String NICKNAME = "nickName";
        String PASSWORD = "password";
        String SMS_VERIFY = "smsVerify";
        if (parameters == null
                || !parameters.containsKey(PHONE)
                || !parameters.containsKey(NICKNAME)
                || !parameters.containsKey(PASSWORD)
                || !parameters.containsKey(SMS_VERIFY)
                ) {
            throw new IllegalArgumentException(getParameterDescription(PHONE, R_PHONE, NICKNAME, PASSWORD, SMS_VERIFY));
        }

        Type modelType = new TypeToken<RequestResult<User>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/register", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 企业用户注册
     *
     * @param parameters      {phone=?,recommendPhone=?,nickName=?,password=?,smsVerify=?,wechat=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<User>> enterpriseUserRegister(Hashtable<String, String> parameters,
                                                                     Response.ErrorListener errorListener,
                                                                     final Response.Listener<RequestResult<User>> successListener) {
//        String PHONE = "phone";
//        String R_PHONE = "recommendPhone";
//        String NICKNAME = "nickName";
//        String PASSWORD = "password";
//        String SMS_VERIFY = "smsVerify";
//        if(parameters == null
//                || !parameters.containsKey(PHONE)
//                || !parameters.containsKey(NICKNAME)
//                || !parameters.containsKey(PASSWORD)
//                || !parameters.containsKey(SMS_VERIFY)
//                ){
//            throw new IllegalArgumentException(getParameterDescription(PHONE, R_PHONE, NICKNAME, PASSWORD, SMS_VERIFY));
//        }

        Type modelType = new TypeToken<RequestResult<User>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/registerCompany", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 获取注册验证短信
     *
     * @param parameters      {phone=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> userRegisterSms(Hashtable<String, String> parameters,
                                                           Response.ErrorListener errorListener,
                                                           final Response.Listener<RequestResult<?>> successListener) {
        String PHONE = "phone";
        if (parameters == null
                || !parameters.containsKey(PHONE)
                ) {
            throw new IllegalArgumentException(getParameterDescription(PHONE));
        }
        parameters.put("token", getSMSAPIToken(parameters.get(PHONE)));
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/registerSms", Request.Method.GET, parameters, errorListener, successListener);
    }

    private String getSMSAPIToken(String phone) {
        return MD5.encode("www.orangebusiness.com.cn+" + phone);
    }

    /**
     * 重置密码
     *
     * @param parameters      {phone=?,password=?,smsVerify=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> userResetPassword(Hashtable<String, String> parameters,
                                                             Response.ErrorListener errorListener,
                                                             final Response.Listener<RequestResult<?>> successListener) {
        String PHONE = "phone";
        String PASSWORD = "password";
        String SMS_VERIFY = "smsVerify";
        if (parameters == null
                || !parameters.containsKey(PHONE)
                || !parameters.containsKey(PASSWORD)
                || !parameters.containsKey(SMS_VERIFY)
                ) {
            throw new IllegalArgumentException(getParameterDescription(PHONE, PASSWORD, SMS_VERIFY));
        }

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/resetPassword", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 获取重设密码验证短信
     *
     * @param parameters      {phone=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> userResetPasswordSms(Hashtable<String, String> parameters,
                                                                Response.ErrorListener errorListener,
                                                                final Response.Listener<RequestResult<?>> successListener) {
        String PHONE = "phone";
        if (parameters == null
                || !parameters.containsKey(PHONE)
                ) {
            throw new IllegalArgumentException(getParameterDescription(PHONE));
        }
        parameters.put("token", getSMSAPIToken(parameters.get(PHONE)));
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/resetPasswordSms", Request.Method.GET, parameters, errorListener, successListener);
    }

    private RequestAction<RequestListResult<User>> getUserBaseInfo(String apiPath, ArrayList<String> queryValues,
                                                                   Response.ErrorListener errorListener,
                                                                   final Response.Listener<RequestListResult<User>> successListener) {
        Hashtable<String, String> parameters = new Hashtable<>();

        StringBuilder parameterString = new StringBuilder();
        for (String value : queryValues) {
            parameterString.append(value);
            parameterString.append(',');
        }
        int len = parameterString.length();
        parameterString.delete(len - 1, len);

        if (apiPath.endsWith("getBaseInfoByPhone")) {
            parameters.put("phone", parameterString.toString());
        } else if (apiPath.endsWith("getBaseInfoByHX")) {
            parameters.put("hxAccount", parameterString.toString());
        } else {
            throw new IllegalArgumentException("Unknow API path!");
        }

        Type modelType = new TypeToken<RequestListResult<User>>() {
        }.getType();
        return buildRequestAction(modelType, apiPath, Request.Method.GET, parameters, errorListener, successListener);

    }

    /**
     * 根据手机号获取基本用户信息
     *
     * @param phone           手机号
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<User>> userBaseInfoByPhone(ArrayList<String> phone,
                                                                      Response.ErrorListener errorListener,
                                                                      final Response.Listener<RequestListResult<User>> successListener) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("phone can't be empty");
        }

        return this.getUserBaseInfo("/user/getBaseInfoByPhone", phone, errorListener, successListener);
    }

    /**
     * 根据手机号获取用户昵称
     *
     * @param phone           手机号
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<String>> userNameByPhone(String phone,
                                                                Response.ErrorListener errorListener,
                                                                final Response.Listener<RequestResult<String>> successListener) {
        if (TextUtils.isEmpty(phone)) {
            throw new IllegalArgumentException("phone can't be empty");
        }

        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("phone", phone);

        String apiPath = "/user/getUserNameByPhone";
        Type modelType = new TypeToken<RequestResult<String>>() {
        }.getType();

        return buildRequestAction(modelType, apiPath, Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 注册第一步的验证
     *
     * @param parameters      {phone=?,recommendPhone=?,smsVerify=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<String>> userCheckPhone(Hashtable<String, String> parameters,
                                                               Response.ErrorListener errorListener,
                                                               final Response.Listener<RequestResult<String>> successListener) {
        String PHONE = "phone";
        String SMS_VERIFY = "smsVerify";
        if (parameters == null
                || !parameters.containsKey(PHONE)
                || !parameters.containsKey(SMS_VERIFY)
                ) {
            throw new IllegalArgumentException(getParameterDescription(PHONE, SMS_VERIFY));
        }

        String apiPath = "/user/checkPhone";
        Type modelType = new TypeToken<RequestResult<String>>() {
        }.getType();

        return buildRequestAction(modelType, apiPath, Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 企业注册第一步的验证
     *
     * @param parameters      {phone=?,recommendPhone=?,smsVerify=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<String>> checkCompanyPhone(Hashtable<String, String> parameters,
                                                                  Response.ErrorListener errorListener,
                                                                  final Response.Listener<RequestResult<String>> successListener) {
        String PHONE = "phone";
        String SMS_VERIFY = "smsVerify";
        if (parameters == null
                || !parameters.containsKey(PHONE)
                || !parameters.containsKey(SMS_VERIFY)
                ) {
            throw new IllegalArgumentException(getParameterDescription(PHONE, SMS_VERIFY));
        }

        String apiPath = "/user/checkCompanyPhone";
        Type modelType = new TypeToken<RequestResult<String>>() {
        }.getType();

        return buildRequestAction(modelType, apiPath, Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 忘记密码第一步的验证
     *
     * @param parameters      {phone=?,smsVerify=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> userCheckResetPwdSms(Hashtable<String, String> parameters,
                                                                Response.ErrorListener errorListener,
                                                                final Response.Listener<RequestResult<?>> successListener) {
        String PHONE = "phone";
        String SMS_VERIFY = "smsVerify";
        if (parameters == null
                || !parameters.containsKey(PHONE)
                || !parameters.containsKey(SMS_VERIFY)
                ) {
            throw new IllegalArgumentException(getParameterDescription(PHONE, SMS_VERIFY));
        }

        String apiPath = "/user/checkResetPasswordSms";
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();

        return buildRequestAction(modelType, apiPath, Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 根据环信号获取基本用户信息
     *
     * @param hxAccount       环信号
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<User>> userBaseInfoByHxAccount(ArrayList<String> hxAccount,
                                                                          Response.ErrorListener errorListener,
                                                                          final Response.Listener<RequestListResult<User>> successListener) {
        if (hxAccount == null || hxAccount.isEmpty()) {
            throw new IllegalArgumentException("Huan Xin Account can't be empty");
        }

        return this.getUserBaseInfo("/user/getBaseInfoByHX", hxAccount, errorListener, successListener);

    }

    /**
     * 层级列表
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Level>> listLevel(Hashtable<String, String> parameters,
                                                             Response.ErrorListener errorListener,
                                                             Response.Listener<RequestListResult<Level>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Level>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/listLevel", Request.Method.GET, parameters, errorListener, successListener);
    }


    /**
     * 私聊用户列表
     *
     * @param parameters      {}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<User.LeveledUsers>> listUsersByChat(Hashtable<String, String> parameters,
                                                                           Response.ErrorListener errorListener,
                                                                           Response.Listener<RequestResult<User.LeveledUsers>> successListener) {
        Type modelType = new TypeToken<RequestResult<User.LeveledUsers>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/listUsersByChat", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 组群邀请用户列表
     *
     * @param parameters      {}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<User.LeveledUsers>> listUsersByGroupInvite(Hashtable<String, String> parameters,
                                                                                  Response.ErrorListener errorListener,
                                                                                  Response.Listener<RequestResult<User.LeveledUsers>> successListener) {
        Type modelType = new TypeToken<RequestResult<User.LeveledUsers>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/listUsersByGroupInvite", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 会员层级用户列表
     *
     * @param parameters      {page=?,levelIndex=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<User>> listUsersByLevel(Hashtable<String, String> parameters,
                                                                   Response.ErrorListener errorListener,
                                                                   Response.Listener<RequestListResult<User>> successListener) {
        processListAPIParameters(parameters);

        String KEY_LEVEL_INDEX = "levelIndex";
        if (parameters == null
                || !parameters.containsKey(KEY_LEVEL_INDEX)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_LEVEL_INDEX));
        }

        Type modelType = new TypeToken<RequestListResult<User>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/listUsersByLevel", Request.Method.GET, parameters, errorListener, successListener);
    }


    /**
     * 会员搜索
     *
     * @param parameters      {page=?,levelIndex=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<User>> userSearch(Hashtable<String, String> parameters,
                                                                   Response.ErrorListener errorListener,
                                                                   Response.Listener<RequestListResult<User>> successListener) {
        processListAPIParameters(parameters);

//        String KEY_LEVEL_INDEX = "levelIndex";
//        if (parameters == null
//                || !parameters.containsKey(KEY_LEVEL_INDEX)
//                ) {
//            throw new IllegalArgumentException(getParameterDescription(KEY_LEVEL_INDEX));
//        }

        Type modelType = new TypeToken<RequestListResult<User>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/search", Request.Method.GET, parameters, errorListener, successListener);
    }
    /**
     * 下级会员搜索
     *
     * @param parameters      {page=?,levelIndex=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<User>> userDownSearch(Hashtable<String, String> parameters,
                                                                 Response.ErrorListener errorListener,
                                                                 Response.Listener<RequestListResult<User>> successListener) {
        processListAPIParameters(parameters);

//        String KEY_LEVEL_INDEX = "levelIndex";
//        if (parameters == null
//                || !parameters.containsKey(KEY_LEVEL_INDEX)
//                ) {
//            throw new IllegalArgumentException(getParameterDescription(KEY_LEVEL_INDEX));
//        }

        Type modelType = new TypeToken<RequestListResult<User>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/searchDownUser", Request.Method.GET, parameters, errorListener, successListener);
    }
    /**
     * 用户类别列表
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<UserType>> listUserType(Hashtable<String, String> parameters,
                                                                   Response.ErrorListener errorListener,
                                                                   Response.Listener<RequestListResult<UserType>> successListener) {
        processListAPIParameters(parameters);
        Type modelType = new TypeToken<RequestListResult<UserType>>() {
        }.getType();
        return buildRequestAction(modelType, "/user/listUserType", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 上传头像
     *
     * @param parameters      {filePath=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     */
    public void updateHead(Hashtable<String, String> parameters,
                           Response.ErrorListener errorListener,
                           Response.Listener<RequestResult<?>> successListener) {

        String KEY_FILEPATH = "filePath";
        if (parameters == null
                || !parameters.containsKey(KEY_FILEPATH)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_FILEPATH));
        }

        final String apiPath = "/user/updateHead";

        String urlString = BASE_URL + apiPath;
        Hashtable<String, File> imageFiles = new Hashtable<>();
        imageFiles.put("image", new File(parameters.remove(KEY_FILEPATH)));
        uploadFiles(urlString, null, imageFiles, errorListener, successListener);
    }

    private void uploadFiles(final String urlString,
                             final Hashtable<String, String> parameters,
                             final Hashtable<String, File> imageFiles,
                             final Response.ErrorListener errorListener,
                             final Response.Listener<RequestResult<?>> successListener) {
        MyApplication.bgExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Exception exception = null;
                RequestResult<?> result = null;
                try {
                    Hashtable<String, String> headers = new Hashtable<>();
                    headers.put(RequestAction.COOKIE_KEY, RequestAction.SESSID + "=" + RequestAction.getSessionId());
                    String responseJSON = BaseHttp.postFormWithFiles(urlString, headers, parameters, imageFiles);
                    result = RequestAction.GSON.fromJson(responseJSON, RequestResult.class);
                } catch (Exception e) {
                    exception = e;
                    e.printStackTrace();
                } finally {
                    final RequestResult<?> finalResult = result;
                    final Exception finalException = exception;

                    MyApplication.uiHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (finalResult != null) {
                                successListener.onResponse(finalResult);
                                checkRequestResult(finalResult);
                            } else {
                                String exceptionMessage = (finalException != null) ? finalException.getMessage() : "";
                                errorListener.onErrorResponse(new VolleyError(exceptionMessage));
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 修改用户信息
     *
     * @param user            修改后的用户对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> updateUser(User user,
                                                      Response.ErrorListener errorListener,
                                                      Response.Listener<RequestResult<?>> successListener) {

        byte[] body = null;
        try {
            body = RequestAction.GSON.toJson(user).getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String apiPath = "/user/update/" + user.id;
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(
                modelType,
                apiPath,
                Request.Method.POST,
                null,
                new RequestAction.RequestBody("application/json", body),
                true,
                errorListener,
                successListener);
    }

    /**
     * 产品列表
     *
     * @param parameters      {page=?,categoryId=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Product>> listProduct(Hashtable<String, String> parameters,
                                                                 Response.ErrorListener errorListener,
                                                                 final Response.Listener<RequestListResult<Product>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Product>>() {
        }.getType();
        return buildRequestAction(modelType, "/product/list", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 库存产品列表
     *
     * @param parameters      {page=?,categoryId=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Product.StockProduct>> listStockProduct(Hashtable<String, String> parameters,
                                                                                   Response.ErrorListener errorListener,
                                                                                   final Response.Listener<RequestListResult<Product.StockProduct>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Product.StockProduct>>() {
        }.getType();
        return buildRequestAction(modelType, "/product/listProductStock", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 搜索产品
     *
     * @param parameters      {page=?,keyword=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Product>> listProductSearch(Hashtable<String, String> parameters,
                                                                       Response.ErrorListener errorListener,
                                                                       final Response.Listener<RequestListResult<Product>> successListener) {
        processListAPIParameters(parameters);

        String KEY_KEYWORD = "keyword";
        if (parameters == null
                || !parameters.containsKey(KEY_KEYWORD)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_KEYWORD));
        }

        Type modelType = new TypeToken<RequestListResult<Product>>() {
        }.getType();
        return buildRequestAction(modelType, "/product/search", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 搜索库存产品
     *
     * @param parameters      {page=?,keyword=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Product.StockProduct>> listStockProductSearch(Hashtable<String, String> parameters,
                                                                                         Response.ErrorListener errorListener,
                                                                                         final Response.Listener<RequestListResult<Product.StockProduct>> successListener) {
        processListAPIParameters(parameters);

        String KEY_KEYWORD = "keyword";
        if (parameters == null
                || !parameters.containsKey(KEY_KEYWORD)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_KEYWORD));
        }

        Type modelType = new TypeToken<RequestListResult<Product.StockProduct>>() {
        }.getType();
        return buildRequestAction(modelType, "/product/searchProductStock", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 产品详情
     *
     * @param parameters      {id=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<Product>> getProduct(Hashtable<String, String> parameters,
                                                            Response.ErrorListener errorListener,
                                                            final Response.Listener<RequestResult<Product>> successListener) {

        String KEY_ID = "id";
        if (parameters == null
                || !parameters.containsKey(KEY_ID)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_ID));
        }
        String id = parameters.get(KEY_ID);

        Type modelType = new TypeToken<RequestResult<Product>>() {
        }.getType();
        return buildRequestAction(modelType, "/product/get/" + id, Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 提交订单
     *
     * @param orderAdd        Order.OrderAdd 对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> addOrder(Order.OrderAdd orderAdd,
                                                    Response.ErrorListener errorListener,
                                                    final Response.Listener<RequestResult<?>> successListener) {
        byte[] content = new byte[0];
        try {
            content = RequestAction.GSON.toJson(orderAdd).getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/add", Request.Method.POST, null, new RequestAction.RequestBody("application/json", content), true, errorListener, successListener);
    }
    /**
     * 提交订单
     *
     * @param orderAdd        Order.OrderAdd 对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> newOrder(Order.OrderAdd orderAdd,
                                                    Response.ErrorListener errorListener,
                                                    final Response.Listener<RequestResult<?>> successListener) {
        byte[] content = new byte[0];
        try {
            content = RequestAction.GSON.toJson(orderAdd).getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/newOrder", Request.Method.POST, null, new RequestAction.RequestBody("application/json", content), true, errorListener, successListener);
    }

    /**
     * 多张订单上传付款凭证
     *
     * @param multiOrderUpload        Order.OrderAdd 对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> payOrder(MultiOrderUpload multiOrderUpload,
                                                    Response.ErrorListener errorListener,
                                                    final Response.Listener<RequestResult<?>> successListener) {
        byte[] content = new byte[0];
        try {
            content = RequestAction.GSON.toJson(multiOrderUpload).getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/payOrder", Request.Method.POST, null, new RequestAction.RequestBody("application/json", content), true, errorListener, successListener);
    }
    /**
     * 批量取消订单
     *
     * @param parameters      {ids="1,2,3,4";isSeller（0:买家,1:卖家）}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> cancelOrder(Hashtable<String, String> parameters,
                                                       Response.ErrorListener errorListener,
                                                       final Response.Listener<RequestResult<?>> successListener) {
        String KEY_IDS = "ids";
        if (parameters == null
                || !parameters.containsKey(KEY_IDS)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_IDS));
        }

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/cancel", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 合并采购
     *
     * @param parameters      {ids="1,2,3,4"}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> mergeOrder(Hashtable<String, String> parameters,
                                                      Response.ErrorListener errorListener,
                                                      final Response.Listener<RequestResult<?>> successListener) {
        String KEY_IDS = "ids";
        if (parameters == null
                || !parameters.containsKey(KEY_IDS)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_IDS));
        }

        Type modelType = new TypeToken<RequestResult<Product>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/merge", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 合并采购
     *
     * @param parameters      {ids="1,2,3,4"}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> mergeOrderByType(Hashtable<String, String> parameters,
                                                      Response.ErrorListener errorListener,
                                                      final Response.Listener<RequestResult<?>> successListener) {
        String KEY_IDS = "ids";
        if (parameters == null
                || !parameters.containsKey(KEY_IDS)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_IDS));
        }

        Type modelType = new TypeToken<RequestResult<Product>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/mergeOrder", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 转采购订单
     *
     * @param orderId
     * @param errorListener
     * @param successListener
     * @return
     */
    public RequestAction<RequestResult<?>> transOrder(String orderId,
                                                      Response.ErrorListener errorListener,
                                                      final Response.Listener<RequestResult<?>> successListener) {
        if (TextUtils.isEmpty(orderId)) {
            throw new IllegalArgumentException(getParameterDescription(orderId));
        }

        Type modelType = new TypeToken<RequestResult<Product>>() {
        }.getType();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("ids", orderId);
        return buildRequestAction(modelType, "/order/merge", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 批量确认订单
     *
     * @param orderConfirm    确认订单时所提交的对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> confirmOrder(Order.OrderConfirm orderConfirm,
                                                        Response.ErrorListener errorListener,
                                                        final Response.Listener<RequestResult<?>> successListener) {
        if (orderConfirm == null) {
            throw new IllegalArgumentException("orderConfirm can't be null");
        }

        byte[] body = null;
        try {
            String json = orderConfirm.toJson();
            body = json.getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Type modelType = new TypeToken<RequestResult<Product>>() {
        }.getType();

        String apiPath = "/order/confirm";
        return buildRequestAction(
                modelType,
                apiPath,
                Request.Method.POST,
                null,
                new RequestAction.RequestBody("application/json", body),
                true,
                errorListener,
                successListener);
    }

    /**
     * 确认收货订单
     *
     * @param parameters      {ids="1,2,3,4"}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> confirmReceivedOrder(Hashtable<String, String> parameters,
                                                                Response.ErrorListener errorListener,
                                                                final Response.Listener<RequestResult<?>> successListener) {
        String KEY_IDS = "ids";
        if (parameters == null
                || !parameters.containsKey(KEY_IDS)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_IDS));
        }

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/confirmReceived", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 确认发货
     *
     * @param orderDeliver    确认发货所提交的对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> deliverOrder(Order.OrderDeliver orderDeliver,
                                                        Response.ErrorListener errorListener,
                                                        final Response.Listener<RequestResult<?>> successListener) {
        if (orderDeliver == null) {
            throw new IllegalArgumentException("orderDeliver can't be null");
        }

        byte[] body = null;
        try {
            String json = orderDeliver.newToJson();
            body = json.getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String apiPath = "/order/deliver";
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(
                modelType,
                apiPath,
                Request.Method.POST,
                null,
                new RequestAction.RequestBody("application/json", body),
                true,
                errorListener,
                successListener);
    }

    /**
     * 订单详情
     *
     * @param parameters      {id=?, isSeller=?}
     * @param orderType       订单类型
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<Order>> getOrder(Hashtable<String, String> parameters,
                                                        Order.OrderType orderType,
                                                        Response.ErrorListener errorListener,
                                                        final Response.Listener<RequestResult<Order>> successListener) {
        String KEY_ID = "id";
        String KEY_IS_SELLER = "isSeller";
        if (parameters == null
                || !parameters.containsKey(KEY_ID)) {
            throw new IllegalArgumentException(getParameterDescription(KEY_ID, KEY_IS_SELLER));
        }
        String id = parameters.remove(KEY_ID);
        if (!parameters.containsKey(KEY_IS_SELLER)) {
            parameters.put(KEY_IS_SELLER, "0");
        }

        if (orderType == null) {
            orderType = Order.OrderType.OrderListTypeHandle;
        }

        String KEY_STATUS = "status";
        parameters.put(KEY_STATUS, String.valueOf(orderType.getIntValue()));

        Type modelType = new TypeToken<RequestResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/get/" + id, Request.Method.GET, parameters, errorListener, successListener);
    }


    /**
     * 上传付款凭证
     *
     * @param parameters      {filePath=?,orderId=?,payMoney=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     */
    public void updatePayImage(Hashtable<String, String> parameters,
                               Response.ErrorListener errorListener,
                               Response.Listener<RequestResult<?>> successListener) {

        String KEY_FILEPATH = "filePath";
        String KEY_ORDER_ID = "orderId";
        if (parameters == null
                || !parameters.containsKey(KEY_FILEPATH)
                || !parameters.containsKey(KEY_ORDER_ID)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_FILEPATH, KEY_ORDER_ID));
        }

        String orderId = parameters.remove(KEY_ORDER_ID);
        final String apiPath = "/order/updatePayImage/" + orderId;

        String urlString = BASE_URL + apiPath;
        Hashtable<String, File> imageFiles = new Hashtable<>();
        imageFiles.put("file", new File(parameters.remove(KEY_FILEPATH)));

        uploadFiles(urlString, parameters, imageFiles, errorListener, successListener);
    }

    /**
     * 上传单个付款凭证
     *
     * @param parameters      {filePath=?,orderId=?,payMoney=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     */
    public void uploadPayImage(Hashtable<String, String> parameters,
                               Response.ErrorListener errorListener,
                               Response.Listener<RequestResult<?>> successListener) {

        String KEY_FILEPATH = "filePath";
        String KEY_ORDER_ID = "orderId";
        if (parameters == null
                || !parameters.containsKey(KEY_FILEPATH)
                || !parameters.containsKey(KEY_ORDER_ID)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_FILEPATH, KEY_ORDER_ID));
        }

        String orderId = parameters.remove(KEY_ORDER_ID);
        final String apiPath = "/order/uploadPayImage/" + orderId;

        String urlString = BASE_URL + apiPath;
        Hashtable<String, File> imageFiles = new Hashtable<>();
        imageFiles.put("file", new File(parameters.remove(KEY_FILEPATH)));

        uploadFiles(urlString, parameters, imageFiles, errorListener, successListener);
    }

    /**
     * 下单界面上传单个付款凭证
     *
     * @param parameters      {filePath=?,orderId=?,payMoney=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     */
    public void orderPayImage(Hashtable<String, String> parameters,
                              Response.ErrorListener errorListener,
                              Response.Listener<RequestResult<?>> successListener) {

        String KEY_FILEPATH = "filePath";
//        String KEY_ORDER_ID = "orderId";
        if (parameters == null|| !parameters.containsKey(KEY_FILEPATH)) {
            throw new IllegalArgumentException(getParameterDescription(KEY_FILEPATH));
        }

//        String orderId = parameters.remove(KEY_ORDER_ID);
        final String apiPath = "/order/payImage";

        String urlString = BASE_URL + apiPath;
        Hashtable<String, File> imageFiles = new Hashtable<>();
        imageFiles.put("file", new File(parameters.remove(KEY_FILEPATH)));

        uploadFiles(urlString, parameters, imageFiles, errorListener, successListener);
    }

    /**
     * 买家:待处理
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderByBuyerWaitingHandle(Hashtable<String, String> parameters,
                                                                                        Response.ErrorListener errorListener,
                                                                                        final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listBuyerWaitingHandle", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 买家:待发货
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderByBuyerWaitingDeliver(Hashtable<String, String> parameters,
                                                                                         Response.ErrorListener errorListener,
                                                                                         final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listBuyerWaitingDeliver", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 买家:待付款
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderByBuyerWaitingPay(Hashtable<String, String> parameters,
                                                                                     Response.ErrorListener errorListener,
                                                                                     final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listBuyerWaitingPay", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 买家:已发货
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderByBuyerDelivered(Hashtable<String, String> parameters,
                                                                                    Response.ErrorListener errorListener,
                                                                                    final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listBuyerDelivered", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 买家:已收货
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderByBuyerReceived(Hashtable<String, String> parameters,
                                                                                   Response.ErrorListener errorListener,
                                                                                   final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listBuyerReceived", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 卖家:待处理
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderBySellerWaitingHandle(Hashtable<String, String> parameters,
                                                                                         Response.ErrorListener errorListener,
                                                                                         final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listSellerWaitingHandle", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 卖家:待发货
     *
     * @param parameters      {page=?}
     *                        isMerge（1：已转采购；0：未转采购）
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderBySellerWaitingDeliver(Hashtable<String, String> parameters,
                                                                                          Response.ErrorListener errorListener,
                                                                                          final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listSellerWaitingDeliver", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 卖家:待付款
     *
     * @param parameters      {page=?}
     *                        isPay （1：已上传；0：未上传）
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderBySellerWaitingPay(Hashtable<String, String> parameters,
                                                                                      Response.ErrorListener errorListener,
                                                                                      final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listSellerWaitingPay", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 卖家:已发货
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderBySellerDelivered(Hashtable<String, String> parameters,
                                                                                     Response.ErrorListener errorListener,
                                                                                     final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listSellerDelivered", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 卖家:已收货
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> listOrderBySellerReceived(Hashtable<String, String> parameters,
                                                                                    Response.ErrorListener errorListener,
                                                                                    final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/listSellerReceived", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 订单搜索
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<MyOrderRequestListResult<Order>> orderSearch(Hashtable<String, String> parameters,
                                                                      Response.ErrorListener errorListener,
                                                                      final Response.Listener<MyOrderRequestListResult<Order>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<MyOrderRequestListResult<Order>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/search", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 卖家:我的销售订单列表、我的采购列表
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Order.OrderTrade>> listSalesOrder(Order.OrderTradeType tradeType, Hashtable<String, String> parameters,
                                                                             Response.ErrorListener errorListener,
                                                                             final Response.Listener<RequestListResult<Order.OrderTrade>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Order.OrderTrade>>() {
        }.getType();
        String apiPath = (tradeType == Order.OrderTradeType.Sell) ? "/order/listSalesOrder" : "/order/listPurchasOrder";
        return buildRequestAction(modelType, apiPath, Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 销售订单详情
     *
     * @param parameters      {id=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<Order.OrderTrade>> getSalesOrder(Order.OrderTradeType tradeType, Hashtable<String, String> parameters,
                                                                        Response.ErrorListener errorListener,
                                                                        Response.Listener<RequestResult<Order.OrderTrade>> successListener) {

        String KEY_ID = "id";
        if (parameters == null
                || !parameters.containsKey(KEY_ID)) {
            throw new IllegalArgumentException(getParameterDescription(KEY_ID));
        }
        String id = parameters.remove(KEY_ID);

        Type modelType = new TypeToken<RequestResult<Order.OrderTrade>>() {
        }.getType();
        String apiPath = ((tradeType == Order.OrderTradeType.Sell) ? "/order/getSalesOrder/" : "/order/getPurchasOrder/") + id;
        return buildRequestAction(modelType, apiPath, Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 添加或更新我的销售订单、我的采购订单
     *
     * @param orderTradeAdd   对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> addOrUpdateSalesOrder(Order.OrderTradeType tradeType, Order.OrderTradeAdd orderTradeAdd,
                                                                 Response.ErrorListener errorListener,
                                                                 Response.Listener<RequestResult<?>> successListener) {

        byte[] body = null;
        try {
            body = orderTradeAdd.toJson().getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String apiPath;
        if (tradeType == Order.OrderTradeType.Sell) {
            apiPath = TextUtils.isEmpty(orderTradeAdd.id) ? "/order/salesOrderAdd" : "/order/salesOrderUpdate/" + orderTradeAdd.id;
        } else {
            apiPath = TextUtils.isEmpty(orderTradeAdd.id) ? "/order/purchasOrderAdd" : "/order/purchasOrderUpdate/" + orderTradeAdd.id;
        }
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(
                modelType,
                apiPath,
                Request.Method.POST,
                null,
                new RequestAction.RequestBody("application/json", body),
                true,
                errorListener,
                successListener);
    }


    /**
     * 批量取消我的销售订单、我的采购订单
     *
     * @param parameters      {ids="1,2,3,4"}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> cancelSalesOrder(Order.OrderTradeType tradeType, Hashtable<String, String> parameters,
                                                            Response.ErrorListener errorListener,
                                                            final Response.Listener<RequestResult<?>> successListener) {
        String KEY_IDS = "ids";
        if (parameters == null
                || !parameters.containsKey(KEY_IDS)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_IDS));
        }

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        String apiPath = (tradeType == Order.OrderTradeType.Sell) ? "/order/salesOrderCancel" : "/order/purchasOrderCancel";
        return buildRequestAction(modelType, apiPath, Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 获取通知列表
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Notice>> listNotice(Hashtable<String, String> parameters,
                                                               Response.ErrorListener errorListener,
                                                               final Response.Listener<RequestListResult<Notice>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Notice>>() {
        }.getType();
        return buildRequestAction(modelType, "/notice/list", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 获取通知详情
     *
     * @param parameters      {id=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<Notice>> getNotice(Hashtable<String, String> parameters,
                                                          Response.ErrorListener errorListener,
                                                          final Response.Listener<RequestResult<Notice>> successListener) {

        String KEY_ID = "id";
        if (parameters == null
                || !parameters.containsKey(KEY_ID)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_ID));
        }
        String id = parameters.get(KEY_ID);

        Type modelType = new TypeToken<RequestResult<Notice>>() {
        }.getType();
        return buildRequestAction(modelType, "/notice/get/" + id, Request.Method.GET, null, errorListener, successListener);
    }

    /**
     * 获取收货地址列表
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Address>> listAddress(Hashtable<String, String> parameters,
                                                                 Response.ErrorListener errorListener,
                                                                 final Response.Listener<RequestListResult<Address>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Address>>() {
        }.getType();
        return buildRequestAction(modelType, "/address/list", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 添加收货地址
     *
     * @param address         Address 对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> addAddress(Address address,
                                                      Response.ErrorListener errorListener,
                                                      final Response.Listener<RequestResult<?>> successListener) {
        return this.postAddress("/address/add", Request.Method.POST, address, errorListener, successListener);
    }

    /**
     * 修改收货地址
     *
     * @param address         Address 对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> updateAddress(Address address,
                                                         Response.ErrorListener errorListener,
                                                         final Response.Listener<RequestResult<?>> successListener) {
        if (address == null
                || !address.isValidate()
                ) {
            throw new IllegalArgumentException("address is invalidate");
        }
        String id = address.id;
        return this.postAddress("/address/update/" + id, Request.Method.PUT, address, errorListener, successListener);
    }

    /**
     * 通用put地址对象的方法
     *
     * @param apiPath         API相对路径
     * @param address         Address 对象
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    private RequestAction<RequestResult<?>> postAddress(String apiPath,
                                                        int method,
                                                        Address address,
                                                        Response.ErrorListener errorListener,
                                                        final Response.Listener<RequestResult<?>> successListener) {
        byte[] content = new byte[0];
        try {
            content = RequestAction.GSON.toJson(address).getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, apiPath, method, null, new RequestAction.RequestBody("application/json", content), true, errorListener, successListener);
    }

    /**
     * 删除收货地址
     *
     * @param parameters      {id=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> deleteAddress(Hashtable<String, String> parameters,
                                                         Response.ErrorListener errorListener,
                                                         final Response.Listener<RequestResult<?>> successListener) {
        String KEY_ID = "id";
        if (parameters == null
                || !parameters.containsKey(KEY_ID)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_ID));
        }
        String id = parameters.get(KEY_ID);

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/address/delete/" + id, Request.Method.DELETE, parameters, errorListener, successListener);
    }

    /**
     * 课件分类列表
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Category>> listCourseCategories(Hashtable<String, String> parameters,
                                                                           Response.ErrorListener errorListener,
                                                                           final Response.Listener<RequestListResult<Category>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Category>>() {
        }.getType();
        return buildRequestAction(modelType, "/category/listCourseCategories", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 产品分类列表
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Category>> listProductCategories(Hashtable<String, String> parameters,
                                                                            Response.ErrorListener errorListener,
                                                                            final Response.Listener<RequestListResult<Category>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Category>>() {
        }.getType();
        return buildRequestAction(modelType, "/category/listProductCategories", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 课件列表
     *
     * @param parameters      {page=?,categoryId=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Course>> listCourse(Hashtable<String, String> parameters,
                                                               Response.ErrorListener errorListener,
                                                               final Response.Listener<RequestListResult<Course>> successListener) {
        processListAPIParameters(parameters);

        String CATEGORY_ID = "categoryId";
        if (parameters == null
                || !parameters.containsKey(CATEGORY_ID)
                ) {
            throw new IllegalArgumentException(getParameterDescription(CATEGORY_ID));
        }

        Type modelType = new TypeToken<RequestListResult<Course>>() {
        }.getType();
        return buildRequestAction(modelType, "/course/list", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 搜索课件
     *
     * @param parameters      {page=?,keyword=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Course>> listCourseSearch(Hashtable<String, String> parameters,
                                                                     Response.ErrorListener errorListener,
                                                                     final Response.Listener<RequestListResult<Course>> successListener) {
        processListAPIParameters(parameters);

        String KEY_KEYWORD = "keyword";
        if (parameters == null
                || !parameters.containsKey(KEY_KEYWORD)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_KEYWORD));
        }

        Type modelType = new TypeToken<RequestListResult<Course>>() {
        }.getType();
        return buildRequestAction(modelType, "/course/search", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 课件详情
     *
     * @param parameters      {id=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<Course>> getCourse(Hashtable<String, String> parameters,
                                                          Response.ErrorListener errorListener,
                                                          final Response.Listener<RequestResult<Course>> successListener) {
        String KEY_ID = "id";
        if (parameters == null
                || !parameters.containsKey(KEY_ID)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_ID));
        }
        String id = parameters.get(KEY_ID);

        Type modelType = new TypeToken<RequestResult<Course>>() {
        }.getType();
        return buildRequestAction(modelType, "/course/get/" + id, Request.Method.GET, null, errorListener, successListener);
    }

    /**
     * 删除群成员
     * @param parameters groupid userid
     * @param errorListener 错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> removeMember(Hashtable<String, String> parameters,
                                                          Response.ErrorListener errorListener,
                                                          final Response.Listener<RequestResult<?>> successListener){

        Type modelType = new TypeToken<RequestResult<?>>(){}.getType();
        return buildRequestAction(modelType, "/group/removeMember", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 删除群
     * @param parameters groupid
     * @param errorListener 错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> removeGroup(Hashtable<String, String> parameters,
                                                        Response.ErrorListener errorListener,
                                                        final Response.Listener<RequestResult<?>> successListener){

        Type modelType = new TypeToken<RequestResult<?>>(){}.getType();
        return buildRequestAction(modelType, "/group/removeGroup", Request.Method.POST, parameters, errorListener, successListener);
    }
    /**
     * 获取组群列表
     *
     * @param parameters      {page=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<Group>> listGroup(Hashtable<String, String> parameters,
                                                             Response.ErrorListener errorListener,
                                                             final Response.Listener<RequestListResult<Group>> successListener) {
        processListAPIParameters(parameters);

        Type modelType = new TypeToken<RequestListResult<Group>>() {
        }.getType();
        return buildRequestAction(modelType, "/group/list", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 按用户类别建群
     *
     * @param parameters      {name=?,summary=?,userTypeId=?,isAllowInvite=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> addByUserType(Hashtable<String, String> parameters,
                                                         Response.ErrorListener errorListener,
                                                         final Response.Listener<RequestResult<?>> successListener) {
        String KEY_NAME = "name";
        String KEY_SUMMARY = "summary";
        String KEY_USER_TYPE_ID = "userTypeId";
        String KEY_IS_ALLOW_INVITE = "isAllowInvite";
        if (parameters == null
                || !parameters.containsKey(KEY_NAME)
                || !parameters.containsKey(KEY_SUMMARY)
                || !parameters.containsKey(KEY_USER_TYPE_ID)
                || !parameters.containsKey(KEY_IS_ALLOW_INVITE)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_NAME, KEY_SUMMARY, KEY_USER_TYPE_ID, KEY_IS_ALLOW_INVITE));
        }

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/group/addByUserType", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 创建普通组群
     *
     * @param parameters      {name=?,summary=?,isAllowInvite=?}
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> addGroup(Hashtable<String, String> parameters,
                                                    Response.ErrorListener errorListener,
                                                    final Response.Listener<RequestResult<?>> successListener) {
        String KEY_NAME = "name";
        String KEY_SUMMARY = "summary";
        String KEY_IS_ALLOW_INVITE = "isAllowInvite";
        if (parameters == null
                || !parameters.containsKey(KEY_NAME)
                || !parameters.containsKey(KEY_SUMMARY)
                || !parameters.containsKey(KEY_IS_ALLOW_INVITE)
                ) {
            throw new IllegalArgumentException(getParameterDescription(KEY_NAME, KEY_SUMMARY, KEY_IS_ALLOW_INVITE));
        }

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/group/add", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 向自己的群加人
     *
     * @param hxGroupId       环信组群id
     * @param userIds         用户id
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> addMember(String hxGroupId,
                                                     Set<String> userIds,
                                                     Response.ErrorListener errorListener,
                                                     final Response.Listener<RequestResult<?>> successListener) {
        if (TextUtils.isEmpty(hxGroupId)) {
            throw new IllegalArgumentException("empty hxGroupId");
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("empty userIds");
        }
        StringBuilder strb = new StringBuilder();
        for (String userId : userIds) {
            strb.append(userId).append(",");
        }
        int len = strb.length();
        strb.delete(len - 1, len);

        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("hxGroupId", hxGroupId);
        parameters.put("userId", strb + "");

        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/group/addMember", Request.Method.POST, parameters, errorListener, successListener);
    }


    /**
     * 确认付款
     *
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> orderConfirmMoney(Order order,
                                                             Response.ErrorListener errorListener,
                                                             final Response.Listener<RequestResult<?>> successListener) {


        Type modelType = new TypeToken<RequestResult<?>>(){}.getType();
        return buildRequestAction(modelType, "/order/confirmMoney/"+order.id, Request.Method.POST,null, errorListener, successListener);
    }

    /**
     * 获取融云token
     *
     * @param errorListener 错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> getRongyunToken(
                                                             Response.ErrorListener errorListener,
                                                             final Response.Listener<RequestResult<?>> successListener){


        Type modelType = new TypeToken<RequestResult<?>>(){}.getType();
        return buildRequestAction(modelType, "/rongyun/getToken", Request.Method.GET,null, errorListener, successListener);
    }


    /**
     * 群成员
     * @param parameters {pageSize=?,page=?,id=?}
     * @param errorListener 错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<GroupDetail>> groupListUsers(Hashtable<String, String> parameters,
                                                    Response.ErrorListener errorListener,
                                                    final Response.Listener<RequestResult<GroupDetail>> successListener){
//        String KEY_NAME = "name";
//        String KEY_SUMMARY = "summary";
//        String KEY_IS_ALLOW_INVITE = "isAllowInvite";
//        if(parameters == null
//                || !parameters.containsKey(KEY_NAME)
//                || !parameters.containsKey(KEY_SUMMARY)
//                || !parameters.containsKey(KEY_IS_ALLOW_INVITE)
//                ){
//            throw new IllegalArgumentException(getParameterDescription(KEY_NAME, KEY_SUMMARY, KEY_IS_ALLOW_INVITE));
//        }

        Type modelType = new TypeToken<RequestResult<GroupDetail>>(){}.getType();
        return buildRequestAction(modelType, "/group/listUsers", Request.Method.GET, parameters, errorListener, successListener);
    }

    /**
     * 删除付款凭证
     *
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> deletePayImage(Hashtable<String, String> parameters,
                                                          Response.ErrorListener errorListener,
                                                          final Response.Listener<RequestResult<?>> successListener) {


        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/deletePayImage", Request.Method.POST, parameters, errorListener, successListener);
    }

    /**
     * 删除付款凭证
     *
     * @param errorListener   错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestResult<?>> confirmUploadPay(Hashtable<String, String> parameters,
                                                            Response.ErrorListener errorListener,
                                                            final Response.Listener<RequestResult<?>> successListener) {


        Type modelType = new TypeToken<RequestResult<?>>() {
        }.getType();
        return buildRequestAction(modelType, "/order/confirmUploadPay", Request.Method.POST, parameters, errorListener, successListener);
    }


    /**
     * 测试登录
     * @param parameters {pageSize=?,page=?,id=?}
     * @param errorListener 错误callback
     * @param successListener 成功callback
     * @return RequestAction
     */
    public RequestAction<RequestListResult<?>> loginflag(Hashtable<String, String> parameters,
                                                                    Response.ErrorListener errorListener,
                                                                    final Response.Listener<RequestListResult<?>> successListener){
//        String KEY_NAME = "name";
//        String KEY_SUMMARY = "summary";
//        String KEY_IS_ALLOW_INVITE = "isAllowInvite";
//        if(parameters == null
//                || !parameters.containsKey(KEY_NAME)
//                || !parameters.containsKey(KEY_SUMMARY)
//                || !parameters.containsKey(KEY_IS_ALLOW_INVITE)
//                ){
//            throw new IllegalArgumentException(getParameterDescription(KEY_NAME, KEY_SUMMARY, KEY_IS_ALLOW_INVITE));
//        }

        Type modelType = new TypeToken<RequestListResult<?>>(){}.getType();
        return buildRequestAction(modelType, "/initdata/loginflag", Request.Method.GET, parameters, errorListener, successListener);
    }
}
