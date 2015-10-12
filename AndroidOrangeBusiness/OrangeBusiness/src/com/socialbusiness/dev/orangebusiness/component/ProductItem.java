package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.graphics.Matrix.ScaleToFit;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

public class ProductItem extends LinearLayout{

	private NetworkImageView mImageView;
	private TextView mName;
	private TextView mPrice;
	private TextView mAddBtn;
	
	private Product mProduct;
	
	public ProductItem(Context context) {
		super(context);
		initView();
	}
	
	public ProductItem(Context context, AttributeSet attr) {
		super(context, attr);
		initView();
	}
	
	private void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.view_product_item, this);
		findView(view);
		registerListener();
	}
	
	private void findView(View view) {
		mImageView = (NetworkImageView) view.findViewById(R.id.view_product_item_image);
		mName = (TextView) view.findViewById(R.id.view_product_item_title);
		mPrice = (TextView) view.findViewById(R.id.view_product_item_price);
		mAddBtn = (TextView) view.findViewById(R.id.view_product_item_add_to_shopping_cart);
		
		mImageView.setErrorImageResId(R.drawable.ic_default_mid);
		mImageView.setDefaultImageResId(R.drawable.ic_default_mid);
		mImageView.setScaleType(ScaleType.CENTER_CROP);
	}
	
	private void registerListener() {
		mAddBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ShopCartDbHelper helper = new ShopCartDbHelper(getContext());
				helper.addShopCart(mProduct);
				ToastUtil.show(getContext(), "加入购物车成功");
			}
		});
	}
	
	public void setProductItemData(Product product, boolean isStockProduct) {
		this.mProduct = product;
		if (product != null) {
			mImageView.setImageUrl(APIManager.toAbsoluteUrl(product.getCoverImage()), 
					APIManager.getInstance(getContext()).getImageLoader());
			mName.setText(product.name);
			mPrice.setText(getContext().getResources().getString(R.string.yuan_symbol) + " " + product.price);
            if (isStockProduct) {
                mAddBtn.setBackground(null);
                mAddBtn.setText("库存数量：" + product.amount);
                mAddBtn.setTextColor(getContext().getResources().getColor(R.color.text_color_dark));
                mAddBtn.setOnClickListener(null);
            } else if (product.canBuy == 0) {
				mAddBtn.setVisibility(View.GONE);
			}
		}
	}
}
