package com.socialbusiness.dev.orangebusiness.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.component.headerlistview.SectionAdapter;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.MD5;

import java.util.HashSet;
import java.util.List;

public class ContactAdapter extends SectionAdapter {
	
	private List<User> mUps;
	private List<User> mCenters;
	private List<User> mDowns;
	private List<User> mSearchResultList;
	private boolean isGroupMode;	//是否显示header group
	private boolean isDisplayGroup;	//是否显示群组项
	private boolean isSelectable;
	private HashSet<String> mSelectUserIds = new HashSet<>();
	private List<String> mGroupMembers;
	
	public void setData(List<User> searchResultList) {
		mSearchResultList = searchResultList;
		isGroupMode = false;
		isDisplayGroup = false;
		mSelectUserIds.clear();
	}
	
	public void setData(List<User> ups, List<User> centers, List<User> downs) {
		mUps = ups;
		mCenters = centers;
		mDowns = downs;
		isGroupMode = true;
		isDisplayGroup = true;
		mSelectUserIds.clear();
	}
	
	public void setGroupMembers(List<String> groupMembers) {
		mGroupMembers = groupMembers;
	}
	
	public void setDisplayGroup(boolean displayGroup) {
		isDisplayGroup = displayGroup;
	}
	
	public void setSelectable(boolean selectable) {
		isSelectable = selectable;
	}
	
	public boolean isGroupMode() {
		return isGroupMode;
	}
	
	public HashSet<String> getSelectedUserIds() {
		return mSelectUserIds;
	}

	@Override
	public void onRowItemClick(AdapterView<?> parent, View view, int section,
			int row, long id) {
		super.onRowItemClick(parent, view, section, row, id);
		if(isSelectable) {
			User user = getRowItem(section, row);
			if(user != null) {
				String userId = user.id;
				if(mGroupMembers != null && mGroupMembers.contains(MD5.encode(userId))) {
					//已经是群成员则不处理
					return ;
				}
				if(mSelectUserIds.contains(userId)) {
					mSelectUserIds.remove(userId);
				}
				else {
					mSelectUserIds.add(userId);
				}
				notifyDataSetChanged();
			}
		}
	}

	@Override
	public int numberOfSections() {
		return isGroupMode ? 4 : 1;
	}

	@Override
	public int numberOfRows(int section) {
		if(!isGroupMode) {
			return mSearchResultList != null ? mSearchResultList.size() : 0;
		}
		if(section == 0) {
			//群组Item
			return isDisplayGroup ? 1 : 0;
		}
		
		List<User> userList = getListBySection(section);
		return userList != null ? userList.size() : 0;
	}
	
	private List<User> getListBySection(int section) {
		//0群组  1上级  2平级  3下级
		List<User> list = null;
		if(section == 1) {
			list = mUps;
		}
		else if(section == 2) {
			list = mCenters;
		}
		else if(section == 3) {
			list = mDowns;
		}
		return list;
	}

	@Override
	public View getRowView(int section, int row, View convertView,
			ViewGroup parent) {
		Context context = parent.getContext();
		ViewHolder viewHolder = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.row_contact_item, null);
			viewHolder = new ViewHolder();
			viewHolder.avatarIV = (NetworkImageView) convertView.findViewById(R.id.row_contact_item_avatar);
			viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.row_contact_item_checkbox);
			viewHolder.nicknameTV = (TextView) convertView.findViewById(R.id.row_contact_item_name);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		ImageLoader imageLoader = APIManager.getInstance(context.getApplicationContext()).getImageLoader();

		
		if(isGroupMode && isDisplayGroup && section == 0) {
			//群组Item
			viewHolder.nicknameTV.setText("群");
            viewHolder.avatarIV.setDefaultImageResId(R.drawable.ic_group);
			viewHolder.avatarIV.setImageUrl(null, imageLoader);
		}
		else {
			final User user = getRowItem(section, row);
			if(user != null) {
				boolean isStaff = user.isStaff == 1;
				viewHolder.nicknameTV.setTextColor(isStaff ? context.getResources().getColor(R.color.orange_text) : Color.BLACK);
                viewHolder.avatarIV.setDefaultImageResId(R.drawable.ic_person);
                viewHolder.avatarIV.setImageUrl(APIManager.toAbsoluteUrl(user.head), imageLoader);
				viewHolder.nicknameTV.setText(user.nickName);
				viewHolder.checkbox.setVisibility(isSelectable ? View.VISIBLE : View.GONE);
				if(mGroupMembers != null && mGroupMembers.contains(MD5.encode(user.id))) {
					//已经是群成员了
					viewHolder.checkbox.setButtonDrawable(R.drawable.checkbox_bg_gray_selector);
					viewHolder.checkbox.setChecked(true);
				}
				else {
					viewHolder.checkbox.setButtonDrawable(R.drawable.checkbox_bg_selector);
					viewHolder.checkbox.setChecked(mSelectUserIds.contains(user.id));
				}
				viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(mGroupMembers != null && mGroupMembers.contains(MD5.encode(user.id))) {
							//已经是群成员则不处理
							return ;
						}
						boolean isChecked = ((CheckBox) v).isChecked();
						if(isChecked) {
							mSelectUserIds.add(user.id);
						}
						else {
							mSelectUserIds.remove(user.id);
						}
					}
				});
			}
		}
		
		return convertView;
	}

	@Override
	public User getRowItem(int section, int row) {
		if(!isGroupMode) {
			if(mSearchResultList != null && row < mSearchResultList.size()) {
				return mSearchResultList.get(row);
			}
			else {
				return null;
			}
		}
		
		if(section == 0) {
			//群组Item
			return null;
		}
		
		List<User> userList = getListBySection(section);
		if(userList != null && row < userList.size()) {
			return userList.get(row);
		}
		return null;
	}
	
	public boolean hasSectionHeaderView(int section) {
		if(isGroupMode) {
			if(section == 0) {
				//群组Item
				return false;
			}
			List<User> usersInSection = getListBySection(section);
			return usersInSection != null && !usersInSection.isEmpty();
		}
		return false;
	}
	
	@Override
	public String getSectionHeaderItem(int section) {
//		String[] sections = {"", "上一级", "平级", "下一级"};
		String[] sections = {"", "我的供应商", "我的伙伴", "我的客户"};
		if(!isGroupMode || section < 0 || section > sections.length) {
			return "";
		}
		return sections[section];
	}
	
	@Override
	public boolean disableHeaders() {
		return !isGroupMode;
	}
	
	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		if(isGroupMode && section == 0) {
			//群组Item不需要Header
			return null;
		}
		
		Context context = parent.getContext();
		HeaderViewHolder viewHolder = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.row_contact_item_header, null);
			viewHolder = new HeaderViewHolder();
			viewHolder.headerTV = (TextView) convertView.findViewById(R.id.row_contact_item_header_text);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (HeaderViewHolder) convertView.getTag();
		}
		
		viewHolder.headerTV.setText(getSectionHeaderItem(section));
		return convertView;
	}
	
	private final class ViewHolder {
		CheckBox checkbox;
		NetworkImageView avatarIV;
		TextView nicknameTV;
	}
	
	private final class HeaderViewHolder {
		TextView headerTV;
	}

}
