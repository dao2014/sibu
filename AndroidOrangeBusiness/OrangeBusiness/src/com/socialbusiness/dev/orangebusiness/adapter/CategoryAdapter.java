package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.me.CoursewareListActivity;
import com.socialbusiness.dev.orangebusiness.activity.product.ProductListActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.Category;

import java.util.List;

public class CategoryAdapter extends BaseAdapter {

    private ViewHolder viewHolder = null;
    private List<Category> categoryList;

    private Context context;
    public boolean isCourseCategory = false;

    private int leftnum = 0;
    private int rightnum = 0;
    private int count = 0;

    private int productTypeInt = Constant.DEFAULT_ILLEGAL_PRODUCT_TYPE;
    public CategoryAdapter(Context context) {
        this.context = context;
    }

    public void setCategoryList(List<Category> list) {
        categoryList = list;
    }
    public void setProductType(int typeInt) {
        this.productTypeInt = typeInt;
    }

    @Override
    public int getCount() {
        if (categoryList != null) {
            return (categoryList.size() % 2 == 0) ? (categoryList.size() / 2) : (categoryList.size() / 2 + 1);
        }
        return 0;
    }

    @Override
    public List<Integer> getItem(int position) {
        return null;
    }

    private void initNumVariables(int position) {
        if (categoryList != null) {
            count = categoryList.size();
        }
        leftnum = position * 2;
        rightnum = position * 2 + 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LinearLayout.inflate(parent.getContext(), R.layout.item_category_list, null);
            viewHolder = new ViewHolder();
            viewHolder.mLayoutLeft = (LinearLayout) convertView.findViewById(R.id.item_product_list_leftLayout);
            viewHolder.mImageLeft = (NetworkImageView) convertView.findViewById(R.id.item_product_list_leftimage);
            viewHolder.mTitleLeft = (TextView) convertView.findViewById(R.id.item_product_list_lefttitle);
            viewHolder.mLayoutRight = (LinearLayout) convertView.findViewById(R.id.item_product_list_rightLayout);
            viewHolder.mImageRight = (NetworkImageView) convertView.findViewById(R.id.item_product_list_rightimage);
            viewHolder.mTitleRight = (TextView) convertView.findViewById(R.id.item_product_list_righttitle);

            viewHolder.mImageLeft.setScaleType(ScaleType.FIT_XY);
            viewHolder.mImageLeft.setErrorImageResId(R.drawable.ic_default);
//            viewHolder.mImageLeft.setDefaultImageResId(R.drawable.ic_default);
            viewHolder.mImageRight.setScaleType(ScaleType.FIT_XY);
            viewHolder.mImageRight.setErrorImageResId(R.drawable.ic_default);
//            viewHolder.mImageRight.setDefaultImageResId(R.drawable.ic_default);

            viewHolder.mLayoutLeft.setVisibility(View.INVISIBLE);
            viewHolder.mLayoutRight.setVisibility(View.INVISIBLE);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (categoryList != null) {
            initNumVariables(position);
            setItemData();
        }

        return convertView;
    }

    private void setItemData() {
        if (categoryList != null) {
            Integer left = leftnum >= count ? null : leftnum;
            Integer right = rightnum >= count ? null : rightnum;

            if (left != null) {
                final Category leftCategory = categoryList.get(leftnum);
                if (leftCategory != null) {
                    viewHolder.mImageLeft.setImageUrl(APIManager.toAbsoluteUrl(leftCategory.image),
                            APIManager.getInstance(context).getImageLoader());
                    viewHolder.mTitleLeft.setText(leftCategory.name + "");
                }
                viewHolder.mLayoutLeft.setVisibility(View.VISIBLE);
                viewHolder.mLayoutRight.setVisibility(View.INVISIBLE);
                viewHolder.mLayoutLeft.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        if (isCourseCategory) {
                            intent.setClass(context, CoursewareListActivity.class);
                        } else {
                            intent.setClass(context, ProductListActivity.class);
                            intent.putExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, productTypeInt);
                        }
                        intent.putExtra("categoryId", leftCategory.id);
                        context.startActivity(intent);
                    }
                });
            }
            if (right != null) {
                final Category rightCategory = categoryList.get(rightnum);
                if (rightCategory != null) {
                    viewHolder.mImageRight.setImageUrl(APIManager.toAbsoluteUrl(rightCategory.image),
                            APIManager.getInstance(context).getImageLoader());
                    viewHolder.mTitleRight.setText(rightCategory.name + "");
                }
                viewHolder.mLayoutRight.setVisibility(View.VISIBLE);
                viewHolder.mLayoutRight.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        if (isCourseCategory) {
                            intent.setClass(context, CoursewareListActivity.class);
                        } else {
                            intent.setClass(context, ProductListActivity.class);
                            intent.putExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, productTypeInt);
                        }
                        intent.putExtra("categoryId", rightCategory.id);
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    private class ViewHolder {
        LinearLayout mLayoutLeft;
        NetworkImageView mImageLeft;
        TextView mTitleLeft;
        LinearLayout mLayoutRight;
        NetworkImageView mImageRight;
        TextView mTitleRight;
    }
}
