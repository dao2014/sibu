package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;

public class IndecatorView extends View {
	
	private final String TAG = "IndecatorView";
	
	private int mCount;
	private int mSelection;
	
	private Paint mOnPaint;
	private Paint mOffPaint;
	
	private int mRadius;
	
	private int mSpace = 5;
	
	private int mGravity = Gravity.CENTER;
	
	public IndecatorView(Context context) {
		super(context);
	}

	public IndecatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setIndicatorColor(int offColor,int onColor) {
		setIndicatorColor(offColor, onColor, 3);
	}
	
	public void setIndicatorColor(int offColor,int onColor, int radiusOfDp) {
		if(mOffPaint == null) {
			mOffPaint = new Paint();
			mOffPaint.setAntiAlias(true);
		}
		if(mOnPaint == null) {
			mOnPaint = new Paint();
			mOnPaint.setAntiAlias(true);
		}
		mRadius = AndroidUtil.dip2px(getContext(), radiusOfDp);
		mOffPaint.setColor(offColor);
		mOnPaint.setColor(onColor);
		invalidate();
	}

	public void setSpace(int space) {
		mSpace = space;
	}
	
	public int getCount() {
		return mCount;
	}

	public void setCount(int mCount) {
		this.mCount = mCount;
	}
	
	public void setSelected(int position){
		this.mSelection = position;
		invalidate();
	}
	
	public void setGravity(int gravity) {
		this.mGravity = gravity;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(mCount < 1){
			return;
		}
		if(mOffPaint != null && mOnPaint != null && mRadius > 0){
			int totalWidth = mRadius * 2 * mCount + mSpace * (mCount - 1);
			int startX = 0;
			if(mGravity == Gravity.LEFT) {
				startX = 0;
			}
			else if(mGravity == Gravity.CENTER) {
				startX = (this.getMeasuredWidth() - totalWidth) / 2;;
			}
			else {
				startX = this.getMeasuredWidth() - totalWidth;
			}
			
			startX += (getPaddingLeft() - getPaddingRight());
			
			for(int i = 0; i< mCount; i++){
				final float cx = startX + mRadius;
				final float cy = this.getMeasuredHeight() / 2;
				if(mSelection == i){
					canvas.drawCircle(cx, cy, mRadius, mOnPaint);
				}
				else {
					canvas.drawCircle(cx, cy, mRadius, mOffPaint);
				}
				startX += mRadius * 2 + mSpace;
			}
		}
		
	}
	
	public void release(){

	}
	
}
