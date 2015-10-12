package com.socialbusiness.dev.orangebusiness.api;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.model.User;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

/**
 * User: Yaotian Leung
 * Date: 2013-12-27
 * Time: 20:05
 */
public class RequestAction<T> extends Request<T> {
    public static final String TAG = "RequestAction";

    public static final String SET_COOKIE_KEY = "Set-Cookie";
    public static final String COOKIE_KEY = "Cookie";
    public static final String SESSID = "__auth";
    private static String sessionId;
    public static final Gson GSON;

    static {
        IntegerSerializer intTypeAdapter = new IntegerSerializer();
        DateSerializer dateTypeAdapter = new DateSerializer();

        GSON = new GsonBuilder()
                .registerTypeAdapter(Date.class, dateTypeAdapter)
                .registerTypeAdapter(Integer.class, intTypeAdapter)
                .registerTypeAdapter(int.class, intTypeAdapter)
                .create();
    }

    private final String apiPath;
    private final int httpMethod;
    private final Class<T> clazz;
    private final Response.Listener<T> successListener;
    private final Type type;
    private final RequestBody requestBody;
    private final LinkedList<RequestParam> requestParams = new LinkedList<RequestParam>();

    public RequestAction(String api, int httpMethod, Class<T> clazz, Type type, RequestBody body,
                         Response.ErrorListener errorListener, Response.Listener<T> successListener) {
        super(httpMethod, APIManager.BASE_URL + api, errorListener);
        this.apiPath = api;
        this.httpMethod = httpMethod;
        this.clazz = clazz;
        this.type = type;
        this.requestBody = body;
        this.successListener = successListener;

        setRetryPolicy(new DefaultRetryPolicy(APIManager.REQUEST_TIMEOUT_SECONDS * 1000, 0, 0));
    }

    @Override
    protected void deliverResponse(T response) {
        if (successListener != null) {
            successListener.onResponse(response);
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
        Response<T> dataResponse;
        String json;
        try {
            json = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
            if (Constant.DEBUG_MODE) {
                Log.d(TAG, "json:\n" + json);
            }
            if (type == null) {
                dataResponse = Response.success(GSON.fromJson(json, clazz),
                        HttpHeaderParser.parseCacheHeaders(networkResponse));
            } else {
                dataResponse = (Response<T>) Response.success(GSON.fromJson(json, type),
                        HttpHeaderParser.parseCacheHeaders(networkResponse));
            }
            if (dataResponse != null
                    && dataResponse.result != null
                    && (dataResponse.result instanceof RequestResult)) {
                RequestResult data = (RequestResult) dataResponse.result;
                if (data.data != null
                        && (data.data instanceof User)) {
                    String cookie = getSessionCookie(networkResponse.headers);
                    if (!TextUtils.isEmpty(cookie)) {
                        setSessionId(cookie);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "parseNetworkResponse:", e);
            dataResponse = Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "parseNetworkResponse:", e);
            dataResponse = Response.error(new ParseError(e));
        } catch (Exception e) {
            Log.e(TAG, "parseNetworkResponse:", e);
            dataResponse = Response.error(new ParseError(e));
        }

        return dataResponse;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Hashtable<String, String> params = new Hashtable<String, String>();
        for (RequestParam param : requestParams) {
            params.put(param.getKey(), param.getValue());
        }
        return params;
    }

    @Override
    public String getUrl() {
        String urlString = super.getUrl();
        if (this.httpMethod == Method.GET) {
            boolean isFirst = true;
            for (RequestParam p : requestParams) {
                if (isFirst) {
                    isFirst = false;
                    urlString += "?";
                } else {
                    urlString += "&";
                }
                try {
                    urlString += URLEncoder.encode(p.getKey(), "UTF-8");
                    urlString += "=";
                    urlString += URLEncoder.encode(p.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if (urlString.endsWith("&")) {
                urlString = urlString.substring(0, urlString.length() - 1);
            }
        }
        return urlString;
    }

    @Override
    public String getBodyContentType() {
        if (requestBody != null) {
            return requestBody.contentType;
        }
        return super.getBodyContentType();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (this.requestBody != null) {
            return this.requestBody.content;
        }
        return super.getBody();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new Hashtable<>();
        headers.put("Accept", "application/json");

        if (!TextUtils.isEmpty(sessionId)) {
            StringBuilder builder = new StringBuilder();
            builder.append(SESSID);
            builder.append("=");
            builder.append(sessionId);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }

        return headers;
    }

    public void addRequestParams(RequestParam... param) {
        if (param != null) {
            this.requestParams.addAll(Arrays.asList(param));
        }
    }

    public LinkedList<RequestParam> getRequestParams() {
        return requestParams;
    }

    public static String getSessionId() {
        return sessionId;
    }

    public static void setSessionId(String value) {
        synchronized (RequestAction.class) {
            sessionId = value;
        }
    }

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     *
     * @param headers Response Headers.
     */
    private static String getSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(SESSID)) {
            String cookieValue = headers.get(SET_COOKIE_KEY);
            if (cookieValue.length() > 0) {
                String[] splitCookie = cookieValue.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookieValue = splitSessionId[1];
                return cookieValue;
            }
        }
        return "";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getUrl());
        sb.append("==");
        try {
            if (getBody() != null)
                sb.append(new String(getBody()));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        boolean isFirst = true;
//        for (RequestParam reqp : requestParams) {
//            if (isFirst) {
//                isFirst = false;
//                sb.append('?');
//            } else {
//                sb.append('&');
//            }
//            sb.append(reqp.toString());
//        }
        return sb.toString();
    }

    public static class RequestBody {
        public final String contentType;
        private final byte[] content;

        public RequestBody(String contentType, byte[] content) {
            this.contentType = contentType;
            this.content = content;
        }
    }
}
