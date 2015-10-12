package com.socialbusiness.dev.orangebusiness.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by liangyaotian on 2014-09-16.
 */
public abstract class BaseModel implements Serializable{
    @SerializedName("id")
    public String id;

    public boolean isValidate(){
        return !TextUtils.isEmpty(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseModel)) return false;

        BaseModel baseModel = (BaseModel) o;

        if (id != null ? !id.equals(baseModel.id) : baseModel.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
