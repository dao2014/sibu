package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.kollway.android.imageviewer.activity.ImageViewerActivity;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.component.indicator.IconPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class MainViewPagerAdapter extends PagerAdapter implements IconPagerAdapter, OnClickListener {

    Context context;
    private ImageLoader imageLoader;

    public MainViewPagerAdapter(Context context) {
        this.context = context;
        imageLoader = APIManager.getInstance(this.context).getImageLoader();
    }

    @Override
    public int getCount() {
        if (this.data == null)
            return 0;
        return data.size();
    }

    @Override
    public int getRealCount() {
        if (this.data == null)
            return 0;
        return data.size();
    }

    ArrayList<String> data;
    List<View> views = new ArrayList<View>();

    public void onDataSetChange(ArrayList<String> data) {
        this.data = data;

        if (this.data == null) {
            return;
        }
        int size = this.data.size();


        for (int i = 0; i < size; i++) {
            View convertView = LinearLayout.inflate(this.context, R.layout.adapter_goods_gallery_item, null);
            NetworkImageView image = (NetworkImageView) convertView.findViewById(R.id.adapter_goods_gallery_item_icon);
//            image.setErrorImageResId(R.drawable.ic_default);
//            image.setDefaultImageResId(R.drawable.ic_default);
            image.setImageUrl(APIManager.toAbsoluteUrl(this.data.get(i)),
                    imageLoader);
            convertView.setTag(i);
            convertView.setOnClickListener(this);
            views.add(convertView);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {

        View imageLayout = views.get(position % data.size());
        // view.removeView(imageLayout);
        if (imageLayout.getParent() != view)
            ((ViewPager) view).addView(imageLayout);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public int getIconResId(int index) {
        return R.drawable.index_selector;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public void onClick(View v) {
        int position=(Integer)v.getTag();
        Intent intent = new Intent(this.context, ImageViewerActivity.class);
        intent.putStringArrayListExtra(ImageViewerActivity.KEY_URLS, this.data);
        intent.putExtra(ImageViewerActivity.KEY_INDEX, position);
        this.context.startActivity(intent);

    }

}
