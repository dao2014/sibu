package com.socialbusiness.dev.orangebusiness.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.AddAddressActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.AddressListActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.PlaceAnOrderActivity;
import com.socialbusiness.dev.orangebusiness.model.Address;

public class AddressItem extends LinearLayout {

    private TextView mEmployName;
    private TextView mEmployPhone;
    private TextView mEmployAddress;
    private TextView mDelete;
    private TextView mSetDefault;
    private TextView mEditor;

    private Address mAddress;
    private Context context;
    private String from;
    private OnAddressStatusChangeListener listener;
    private int position;

    public AddressItem(Context context) {
        super(context);
        this.context = context;
        AddressListActivity activity = (AddressListActivity) context;
        from = activity.getIntent().getStringExtra("from");//如果有值说明是从 下单入口进的 否则就是从地址管理进的
        initView();
    }

    public AddressItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_address_item, this);
        findView(view);
        registerListener();
    }

    DeleteAddDialog dialog;

    private void findView(View view) {
        dialog = new DeleteAddDialog(getContext());
        dialog.setOnEnsure(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDelete(position);
                }
                dialog.dismiss();
            }
        });
        mEmployName = (TextView) view.findViewById(R.id.view_address_item_name);
        mEmployPhone = (TextView) view.findViewById(R.id.view_address_item_phone);
        mEmployAddress = (TextView) view.findViewById(R.id.view_address_item_address);
        mDelete = (TextView) view.findViewById(R.id.view_address_item_delete);
        mSetDefault = (TextView) view.findViewById(R.id.view_address_item_set);
        mEditor = (TextView) view.findViewById(R.id.view_address_item_editor);
    }

    OnClickListener register;

    private void registerListener() {
        register = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.view_address_item_delete://TODO
                        deleteDialog();
                        break;

                    case R.id.view_address_item_set:
                        if (listener != null) {
                            listener.setDefault(position);
                        }
                        break;

                    case R.id.view_address_item_editor:
                        updateAddress();
                        break;

                    default:
                        break;
                }
            }
        };

        mDelete.setOnClickListener(register);
        mSetDefault.setOnClickListener(register);
        mEditor.setOnClickListener(register);

//        this.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                deleteDialog();
//                return true;
//            }
//        });
    }

    //编辑地址
    private void updateAddress() {
        Intent intent = new Intent();
        intent.putExtra("title", "修改收货地址");
        intent.putExtra("address", mAddress);
        intent.setClass(context, AddAddressActivity.class);
        ((BaseActivity) context).startActivityForResult(intent, AddressListActivity.REQUEST_CODE_UPDATE_ADDRESS);
    }

    public void deleteDialog() {
//

        String[] items = {"删除"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddressItem.this.dialog.show();
            }

        });
        AlertDialog mSelectSexDialog = builder.create();
        mSelectSexDialog.show();



    }

    public void setAddressData(int positon, Address address) {
        if (address != null) {
            this.position = positon;
            mAddress = address;
            mEmployName.setText(address.name);
            mEmployPhone.setText(address.phone);
            String addressDetail = address.province + " " + address.city + " " + address.district + " " + address.detail;
            mEmployAddress.setText(addressDetail);
            if (address.isDefault) {
                if (!TextUtils.isEmpty(from) && from.equals(PlaceAnOrderActivity.TAG)) {
                    mSetDefault.setVisibility(View.GONE);
                }else {
                   // mSetDefault.setText(getResources().getString(R.string.default_address));
                    mSetDefault.setOnClickListener(null);
                    mSetDefault.setEnabled(false);
//                    mSetDefault.setTextColor(getResources().getColor(R.color.white));
                    //mSetDefault.setBackgroundResource(R.drawable.ic_defult_add);
                    mSetDefault.setVisibility(View.VISIBLE);
                }
            } else {
                if (!TextUtils.isEmpty(from) && from.equals(PlaceAnOrderActivity.TAG)) {
                    mSetDefault.setVisibility(View.GONE);
                }else {
                    mSetDefault.setVisibility(View.VISIBLE);
                   // mSetDefault.setText(getResources().getString(R.string.address_set));
                    mSetDefault.setOnClickListener(register);
//                    mSetDefault.setText("默认地址");
                    mSetDefault.setEnabled(true);
                    mSetDefault.setTextColor(getResources().getColor(R.color.white));
                   // mSetDefault.setBackgroundResource(R.drawable.ic_defult_add_no);
                }
            }
        }
    }

    public void setOnAddressStatusChangedListener(OnAddressStatusChangeListener l) {
        this.listener = l;
    }

    public interface OnAddressStatusChangeListener {
        public void onDelete(int position);

        public void setDefault(int position);
    }
}
