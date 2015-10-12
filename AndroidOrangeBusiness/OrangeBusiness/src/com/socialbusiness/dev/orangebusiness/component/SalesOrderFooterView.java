package com.socialbusiness.dev.orangebusiness.component;

import java.math.BigDecimal;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.model.Order;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderTrade;
import com.socialbusiness.dev.orangebusiness.model.Order.OrderTrade.ProductItemFromServer;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.socialbusiness.dev.orangebusiness.util.Validator;

public class SalesOrderFooterView extends LinearLayout {

	private EditText mFreight;
	private TextView mTotalMoney;
	private LinearLayout mOrderNumberLayout;
	private TextView mOrderNumber;
	private RelativeLayout mOrderDateLayout;
	private TextView mOrderDate;

    private EditText mPaysMoney;
	private EditText mFreightWay;
	private EditText mStartFreightNumET;
	private ImageView mStartFreightNumImage;
	private EditText mEndFreightNumET;
	private ImageView mEndFreightNumImage;

    private LinearLayout mCustomerLayout;
    private LinearLayout mRecipientLayout;
    private LinearLayout mPhoneLayout;
    private LinearLayout mAddressLayout;
    private LinearLayout mPostCodeLayout;

	private EditText mCustomerName;
	private EditText mRecipient;
	private EditText mPhone;
	private EditText mAddress;
	private EditText mPostCode;
	private TextView mNextBtn;
	
	private OnClickDateListener dateListener;
	private OnClickScan2DimensionCodeListener scan2DCListener;
	private OnClickNextListener nextListener;
	private OnFreightChangedListener freightListener;

	private OrderTrade mOrderTrade;
    private Order.OrderTradeType mTradeType;
	private float currentFreight;
	
	public SalesOrderFooterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public SalesOrderFooterView(Context context) {
		super(context);
		initView();
	}

