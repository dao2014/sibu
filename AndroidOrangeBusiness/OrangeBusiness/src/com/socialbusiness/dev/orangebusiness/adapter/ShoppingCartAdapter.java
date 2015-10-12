package com.socialbusiness.dev.orangebusiness.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.component.ShoppingCartItem;
import com.socialbusiness.dev.orangebusiness.component.ShoppingCartItem.OnClickItemListener;
import com.socialbusiness.dev.orangebusiness.component.ShoppingCartItem.OnDeleteListener;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShoppingCartAdapter extends BaseAdapter {

    private Context context;
    private List<ShopCartItem> list;
    private OnNoticeDataChangeListener listener;
    private ShopCartDbHelper helper;

    // 用来控制选中状态
    private List<Boolean> isSelectedList;

    public ShoppingCartAdapter(Context context) {
        this.context = context;
    }

    public void setShopCartItemList(List<ShopCartItem> list) {
        this.list = list;
        isSelectedList = new ArrayList<Boolean>();
        helper = new ShopCartDbHelper(context);
        initSelectedData();
    }

    public void updateShopCartItemList(int position, boolean isFromDelete) {
        if (helper != null) {
            this.list = helper.getAllShopCartItems();
//            Log.i("list","=="+this.list);
            if (isSelectedList != null && position < isSelectedList.size() && isFromDelete) {
                isSelectedList.remove(position);
            }
        }
    }

    public List<ShopCartItem> getShopCartItemList() {
        return list;
    }

    private void initSelectedData() {
        if (isSelectedList != null && isSelectedList.size() == list.size()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            isSelectedList.add(true);
        }
    }

    public List<Boolean> getIsSelectedList() {
        return isSelectedList;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return list != null ? list.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    HashMap<Integer,ShoppingCartItem> shopings=new HashMap<>();
    @Override
    public View getView( int position, View convertView, ViewGroup parent) {
        ShoppingCartItem shoppingCartItem = null;
//        if (convertView == null) {
            shoppingCartItem = new ShoppingCartItem(context);
//        } else {
//            shoppingCartItem = (ShoppingCartItem) convertView;
//        }


//        if(shopings.get(position)==null){
//            shoppingCartItem = new ShoppingCartItem(context);
//            shopings.put(position,shoppingCartItem);
//
//        }else{
//            shoppingCartItem=shopings.get(position);
//        }
        if (list != null) {
            doPlusMinus(shoppingCartItem, position);
            doDelete(shoppingCartItem, position);
        }
        return shoppingCartItem;
    }

    private void doDelete(ShoppingCartItem shoppingCartItem, final int position) {
        shoppingCartItem.setOnDeleteListener(new OnDeleteListener() {

            @Override
            public void onDelete() {
                ondelete(position);
            }
        });
    }

    private void ondelete(int position) {
        if (helper.deleteShopCardItemById(list.get(position).id)) {
            updateShopCartItemList(position, true);
            notifyDataSetChanged();
            if (listener != null) {
                listener.onNoticeDataChange(false);
            }
            return;
        }
    }

    private void doPlusMinus(final ShoppingCartItem shoppingCartItem, final int position) {
        final ShopCartItem shopCartItem = list.get(position);
//        Log.e("doPlusMinus", "position==" + position + "nums=" + shopCartItem.quantity);
        final ImageView selectIV = shoppingCartItem.getImageViewSelect();
        final ImageView minusIV = shoppingCartItem.getImageViewMinus();
        final ImageView plusIV = shoppingCartItem.getImageViewPlus();
        final EditText numsET = shoppingCartItem.getNums();
        final TextView moneyTV = shoppingCartItem.getMoney();
        shoppingCartItem.setShopCartItemData(shopCartItem, getIsSelectedList().get(position));
        selectIV.setSelected(getIsSelectedList().get(position));
        shoppingCartItem.setOnclickItemListeners(new OnClickItemListener() {

            @Override
            public void onClickSelectImage() {
                selectIV.setSelected(!selectIV.isSelected());
                getIsSelectedList().set(position, selectIV.isSelected());//更新选中状态
                if (listener != null) {
                    listener.onNoticeDataChange(false);
                }
            }

            @Override
            public void onClickPlus() {
                String numStr = numsET.getText().toString().trim();
                int nums;
                if (TextUtils.isEmpty(numStr) || numStr.equals("0")) {
                    nums = 0;
                } else {
                    nums = Integer.parseInt(numStr);
                }
                float nowMoneySum = Float.parseFloat(moneyTV.getText().toString().trim());

                if (nums >= 9999) {
                    plusIV.setEnabled(false);
                } else {
                    helper.addShopCart(shopCartItem);
                    updateShopCartItemList(position, false);
                    nums += 1;
                    numsET.setText(nums + "");
                    moneyTV.setText(StringUtil.getStringFromFloatKeep2((nowMoneySum + shopCartItem.price)));
                    minusIV.setEnabled(true);
                    if (nums == 9999) {
                        plusIV.setEnabled(false);
                        ;
                    }
                    if (listener != null) {
                        listener.onNoticeDataChange(false);
                    }
                }
            }

            @Override
            public void onClickMinus() {
                String numStr = numsET.getText().toString().trim();
                int nums;
                if (TextUtils.isEmpty(numStr) || numStr.equals("1")) {
                    nums = 0;
                    Dialog isDeleteDialog = new AlertDialog.Builder(context)
                            .setMessage(context.getResources().getString(R.string.is_delete_product))
                            .setPositiveButton(context.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    helper.deleteShopCardItemById(shopCartItem.id);
//                                    list.remove(shopCartItem);
//                                    if (listener != null) {
//                                        listener.onNoticeDataChange(true);
//                                    }
                                    ondelete(position);
                                }
                            })
                            .setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {


                                }
                            }).create();
                    Window window = isDeleteDialog.getWindow();
                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.alpha = 0.8f;
                    window.setAttributes(lp);
                    isDeleteDialog.show();
                    return;
                } else {
                    nums = Integer.parseInt(numStr);
                }
                float nowMoneySum = Float.parseFloat(moneyTV.getText().toString().trim());

                if (nums <= 0) {
                    minusIV.setEnabled(false);
                } else {
                    helper.minusShopCart(shopCartItem);
                    updateShopCartItemList(position, false);
                    nums -= 1;
                    numsET.setText(nums + "");
                    moneyTV.setText(StringUtil.getStringFromFloatKeep2((nowMoneySum - shopCartItem.price)));
                    plusIV.setEnabled(true);
                    if (nums == 0) {
                        minusIV.setEnabled(false);
                    }
                    if (listener != null) {
                        listener.onNoticeDataChange(false);
                    }
                }
            }

            @Override
            public void onInputNums(int nowNum) {

                if (nowNum == 0) {
                    minusIV.setEnabled(false);
                    plusIV.setEnabled(true);
                } else if (nowNum == 9999) {
                    plusIV.setEnabled(false);
                    minusIV.setEnabled(true);
                } else {
                    minusIV.setEnabled(true);
                    plusIV.setEnabled(true);
                }

                helper.resetNum(shopCartItem.id, nowNum);
                updateShopCartItemList(position, false);
                moneyTV.setText(StringUtil.getStringFromFloatKeep2(nowNum * shopCartItem.price));
                if (listener != null) {
                    listener.onNoticeDataChange(false);
                }
            }
        });
    }

    public void setOnNoticeDataChangeListener(OnNoticeDataChangeListener l) {
        this.listener = l;
    }

    public interface OnNoticeDataChangeListener {
        public void onNoticeDataChange(boolean isDeleteItem);
    }
}
