package com.socialbusiness.dev.orangebusiness.fragment.message;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.util.UserGroupUtil;


/**
 * 
 * @author RongCloud_AMing
 *
 */
public class ConversationFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.conversation_fragment, container, false);
//		FragmentTransaction ft = this.getActivity().getSupportFragmentManager().beginTransaction();
//		ConversationGroupListFragment conversationGroupListFragment = new io.rong.imkit.fragment.ConversationGroupListFragment();
//		ft.replace(R.id.one_framelayout, conversationGroupListFragment);
//		ft.commit();
        UserGroupUtil.getFriends(this.getActivity());
		return view;
	}

}
