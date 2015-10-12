package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class KWAutoScrollTextView extends TextView {
	
	private boolean isAutoScrollable;

	@Override
	public boolean isFocused() {
		return isAutoScrollable;
	}

	public KWAutoScrollTextView(Context context) {
		super(context);
		initComponent();
	}

	public KWAutoScrollTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initComponent();
	}

	public KWAutoScrollTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initComponent();
	}
	
	private void initComponent() {
		isAutoScrollable = true;
		setAutoScrollable(isAutoScrollable);
	}
	
	public void setAutoScrollable(boolean scrollable) {
		isAutoScrollable = scrollable;
		setSingleLine(true);
		setEllipsize(scrollable ? TruncateAt.MARQUEE : TruncateAt.END);
		setHorizontallyScrolling(scrollable);
		setMarqueeRepeatLimit(scrollable ? -1 : 0);
	}
}
