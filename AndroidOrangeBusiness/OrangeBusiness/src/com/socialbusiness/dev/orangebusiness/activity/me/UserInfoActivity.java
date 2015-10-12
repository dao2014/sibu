package com.socialbusiness.dev.orangebusiness.activity.me;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.CircleImageView;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.model.UserType;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.DialogUtil;
import com.socialbusiness.dev.orangebusiness.util.MD5;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.Hashtable;
import java.util.List;

import eu.janmuller.android.simplecropimage.CropImage;
import io.rong.imkit.RCloudContext;
import io.rong.imkit.demo.DemoContext;
import io.rong.imlib.RongIMClient;

public class UserInfoActivity extends BaseActivity implements ImageChooserListener {

    private RelativeLayout mHead;
    private CircleImageView mImageView;
    private EditText mNetworkName;
    private EditText mRemark;
    private EditText mType;
    private EditText mRealnameET;
    private View mSexLayout;
    private EditText mSex;
    private EditText mIdNo;

    private EditText mPhone;
    private EditText mWeixin;
    private EditText mQQ;
    private EditText mEmail;
    private EditText mAddr;
    private AlertDialog mSelectSexDialog;
    private AlertDialog mSelectTypeDialog;

    private View mAdminLayout;
    private View mAdminSwitch;
    private View mAdminDivLine;

    private View mMemberLayout;
    private View mMemberSwitch;
    private View mMemberDivLine;
    private View remarkLayoutLine;
    private View remarkLayout;

    private User mUser;
    private User mLoginUser;

    private TextView mSaveChange;

    private int mSexValue;
    private boolean isLoginUser;    //用户资料是否当前登录用户的
    private List<UserType> mUserTypes;
    private UserType mSelectUserType;
    private int mChooserType;
    private ImageChooserManager mImageChooserManager;
    private String mImageFilePath;
    private boolean canSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setUp();
        findView();
        registerListener();

        mLoginUser = SettingsManager.getLoginUser();
        if (mLoginUser == null) {
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(new Intent(Constant.RECEIVER_NO_LOGIN));
            return;
        }

