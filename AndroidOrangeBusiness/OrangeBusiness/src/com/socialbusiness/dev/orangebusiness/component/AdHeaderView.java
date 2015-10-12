package com.socialbusiness.dev.orangebusiness.component;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.model.Ad;

public class AdHeaderView extends LinearLayout {
	private FixedViewPager viewpage;
	private IndecatorView indecatorView;
	private AdHeaderPagerAdapter adapter;
	private ArrayList<Ad> aDList = new ArrayList<Ad>();;
	public AdHeaderView(Context context) {
		super(context);
		initView();
	}

	public AdHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private void initView(){
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.view_adheader_item, this);
		adapter = new AdHeaderPagerAdapter();
		findView(view);
	}
	
	private void findView(View view){
		viewpage = (FixedViewPager) view.findViewById(R.id.view_adheader_item_viewpager);
		indecatorView = (IndecatorView) view.findViewById(R.id.view_adheader_item_indecatorview);
		viewpage.setAdapter(adapter);
//		indecatorView.setIndicatorImage(R.drawable.swipe_indicator_off_green, R.drawable.swipe_indicator_on_green);
		indecatorView.setIndicatorColor(0xAAFFFFFF, 0xFFFFFF);
		regsiterListener();
	}
	
	private void regsiterListener(){
		viewpage.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				indecatorView.setSelected(position%4);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
	
	public class AdHeaderPagerAdapter extends PagerAdapter{
		
		@Override
		public int getCount() {
			return aDList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override  
		public int getItemPosition(Object object) {  
		    return POSITION_NONE;  
		} 
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			NetworkImageView imageView = new NetworkImageView(container.getContext());
			imageView.setScaleType(ScaleType.FIT_XY);
//			imageView.setImageResource(R.drawable.banner_company_list);
			ViewPager.LayoutParams lp = new ViewPager.LayoutParams();
			lp.width = ViewPager.LayoutParams.MATCH_PARENT;
			lp.height = ViewPager.LayoutParams.MATCH_PARENT;
			String bannerUrl = aDList.get(position).imageUrl;
			imageView.setImageUrl(APIManager.toAbsoluteUrl(bannerUrl), APIManager.getInstance(getContext()).getImageLoader());
			container.addView(imageView);
			return imageView ;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
		
		
		
	}
	
	
	public void setAdData(ArrayList<Ad> list){ // 设置广告
		aDList = list;
		viewpage.setCurrentItem(0);
		indecatorView.setCount(aDList.size());
		adapter.notifyDataSetChanged();
	}

}
