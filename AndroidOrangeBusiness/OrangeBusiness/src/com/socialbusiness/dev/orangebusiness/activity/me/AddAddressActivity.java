package com.socialbusiness.dev.orangebusiness.activity.me;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.model.Address;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.socialbusiness.dev.orangebusiness.util.Validator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AddAddressActivity extends BaseActivity {
	
    private EditText mConsignee;
    private EditText mConPhone;
    private TextView mConDistrict;
    private EditText mConDetail;
    private EditText mConCode;
    private TextView mAdd;
    private Address mAddress;
    private String mTitle;

    // 输入框变量
    private String consignee = null;
    private String phoneNumber = null;
    private String province = null;
    private String city = null;
    private String district = null;
    private String detail = null;
    private String postCode = null;

    private String jsonString;
    
    private int provinceIndex;
    private int cityIndex;
    private boolean isProvince = true;
    private boolean isCity = true;
    
    private Dialog alertDialog;
    private InputMethodManager imm;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_add);
        mTitle = getIntent().getStringExtra("title");
        mAddress = (Address) getIntent().getSerializableExtra("address");
        setUp();
        findView();
        registerListener();
    }

    private void setUp() {
    	imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (mTitle != null) {
            setTitle(mTitle);
        } else {
            setTitle(R.string.address_add);
        }
        jsonString = StringUtil.getStringFromAssets(AddAddressActivity.this, "address.json");


        //设置顶部title栏颜色
//        setLeftIcon(R.drawable.ic_back_orange_selector);
//        setHeaderTitleBarBg(R.color.white);
//        setTitleTextColor(R.color.black);
    }

    private void findView() {
        mConsignee = (EditText) this.findViewById(R.id.activity_address_add_name);
        mConPhone = (EditText) this.findViewById(R.id.activity_address_add_phone);
        mConDistrict = (TextView) this.findViewById(R.id.activity_address_add_district);
        mConDetail = (EditText) this.findViewById(R.id.activity_address_add_detail);
        mConCode = (EditText) this.findViewById(R.id.activity_address_add_code);
        mAdd = (TextView) this.findViewById(R.id.activity_address_add_btn);

        if (mAddress != null) {
            getAddressDate();
        }
    }

    // 保存输入，在点击事件中使用
    private void saveAddressData() {
        if (mAddress != null) {
            if (checkData()) {
            	mAddress.name = consignee;
                mAddress.phone = phoneNumber;
                mAddress.province = province;
                mAddress.city = city;
                mAddress.district = district;
                mAddress.detail = detail;
                mAddress.postCode = postCode;
            	callAPIUpdateAddress(mAddress);
            }
        } else {
            if (checkData()) {
                mAddress = new Address();
                mAddress.name = consignee;
                mAddress.phone = phoneNumber;
                mAddress.province = province;
                mAddress.city = city;
                mAddress.district = district;
                mAddress.detail = detail;
                mAddress.postCode = postCode;
                callAPIAddAddress(mAddress);
            }
        }
    }

    // 将传递进来的地址添加到输入框
    private void getAddressDate() {
    	province = mAddress.province;
    	city = mAddress.city;
    	district = mAddress.district;
        mConsignee.setText(mAddress.name);
        mConPhone.setText(mAddress.phone);
        mConDetail.setText(mAddress.detail);
        mConCode.setText(mAddress.postCode);
        mAdd.setText(R.string.address_modify_btn);

        if (!TextUtils.isEmpty(province) 
        		&& !TextUtils.isEmpty(city) 
        		&& !TextUtils.isEmpty(district)) {
        	mConDistrict.setText(province + ", " + city + ", " + district);
        } else if (!TextUtils.isEmpty(province)	&& !TextUtils.isEmpty(city)) {
        	mConDistrict.setText(province + ", " + city);
        } else {
        	mConDistrict.setText(province);
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if (ev.getAction() == MotionEvent.ACTION_DOWN
    			|| ev.getAction() == MotionEvent.ACTION_MOVE
    			|| ev.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
    		View v = getCurrentFocus();
    		if (AndroidUtil.isShouldHideInput(v, ev) && imm != null) {
    			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    		}
    		return super.dispatchTouchEvent(ev);
    	}
    	
    	// 必不可少，否则所有的组件都不会有TouchEvent了
    	if (getWindow().superDispatchTouchEvent(ev)) {
    		return true;
    	}
    	return onTouchEvent(ev);
    }
    
    // 回调添加地址api
    private void callAPIAddAddress(Address mAddress) {
    	showProgressDialog();
        APIManager.getInstance(this).addAddress(mAddress, new Response.ErrorListener() {
        	
            @Override
            public void onErrorResponse(VolleyError error) {
            	hideProgressDialog();
                showException(error);
            }

        }, new Response.Listener<RequestResult<?>>() {
        	
            @Override
            public void onResponse(RequestResult<?> response) {
            	hideProgressDialog();
            	if (hasError(response)) {
            		return;
            	}
            	setResult(RESULT_OK);
				finish();
            }
        });
    }

    // 回调更新地址api
    private void callAPIUpdateAddress(Address mAddress) {
        APIManager.getInstance(this).updateAddress(mAddress, new Response.ErrorListener() {
            
        	@Override
            public void onErrorResponse(VolleyError error) {
        		showException(error);
            }

        }, new Response.Listener<RequestResult<?>>() {
            
        	@Override
            public void onResponse(RequestResult<?> response) {
            	if (hasError(response)) {
            		return;
            	}
            	setResult(RESULT_OK);
            	finish();
            }
        });
    }

    // 检查输入框数据规则
    private boolean checkData() {
        consignee = mConsignee.getText().toString().trim();
        phoneNumber = mConPhone.getText().toString().trim();
        detail = mConDetail.getText().toString().trim();
        postCode = mConCode.getText().toString().trim();

        if (TextUtils.isEmpty(consignee)) {
            ToastUtil.show(this, "姓名不能为空");
            return false;
        }
        if (consignee.length() < 2 || consignee.length() > 15) {
            ToastUtil.show(this, "姓名：2-15个字符限制");
            return false;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            ToastUtil.show(this, "手机号码不能为空");
            return false;
        }
        if (!Validator.isPhone(phoneNumber)) {
            ToastUtil.show(this, "请输入正确手机号码");
            return false;
        }

        if (TextUtils.isEmpty(mConDistrict.getText().toString().trim())) {
            ToastUtil.show(this, "请选择所在地区");
            return false;
        }
        if (TextUtils.isEmpty(detail)) {
            ToastUtil.show(this, "收货人详细地址不能为空");
            return false;
        }
        if (TextUtils.isEmpty(postCode)) {
            ToastUtil.show(this, "收货人邮编号码不能为空");
            return false;
        }
        return true;
    }

    private void registerListener() {
        OnClickListener listeners = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.activity_address_add_btn) {
                    saveAddressData();
                } else if (v == mConDistrict) {
                	if (TextUtils.isEmpty(jsonString)) {
                		return;
                	} else {
                		List<Address.AddressProvince> addressList = gsonAddress(jsonString);
                		if (addressList != null) {
                			doSelect(addressList);
                		}
                	}
                }
            }
        };
        mAdd.setOnClickListener(listeners);
        mConDistrict.setOnClickListener(listeners);
        mConDistrict.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (TextUtils.isEmpty(jsonString)) {
                        return;
                    } else {
                        List<Address.AddressProvince> addressList = gsonAddress(jsonString);
                        if (addressList != null) {
                            doSelect(addressList);
                        }
                    }
                }
            }

        });
    }

    private void doSelect(final List<Address.AddressProvince> list) {
        Log.e("======", "============" + list.size());
        final List<String> listString = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            listString.add(list.get(i).name);
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listString);
        alertDialog = new AlertDialog.Builder(this)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isProvince && which < list.size()) {//点击省
                            provinceIndex = which;
                            isProvince = false;
                            List<String> citylist=new ArrayList<String>();
                            for (int i = 0; i < list.get(which).cities.size(); i++) {
                                citylist.add(list.get(which).cities.get(i).name);
                            }
                            if (citylist.size() == 0) {
                                province = list.get(provinceIndex).name;
                                setProvince();
                                resetSelectValue();
                                return;
                            }
                            doSelectCity(list, arrayAdapter, citylist);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetSelectValue();
                    }
                }).create();

        alertDialog.show();
    }

    private void doSelectCity(final List<Address.AddressProvince> list,
                              final ArrayAdapter<String> arrayAdapter,
                              final List<String> listString) {
        dismissDialog();
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listString);
//    	arrayAdapter.notifyDataSetChanged();
        Log.e("======", "============" + listString.size());
        alertDialog = new AlertDialog.Builder(this)
                .setAdapter(cityAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isCity && which < listString.size()) {//点击市
                            cityIndex = which;
                            isCity = false;
//							listString.clear();
                            List<String> zonelist=new ArrayList<String>();
                            for (int i = 0; i < list.get(provinceIndex).cities.get(which).areas.size(); i++) {
                                zonelist.add(list.get(provinceIndex).cities.get(which).areas.get(i).name);
                            }
                            if (zonelist.size() == 0) {
                                province = list.get(provinceIndex).name;
                                city = list.get(provinceIndex).cities.get(cityIndex).name;
                                setCity();
                                resetSelectValue();
                                return;
                            }
                            doSelectAreas(list, arrayAdapter, zonelist);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetSelectValue();
                    }
                }).create();

        alertDialog.show();
    }

    private void doSelectAreas(final List<Address.AddressProvince> list,
                               final ArrayAdapter<String> arrayAdapter,
                               final List<String> listString) {
        dismissDialog();
        ArrayAdapter<String> zoneAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listString);
