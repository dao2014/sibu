package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.math.BigDecimal;
import java.util.HashMap;

public class OrderDetailItem extends RelativeLayout {

	private NetworkImageView mProductImage;
	private KWAutoScrollTextView mProductName;
	private TextView mProductPrice;
	private TextView mProductNum;
	private TextView mProductSum;
	
	public OrderDetailItem(Context context) {
		super(context);
		initView();
	}
	
	public OrderDetailItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		findViews(inflater.inflate(R.layout.order_detail_item, this));
	}
	
	private void findViews(View view) {
		mProductImage = (NetworkImageView) view.findViewById(R.id.order_detail_item_product_image);
		mProductName = (KWAutoScrollTextView) view.findViewById(R.id.order_detail_item_product_name);
		mProductPrice = (TextView) view.findViewById(R.id.order_detail_item_product_price);
		mProductNum = (TextView) view.findViewById(R.id.order_detail_item_product_num);
		mProductSum = (TextView) view.findViewById(R.id.order_detail_item_product_total_money);
		
		mProductImage.setErrorImageResId(R.drawable.default_pic);
		mProductImage.setDefaultImageResId(R.drawable.default_pic);
	}

	public void setProductValue(HashMap<String, Object> values) {
		if (values == null) {
			return;
		}
		Product product = (Product) values.get("product");
		Integer quantity = (Integer) values.get("quantity");
		if (product != null && quantity != null) {
			mProductImage.setImageUrl(APIManager.toAbsoluteUrl(product.getCoverImage()), 
					APIManager.getInstance(getContext()).getImageLoader());
			mProductName.setText(product.name);
			mProductPrice.setText(getResources().getString(R.string.yuan_symbol) 
					+ StringUtil.getStringFromFloatKeep2(product.price));
			mProductNum.setText((int)quantity + "");
			mProductSum.setText(StringUtil.getStringFromFloatKeep2(
					new BigDecimal(product.price).multiply(new BigDecimal(quantity)).doubleValue()));
		}
	}
	
}
