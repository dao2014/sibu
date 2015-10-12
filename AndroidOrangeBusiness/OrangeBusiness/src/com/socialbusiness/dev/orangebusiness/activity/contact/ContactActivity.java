package com.socialbusiness.dev.orangebusiness.activity.contact;

import android.os.Bundle;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.fragment.contact.ContactFragment;

/**
 * Created by zkboos on 2015/7/15.
 */
public class ContactActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getSupportFragmentManager().beginTransaction().replace(R.id.contact_root, new ContactFragment()).commit();
        setTitle("小伙伴");

    }
}
