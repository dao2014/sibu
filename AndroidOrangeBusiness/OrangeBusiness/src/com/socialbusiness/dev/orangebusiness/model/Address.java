package com.socialbusiness.dev.orangebusiness.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 收货地址
 * Created by liangyaotian on 10/18/14.
 */
public class Address extends BaseModel implements Serializable {

    public boolean isDefault;
    public String name;
    public String phone;
    public String postCode;
    public String province;
    public String city;
    public String district;
    public String detail;

    public static class AddressProvince {
        @SerializedName("name")
        public String name;
        @SerializedName("cities")
        public ArrayList<AddressCity> cities;
    }
    
    public static class AddressCity {
    	@SerializedName("name")
    	public String name;
    	@SerializedName("areas")
    	public ArrayList<AddressArea> areas; 
    }

    public static class AddressArea {
    	@SerializedName("name")
    	public String name;
    }
}
