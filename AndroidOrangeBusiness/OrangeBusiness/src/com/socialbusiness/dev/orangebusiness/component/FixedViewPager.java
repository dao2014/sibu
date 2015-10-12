package com.socialbusiness.dev.orangebusiness.component;

import java.lang.reflect.Field;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;



public class FixedViewPager extends ViewPager {

	private static final String TAG = "ItemViewPager";

	private boolean enableTouch = true;

	private IndicateLintener mIndicateLintener;
	private FixedViewPagerLintener mViewPagerLintener;
	private boolean mLooper = false;
	private static int MSG_AUTO_CHNAGE = 0x00000001;
	private FixedSpeedScroller mScroller;
	private static final int SCROLL_DURATION = 2000;
	private static final int AUTO_SCROLL_DELAY = 7 * 1000;

	public FixedViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FixedViewPager(Context context) {
		super(context);
		init();
	}

	public void init() {
		try {
			Field mField = FixedViewPager.class.getDeclaredField("mScroller");
			mField.setAccessible(true);
			mScroller = new FixedSpeedScroller(getContext(), new LinearInterpolator());
			mField.set(this, mScroller);
			mScroller.setmDuration(SCROLL_DURATION);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setupListener();
	}

	private void setupListener() {
		setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (mViewPagerLintener != null) {
					mViewPagerLintener.onPageSelected(position);
				}
				if (mIndicateLintener != null) {
					mIndicateLintener.onPageSelected(position);
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if (mViewPagerLintener != null) {
					mViewPagerLintener.onPageScrolled(position, positionOffset, positionOffsetPixels);
				}

				if (mIndicateLintener != null) {
					mIndicateLintener.onPageScrolled(position, positionOffset, positionOffsetPixels);
				}

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (mViewPagerLintener != null) {
					mViewPagerLintener.onPageScrollStateChanged(state);
				}

				if (mIndicateLintener != null) {
					mIndicateLintener.onPageScrollStateChanged(state);
				}

			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mScroller != null) {
			mScroller.setUseCustomDuration(false);
		}
		if (mLooper) {
			handler.removeMessages(MSG_AUTO_CHNAGE);
		}
		if (enableTouch) {
			super.onTouchEvent(event);
		} else {
			return false;
		}
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			// Log.d(TAG, "ACTION_UP");
			requestDisallowInterceptTouchEvent(true);
			if (mLooper) {
				handler.sendEmptyMessageDelayed(MSG_AUTO_CHNAGE, AUTO_SCROLL_DELAY);
			}
			break;
		}
		case MotionEvent.ACTION_DOWN: {
			// Log.d(TAG, "ACTION_DOWN");
			requestDisallowInterceptTouchEvent(false);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			// Log.d(TAG, "ACTION_MOVE");
			break;
		}
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (enableTouch) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return false;
		}
	}

	public void setEnableTouch(boolean enableTouch) {
		this.enableTouch = enableTouch;
	}

	public static interface IndicateLintener {
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

		public void onPageScrollStateChanged(int state);

		public void onPageSelected(int position);

	}

	public void setIndicateLintener(IndicateLintener indicateLintener) {
		this.mIndicateLintener = indicateLintener;
	}

	public static interface FixedViewPagerLintener {
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

		public void onPageScrollStateChanged(int state);

		public void onPageSelected(int position);

		public void onRestoreInstanceState(Parcelable state);

	}

	public void setViewPagerListener(FixedViewPagerLintener viewPagerLintener) {
		this.mViewPagerLintener = viewPagerLintener;
	}

	public void setLooper(boolean mLooper) {
		this.mLooper = mLooper;
	}

	public boolean isLooper() {
		return mLooper;
	}

	public void setmLooper(boolean mLooper) {
		this.mLooper = mLooper;
	}

	@Override
	public void setAdapter(PagerAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter != null && mLooper) {
			handler.sendEmptyMessageDelayed(MSG_AUTO_CHNAGE, AUTO_SCROLL_DELAY);
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_AUTO_CHNAGE) {
				if (getAdapter() != null && getAdapter().getCount() > 1) {
					if (mScroller != null) {
						mScroller.setUseCustomDuration(true);
					}
					int size = getAdapter().getCount();
					int currentIndex = getCurrentItem();

					if (currentIndex < size - 1) {
						currentIndex = currentIndex + 1;
						setCurrentItem(currentIndex, true);
					} else {
						setCurrentItem(0, true);
					}

				}
				sendEmptyMessageDelayed(MSG_AUTO_CHNAGE, AUTO_SCROLL_DELAY);
			}
			super.handleMessage(msg);
		}
	};

	public void release() {
		if (handler != null) {
			handler.removeMessages(MSG_AUTO_CHNAGE);
		}
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		Log.v(TAG, "onRestoreInstanceState>>" + state);
		if (mViewPagerLintener != null) {
			mViewPagerLintener.onRestoreInstanceState(state);
		}
	}

}
