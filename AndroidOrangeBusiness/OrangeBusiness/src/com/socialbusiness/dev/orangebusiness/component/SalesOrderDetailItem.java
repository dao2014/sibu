package com.socialbusiness.dev.orangebusiness.component;

import java.math.BigDecimal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderTrade.ProductItemFromServer;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

public class SalesOrderDetailItem extends RelativeLayout {

	private Context context;
	private NetworkImageView mProductImage;
	private TextView mProductName;
	private EditText mProductPrice;
	private EditText mProductNum;
	private TextView mProductSum;
	private ImageView mDeleteImage;
	
	private ProductItemFromServer mProductItem;
    private OnETDataChangeListener etListener;
    private OnDeletePIFSListener pifsListener;
	
	public SalesOrderDetailItem(Context context) {
		super(context);
		this.context = context;
		initView();
	}
	
	public SalesOrderDetailItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}
	
	private void initView() {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		findViews(inflater.inflate(R.layout.view_sales_order_detail_item, this));
		setListeners();
	}

	private void findViews(View view) {
		mProductImage = (NetworkImageView) view.findViewById(R.id.view_sales_order_detail_item_product_image);
		mProductName = (TextView) view.findViewById(R.id.view_sales_order_detail_item_product_name);
		mProductPrice = (EditText) view.findViewById(R.id.view_sales_order_detail_item_product_price);
		mProductNum = (EditText) view.findViewById(R.id.view_sales_order_detail_item_product_num);
		mProductSum = (TextView) view.findViewById(R.id.view_sales_order_detail_item_product_sum);
		mDeleteImage = (ImageView) view.findViewById(R.id.view_sales_order_detail_item_delete);
		
		mProductImage.setDefaultImageResId(R.drawable.ic_default_mid);
		mProductImage.setErrorImageResId(R.drawable.ic_default_mid);
	}
	
	private void setListeners() {
		mDeleteImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isDelete();
			}
		});

		mProductPrice.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mProductItem == null) {
					return;
				}
				String price = mProductPrice.getText().toString().trim();
				if (!TextUtils.isEmpty(price)) {
                	try {
                		if (mProductItem.price == Float.parseFloat(price)) {
                			return;
                		}
                		mProductItem.price = Float.parseFloat(price);
					} catch (Exception e) {
						mProductItem.price = 0f;
                        e.printStackTrace();
					}
                } else {
                	mProductItem.price = 0f;
                }
				mProductItem.product.price = mProductItem.price;
                updateSum();
                callBackDataChangedListener();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

        mProductPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (mProductItem == null) {
                    return false;
                }
                String price = StringUtil.get2DecimalFromString(mProductPrice.getText().toString().trim());
                if (!TextUtils.isEmpty(price)) {
                    try {
                        mProductPrice.setText(price);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

		mProductNum.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mProductItem == null) {
					return;
				}
				String num = mProductNum.getText().toString().trim();
				if (!TextUtils.isEmpty(num)) {
					try {
						if (mProductItem.amount == Integer.parseInt(num)) {
							return;
						}
						mProductItem.amount = Integer.parseInt(num); 
					} catch (Exception e) {
						mProductItem.amount = 0;
					}
				} else {
					mProductItem.amount = 0;
				}
				mProductItem.product.amount = mProductItem.amount;
				updateSum();
				callBackDataChangedListener();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

	}

	private void isDelete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMessage("确定要删除此订单吗？")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (pifsListener != null && mProductItem != null) {
							pifsListener.onDelete(mProductItem);
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.create()
				.show();
	}
	
	private void updateSum() {
    	mProductSum.setText(StringUtil.getStringFromFloatKeep2(
    			new BigDecimal(mProductItem.amount).multiply(new BigDecimal(mProductItem.price)).doubleValue()));
    }
	
	private void callBackDataChangedListener() {
    	if (etListener != null) {
    		etListener.onEditTextChanged();
    	}
    }
	
	public void setProductItemData(ProductItemFromServer item) {
		if (item == null || item.product == null) {
			return;
		}
		
		this.mProductItem = item;
		mProductImage.setImageUrl(APIManager.toAbsoluteUrl(item.product.getCoverImage()), 
				APIManager.getInstance(getContext()).getImageLoader());
		mProductName.setText(StringUtil.showMessage(item.product.name));
		mProductPrice.setText(StringUtil.getStringFromFloatKeep2(item.price));
		mProductNum.setText(item.amount + "");
		updateSum();
	}
	
	public void setOnETDataChangeListener(OnETDataChangeListener listener) {
        this.etListener = listener;
    }
	
	public void setOnDeltePIFSListener(OnDeletePIFSListener l) {
		this.pifsListener = l;
	}
	
	public interface OnETDataChangeListener {
        public void onEditTextChanged();
    }
	
	public interface OnDeletePIFSListener {
		public void onDelete(ProductItemFromServer item);
	}
}