        String userId = getIntent().getStringExtra(Constant.EXTRA_KEY_USER_ID);
        if (TextUtils.isEmpty(userId)) {
            //当前登录用户的个人资料
            isLoginUser = true;
            if (savedInstanceState != null) {
                restoreActivityState(savedInstanceState);
            } else {
                requestOtherUserInfo(mLoginUser.id);//刷新当前用户的个人信息
            }
        } else {
            //其他用户的个人资料
            isLoginUser = false;
            setTitle("个人资料");
            requestOtherUserInfo(userId);
        }
    }

    private void setUp() {
        setTitle(R.string.change_personal_info);

//        showRightTxt(true);
//        setRightTxt("保存修改", 0, Color.WHITE);
//        setRightTxtOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                saveInfoChange();
//            }
//        });
    }

    private void findView() {
        mHead = (RelativeLayout) findViewById(R.id.activity_userinfo_head_layout);
        mImageView = (CircleImageView) this.findViewById(R.id.activity_userinfo_head_image);
        mNetworkName = (EditText) findViewById(R.id.activity_userinfo_network_name);
        mType = (EditText) findViewById(R.id.activity_userinfo_type);
        mRealnameET = (EditText) findViewById(R.id.activity_userinfo_realname);
        mSex = (EditText) findViewById(R.id.activity_userinfo_sex);
        mSexLayout = findViewById(R.id.activity_userinfo_sexLayout);
        mIdNo = (EditText) findViewById(R.id.activity_userinfo_id_card_no);
        mRemark = (EditText) findViewById(R.id.activity_userinfo_remark);

        mPhone = (EditText) findViewById(R.id.activity_userinfo_phone);
        mWeixin = (EditText) findViewById(R.id.activity_userinfo_weixin);
        mQQ = (EditText) findViewById(R.id.activity_userinfo_qq);
        mEmail = (EditText) findViewById(R.id.activity_userinfo_email);
        mAddr = (EditText) findViewById(R.id.activity_userinfo_addr);

        mAdminLayout = findViewById(R.id.activity_userinfo_isAdminLayout);
        mAdminSwitch = findViewById(R.id.activity_userinfo_isAdminSwitch);
        mAdminDivLine = findViewById(R.id.activity_userinfo_isAdminDivLine);

        mMemberLayout = findViewById(R.id.activity_userinfo_isMemberLayout);
        mMemberSwitch = findViewById(R.id.activity_userinfo_isMemberSwitch);
        mMemberDivLine = findViewById(R.id.activity_userinfo_isMemberDivLine);

        mImageView.setDefaultImageResId(R.drawable.image_head_userinfo);
        mImageView.setErrorImageResId(R.drawable.image_head_userinfo);

        mSaveChange = (TextView) findViewById(R.id.activity_userinfo_save_change);

        mPhone.setEnabled(false);

        remarkLayout = findViewById(R.id.remark_layout);
        remarkLayoutLine = findViewById(R.id.remark_layout_line);
    }

    // 保存用户修改的信息
    private void saveInfoChange() {

        User user = User.cloneOne(mUser);
        //网名
        user.nickName = mNetworkName.getText().toString().trim();


        if (TextUtils.isEmpty(user.nickName)) {
            AndroidUtil.setError(mNetworkName, "网名不能为空");
            return;
        }

        //备注
        user.remarkname = mRemark.getText().toString().trim();

        //类别
        if (user.type == null) {
            user.type = new UserType();
            user.type.name = mType.getText().toString().trim();
        }
        if (mSelectUserType != null) {
            user.type = mSelectUserType;
        }

        //真实姓名
        user.trueName = mRealnameET.getText().toString().trim();

        //性别
        user.male = mSexValue;
        //身份证
        user.idCard = mIdNo.getText().toString().trim();
        //手机号码
        user.phone = mPhone.getText().toString().trim();
        //微信号码
        user.wechat = mWeixin.getText().toString().trim();
        //qq号码
        user.qq = mQQ.getText().toString().trim();
        //email
        user.email = mEmail.getText().toString().trim();
        //地址
        user.homeAddress = mAddr.getText().toString().trim();

        //修改我的下级
        if (!isLoginUser && mLoginUser != null && user.upUserId.equals(mLoginUser.id)) {
            //是否工作人员
            user.isStaff = mAdminSwitch.isSelected() ? 1 : 0;
            user.isMember = mMemberSwitch.isSelected() ? 1 : 0;
        }

        boolean flag = checkData(user);

        if (flag) {
            //上传信息到服务器
            callAPIUserRefresh(user);
        }
    }

    private boolean checkData(User user) {
        if (TextUtils.isEmpty(user.nickName)) {
            ToastUtil.show(this, "网名不能为空！");
            return false;
        }
        return true;
    }

    //显示用户信息在输入框
    private void setUserInfoToBox(User user) {
        hideProgressDialog();

        mUser = user;

        if (mUser == null) {
            finish();
            return;
        }
        //头像
        mImageView.setImageUrl(APIManager.toAbsoluteUrl(mUser.head), APIManager.getInstance(this).getImageLoader());
        //网名
        mNetworkName.setText(mUser.nickName);

        //备注
        mRemark.setText(mUser.remarkname);
        //类别
        if (mUser.type != null) {
            mType.setText(mUser.type.name);
        }

        //真实姓名
        mRealnameET.setText(AndroidUtil.nullToEmptyString(user.trueName));

        //性别
        mSexValue = mUser.male;
        mSex.setText(mSexValue == 0 ? "女" : "男");
        //身份证
        mIdNo.setText(mUser.idCard);
        //手机号码
        mPhone.setText(mUser.phone);
        //微信号码
        mWeixin.setText(mUser.wechat);
        //qq号码
        mQQ.setText(mUser.qq);
        //email
        mEmail.setText(mUser.email);
        //地址
        mAddr.setText(mUser.homeAddress);

        if (!isLoginUser) {
            mNetworkName.setEnabled(false);
            mIdNo.setEnabled(false);
            mPhone.setEnabled(false);
            mWeixin.setEnabled(false);
            mQQ.setEnabled(false);
            mEmail.setEnabled(false);
            mAddr.setEnabled(false);
            mRealnameET.setEnabled(false);

            canSwitch = mUser.isStaff == 0;

            if (mLoginUser.id.equals(user.upUserId)) {
                //上级查看下级会员
                mAdminLayout.setVisibility(View.VISIBLE);
                mAdminDivLine.setVisibility(View.VISIBLE);
                mAdminSwitch.setSelected(mUser.isStaff == 1);

                mMemberLayout.setVisibility(View.VISIBLE);
                mMemberDivLine.setVisibility(View.VISIBLE);
                mMemberSwitch.setSelected(mUser.isMember == 1);
            } else {
                mSaveChange.setVisibility(View.GONE);
                showRightTxt(false);
                mType.setEnabled(false);
            }
        } else {
//            mRemark.setEnabled(false);
//            mRemark.setVisibility(View.GONE);
            remarkLayout.setVisibility(View.GONE);
            remarkLayoutLine.setVisibility(View.GONE);
        }
    }

    private void requestOtherUserInfo(String userId) {
        showProgressDialog();
        Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("userId", userId);
        APIManager.getInstance(getApplicationContext()).userRefresh(
                params,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                        showException(error);
                    }
                },
                new Response.Listener<RequestResult<User>>() {

                    @Override
                    public void onResponse(RequestResult<User> response) {
                        hideProgressDialog();
                        if (response != null) {
                            if (response.data != null && response.data.isValidate()) {
                                User user = response.data;
                                setUserInfoToBox(user);
                            } else {
                                ToastUtil.show(getApplicationContext(), "用户不存在或已删除");
                                finish();
                            }
                        } else {
                            ToastUtil.show(getApplicationContext(), "请求用户信息失败，请重试");
                            finish();
                        }
                    }
                });
    }

    private void callAPIUserRefresh(final User user) {
        showProgressDialog();
        APIManager.getInstance(this).updateUser(user, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                ToastUtil.show(UserInfoActivity.this, error.getMessage());
            }
        }, new Response.Listener<RequestResult<?>>() {
            @Override
            public void onResponse(RequestResult<?> response) {
                hideProgressDialog();
                ToastUtil.show(UserInfoActivity.this, response.message);
                if (response.code == 0) {
                    if (isLoginUser) {
                        SettingsManager.saveLoginUserWithoutSetJPush(user);
                        //更新环信的昵称和头像
//                        EasemodManager.getInstance().setLoginUserNickName(user.nickName);
//                        EasemodManager.getInstance().saveUserEntity(user.id,
//                                MD5.encode(user.id), user.nickName, user.head);
                        RongIMClient.UserInfo info = new RongIMClient.UserInfo(MD5.encode(user.id), user.nickName, user.head);
                        if (RCloudContext.getInstance() != null) {
                            RCloudContext.getInstance().getUserInfoCache().put(info.getUserId(), info);
                        }
                        DemoContext.getInstance().userMap.put(info.getUserId(),info);
                    }
                    finish();
                }
            }
        });
    }

    private void registerListener() {
        View.OnClickListener register = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                if (id == R.id.activity_userinfo_save_change) {
                    saveInfoChange();
                } else if (id == R.id.activity_userinfo_sex
                        || id == R.id.activity_userinfo_sexLayout) {
                    if (isLoginUser) {
                        popSelectSexDialog();
                    }
                } else if (id == R.id.activity_userinfo_head_layout) {
                    if (isLoginUser) {
                        updateAvatar();
                    }
                } else if (id == R.id.activity_userinfo_isAdminSwitch) {
                    if (!isLoginUser && canSwitch) {
                        if (!mAdminSwitch.isSelected()) {
                            Dialog isDeleteDialog = new AlertDialog.Builder(UserInfoActivity.this)
                                    .setMessage("如果将该用户修改为工作人员，将不可还原，是否继续？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mAdminSwitch.setSelected(!mAdminSwitch.isSelected());
                                        }
                                    })
                                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {


                                        }
                                    }).create();
                            Window window = isDeleteDialog.getWindow();
                            WindowManager.LayoutParams lp = window.getAttributes();
                            lp.alpha = 0.8f;
                            window.setAttributes(lp);
                            isDeleteDialog.show();
                        } else {
                            mAdminSwitch.setSelected(!mAdminSwitch.isSelected());
                        }

                    }
                } else if (id == R.id.activity_userinfo_isMemberSwitch) {
                    if (!isLoginUser) {
                        mMemberSwitch.setSelected(!mMemberSwitch.isSelected());
                    }
                } else if (id == R.id.activity_userinfo_type) {
                    if (!isLoginUser) {
                        requestUserType();
                    }
                }
            }
        };

        mSaveChange.setOnClickListener(register);
        mSex.setOnClickListener(register);
        mSexLayout.setOnClickListener(register);
        mHead.setOnClickListener(register);
        mAdminSwitch.setOnClickListener(register);
        mMemberSwitch.setOnClickListener(register);
        mType.setOnClickListener(register);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("chooseType", mChooserType);
        outState.putString("imageFilePath", mImageFilePath);
        saveActivityState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreActivityState(savedInstanceState);
    }

    private void saveActivityState(Bundle outState) {
        outState.putString("mNetworkName", mNetworkName.getText().toString().trim());
        outState.putString("mRemark", mRemark.getText().toString().trim());
        outState.putString("mRealnameET", mRealnameET.getText().toString().trim());
        outState.putString("mSex", mSex.getText().toString().trim());
        outState.putString("mIdNo", mIdNo.getText().toString().trim());
        outState.putString("mWeixin", mWeixin.getText().toString().trim());
        outState.putString("mQQ", mQQ.getText().toString().trim());
        outState.putString("mEmail", mEmail.getText().toString().trim());
        outState.putString("mAddr", mAddr.getText().toString().trim());
    }

    private void restoreActivityState(Bundle savedInstanceState) {
        int chooseType = savedInstanceState.getInt("chooseType", 497);
        String imageFilePath = savedInstanceState.getString("imageFilePath");
        if (!TextUtils.isEmpty(imageFilePath)) {
            mImageFilePath = imageFilePath;
        }
        if (chooseType != 497) {
            mChooserType = chooseType;
        }

        if (mNetworkName == null
                || mRealnameET == null
                || mSex == null
                || mIdNo == null
                || mWeixin == null
                || mQQ == null
                || mEmail == null
                || mAddr == null) {
            mNetworkName.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mNetworkName")));
            mRemark.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mRemark")));
            mRealnameET.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mRealnameET")));
            mSex.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mSex")));
            mIdNo.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mIdNo")));
            mWeixin.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mWeixin")));
            mQQ.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mQQ")));
            mEmail.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mEmail")));
            mAddr.setText(AndroidUtil.nullToEmptyString(savedInstanceState.getString("mAddr")));
        }
    }

    private void updateAvatar() {
        DialogUtil.showTakePhotoDialog(this, null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //take picture
                    mChooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
                    mImageChooserManager = new ImageChooserManager(UserInfoActivity.this,
                            ChooserType.REQUEST_CAPTURE_PICTURE,
                            Constant.PATH_IMAGE_TEMP_FOLDER,
                            true);
                    mImageChooserManager.setImageChooserListener(UserInfoActivity.this);
                    try {
                        mImageFilePath = mImageChooserManager.choose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //choose photo
                    mChooserType = ChooserType.REQUEST_PICK_PICTURE;
                    mImageChooserManager = new ImageChooserManager(UserInfoActivity.this,
                            ChooserType.REQUEST_PICK_PICTURE,
                            Constant.PATH_IMAGE_TEMP_FOLDER,
                            true);
                    mImageChooserManager.setImageChooserListener(UserInfoActivity.this);
                    try {
                        mImageFilePath = mImageChooserManager.choose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // 修改类别
    private void requestUserType() {
        if (mUserTypes != null) {
            popSelectTypeDialog();
        } else {
            showProgressDialog();
            Hashtable<String, String> parameters = new Hashtable<String, String>();
            parameters.put("page", "1");
            parameters.put("pageSize", "999");
            APIManager.getInstance(getApplicationContext()).listUserType(
                    parameters,
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideProgressDialog();
                            showException(error, "获取类别列表失败，请重试");
                        }
                    },
                    new Response.Listener<RequestListResult<UserType>>() {

                        @Override
                        public void onResponse(RequestListResult<UserType> response) {
                            hideProgressDialog();
                            if (response != null && response.data != null) {
                                mUserTypes = response.data;
                                popSelectTypeDialog();
                            }
                        }
                    });
        }
    }

    private void popSelectTypeDialog() {
        if (mSelectTypeDialog != null) {
            mSelectTypeDialog.dismiss();
        }
        if (mUserTypes != null && !mUserTypes.isEmpty()) {
            final String[] items = new String[mUserTypes.size()];
            int currentUserTypeIndex = 0;
            for (int i = 0; i < items.length; i++) {
                UserType type = mUserTypes.get(i);
                items[i] = type.name;
                if (mUser.type != null && type.id.equals(mUser.type.id)) {
                    mSelectUserType = mUser.type;
                    currentUserTypeIndex = i;
                }
            }
            mSelectTypeDialog = new AlertDialog.Builder(this)
                    .setTitle("请选择类别")
                    .setSingleChoiceItems(items, currentUserTypeIndex, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectUserType = mUserTypes.get(which);
                            mType.setText(mSelectUserType.name);
                            dialog.dismiss();
                        }
                    })
                    .create();
            mSelectTypeDialog.show();
        }
    }

    private void popSelectSexDialog() {
        if (mSelectSexDialog != null) {
            mSelectSexDialog.dismiss();
        }
        final String[] items = {"女", "男"};

        mSelectSexDialog = new AlertDialog.Builder(this)
                .setTitle("选择性别")
                .setSingleChoiceItems(items, mSexValue, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSexValue = which;
                        mSex.setText(items[which]);
                        dialog.dismiss();
                    }

                })
                .create();
        mSelectSexDialog.show();
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        if (image != null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    hideProgressDialog();
                    mImageFilePath = image.getFileThumbnail();
                    Intent intent = new Intent(UserInfoActivity.this, CropImage.class);
                    intent.putExtra(CropImage.IMAGE_PATH, mImageFilePath);
                    intent.putExtra(CropImage.SCALE, true);

                    intent.putExtra(CropImage.ASPECT_X, 3);
                    intent.putExtra(CropImage.ASPECT_Y, 3);
                    intent.putExtra(CropImage.OUTPUT_X, 256);
                    intent.putExtra(CropImage.OUTPUT_Y, 256);
                    startActivityForResult(intent, 2000);
                }
            });
        }
    }

    @Override
    public void onError(String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ChooserType.REQUEST_PICK_PICTURE
                    || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE) {
                //选择图片或拍照的result
                if (mImageChooserManager == null) {
                    reinitializeImageChooser();
                }
                showProgressDialog();
                mImageChooserManager.submit(requestCode, data);
            } else if (requestCode == 2000) {
                //裁剪图片的result
                if (data == null) {
                    return;
                }
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    return;
                }
                Bitmap bm = null;
                try {
                    bm = BitmapFactory.decodeFile(path);
                } catch (Exception e) {

                }
                uploadAvatar();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void reinitializeImageChooser() {
        mImageChooserManager = new ImageChooserManager(this, mChooserType, "路径", true);
        mImageChooserManager.setImageChooserListener(this);
        mImageChooserManager.reinitialize(mImageFilePath);
    }

    private void uploadAvatar() {
        showProgressDialog().setCancelable(false);
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("filePath", mImageFilePath);
        APIManager.getInstance(getApplicationContext()).updateHead(
                parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                    }
                },
                new Response.Listener<RequestResult<?>>() {

                    @Override
                    public void onResponse(RequestResult<?> response) {
                        hideProgressDialog();
                        if (response != null && response.code == 0) {
                            try {
                                User user = SettingsManager.getLoginUser();
                                user.head = (String) response.data;
                                SettingsManager.saveLoginUserWithoutSetJPush(user);
                                mImageView.setImageUrl(APIManager.toAbsoluteUrl(user.head), APIManager.getInstance(UserInfoActivity.this).getImageLoader());
                                ToastUtil.show(getApplicationContext(), "修改头像成功");

                                if (mLoginUser != null) {
                                    mLoginUser.head = user.head;
                                }
                                if (mUser != null) {
                                    mUser.head = user.head;
                                }

                                //更新环信的头像
//                                EasemodManager.getInstance().setLoginUserAvatar(user.head);
//                                EasemodManager.getInstance().saveUserEntity(user.id,
//                                        MD5.encode(user.id), user.nickName, user.head);

                                RongIMClient.UserInfo info = new RongIMClient.UserInfo(MD5.encode(user.id), user.nickName, user.head);
                                if (RCloudContext.getInstance() != null) {
                                    RCloudContext.getInstance().getUserInfoCache().put(info.getUserId(), info);
                                }
                                DemoContext.getInstance().userMap.put(info.getUserId(),info);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastUtil.show(getApplicationContext(), "上传头像失败，请重试");
                        }
                    }
                });
    }
}
