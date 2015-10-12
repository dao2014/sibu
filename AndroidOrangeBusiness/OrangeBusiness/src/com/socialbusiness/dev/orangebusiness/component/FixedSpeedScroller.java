package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FixedSpeedScroller extends Scroller {
	private int mDuration = 1000;
	private boolean mUseCustomDuration = true;

	public FixedSpeedScroller(Context context) {
		super(context);
	}

	public FixedSpeedScroller(Context context, Interpolator interpolator) {
		super(context, interpolator);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		// Ignore received duration, use fixed one instead
		if (mUseCustomDuration) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		} else {
			super.startScroll(startX, startY, dx, dy, duration);
		}
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy) {
		// Ignore received duration, use fixed one instead
		if (mUseCustomDuration) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		} else {
			super.startScroll(startX, startY, dx, dy);
		}
	}

	public void setmDuration(int time) {
		mDuration = time;
	}

	public int getmDuration() {
		return mDuration;
	}

	public boolean isUseCustomDuration() {
		return mUseCustomDuration;
	}

	public void setUseCustomDuration(boolean mUseCustomDuration) {
		this.mUseCustomDuration = mUseCustomDuration;
	}

}