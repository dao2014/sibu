package com.socialbusiness.dev.orangebusiness.model;

import android.text.TextUtils;

/**
 * 购物车里的物品
 * Created by liangyaotian on 10/18/14.
 */
public class ShopCartItem extends Product {
	
	public int quantity;
	public String image;
    public float myPrice=0.0f;
	
	 /**
     * 获取产品的封面图片url
     * @return
     */
    public String getCoverImage() {
    	if(!TextUtils.isEmpty(image)) {
    		return image;
    	}
    	return super.getCoverImage();
    }

    @Override
    public String toString() {
        return "ShopCartItem{" +
                "quantity=" + quantity +
                ", image='" + image + '\'' +
                ", myPrice=" + myPrice +
                '}';
    }
}
