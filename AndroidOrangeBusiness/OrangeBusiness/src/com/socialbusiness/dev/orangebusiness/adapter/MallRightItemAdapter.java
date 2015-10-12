package com.socialbusiness.dev.orangebusiness.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.product.ProductDetailActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import java.util.HashMap;

/**
 * Created by zkboos on 2015/7/3.
 */
public class MallRightItemAdapter extends AdapterBase<Product> implements View.OnClickListener {

    public MallRightItemAdapter(Context context) {
        super(context);
        this.mContext = context;
        layoutInflater = LayoutInflater.from(mContext);
    }

    View.OnClickListener addDeleteClick;
    ShopCartDbHelper helper;
    HashMap<String, ShopCartItem> shopCartItemHashMap;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private HolderClickListener mHolderClickListener;

    public void setShopCartItemHashMap(HashMap<String, ShopCartItem> shopCartItemHashMap) {
        this.shopCartItemHashMap = shopCartItemHashMap;
    }

    public void setHelper(ShopCartDbHelper helper) {
        this.helper = helper;
    }

    public void setAddDeleteClick(View.OnClickListener addDeleteClick) {
        this.addDeleteClick = addDeleteClick;
    }

    @Override
    protected View getExView(int position, View convertView, ViewGroup parent) {
        Handle handle = null;
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.mall_right_item, null);
            handle = new Handle();


            handle.mImageView = (NetworkImageView) convertView.findViewById(R.id.mall_right_item_image);
            handle.mName = (TextView) convertView.findViewById(R.id.mall_right_item_name);
            handle.mPrice = (TextView) convertView.findViewById(R.id.mall_right_item_price);
            handle.mDesc = (TextView) convertView.findViewById(R.id.mall_right_item_desc);
            handle.item_shopping_cart_num_minus_image = convertView.findViewById(R.id.mall_right_item_delete);
            handle.item_shopping_cart_num_plus_image = convertView.findViewById(R.id.mall_right_item_add);
            handle.item_shopping_cart_num_edittext = (EditText) convertView.findViewById(R.id.item_shopping_cart_num_edittext);
            handle.mImageView.setErrorImageResId(R.drawable.ic_default_mid);
            handle.mImageView.setDefaultImageResId(R.drawable.ic_default_mid);
            convertView.setTag(handle);
        } else {
            handle = (Handle) convertView.getTag();
        }
//        convertView.setTag(position);
//        convertView.setOnClickListener(this);

        Product product = getList().get(position);



        handle.mImageView.setImageUrl(product.getCoverImage(),
                APIManager.getInstance(context).getImageLoader());
        handle.mImageView.setTag(product);
        handle.mImageView.setOnClickListener(this);

        handle.mName.setText(product.name);
        handle.mName.setTag(product);
        handle.mName.setOnClickListener(this);
        handle.mPrice.setText(StringUtil.getStringFromFloatKeep2(product.price));

         handle.item_shopping_cart_num_minus_image.setTag(product);
        handle.item_shopping_cart_num_minus_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mHolderClickListener != null) {

                    mHolderClickListener.onHolderClick(null,null,v);
                }
            }
        });

        handle.item_shopping_cart_num_plus_image.setTag(product);
//        handle.item_shopping_cart_num_plus_image.setOnClickListener(this.addDeleteClick);
        final Handle finalHandle = handle;
        handle.item_shopping_cart_num_plus_image.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            // TODO Auto-generated method stub
                                                                            if (mHolderClickListener != null) {
                                                                                int[] start_location = new int[2];
                                                                                finalHandle.mImageView.getLocationInWindow(start_location);
                                                                                Drawable drawable = finalHandle.mImageView.getDrawable();
                                                                                mHolderClickListener.onHolderClick(drawable,start_location,v);
                                                                            }
                                                                        }
                                                                    });
        ShopCartItem item;
        if(shopCartItemHashMap==null)
        item= this.helper.getShopCartItemByProductId(product.id);
        else item=shopCartItemHashMap.get(product.id);
        if (item == null || item.quantity == 0) {
            handle.item_shopping_cart_num_edittext.setVisibility(View.INVISIBLE);
            handle.item_shopping_cart_num_minus_image.setVisibility(View.INVISIBLE);
        } else {
            handle.item_shopping_cart_num_edittext.setVisibility(View.VISIBLE);
            handle.item_shopping_cart_num_minus_image.setVisibility(View.VISIBLE);
            handle.item_shopping_cart_num_edittext.setText("" + item.quantity);
        }
//        mDesc.setText(Html.fromHtml(product.desc));
        return convertView;
    }

    final class Handle {
        NetworkImageView mImageView;
        TextView mName;
        TextView mPrice;
        TextView mDesc;

        View item_shopping_cart_num_minus_image;
        EditText item_shopping_cart_num_edittext;
        View item_shopping_cart_num_plus_image;
    }

    @Override
    protected void onReachBottom() {

    }

    @Override
    public void onClick(View v) {

//        Integer position = (Integer) v.getTag();
//        Log.e("====", "===" + position);

        if(v.getTag() instanceof  Product){
            Product item = (Product) v.getTag();
            if (item == null) {
                return;
            }
            Intent intent = new Intent(this.context, ProductDetailActivity.class);
            intent.putExtra("id", item.id);
            intent.putExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, Constant.DEFAULT_ILLEGAL_PRODUCT_TYPE);
            this.context.startActivity(intent);
        }
    }
    public void SetOnSetHolderClickListener(HolderClickListener holderClickListener){
        this.mHolderClickListener = holderClickListener;
    }
    public interface HolderClickListener{
        public void onHolderClick(Drawable drawable,int[] start_location,View v);
    }
}
