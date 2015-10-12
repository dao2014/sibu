package com.socialbusiness.dev.orangebusiness.fragment.contact;

import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.contact.CustomRongSecActivity;
import com.socialbusiness.dev.orangebusiness.activity.contact.GroupListActivity;
import com.socialbusiness.dev.orangebusiness.adapter.ContactAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.headerlistview.HeaderListView;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.model.User.LeveledUsers;
import com.socialbusiness.dev.orangebusiness.util.KWAsyncTask;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.MD5;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends BaseFragment {

    private EditText mSearchET;
    private HeaderListView mContactLV;
    private ContactAdapter mAdapter;

    private final List<User> mAllContacts = new ArrayList<User>();
    private LeveledUsers mLeveldUsers;
    private KWAsyncTask mSearchTask;

    @Override
    public void setContentView(LayoutInflater inflater,
                               ViewGroup mLayerContextView) {
        View v = inflater.inflate(R.layout.fragment_contact, mLayerContextView);
        findViews(v);
        registerListeners();
        loadData();
    }

    private void findViews(View v) {
        mSearchET = (EditText) v.findViewById(R.id.fragment_contact_searchET);
        mContactLV = (HeaderListView) v.findViewById(R.id.fragment_contact_contactLV);

        mContactLV.setScrollBarVisible(false);
//        showFragment();
    }

    private void registerListeners() {
        mContactLV.setOnItemClickListener(new HeaderListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int section, int row, int globalPosition, long id) {
                if (mAdapter.isGroupMode() && section == 0) {
                    //点击群组Item
                    Intent intent = new Intent(getActivity(), GroupListActivity.class);
                    startActivity(intent);
                    return;
                }

                User user = mAdapter.getRowItem(section, row);
                if (user != null) {
//					Intent intent = new Intent(getActivity(), ChatActivity.class);
//					intent.putExtra(ChatActivity.EXTRA_KEY_NICKNAME, user.nickName);
//					intent.putExtra(ChatActivity.EXTRA_KEY_USERNAME, MD5.encode(user.id));
//					startActivity(intent);
                    Intent intent = new Intent();
                    intent.setData(Uri.parse(CustomRongSecActivity.SCHEME+getActivity().getPackageName()).buildUpon().appendPath(CustomRongSecActivity.PATH_conversation).appendPath(CustomRongSecActivity.PATH_PRIVATE)
                            .appendQueryParameter(CustomRongSecActivity.TARGETID, MD5.encode(user.id)).appendQueryParameter(CustomRongSecActivity.TITLE, user.nickName).build());
                    Log.e("==","=="+intent);
                    startActivity(intent);
                }
            }

        });

        mSearchET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mAllContacts.isEmpty()) {
                    doSearch(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadData() {
        mAllContacts.clear();
        showLoading();
        APIManager.getInstance(getMyApplication()).listUsersByChat(null, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                showTips(0, "加载小伙伴列表失败，请点击重试");
                setOnClickReloadListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        loadData();
                    }
                });
            }

        }, new Response.Listener<RequestResult<User.LeveledUsers>>() {

            @Override
            public void onResponse(RequestResult<LeveledUsers> response) {
                hideLoading();
                setOnClickReloadListener(null);
                if (mContactLV != null && response != null && response.data != null) {
                    hideTips();
                    mLeveldUsers = response.data;
                    setupDataForGroupMode(mLeveldUsers);
                    processData();
                } else {
                    showTips(0, "您还没有小伙伴哦");
                }
            }

        });
    }

    private void processData() {
        if (mLeveldUsers == null) {
            return;
        }

        final LeveledUsers leveldUsers = mLeveldUsers;
        new Thread(new Runnable() {

            @Override
            public void run() {
                final List<User> allContacts = new ArrayList<User>();
                if (leveldUsers.up != null && !leveldUsers.up.isEmpty()) {
                    allContacts.addAll(leveldUsers.up);
//					EasemodManager.getInstance().saveUserEntitys(User.convertToUserEntitys(leveldUsers.up));
                }
                if (leveldUsers.up != null && !leveldUsers.center.isEmpty()) {
                    allContacts.addAll(leveldUsers.center);
//					EasemodManager.getInstance().saveUserEntitys(User.convertToUserEntitys(leveldUsers.center));
                }
                if (leveldUsers.up != null && !leveldUsers.down.isEmpty()) {
                    allContacts.addAll(leveldUsers.down);
//					EasemodManager.getInstance().saveUserEntitys(User.convertToUserEntitys(leveldUsers.down));
                }
//                EasemodManager.getInstance().readUserEntitysFromDb();
                mAllContacts.clear();
                mAllContacts.addAll(allContacts);
//                int size=mAllContacts.size();
//                ArrayList<RongIMClient.UserInfo> infos=new ArrayList<RongIMClient.UserInfo>();
//                for(int i=0;i<size;i++){
//                    User u=mAllContacts.get(i);
//                    RongIMClient.UserInfo info=new RongIMClient.UserInfo(u.id,u.nickName,u.head);
//                    infos.add(info);
//
//                }
//                DemoContext.getInstance().setFriends(infos);
            }
        }).start();

    }

    private void setupDataForGroupMode(LeveledUsers leveledUsers) {
        if (mContactLV != null) {
            mAdapter = new ContactAdapter();
            if (leveledUsers != null) {
                mAdapter.setData(leveledUsers.up, leveledUsers.center, leveledUsers.down);
            }
            mContactLV.setAdapter(mAdapter);
        }
    }

    private void setupDataForSearchMode(List<User> searchResultList) {
        if (mContactLV != null) {
            mAdapter = new ContactAdapter();
            mAdapter.setData(searchResultList);
            mContactLV.setAdapter(mAdapter);
        }
    }

    private void doSearch(CharSequence input) {
        if (mSearchTask != null) {
            mSearchTask.cancel();
            mSearchTask = null;
        }
        final String keyword = input.toString().trim();
        if ("".equals(keyword.trim())) {
            setupDataForGroupMode(mLeveldUsers);
        } else {
            final List<User> allContacts = mAllContacts;
            mSearchTask = new KWAsyncTask() {

                @Override
                protected Object doInBackground(Object... parameters) {
                    List<User> searchResutList = new ArrayList<User>();
                    for (User user : allContacts) {
                        if (user.nickName.contains(keyword) && !searchResutList.contains(user)) {
                            searchResutList.add(user);
                        }
                    }
                    return searchResutList;
                }

                @Override
                protected void onPostExecute(Object result) {
                    List<User> searchResutList = (List<User>) result;
                    setupDataForSearchMode(searchResutList);
                }
            };
            mSearchTask.execute();
        }
    }
}
