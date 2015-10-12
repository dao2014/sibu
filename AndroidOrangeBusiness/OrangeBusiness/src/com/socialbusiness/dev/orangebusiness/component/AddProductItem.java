package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.content.res.ColorStateList;
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

public class AddProductItem extends RelativeLayout {

	private NetworkImageView mProductImage;
	private TextView mProductName;
	private TextView mProductPrice;
	private TextView mAddBtn;
	
	private OnAddClickListener addListener;
	private Product mProduct;
	private boolean mIsAdd;
	
	public AddProductItem(Context context) {
		super(context);
		initView();
	}
	
	public AddProductItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		findViews(inflater.inflate(R.layout.view_add_product_item, this));
	}
	
	private void findViews(View view) {
		mProductImage = (NetworkImageView) view.findViewById(R.id.view_add_product_item_image);
		mProductName = (TextView) view.findViewById(R.id.view_add_product_item_title);
		mProductPrice = (TextView) view.findViewById(R.id.view_add_product_item_price);
		mAddBtn = (TextView) view.findViewById(R.id.view_add_product_item_add_product);
		
		mProductImage.setErrorImageResId(R.drawable.ic_default_mid);
		mProductImage.setDefaultImageResId(R.drawable.ic_default_mid);
		
		ColorStateList csl = (ColorStateList) getResources().getColorStateList(R.drawable.sl_add_product_txt_color);
		if (csl != null) {
			mAddBtn.setTextColor(csl);
		}
		setListeners();
	}

	private void setListeners() {
		mAddBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (addListener != null && mProduct != null) {
					mIsAdd = !mIsAdd;
					mAddBtn.setSelected(mIsAdd);
					if (mIsAdd) {
						mAddBtn.setText(getResources().getString(R.string.product_added));
					} else {
						mAddBtn.setText(getResources().getString(R.string.product_add));
					}
					addListener.OnAddClick(mProduct.id, mIsAdd);
				}
			}
		});
	}
	
	public void setProductData(Product product, boolean isAdd) {
		if (product != null) {
			this.mProduct = product;
			this.mIsAdd = isAdd;
			mProductImage.setImageUrl(APIManager.toAbsoluteUrl(product.getCoverImage()), 
					APIManager.getInstance(getContext()).getImageLoader());
			mProductName.setText(StringUtil.showMessage(product.name));
			mProductPrice.setText(getResources().getString(R.string.yuan_symbol) 
					+ StringUtil.getStringFromFloatKeep2(product.price));
			mAddBtn.setSelected(isAdd);
			if (mIsAdd) {
				mAddBtn.setText(getResources().getString(R.string.product_added));
			} else {
				mAddBtn.setText(getResources().getString(R.string.product_add));
			}
		}
	}
	
	public void setOnAddClickListener(OnAddClickListener l) {
		addListener = l;
	}
	
	public interface OnAddClickListener {
		public void OnAddClick(String id, boolean isAdd);
	}
}
