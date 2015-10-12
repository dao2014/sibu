package com.socialbusiness.dev.orangebusiness.fragment.message;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.contact.ContactActivity;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;

/**
 * Created by zkboos on 2015/7/10.
 */
public class TabConversationListFrament extends BaseFragment {


    CustomConversationListFragment list;

    public CustomConversationListFragment getList() {
        return list;
    }

    @Override
    public void setContentView(LayoutInflater inflater, ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.frament_conversation, mLayerContextView);
        findViews(mLayerContextView);
        setListeners();
    }

    public void setListeners() {

    }

    private void findViews(ViewGroup view) {
        list=new CustomConversationListFragment();

        getChildFragmentManager().beginTransaction().replace(R.id.conversation_list_layout, list).commit();
        initTitle(view);
    }
    private ImageView mBtnLeft;
    private TextView mTVLeftLabel;
    private TextView mTxtTitle;
    private ImageView mTitleImage;
    private ImageView mBtnRight;
    private TextView mTxtRight;
    LinearLayout mLyerLeft;
    private void initTitle(View view) {
        mLyerLeft = (LinearLayout) view.findViewById(R.id.view_header_mLyerLeft);
        mBtnLeft = (ImageView) view.findViewById(R.id.view_header_mBtnLeft);
        mBtnRight = (ImageView) view.findViewById(R.id.view_header_mBtnRight);
        mTVLeftLabel = (TextView) view.findViewById(R.id.view_header_mTVLeftLabel);
        mTxtRight = (TextView) view.findViewById(R.id.view_header_mTxtRight);
        mTxtTitle = (TextView) view.findViewById(R.id.view_header_mTxtTitle);
        mTitleImage = (ImageView) view.findViewById(R.id.view_header_mBtnTitle);
        showBackBtn(false);
        setTitle(R.string.main_tab_message);
        setRightIcon(R.drawable.ic_xhb, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ContactActivity.class);
                startActivity(intent);
            }
        });
    }
    public void showBackBtn(boolean isShow){
        if(isShow){
            mLyerLeft.setVisibility(View.VISIBLE);
        }else{
            mLyerLeft.setVisibility(View.INVISIBLE);
        }
    }
    public void setTitle(int titleId){
        mTitleImage.setVisibility(View.GONE);
        mTxtTitle.setVisibility(View.VISIBLE);
        this.mTxtTitle.setText(titleId);
    }

    public void setRightIcon(int resId, View.OnClickListener listener){
        mBtnRight.setImageResource(resId);
        mBtnRight.setOnClickListener(listener);
        showRightBtn(true);
    }
    public void showRightBtn(boolean isShow) {
        if(isShow){
            mBtnRight.setVisibility(View.VISIBLE);
        }else{
            mBtnRight.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showFragment() {
        super.showFragment();
//        getChildFragmentManager().beginTransaction().replace(R.id.conversation_list_layout, list).commit();
    }

    @Override
    public void hideFragment() {
        super.hideFragment();
    }
}
