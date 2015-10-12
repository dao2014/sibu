package com.socialbusiness.dev.orangebusiness.activity.me;

import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.model.Notice;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;

public class NoticeDetailActivity_Old extends BaseActivity {
    private TextView mTitle;
    private TextView mDate;
    private TextView mAuthor;
    private TextView mContent;
    private Notice mNotice;
    private String mID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);
        setUp();
        findView();
        callAPIGetNotice();
    }

    public void setUp() {
        setTitle(R.string.notice_detail);
        mID = getIntent().getStringExtra("id");
    }

    public void findView() {
//        mTitle = (TextView) this.findViewById(R.id.activity_notice_detail_title);
//        mDate = (TextView) this.findViewById(R.id.activity_notice_detail_time);
//        mAuthor = (TextView) this.findViewById(R.id.activity_notice_detail_issuername);
//        mContent = (TextView) this.findViewById(R.id.activity_notice_detail_content);
    }

    public void callAPIGetNotice() {
        showLoading();
        hideNoData();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("id", mID);
        APIManager.getInstance(this).getNotice(parameters, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
//                ToastUtil.show(NoticeDetailActivity.this, error.getMessage());
            }
        }, new Response.Listener<RequestResult<Notice>>() {
            @Override
            public void onResponse(RequestResult<Notice> response) {
                hideLoading();
                if (response != null) {
                    if (response.data != null) {
                        mNotice = response.data;
                        setNoticeDate(mNotice);
                    }else {
                        showNoDataTips();
                    }
                }

            }
        });
    }

    public void setNoticeDate(Notice notice) {
        mTitle.setText(notice.title+ "");
        mDate.setText(notice.date+ "");
        mAuthor.setText(notice.author != null ? notice.author : "");
        mContent.setText(notice.content+ "");
    }
}