//    	arrayAdapter.notifyDataSetChanged();
        alertDialog = new AlertDialog.Builder(this)
                .setAdapter(zoneAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listString != null && which < listString.size()) {//点击区
                            isProvince = true;
                            isCity = true;
                            province = list.get(provinceIndex).name;
                            city = list.get(provinceIndex).cities.get(cityIndex).name;
                            district = listString.get(which);
                            setDistrict();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface arg0) {
                        resetSelectValue();
                    }
                }).create();

        alertDialog.show();
    }
    
    private void dismissDialog() {
    	if (alertDialog != null) {
    		alertDialog.dismiss();
    	}
    }
    
    private void resetSelectValue() {
    	if (!isProvince) {
			isProvince = true;
			isCity = true;
		}
    }
    
    private void setProvince() {
    	if (TextUtils.isEmpty(province)) {
    		return;
    	}
    	city = "";
    	district = "";
    	mConDistrict.setText(province);
    }
    
    private void setCity() {
    	if (TextUtils.isEmpty(province) || TextUtils.isEmpty(city)) {
    		return;
    	}
    	district = "";
    	mConDistrict.setText(province + ", " + city);
    }
    
    private void setDistrict() {
    	if (TextUtils.isEmpty(province) 
    			|| TextUtils.isEmpty(city) 
    			|| TextUtils.isEmpty(district)) {
    		return;
    	}
    	mConDistrict.setText(province + ", " + city + ", " + district);
    }
    
    private List<Address.AddressProvince> gsonAddress(String jsonString) {
    	Type type = new TypeToken<List<Address.AddressProvince>>(){}.getType();
    	Gson gson = new Gson();
    	List<Address.AddressProvince> selection = gson.fromJson(jsonString, type);
        Log.e("========","====="+selection.get(17).cities);
    	return selection;
    }
    
}
