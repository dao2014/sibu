package com.socialbusiness.dev.orangebusiness.model;

import com.google.gson.annotations.SerializedName;

/**
 * 广告
 * Created by liangyaotian on 2014-09-16.
 */
public class Ad extends BaseModel {
    @SerializedName("company_id")
    public int companyId;
    @SerializedName("image")
    public String imageUrl;
    @SerializedName("name")
    public String title;
}