	private void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		findViews(inflater.inflate(R.layout.view_sales_order_footer, this));
		setListeners();
	}
	
	private void findViews(View view) {
		mFreight = (EditText) view.findViewById(R.id.view_sales_order_footerview_transport_fee);
		mTotalMoney = (TextView) view.findViewById(R.id.view_sales_order_footerview_money_sum);
		mOrderNumberLayout = (LinearLayout) view.findViewById(R.id.view_sales_order_footerview_order_number_layout);
		mOrderNumber = (TextView) view.findViewById(R.id.view_sales_order_footerview_order_number);
		mOrderDateLayout = (RelativeLayout) view.findViewById(R.id.view_sales_order_footerview_date_layout);
		mOrderDate = (TextView) view.findViewById(R.id.view_sales_order_footerview_date_center);

        mPaysMoney = (EditText) view.findViewById(R.id.view_sales_order_footerview_pays_money_edittext);
		mFreightWay = (EditText) view.findViewById(R.id.view_sales_order_footerview_freight_way);
		mStartFreightNumET = (EditText) view.findViewById(R.id.view_sales_order_footerview_freight_start_num);
		mStartFreightNumImage = (ImageView) view.findViewById(R.id.view_sales_order_footerview_freight_start_num_image);
		mEndFreightNumET = (EditText) view.findViewById(R.id.view_sales_order_footerview_freight_end_num);
		mEndFreightNumImage = (ImageView) view.findViewById(R.id.view_sales_order_footerview_freight_end_num_image);

        mCustomerLayout = (LinearLayout) view.findViewById(R.id.view_sales_order_footerview_customer_name_layout);
        mRecipientLayout = (LinearLayout) view.findViewById(R.id.view_sales_order_footerview_recipient_layout);
        mPhoneLayout = (LinearLayout) view.findViewById(R.id.view_sales_order_footerview_phone_layout);
        mAddressLayout = (LinearLayout) view.findViewById(R.id.view_sales_order_footerview_address_layout);
        mPostCodeLayout = (LinearLayout) view.findViewById(R.id.view_sales_order_footerview_postcode_layout);

		mCustomerName = (EditText) view.findViewById(R.id.view_sales_order_footerview_customer_name);
		mRecipient = (EditText) view.findViewById(R.id.view_sales_order_footerview_recipient);
		mPhone = (EditText) view.findViewById(R.id.view_sales_order_footerview_phone);
		mAddress = (EditText) view.findViewById(R.id.view_sales_order_footerview_address);
		mPostCode = (EditText) view.findViewById(R.id.view_sales_order_footerview_postcode);
		mNextBtn = (TextView) view.findViewById(R.id.view_sales_order_footerview_next_btn);
		
		mTotalMoney.setText("0");
		mFreight.setText("0");
		mOrderNumberLayout.setVisibility(View.GONE);
	}
	
	private void setListeners() {
		mFreight.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String freight = mFreight.getText().toString().trim();
				if (!TextUtils.isEmpty(freight)) {
					try {
						currentFreight = Float.parseFloat(freight);
					} catch (Exception e) {
						currentFreight = 0f;
					}
				} else {
					currentFreight = 0f;
				}
				
				if (mOrderTrade != null) {
					mOrderTrade.freight = currentFreight;
				} 
				if (freightListener != null) {
					freightListener.onFreightChange();
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

        mFreight.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String price = StringUtil.get2DecimalFromString(mFreight.getText().toString().trim());
                if (!TextUtils.isEmpty(price)) {
                    try {
                        mFreight.setText(price);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
		
		OnClickListener listeners = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (v == mOrderDateLayout && dateListener != null) {	//日期
					dateListener.onClickDate();
				} else if (scan2DCListener != null 
						&& (v == mStartFreightNumImage || v == mEndFreightNumImage)) {
					scan2DCListener.OnClickScan(v, v == mStartFreightNumImage);
				} else if (nextListener != null && v == mNextBtn) {
					if (checkToNext()) {
						nextListener.onClickNext();
					}
				}
			}
		};
		
		mOrderDateLayout.setOnClickListener(listeners);
		mStartFreightNumImage.setOnClickListener(listeners);
		mEndFreightNumImage.setOnClickListener(listeners);
		mNextBtn.setOnClickListener(listeners);
	}
	
	public void displayDateAndTime(int mYear, int mMonth, int mDay, int mHour, int mMinute) {	// 显示日期
		if (mOrderDate == null) {
			return;
		}
		mOrderDate.setText(new StringBuilder().append(mYear).append("-")
				.append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1)).append("-")
				.append((mDay < 10) ? "0" + mDay : mDay).append(" ")
				.append((mHour < 10) ? "0" + mHour : mHour).append(":")
				.append((mMinute < 10) ? "0" + mMinute : mMinute));
	}
	
	public void setData(OrderTrade orderTrade) { // 初始化一些数据
		this.mOrderTrade = orderTrade;
		currentFreight = orderTrade.freight;
		setMoneySum(getTotalMoney(orderTrade.products));
        mPaysMoney.setText(StringUtil.getStringFromFloatKeep2(mOrderTrade.payMoney));
		mFreight.setText(StringUtil.getStringFromFloatKeep2(currentFreight));
		mOrderNumberLayout.setVisibility(View.VISIBLE);
		mOrderNumber.setText(StringUtil.showMessage(orderTrade.code));
		mOrderDate.setText(StringUtil.showMessage(orderTrade.orderDate));
		mFreightWay.setText(StringUtil.showMessage(mOrderTrade.express));
		mStartFreightNumET.setText(StringUtil.showMessage(mOrderTrade.expressStart));
		mEndFreightNumET.setText(StringUtil.showMessage(mOrderTrade.expressEnd));

        if (mTradeType != null && mTradeType == Order.OrderTradeType.Sell) {
            // 我的零售
		    mCustomerName.setText(StringUtil.showMessage(mOrderTrade.name));
		    mRecipient.setText(StringUtil.showMessage(mOrderTrade.username));
		    mPhone.setText(StringUtil.showMessage(mOrderTrade.phone));
		    mAddress.setText(StringUtil.showMessage(mOrderTrade.address));
		    mPostCode.setText(StringUtil.showMessage(mOrderTrade.postCode));
        }
	}

    public void hideViews(Order.OrderTradeType tradeType) {
        if (tradeType == null) {
            return;
        }
        this.mTradeType = tradeType;
        if (tradeType == Order.OrderTradeType.Buy) {
            mCustomerLayout.setVisibility(View.GONE);
            mRecipientLayout.setVisibility(View.GONE);
            mPhoneLayout.setVisibility(View.GONE);
            mAddressLayout.setVisibility(View.GONE);
            mPostCodeLayout.setVisibility(View.GONE);
        }
    }

	private double getTotalMoney(List<ProductItemFromServer> mProductList) {
		if (mProductList == null || mProductList.size() == 0) {
			return 0f;
		}
		BigDecimal moneySum = new BigDecimal(0f);
		for (int i = 0; i < mProductList.size(); i++) {
			moneySum = moneySum.add(new BigDecimal(mProductList.get(i).amount)
					.multiply(new BigDecimal(mProductList.get(i).price)));
		}
		return moneySum.doubleValue();
	}
	
	private boolean checkToNext() {
		if (TextUtils.isEmpty(getOrderDate())) {
			ToastUtil.show(getContext(), "订单日期不能为空");
			return false;
		}
		if (TextUtils.isEmpty(getFreightWay())) {
			ToastUtil.show(getContext(), "货运方式不能为空");
			return false;
		}

        if (mTradeType == Order.OrderTradeType.Sell) {
            if (TextUtils.isEmpty(getCustomerName())) {
                ToastUtil.show(getContext(), "客户名称不能为空");
                return false;
            }
            if (TextUtils.isEmpty(getRecipient())) {
                ToastUtil.show(getContext(), "收件人不能为空");
                return false;
            }
            if (!Validator.isPhone(getPhone())) {
                ToastUtil.show(getContext(), "请输入正确的11位手机号码");
                return false;
            }
            if (TextUtils.isEmpty(getAddress())) {
                ToastUtil.show(getContext(), "收货地址不能为空");
                return false;
            }
            if (TextUtils.isEmpty(getPostcode()) || getPostcode().length() != 6) {
                ToastUtil.show(getContext(), "请输入正确的6位邮编号码");
                return false;
            }
        }
		return true;
    }
	
	public void setMoneySum(double sum) {
		if (mTotalMoney != null) {
			mTotalMoney.setText(StringUtil.getStringFromFloatKeep2(
					new BigDecimal(sum).add(new BigDecimal(currentFreight)).doubleValue()));
		}
	}
	
	public void setStart2DCText(String code) {
		if (mStartFreightNumET != null) {
			mStartFreightNumET.setText(StringUtil.showMessage(code));
		}
	}
	
	public void setEnd2DCText(String code) {
		if (mEndFreightNumET != null) {
			mEndFreightNumET.setText(StringUtil.showMessage(code));
		}
	}

    /**运费*/
	public float getFee() {
    	String str = mFreight.getText().toString().trim();
    	float fee = (mOrderTrade != null ? mOrderTrade.freight : 0f);
    	if (!TextUtils.isEmpty(str)) {
    		try {
    			fee = Float.parseFloat(str);
			} catch (Exception e) {
				fee = (mOrderTrade != null ? mOrderTrade.freight : 0f);
			}
    	}
    	return fee;
    }

    /**付款金额*/
    public float getPayMoney() {
        String payStr = mPaysMoney.getText().toString().trim();
        float payMoney = (mOrderTrade != null ? mOrderTrade.payMoney : 0f);
        if (!TextUtils.isEmpty(payStr)) {
            try {
                payMoney = Float.parseFloat(payStr);
            } catch (Exception e) {
                payMoney = (mOrderTrade != null ? mOrderTrade.payMoney : 0f);
            }
        }
        return payMoney;
    }

    /**订单日期*/
	public String getOrderDate() {
		return mOrderDate != null ? mOrderDate.getText().toString().trim() : null;
	}

    /**货运方式*/
	public String getFreightWay() {
		return mFreightWay != null ? mFreightWay.getText().toString().trim() : null;
	}

    /**起始货运单号*/
	public String getStart2DC() {
		return mStartFreightNumET != null ? mStartFreightNumET.getText().toString().trim() : null;
	}

    /**终止货运单号*/
	public String getEnd2DC() {
		return mEndFreightNumET != null ? mEndFreightNumET.getText().toString().trim() : null;
	}

    /**客户名称*/
	public String getCustomerName() {
		return mCustomerName != null ? mCustomerName.getText().toString().trim() : null;
	}

    /**收件人*/
	public String getRecipient() {
		return mRecipient != null ? mRecipient.getText().toString().trim() : null;
	}

    /**手机号*/
	public String getPhone() {
		return mPhone != null ? mPhone.getText().toString().trim() : null;
	}

    /**收货地址*/
	public String getAddress() {
		return mAddress != null ? mAddress.getText().toString().trim() : null;
	}

    /**邮编*/
	public String getPostcode() {
		return mPostCode != null ? mPostCode.getText().toString().trim() : null;
	}
	
	public void setOnClickDateListener(OnClickDateListener l) {
		this.dateListener = l;
	}
	
	public void setOnClickScan2DCListener(OnClickScan2DimensionCodeListener l) {
		this.scan2DCListener = l;
	}
	
	public void setOnclickNextListener(OnClickNextListener l) {
		this.nextListener = l;
	}
	
	public void setOnFreightChangedListener(OnFreightChangedListener l) {
		this.freightListener = l;
	}
	
	public interface OnClickDateListener {
		public void onClickDate();
	}
	
	public interface OnClickScan2DimensionCodeListener {
		public void OnClickScan(View v, boolean isStart);
	}
	
	public interface OnClickNextListener {
		public void onClickNext();
	}
	
	public interface OnFreightChangedListener {
		public void onFreightChange();
	}
}
