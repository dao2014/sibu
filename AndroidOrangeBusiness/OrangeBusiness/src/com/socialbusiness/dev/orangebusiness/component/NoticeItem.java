package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.model.Notice;

public class NoticeItem extends LinearLayout {
	public TextView mNoticeTitle;
	public TextView mNoticeTime;
	public TextView mNoticeAuthor;

	public NoticeItem(Context context) {
		super(context);
		initView();
	}

	public NoticeItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.view_notice_item, this);
		findView(view);
	}

	public void findView(View view) {
		mNoticeTitle = (TextView) view
				.findViewById(R.id.view_notice_item_title);
		mNoticeTime = (TextView) view.findViewById(R.id.view_notice_item_time);
        mNoticeAuthor = (TextView) view
				.findViewById(R.id.view_notice_item_issuer);
	}

    public void setNoticeData(Notice notice) {
        if (notice != null) {
            mNoticeTitle.setText(notice.title);
            mNoticeTime.setText(notice.date);
            mNoticeAuthor.setText(notice.author != null ? notice.author : "");
        }
    }
}
