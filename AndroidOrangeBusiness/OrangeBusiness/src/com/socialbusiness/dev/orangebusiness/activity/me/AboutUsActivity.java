package com.socialbusiness.dev.orangebusiness.activity.me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;

public class AboutUsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aboutus);
		setUp();
		findView();
		regsiterListeren();
	}
	
	private void setUp(){
		setTitle(R.string.settings_massege_about);
	}
	
	private void findView(){
        TextView aboutPhone=(TextView)findViewById(R.id.about_phone);
        aboutPhone.setText(Html.fromHtml( "<a href=\"\">" + "13802918309" + "</a>"));
        aboutPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:13802918309" ));
                startActivity(intent);
            }
        });
	}
	
	private void regsiterListeren(){
		
	}

}
