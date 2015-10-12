package com.socialbusiness.dev.orangebusiness.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by liangyaotian on 2014-09-16.
 */
public class RequestListResult <T> implements Serializable {
    public int code;
    public String message;
    public int page;
    public int pageSize;
    public int totalCount;
    public ArrayList<T> data;
}
