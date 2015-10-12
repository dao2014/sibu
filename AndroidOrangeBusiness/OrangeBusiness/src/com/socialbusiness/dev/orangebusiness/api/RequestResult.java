package com.socialbusiness.dev.orangebusiness.api;

import java.io.Serializable;

public class RequestResult<T> implements Serializable {
    /**0成功，1未登录，2其他错误*/
    public static final int RESULT_SUCESS=0;
    public int code;
    public String message;
    public T data;

    @Override
    public String toString() {
        return "RequestResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
