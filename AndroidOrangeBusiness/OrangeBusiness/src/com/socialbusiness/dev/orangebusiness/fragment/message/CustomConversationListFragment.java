package com.socialbusiness.dev.orangebusiness.fragment.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.socialbusiness.dev.orangebusiness.util.UserGroupUtil;

import io.rong.imkit.fragment.ConversationListFragment;

/**
 * Created by zkboos on 2015/6/26.
 */
public class CustomConversationListFragment extends ConversationListFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view=super.onCreateView(inflater, container, savedInstanceState);
        UserGroupUtil.getFriends(this.getActivity(), this);
        return view;
    }

    public void setTotalUnRead(){
        this.setGroupUnReadMessageCount();
    }
    public void refreshList(){
        resetData();
//        this.mConversationListAdapter.notifyDataSetChanged();
//        onResume();
//        setGroupUnReadMessageCount();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
