package com.socialbusiness.dev.orangebusiness.activity.contact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Group;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.KWAsyncTask;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.UserGroupUtil;

import java.util.Hashtable;
import java.util.List;

import io.rong.imlib.RongIMClient;

public class GroupListActivity extends BaseActivity {

    private static final int REQUEST_CODE_CREATE_GROUP = 121;

    private ListView mGroupLV;

    private GroupListAdapter mAdapter;
    private KWAsyncTask mRequestTask;
    private User mLoginUser;
    private RefreshReceiver mRefreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        findViews();
        registenerListeners();
        loadData();
    }

    private void findViews() {
        setTitle("群列表");
        mGroupLV = (ListView) findViewById(R.id.activity_group_list);
    }

    private void registenerListeners() {
        mGroupLV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Group group = (Group) parent.getAdapter().getItem(position);
                Uri uri = Uri.parse(CustomRongSecActivity.SCHEME + getPackageName()).buildUpon()
                        .appendPath(CustomRongSecActivity.PATH_conversation).appendPath(RongIMClient.ConversationType.GROUP.getName().toLowerCase())
                        .appendQueryParameter(CustomRongSecActivity.TARGETID, group.id).appendQueryParameter(CustomRongSecActivity.TITLE, group.name).build();


                Log.e("===","==="+uri);
                Intent intent=new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(CustomRongSecActivity.REAL_GROUP_ID,group.id);
                startActivity(intent);
            }
        });

        setRightIcon(R.drawable.ic_add_address, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupListActivity.this, GroupCreateActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CREATE_GROUP);
            }
        });
    }

    private void loadData() {
        mLoginUser = SettingsManager.getLoginUser();
        if (mLoginUser == null) {
            Intent intent = new Intent(Constant.RECEIVER_NO_LOGIN);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }

        mGroupLV.setVisibility(View.INVISIBLE);
        showLoading();

        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", "1");
        parameters.put("pageSize", APIManager.MAX_PAGE_SIZE);
        APIManager.getInstance(getApplication()).listGroup(parameters, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                showException(error);
            }
        }, new Response.Listener<RequestListResult<Group>>() {
            @Override
            public void onResponse(RequestListResult<Group> response) {
                hideLoading();

                mAdapter = new GroupListAdapter(response.data, mLoginUser);
                mGroupLV.setAdapter(mAdapter);
                mGroupLV.setVisibility(View.VISIBLE);
                UserGroupUtil.initRongyunGroup(response.data);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CREATE_GROUP && resultCode == RESULT_OK) {
            loadData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (mRefreshReceiver != null) {
            unregisterReceiver(mRefreshReceiver);
        }
        super.onDestroy();
    }

    private static class GroupListAdapter extends BaseAdapter {

        private List<Group> mGroups;
        private User mLoginUser;

        public GroupListAdapter(List<Group> groups, User loginUser) {
            mGroups = groups;
            mLoginUser = loginUser;
        }

        @Override
        public int getCount() {
            return mGroups != null ? mGroups.size() : 0;
        }

        @Override
        public Group getItem(int position) {
            return mGroups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = parent.getContext();
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row_contact_item, null);
                holder = new ViewHolder();
                holder.groupIconIV = (NetworkImageView) convertView.findViewById(R.id.row_contact_item_avatar);
                holder.groupNameTV = (TextView) convertView.findViewById(R.id.row_contact_item_name);
                holder.groupIconIV.setDefaultImageResId(R.drawable.ic_group);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Group group = getItem(position);

//			holder.groupNameTV.setText(GroupManager.getGroupDisplayName(group));
            holder.groupNameTV.setText(group.name);

            return convertView;
        }

        private static class ViewHolder {
            NetworkImageView groupIconIV;
            TextView groupNameTV;
        }

    }

    private class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isDestroy()) {
                loadData();
            }
        }

    }
}
