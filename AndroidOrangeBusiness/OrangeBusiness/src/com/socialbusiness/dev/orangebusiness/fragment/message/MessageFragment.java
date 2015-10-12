package com.socialbusiness.dev.orangebusiness.fragment.message;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;

public class MessageFragment extends BaseFragment {
	
	private static final String TAG = "MessageFragment";
	@Override
	public void setContentView(LayoutInflater inflater,
			ViewGroup mLayerContextView) {
		View view = inflater.inflate(R.layout.fragment_message, mLayerContextView);
		findViews(view);
		registerListeners();
		loadData();
	}

	private void findViews(View view) {
		
		showFragment();
	}
	
	private void registerListeners() {

	}
	
	private void loadData() {

	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// if(((AdapterContextMenuInfo)menuInfo).position > 0){ m,
		// }
	}

	//TODO 更新Tabbar上的未读数
	private void updateTabbarUnreadLabel() {
//		int count = EMChatManager.getInstance().getUnreadMsgsCount();
//		if (count > 0) {
//			unreadLabel.setText(String.valueOf(count));
//			unreadLabel.setVisibility(View.VISIBLE);
//		} else {
//			unreadLabel.setVisibility(View.INVISIBLE);
//		}
	}
}
