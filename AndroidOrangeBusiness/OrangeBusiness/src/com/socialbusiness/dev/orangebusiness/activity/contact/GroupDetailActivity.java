/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.socialbusiness.dev.orangebusiness.activity.contact;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.AlertDialog;
import com.socialbusiness.dev.orangebusiness.component.ExpandGridView;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.GroupDetail;
import com.socialbusiness.dev.orangebusiness.model.GroupUser;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.KWAsyncTask;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.UserGroupUtil;

import java.util.Hashtable;
import java.util.List;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

import static io.rong.imlib.RongIMClient.ConversationNotificationStatus.DO_NOT_DISTURB;
import static io.rong.imlib.RongIMClient.ConversationNotificationStatus.NOTIFY;

public class GroupDetailActivity extends BaseActivity
        implements OnClickListener {
    private static final String TAG = "GroupDetailsActivity";
    private static final int REQUEST_CODE_ADD_USER = 0;
    private static final int REQUEST_CODE_EXIT = 1;
    private static final int REQUEST_CODE_EXIT_DELETE = 2;
    private static final int REQUEST_CODE_CLEAR_ALL_HISTORY = 3;


    public static final String EXTRA_KEY_GROUP_ID = "EXTRA_KEY_GROUP_ID";
    public static final String EXTRA_KEY_GROUP_NAME = "EXTRA_KEY_GROUP_NAME";

    private ExpandGridView userGridview;
    private String groupId;
    //	private ProgressBar loadingPB;
    private Button exitBtn;
    private Button deleteBtn;
    private GridAdapter adapter;
    private int referenceWidth;
    private int referenceHeight;
    private ProgressDialog progressDialog;

    private RelativeLayout rl_switch_block_groupmsg;
    /**
     * 屏蔽群消息imageView
     */
    private ImageView iv_switch_block_groupmsg;
    /**
     * 关闭屏蔽群消息imageview
     */
    private ImageView iv_switch_unblock_groupmsg;

    //清空所有聊天记录
    private RelativeLayout clearAllHistory;

    private KWAsyncTask mRequestTask;

    private RefreshViewReceiver mRefreshViewReceiver;

    private String mDisplayGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        findViews();
        registerListeners();
        setupData();


//		mRefreshViewReceiver = new RefreshViewReceiver();
//		IntentFilter filter = new IntentFilter(EasemodManager.BROADCAST_NOTIFY_REFRESH_VIEW);
//		registerReceiver(mRefreshViewReceiver, filter);

    }

    private void findViews() {
        clearAllHistory = (RelativeLayout) findViewById(R.id.clear_all_history);
        userGridview = (ExpandGridView) findViewById(R.id.gridview);
//		loadingPB = (ProgressBar) findViewById(R.id.progressBar);
        exitBtn = (Button) findViewById(R.id.btn_exit_grp);
        deleteBtn = (Button) findViewById(R.id.btn_exitdel_grp);
        rl_switch_block_groupmsg = (RelativeLayout) findViewById(R.id.rl_switch_block_groupmsg);
        iv_switch_block_groupmsg = (ImageView) findViewById(R.id.iv_switch_block_groupmsg);
        iv_switch_unblock_groupmsg = (ImageView) findViewById(R.id.iv_switch_unblock_groupmsg);

        Drawable referenceDrawable = getResources().getDrawable(R.drawable.smiley_add_btn);
        referenceWidth = referenceDrawable.getIntrinsicWidth();
        referenceHeight = referenceDrawable.getIntrinsicHeight();
    }

    private void registerListeners() {
        adapter = new GridAdapter(GroupDetailActivity.this, R.layout.griditem_group_user_item, null);
        userGridview.setAdapter(adapter);

        rl_switch_block_groupmsg.setOnClickListener(this);

        // 设置OnTouchListener
        userGridview.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (adapter.isInDeleteMode) {
                            adapter.isInDeleteMode = false;
                            adapter.notifyDataSetChanged();
                            return true;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        clearAllHistory.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailActivity.this, AlertDialog.class);
                intent.putExtra("cancel", true);
                intent.putExtra("titleIsCancel", true);
                intent.putExtra("msg", "确定清空此群的聊天记录吗？");
                startActivityForResult(intent, REQUEST_CODE_CLEAR_ALL_HISTORY);
            }
        });
    }

    private void setupData() {
        // 获取传过来的groupid
        groupId = getIntent().getStringExtra(EXTRA_KEY_GROUP_ID);
        mDisplayGroupName = getIntent().getStringExtra(EXTRA_KEY_GROUP_NAME);
        Log.e("=========", "=======" + groupId);
        if (groupId == null) {
            return;
        }

        // 保证每次进详情看到的都是最新的group
        updateGroup();
    }

    RequestResult<GroupDetail> responseTmp;

    protected void updateGroup() {
        showProgressDialog();
        Hashtable<String, String> param = new Hashtable();
        param.put(APIManager.KEY_PAGE_SIZE, APIManager.MAX_PAGE_SIZE);
        param.put(APIManager.KEY_PAGE, "1");
        param.put("id", groupId);
        APIManager.getInstance(this).groupListUsers(param, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
            }
        }, new Response.Listener<RequestResult<GroupDetail>>() {
            @Override
            public void onResponse(RequestResult<GroupDetail> response) {
                hideProgressDialog();
                Log.e("======", "==" + response.data);
                if(hasError(response)){
                    return;
                }
                responseTmp = response;
                setTitle(String.format("%s(%s人)", mDisplayGroupName, response.data.users.size()));
                adapter.onDataChange(response.data.users);
                displayButtonByGroupType();


            }
        });
