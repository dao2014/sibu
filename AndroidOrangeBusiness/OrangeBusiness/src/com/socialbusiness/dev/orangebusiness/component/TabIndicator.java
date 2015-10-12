package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.component.FixedViewPager.IndicateLintener;
import com.socialbusiness.dev.orangebusiness.util.CommonUtil;

public class TabIndicator extends View implements IndicateLintener {
    private static final String TAG = "TabIndicator";
    private Paint mPaintSelectRect = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintUnSelectRect = new Paint(Paint.ANTI_ALIAS_FLAG);
    Path tixpath = new Path();
    private FixedViewPager mViewPager;
    private int mCurrentPage;
    private float mPageOffset;
    private int mScrollState;
    private int mLastOffset;

    private int textWidth;

    private TextView textView;

    public void setTextView(TextView textView) {
        this.textView = textView;
        String text = this.textView.getText().toString();
        int lenght = CommonUtil.getTextViewLength(this.textView, text);
        if (text.length() > 3) {
//            lenght+=((text.length()-3)*20);
            lenght += (lenght / text.length() * 2);
        }
        setTextWidth(lenght);
    }

    public int getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    private int mSelectRectHeight;
    private int mUnSelectRectHeight;

    public TabIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public TabIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TabIndicator(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        mSelectRectHeight = (int) getResources().getDimension(R.dimen.view_tab_indictor_select_rect_height);
        mUnSelectRectHeight = (int) getResources().getDimension(R.dimen.view_tab_indictor_unselect_rect_height);
        mPaintSelectRect.setColor(getResources().getColor(R.color.tab_indicator_select_rect));
        mPaintUnSelectRect.setColor(getResources().getColor(R.color.tab_indicator_unselect_rect));
    }

    public void setViewPager(FixedViewPager view) {
        if (view == null) {
            mViewPager = view;
            mCurrentPage = 0;
            invalidate();
            return;
        }
        if (mViewPager == view) {
            return;
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mCurrentPage = mViewPager.getCurrentItem();
        // Log.v(TAG, "mCurrentPage>>" + mCurrentPage);
        mViewPager.setIndicateLintener(this);
        invalidate();
    }

    int tabOffetMius = 40;
    int ridus = 4;//梯形偏移量

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mViewPager == null) {
            return;
        }
        int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }
//		canvas.drawRect(0, mSelectRectHeight-mUnSelectRectHeight, getWidth(), mSelectRectHeight, mPaintUnSelectRect);
        int tabWidth = getWidth() / count;

        if (textView != null)
            tabOffetMius = (tabWidth - textWidth) / 2;
        int offset = mPageOffset != 0 ? (int) ((mPageOffset + mCurrentPage) * tabWidth) : mCurrentPage * tabWidth;
//		Log.v(TAG, "offset>>" + offset);
        //绘画长方形
        canvas.drawRect(offset + tabOffetMius, 0, tabWidth + offset - tabOffetMius, mSelectRectHeight, mPaintSelectRect);


        //绘画梯形
//        tixpath.reset();
//        tixpath.moveTo(offset + tabOffetMius , 0); //左顶点 也即起始点
//        tixpath.lineTo(tabWidth + offset - tabOffetMius, 0); //右顶点
//        tixpath.lineTo(tabWidth + offset - tabOffetMius+ridus, mSelectRectHeight); //右底部
//        tixpath.lineTo(offset + tabOffetMius-ridus, mSelectRectHeight); // 左底部
//        tixpath.close();
//        canvas.drawPath(tixpath, mPaintSelectRect);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//		Log.v(TAG, "onPageScrolled>>" + position + "," + positionOffset + "," + positionOffsetPixels);
        mCurrentPage = position;
        this.mPageOffset = positionOffset;
        postInvalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        this.mScrollState = state;

    }

    @Override
    public void onPageSelected(int position) {

    }
}
