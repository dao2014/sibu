package com.socialbusiness.dev.orangebusiness.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * 产品
 * Created by liangyaotian on 10/18/14.
 */
public class Product extends BaseModel{

    public static enum ProductType {
        KWMProductTypeNormal(0),
        /**我的库存产品*/
        KWMProductTypeStock(1);

        private int mIntValue;
        private ProductType(int intValue) {
            mIntValue = intValue;
        }

        public int getIntValue() {
            return mIntValue;
        }
    }

    public String name;
    public String video;
    public String desc;
    /// [String]
    public ArrayList<String> images;
    /// 产品属性Map--"属性名":"属性值"
    public ArrayList<Hashtable<String, String>> properties = new ArrayList<>();
    /// 产品详情条目Map--"条目名":"条目值"
    public ArrayList<Hashtable<String, String>> items = new ArrayList<>();
    public String code;
    public float price;
    public User seller;
    public int boxNum;
    public String unit;
    /**
     * 是否允许加入购物车，1代表可以，0代表不可以
     */
    public int canBuy;
    public String shareText;
    
    public int amount;	/*-- 产品数量，与API无关 --*/
    
    /**
     * 获取产品的封面图片url
     * @return
     */
    public String getCoverImage() {
    	if(images == null || images.isEmpty()) {
    		return "";
    	}
    	return images.get(0);
    }

    /**
     * 库存产品
     */
    public static class StockProduct extends Product {
        @SerializedName("stock")
        public int stockCount;
    }
}