//

    }

    User loginUser;

    private void displayButtonByGroupType() {
        if (isDestroy()) {
            return;
        }
        loginUser = SettingsManager.getLoginUser();
        if (loginUser == null) {
            sendBroadcast(new Intent(Constant.RECEIVER_NO_LOGIN));
            return;
        }

        if (GroupDetail.OWNER == responseTmp.data.isOwner) {
            exitBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.VISIBLE);
            adapter.addMemberAble = true;
            adapter.deleteMemberAble = true;
        }else{

            exitBtn.setVisibility(View.VISIBLE);
            if(responseTmp.data.isAllowInvite==GroupDetail.AllowInvite){
                adapter.addMemberAble = true;
                adapter.deleteMemberAble = false;
            }else{
                adapter.addMemberAble = false;
                adapter.deleteMemberAble = false;
            }
            RongIM.getInstance().getConversationNotificationStatus(RongIMClient.ConversationType.GROUP, groupId, new RongIMClient.GetConversationNotificationStatusCallback() {
                @Override
                public void onSuccess(final RongIMClient.ConversationNotificationStatus conversationNotificationStatus) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rl_switch_block_groupmsg.setVisibility(View.VISIBLE);

                            switch (conversationNotificationStatus) {
                                case DO_NOT_DISTURB:
                                    iv_switch_block_groupmsg.setVisibility(View.VISIBLE);
                                    iv_switch_unblock_groupmsg.setVisibility(View.INVISIBLE);
                                    break;
                                case NOTIFY:
                                    iv_switch_block_groupmsg.setVisibility(View.INVISIBLE);
                                    iv_switch_unblock_groupmsg.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                    });
                }

                @Override
                public void onError(ErrorCode errorCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rl_switch_block_groupmsg.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }


//        // 如果自己是群主，显示解散按钮
//        if (GroupDetail.OWNER == responseTmp.data.isOwner) {
//            if (groupId.equals(loginUser.masterGroupHxId)) {
//                //主群，不能解散
//                exitBtn.setVisibility(View.GONE);
//                deleteBtn.setVisibility(View.GONE);
//                adapter.addMemberAble = false;
//                adapter.deleteMemberAble = false;
//            } else {
//                //显示解散按钮
//                exitBtn.setVisibility(View.GONE);
//                deleteBtn.setVisibility(View.VISIBLE);
//                adapter.addMemberAble = true;
//            }
//            //群主不能屏蔽群消息，故隐藏此项
//            rl_switch_block_groupmsg.setVisibility(View.GONE);
//        } else {
//            if (groupId.equals(loginUser.upMasterGroupHxId)) {
//                //上一级主群，不能退出
//                exitBtn.setVisibility(View.GONE);
//                deleteBtn.setVisibility(View.GONE);
//                adapter.addMemberAble = false;
//                adapter.deleteMemberAble = false;
//            } else {
//                //显示退出按钮
//                exitBtn.setVisibility(View.VISIBLE);
//                deleteBtn.setVisibility(View.GONE);
//            }
////			if (group.getMsgBlocked()) {
////	        	iv_switch_block_groupmsg.setVisibility(View.VISIBLE);
////				iv_switch_unblock_groupmsg.setVisibility(View.INVISIBLE);
////	        }
////			else {
////	        	iv_switch_block_groupmsg.setVisibility(View.INVISIBLE);
////				iv_switch_unblock_groupmsg.setVisibility(View.VISIBLE);
////	        }
//
//
//
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(GroupDetailActivity.this);
                progressDialog.setCanceledOnTouchOutside(false);
//				progressDialog.show();
            }
            switch (requestCode) {
                case REQUEST_CODE_ADD_USER:// 添加群成员
//				final String[] newmembers = data.getStringArrayExtra("newmembers");
                    updateGroup();
                    break;
                case REQUEST_CODE_EXIT: // 退出群
                    progressDialog.setMessage("正在退出群聊...");
                    progressDialog.show();
                    exitGrop();
                    break;
                case REQUEST_CODE_EXIT_DELETE: // 解散群
                    progressDialog.setMessage("正在解散群聊...");
                    progressDialog.show();
                    deleteGrop();
                    break;
                case REQUEST_CODE_CLEAR_ALL_HISTORY:
                    //清空此群聊的聊天记录
                    progressDialog.setMessage("正在清空群消息...");
                    progressDialog.show();
                    clearGroupHistory();
                    break;

                default:
                    break;
            }
        }
    }


    /**
     * 点击退出群组按钮
     *
     * @param view
     */
    public void exitGroup(View view) {
        startActivityForResult(new Intent(this, ExitGroupDialog.class), REQUEST_CODE_EXIT);

    }

    /**
     * 点击解散群组按钮
     *
     * @param view
     */
    public void exitDeleteGroup(View view) {
        startActivityForResult(new Intent(this, ExitGroupDialog.class).putExtra("deleteToast", getString(R.string.dissolution_group_hint)),
                REQUEST_CODE_EXIT_DELETE);

    }


    /**
     * 清空群聊天记录
     */
    public void clearGroupHistory() {

        RongIM.getInstance().clearMessages(this, RongIMClient.ConversationType.GROUP, groupId);
//		EMChatManager.getInstance().clearConversation(group.getGroupId());
        progressDialog.dismiss();
//		adapter.refresh(EMChatManager.getInstance().getConversation(toChatUsername));

    }


    /**
     * 退出群组
     */
    private void exitGrop() {

        Hashtable<String, String> params = new Hashtable<>();
        params.put("groupId", groupId);
        params.put("userId", loginUser.id);
        APIManager.getInstance(GroupDetailActivity.this).removeMember(params, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                showException(error);
            }
        }, new Response.Listener<RequestResult<?>>() {
            @Override
            public void onResponse(RequestResult<?> response) {
                progressDialog.dismiss();
                if(response.code==RequestResult.RESULT_SUCESS){
                    RongIM.getInstance().quitGroup(groupId, null);
                    RCloudContext.getInstance().getGroupCache().remove(groupId);
                    Log.e("groupId_delete===","=="+groupId);
                    UserGroupUtil.getGroups(getMyApplication());
                    goToMainActivity();
                }else{
                    hasError(response);
                }

            }
        });


    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * 删除群成员
     *
     * @param user
     */
    protected void deleteMembersFromGroup(final GroupUser user) {
        if (loginUser.id.equals(user.userId)) {
            startActivity(new Intent(GroupDetailActivity.this, AlertDialog.class).putExtra("msg", "不能删除自己"));
            return;
        }
        final ProgressDialog deleteDialog = new ProgressDialog(GroupDetailActivity.this);
        deleteDialog.setMessage("正在移除...");
        deleteDialog.setCanceledOnTouchOutside(false);
        deleteDialog.show();
        Hashtable<String, String> params = new Hashtable<>();
        params.put("groupId", groupId);
        params.put("userId", user.userId);
        APIManager.getInstance(GroupDetailActivity.this).removeMember(params, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                deleteDialog.dismiss();
                showException(error);
            }
        }, new Response.Listener<RequestResult<?>>() {
            @Override
            public void onResponse(RequestResult<?> response) {
                adapter.isInDeleteMode = false;
                deleteDialog.dismiss();
//                                notifyDataSetChanged();
                updateGroup();
            }
        });
    }

    /**
     * 解散群组
     */
    private void deleteGrop() {
        Hashtable<String, String> param = new Hashtable<>();
        param.put("groupId", groupId);
        APIManager.getInstance(this).removeGroup(param, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                showException(error);
            }
        }, new Response.Listener<RequestResult<?>>() {
            @Override
            public void onResponse(RequestResult<?> response) {
                progressDialog.dismiss();
                if(response.code==RequestResult.RESULT_SUCESS){
//                    RongIM.getInstance().clearMessages(GroupDetailActivity.this, RongIMClient.ConversationType.GROUP,groupId);
                    RongIM.getInstance().quitGroup(groupId,null);
                    Log.e("groupId_delete===","=="+groupId);
                    RCloudContext.getInstance().getGroupCache().remove(groupId);
                    UserGroupUtil.getGroups(getMyApplication());
                    goToMainActivity();
                }else{
                    hasError(response);
                }
            }
        });

    }

    /**
     * 群组成员gridadapter
     *
     * @author admin_new
     */
    private class GridAdapter extends BaseAdapter {

        private int res;
        public boolean isInDeleteMode;
        public boolean deleteMemberAble;
        public boolean addMemberAble;
        public List<GroupUser> objects;

        public void onDataChange(List<GroupUser> users) {
            adapter.objects = users;
            adapter.notifyDataSetChanged();
        }

        public GridAdapter(Context context, int textViewResourceId, List<GroupUser> objects) {
            this.objects = objects;
            res = textViewResourceId;
            isInDeleteMode = false;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(GroupDetailActivity.this).inflate(res, null);
                holder = new ViewHolder();
                holder.avatarIV = (NetworkImageView) convertView.findViewById(R.id.griditem_group_user_item_avatar);
                holder.nicknameTV = (TextView) convertView.findViewById(R.id.griditem_group_user_item_userNickName);
                holder.badgeView = convertView.findViewById(R.id.griditem_group_user_item_badgeDelete);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(referenceWidth, referenceHeight);
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                holder.avatarIV.setLayoutParams(lp);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.nicknameTV.setText("");
//			final Button button = (Button) convertView.findViewById(R.id.button_avatar);
            // 最后一个item，减人按钮
            if (position == getCount() - 1) {
                // 设置成删除按钮
//				button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.smiley_minus_btn, 0, 0);
                holder.avatarIV.setDefaultImageResId(R.drawable.smiley_minus_btn);
                holder.avatarIV.setImageUrl(null, null);
                // 如果不是创建者或者没有相应权限，不提供加减人按钮
                if (!deleteMemberAble) {
                    // if current user is not group admin, hide add/remove btn
                    convertView.setVisibility(View.GONE);
//                    parent.removeView(convertView);
                } else { // 显示删除按钮
                    if (isInDeleteMode) {
                        // 正处于删除模式下，隐藏删除按钮
                        convertView.setVisibility(View.GONE);
                    } else {
                        // 正常模式
                        convertView.setVisibility(View.VISIBLE);
//						convertView.findViewById(R.id.badge_delete).setVisibility(View.INVISIBLE);
                        holder.badgeView.setVisibility(View.GONE);
                    }
                    convertView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isInDeleteMode = true;
                            notifyDataSetChanged();
                        }
                    });
                }
            } else if (position == getCount() - 2) { // 添加群组成员按钮
                holder.avatarIV.setDefaultImageResId(R.drawable.smiley_add_btn);
                holder.avatarIV.setImageUrl(null, null);
                //如果不是创建者或者没有相应权限
                if (!addMemberAble) {
                    // if current user is not group admin, hide add/remove btn
                    convertView.setVisibility(View.GONE);
                } else {
                    // 正处于删除模式下,隐藏添加按钮
                    if (isInDeleteMode) {
                        convertView.setVisibility(View.GONE);
                    } else {
                        convertView.setVisibility(View.VISIBLE);
                        holder.badgeView.setVisibility(View.GONE);
                    }
                    convertView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 进入选人页面
                            startActivityForResult(
                                    (new Intent(GroupDetailActivity.this, GroupAddMemberActivity.class)
                                            .putExtra(EXTRA_KEY_GROUP_ID, groupId)),
                                    REQUEST_CODE_ADD_USER);
                        }
                    });
                }
            } else { // 普通item，显示群组成员
                convertView.setVisibility(View.VISIBLE);
                final GroupUser groupUser = (GroupUser) getItem(position);
                holder.avatarIV.setDefaultImageResId(R.drawable.ic_person);

                if (groupUser != null) {
                    holder.nicknameTV.setText(groupUser.nickName);
                    holder.avatarIV.setImageUrl(APIManager.toAbsoluteUrl(groupUser.head),
                            APIManager.getInstance(getApplicationContext()).getImageLoader());
                }

                if (isInDeleteMode) {
                    // 如果是删除模式下，显示减人图标
                    holder.badgeView.setVisibility(View.VISIBLE);
                } else {
                    holder.badgeView.setVisibility(View.GONE);
                }
                holder.badgeView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isInDeleteMode) {
                            // 如果是删除自己，return

                            //删除用户
                            deleteMembersFromGroup(groupUser);
                        } else {
                            // 正常情况下点击user，可以进入用户详情或者聊天页面等等
                            // startActivity(new
                            // Intent(GroupDetailsActivity.this,
                            // ChatActivity.class).putExtra("userId",
                            // user.getUsername()));
                        }
                    }


                });
            }
            return convertView;
        }

        @Override
        public int getCount() {
            if (this.objects == null || objects.size() == 0) {
                return 0;
            }
            return this.objects.size() + 2;
        }

        @Override
        public Object getItem(int position) {
            return this.objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            NetworkImageView avatarIV;
            TextView nicknameTV;
            View badgeView;
        }
    }


    public void back(View view) {
        setResult(RESULT_OK);
        finish();
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        hideProgressDialog();
        if (mRefreshViewReceiver != null) {
            unregisterReceiver(mRefreshViewReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_switch_block_groupmsg:
                if (iv_switch_block_groupmsg.getVisibility() == View.VISIBLE) {
                    System.out.println("change to unblock group msg");

                    showProgressDialog();
                    RongIM.getInstance().setConversationNotificationStatus(RongIMClient.ConversationType.GROUP, groupId, NOTIFY, new RongIMClient.SetConversationNotificationStatusCallback() {
                        @Override
                        public void onSuccess(RongIMClient.ConversationNotificationStatus conversationNotificationStatus) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv_switch_block_groupmsg.setVisibility(View.INVISIBLE);
                                    iv_switch_unblock_groupmsg.setVisibility(View.VISIBLE);
                                    hideProgressDialog();
                                }
                            });

                        }

                        @Override
                        public void onError(ErrorCode errorCode) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressDialog();
                                }
                            });
                        }
                    });

                } else {
                    System.out.println("change to block group msg");
                    showProgressDialog();
                    RongIM.getInstance().setConversationNotificationStatus(RongIMClient.ConversationType.GROUP, groupId, DO_NOT_DISTURB, new RongIMClient.SetConversationNotificationStatusCallback() {
                        @Override
                        public void onSuccess(RongIMClient.ConversationNotificationStatus conversationNotificationStatus) {
                          runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  iv_switch_block_groupmsg.setVisibility(View.VISIBLE);
                                  iv_switch_unblock_groupmsg.setVisibility(View.INVISIBLE);
                                  hideProgressDialog();
                              }
                          });

                        }

                        @Override
                        public void onError(ErrorCode errorCode) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressDialog();
                                }
                            });

                        }
                    });


                }
                break;
            default:
        }

    }

    private class RefreshViewReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

    }


}
