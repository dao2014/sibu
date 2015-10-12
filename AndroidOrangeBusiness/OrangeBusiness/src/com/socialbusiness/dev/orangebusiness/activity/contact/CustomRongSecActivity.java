package com.socialbusiness.dev.orangebusiness.activity.contact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.util.Log;

import io.rong.imkit.RongSecActivity;

/**
 * Created by SAP100 on 2015/4/2.
 */
public class CustomRongSecActivity extends RongSecActivity {

    public final static String PATH_GROUP = "group";
    public final static String REAL_GROUP_ID = "groupid";
    public final static String PATH_PRIVATE = "private";
    public final static String SCHEME = "rong://";
    public final static String PATH_conversation = "conversation";
    public final static String TARGETID = "targetId";
    public final static String TITLE = "title";


    String lastPath;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        lastPath = getIntent().getData().getLastPathSegment();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.view_header_for_emlib, getBar(), false);
        view.setBackgroundColor(Color.TRANSPARENT);
        getBar().addView(view);
        view.findViewById(R.id.emHeaderBackBtn).setOnClickListener(onclick);
        if (PATH_GROUP.equals(lastPath)) {
            View emHeaderGroupBtn = view.findViewById(R.id.emHeaderGroupBtn);
            emHeaderGroupBtn.setVisibility(View.VISIBLE);
            emHeaderGroupBtn.setOnClickListener(onclick);


        }
        View setImage=findViewById(R.id.rc_conversation_settings_image);
        if(setImage!=null){
            setImage.setVisibility(View.GONE);
        }
        Log.e("CustomRongSecActivity","=="+getIntent().getData().getQueryParameter(TARGETID));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (PATH_GROUP.equals(lastPath)) {
            if (findViewById(io.rong.imkit.demo.R.id.rc_conversation_settings_image) != null) {
                findViewById(io.rong.imkit.demo.R.id.rc_conversation_settings_image).setVisibility(View.GONE);
            }
        }
    }

    View.OnClickListener onclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.emHeaderBackBtn:
                    finish();
                    break;
                case R.id.emHeaderGroupBtn:
                    Intent intent = new Intent(CustomRongSecActivity.this, GroupDetailActivity.class);
                    intent.putExtra(GroupDetailActivity.EXTRA_KEY_GROUP_ID, getIntent().getData().getQueryParameter(TARGETID));
//                    intent.putExtra(GroupDetailActivity.EXTRA_KEY_GROUP_ID,getIntent().getStringExtra(REAL_GROUP_ID));
                    intent.putExtra(GroupDetailActivity.EXTRA_KEY_GROUP_NAME, getIntent().getData().getQueryParameter(TITLE));
                    startActivity(intent);
                    break;
            }
        }
    };
}
