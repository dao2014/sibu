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
import com.socialbusiness.dev.orangebusiness.model.User;

/**
 * Created by enwey on 2014/11/3.
 */
public class VipItem extends RelativeLayout {
    private CircleImageView mImage;
    private TextView mVipName;
    private TextView mVipLevel;

    public VipItem(Context context) {
        super(context);
        initView();
    }

    public VipItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_vip_item, this);
        findView(view);
    }

    private void findView(View view) {
        mVipName = (TextView) view.findViewById(R.id.view_vip_item_name);
        mImage = (CircleImageView) view.findViewById(R.id.view_vip_item_image);
        mVipLevel = (TextView) view.findViewById(R.id.view_vip_item_level);

        mImage.setDefaultImageResId(R.drawable.image_head_userinfo);
        mImage.setErrorImageResId(R.drawable.image_head_userinfo);
    }
    public void setVipValue(User user, String mLevelName) {
        if (user != null) {
            mImage.setImageUrl(APIManager.toAbsoluteUrl(user.head),
                    APIManager.getInstance(getContext()).getImageLoader());
            mVipName.setText(user.nickName);
            if (mLevelName != null) {
                mVipLevel.setText(mLevelName);
            }
        }
    }
}
