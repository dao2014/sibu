package com.socialbusiness.dev.orangebusiness.fragment.me;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.login.LoginActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.AddressListActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.CourseClassifyListActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.LevelListActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.NoticeListActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.SettingsActivity;
import com.socialbusiness.dev.orangebusiness.activity.me.UserInfoActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.CommonUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;

import stackblur.StackBlurManager;

public class MeFragment extends BaseFragment {

    private View mHeaderLayout;
    private NetworkImageView mImageHeader;
    private TextView mUsername;
    private TextView mCredit;
    private TextView mType;
    private RelativeLayout mMyProductLayout;
    private RelativeLayout mNotificationLayout;
    private RelativeLayout mMyLowerLevelLayout;
    private RelativeLayout mManagerAddrsLayout;
    private RelativeLayout mCourseLayout;
    private RelativeLayout mScanningLayout;
    private View fragment_me_set_layout;

    @Override
    public void setContentView(LayoutInflater inflater,
                               ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.fragment_me, mLayerContextView);
        setUp();
        findView(mLayerContextView);
        registerListener();
        showRightBtn();
    }

    int headerWith;

    private void setUp() {
        headerWith = (int) CommonUtil.dip2px(getActivity(), 110);
    }

    private void findView(View view) {
        mHeaderLayout = view.findViewById(R.id.fragment_me_header_layout);
        mImageHeader = (NetworkImageView) view.findViewById(R.id.fragment_me_image);
        mUsername = (TextView) view.findViewById(R.id.fragment_me_username);
        mCredit = (TextView) view.findViewById(R.id.fragment_me_credit_value);
        mType = (TextView) view.findViewById(R.id.fragment_me_type_value);
        mMyProductLayout = (RelativeLayout) view.findViewById(R.id.fragment_me_my_product_layout);
        mNotificationLayout = (RelativeLayout) view.findViewById(R.id.fragment_me_notification_layout);
        mMyLowerLevelLayout = (RelativeLayout) view.findViewById(R.id.fragment_me_my_lower_level_layout);
        mManagerAddrsLayout = (RelativeLayout) view.findViewById(R.id.fragment_me_manager_addr_layout);
        mCourseLayout = (RelativeLayout) view.findViewById(R.id.fragment_me_course_layout);
        mScanningLayout = (RelativeLayout) view.findViewById(R.id.fragment_me_2dimension_layout);

        mImageHeader.setDefaultImageResId(R.drawable.image_header);
        mImageHeader.setErrorImageResId(R.drawable.image_header);

        fragment_me_set_layout = view.findViewById(R.id.fragment_me_set_layout);
        //设置
        fragment_me_set_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getMainActivity(),
                        SettingsActivity.class);
                startActivityForResult(intent, 0);
            }
        });
//        mImageHeader.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//
//            @Override
//            public boolean onPreDraw() {
//                mImageHeader.getViewTreeObserver().removeOnPreDrawListener(this);
////                image.buildDrawingCache();
////                Bitmap bmp = image.getDrawingCache();
////                blur(bmp, text, true);
//                android.util.Log.e("====","=="+mImageHeader.getWidth()+"=="+mImageHeader.getHeight());
//                mImageHeader.buildDrawingCache();
//
//                return true;
//            }
//        });

//        mHeaderLayout.setBackgroundDrawable(new BitmapDrawable(FastBlur.doBlur(Bitmap.createBitmap(10,10, Bitmap.Config.RGB_565), 2, true)));
    }

    public void setMeData(User user) {
        //头像
        if (user != null) {
            mImageHeader.setImageUrl(null, APIManager.getInstance(getActivity()).getImageLoader());
            mImageHeader.setImageUrl(APIManager.toAbsoluteUrl(user.head),
                    APIManager.getInstance(getActivity()).getImageLoader());
            mUsername.setText(StringUtil.showMessage(user.nickName));
            mCredit.setText(StringUtil.showMessage(user.credit));
            if (user.type != null) {
                mType.setText(StringUtil.showMessage(user.type.name));
            } else {
                mType.setText("");
            }
            Log.e("====", "==" + APIManager.toAbsoluteUrl(user.head));
            APIManager.getInstance(getActivity()).getImageLoader().get(APIManager.toAbsoluteUrl(user.head), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

//                    Log.e("====", "==" + response.getRequestUrl() + "==" + response.getBitmap().getWidth());
//                    ImageUtil.blur(response.getBitmap(), mHeaderLayout);
//                    mImageHeader.setImageBitmap(response.getBitmap());
//                    mHeaderLayout.setBackgroundDrawable(new BitmapDrawable(FastBlur.doBlur(response.getBitmap(), 5, true)));
                    if(response.getBitmap()!=null)
                    mHeaderLayout.setBackgroundDrawable(new BitmapDrawable(new StackBlurManager(response.getBitmap()).process(5)));
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }, headerWith, headerWith);
        }
    }

    private void registerListener() {
        View.OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                switch (v.getId()) {
                    case R.id.fragment_me_header_layout:
                        intent.setClass(getActivity(), UserInfoActivity.class);  //个人资料
                        startActivity(intent);
                        break;

                    case R.id.fragment_me_my_product_layout:
                        showMessageByDialog(getResources().getString(R.string.my_product_message));
//                        intent.setClass(getActivity(), CourseClassifyListActivity.class);
//                        intent.putExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, Product.ProductType.KWMProductTypeStock.getIntValue());
//                        startActivity(intent);
                        break;

                    case R.id.fragment_me_notification_layout:
                        intent.setClass(getActivity(), NoticeListActivity.class);  //公告通知
                        startActivity(intent);
                        break;

                    case R.id.fragment_me_my_lower_level_layout:
                        intent.setClass(getActivity(), LevelListActivity.class);   //我的客户\他的客户
                        startActivity(intent);
                        break;

                    case R.id.fragment_me_manager_addr_layout:
                        intent.setClass(getActivity(), AddressListActivity.class);  //收货地址
                        startActivity(intent);
                        break;

                    case R.id.fragment_me_course_layout:
                        intent.setClass(getActivity(), CourseClassifyListActivity.class);  //课件
                        startActivity(intent);
                        break;

                    case R.id.fragment_me_2dimension_layout:
                        showMessageByDialog(getResources().getString(R.string.function_constructing));
                        break;

                    default:
                        break;
                }
            }
        };

        mHeaderLayout.setOnClickListener(listener);
        mMyProductLayout.setOnClickListener(listener);
        mNotificationLayout.setOnClickListener(listener);
        mMyLowerLevelLayout.setOnClickListener(listener);
        mManagerAddrsLayout.setOnClickListener(listener);
        mCourseLayout.setOnClickListener(listener);
        mScanningLayout.setOnClickListener(listener);
    }

    private void showRightBtn() {
        getMainActivity().setRightIcon(R.drawable.ic_settings_selector,
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
//						Intent intent = new Intent(getMainActivity(),
//								SettingsActivity.class);
//						startActivityForResult(intent, 0);
                    }
                });
    }

    @Override
    public void showFragment() {
        super.showFragment();
        showRightBtn();
//        Intent intent = new Intent(getMainActivity(),
//                SettingsActivity.class);
//        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == Activity.RESULT_OK) {
            boolean logout = data.getBooleanExtra(Constant.EXTRA_KEY_LOGOUT,
                    false);
            if (logout) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

                SettingsManager.destroyLogin();
//				EasemodManager.getInstance().logout();
                getMainActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        User loginUser = SettingsManager.getLoginUser();
        if (loginUser != null) {
            setMeData(loginUser);
        }
    }
}
