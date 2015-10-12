package com.socialbusiness.dev.orangebusiness.api;

import android.text.TextUtils;

/**
 * User: Yaotian Leung
 * Date: 2014-01-02
 * Time: 10:53
 */
public class RequestParam {
    public static final String KEY_METHOD = "method";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_TOKEN = "token";

    private final String key;
    private final String value;

    public RequestParam(){
        this(KEY_TIMESTAMP, System.currentTimeMillis() + "");
    }

    public RequestParam(String key, String value) {
        if(TextUtils.isEmpty(key)){
            throw new IllegalArgumentException("param key cant be empty!");
        }
        if(value == null){
            value = "";
        }

        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
