package com.socialbusiness.dev.orangebusiness.activity.me;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import io.rong.imkit.RongIM;
import io.rong.imkit.demo.DemoContext;
import io.rong.imkit.demo.ui.WinToast;
import io.rong.imkit.demo.utils.DateUtils;
import io.rong.imlib.RongIMClient;

public class SettingsActivity extends BaseActivity implements OnClickListener{

	private TextView mQuit;
    public RelativeLayout mFeedBack;
    public RelativeLayout mCkeck;
    private TextView mVersionName;
    public RelativeLayout mAbout;
    private View mMsgAlertSwitchLayout;
    private View mMsgAlertSwitch;
    private View mMsgAlertDivLine;
    private View mSoundSwitchLayout;
    private View mSoundSwitch;
    private View mSoundDivLine;
    private View mVibrateSwitchLayout;
    private View mVibrateSwitch;
    private View mVibrateDivLine;


    private static final String TAG = SettingsActivity.class.getSimpleName();

    /**
     * 关闭勿扰模式
     */
    private LinearLayout mCloseNotifacation;
    /**
     * 开始时间 RelativeLayout
     */
    private RelativeLayout mStartNotifacation;
    /**
     * 关闭时间 RelativeLayout
     */
    private RelativeLayout mEndNotifacation;
    /**
     * 开始时间
     */
    private TextView mStartTimeNofication;
    /**
     * 关闭时间
     */
    private TextView mEndTimeNofication;
    /**
     * 开关
     */
    private ImageView mNotificationCheckBox;
    /**
     * ActionBar
     */
//    ActionBar mActionBar;
    /**
     * 开始时间
     */
    private String mStartTime;
    /**
     * 结束时间
     */
    private String mEndTime;
    /**
     * 小时
     */
    int hourOfDays;
    /**
     * 分钟
     */
    int minutes;
    private String mTimeFormat = "HH:mm:ss";
    boolean mIsSetting = false;
    private Handler mThreadHandler;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SharedPreferences.Editor editor;
            switch (msg.what) {
                case 1:
                    mNotificationCheckBox.setSelected(true);
                    mCloseNotifacation.setVisibility(View.VISIBLE);
                    if (msg != null) {
                        mStartTime = msg.obj.toString();
                        hourOfDays = Integer.parseInt(mStartTime.substring(0, 2));
                        minutes = Integer.parseInt(mStartTime.substring(3, 5));
                        int spanMins = msg.arg1;

                        String time = DateUtils.dateToString(DateUtils.addMinutes(DateUtils.stringToDate(mStartTime, mTimeFormat), spanMins), mTimeFormat);
                        mStartTimeNofication.setText(mStartTime);
                        mEndTimeNofication.setText(time);

                        editor = DemoContext.getInstance().getSharedPreferences().edit();
                        editor.putString("START_TIME", mStartTime);
                        editor.putString("END_TIME", DateUtils.dateToString(DateUtils.addMinutes(DateUtils.stringToDate(mStartTime, mTimeFormat), spanMins), mTimeFormat));
                        editor.commit();
                    }
                    break;
                case 2:
                    mNotificationCheckBox.setSelected(false);
                    mCloseNotifacation.setVisibility(View.GONE);
                    editor = DemoContext.getInstance().getSharedPreferences().edit();
                    editor.remove("IS_SETTING");
                    editor.commit();
                    hideProgressDialog();
                    break;

                case 3:
                    mNotificationCheckBox.setSelected(true);
                    mCloseNotifacation.setVisibility(View.VISIBLE);

                    if (DemoContext.getInstance().getSharedPreferences() != null) {
                        String endtime = DemoContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                        String starttimes = DemoContext.getInstance().getSharedPreferences().getString("START_TIME", null);

                        if (endtime != null && starttimes != null && !"".equals(endtime) && !"".equals(starttimes)) {
                            Date datastart = DateUtils.stringToDate(starttimes, mTimeFormat);
                            Date dataend = DateUtils.stringToDate(endtime, mTimeFormat);
                            long spansTime = DateUtils.compareMin(datastart, dataend);
                            mStartTimeNofication.setText(starttimes);
                            mEndTimeNofication.setText(endtime);
                            setConversationTime(starttimes, (int) spansTime);
                        } else {
                            mStartTimeNofication.setText("23:59:59");
                            mEndTimeNofication.setText("00:00:00");
                            editor = DemoContext.getInstance().getSharedPreferences().edit();
                            editor.putString("START_TIME", "23:59:59");
                            editor.putString("END_TIME", "00:00:00");
                            editor.commit();
                        }
                    }
                    break;

            }
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setUp();
		findView();
		registerListener();
		refreshView();
        initData();
	}
	
	private void setUp() {
		setTitle(R.string.settings);
	}
	
	private void findView() {
		mQuit = (TextView) findViewById(R.id.activity_settings_quit);
        mFeedBack = (RelativeLayout) this.findViewById(R.id.activity_settings_other_feedback);
        mCkeck = (RelativeLayout) this.findViewById(R.id.activity_settings_other_check);
        mVersionName = (TextView) findViewById(R.id.activity_settings_version_name);
        mAbout = (RelativeLayout) this.findViewById(R.id.activity_settings_other_about);
        mMsgAlertSwitchLayout = findViewById(R.id.activity_settings_msgAlertSwitchLayout);
        mMsgAlertSwitch = findViewById(R.id.activity_settings_msgAlertSwitch);
        mMsgAlertDivLine = findViewById(R.id.activity_settings_msgAlertDivLine);
        mSoundSwitchLayout = findViewById(R.id.activity_settings_soundSwitchLayout);
        mSoundSwitch = findViewById(R.id.activity_settings_soundSwitch);
        mSoundDivLine = findViewById(R.id.activity_settings_soundDivLine);
        mVibrateSwitchLayout = findViewById(R.id.activity_settings_vibrateSwitchLayout);
        mVibrateSwitch = findViewById(R.id.activity_settings_vibrateSwitch);
        mVibrateDivLine = findViewById(R.id.activity_settings_vibrateDivLine);
        
        setVersionName();


        mCloseNotifacation = (LinearLayout) findViewById(R.id.close_notification);
        mStartNotifacation = (RelativeLayout) findViewById(R.id.start_notification);
        mStartTimeNofication = (TextView) findViewById(R.id.start_time_notification);
        mEndNotifacation = (RelativeLayout) findViewById(R.id.end_notification);
        mEndTimeNofication = (TextView) findViewById(R.id.end_time_notification);
        mNotificationCheckBox = (ImageView) findViewById(R.id.notification_checkbox);
        mThreadHandler = new Handler();
        Calendar calendar = Calendar.getInstance();
        hourOfDays = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);
	}
	
	private void setVersionName() {
		try {
			PackageManager manager = getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			mVersionName.setText("v" + StringUtil.showMessage(info.versionName));
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void registerListener() {
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
                int i = v.getId();
                if (i == R.id.activity_settings_quit) {
                    logout(intent);
                    return;

                } else if (i == R.id.activity_settings_other_feedback) {
                    FeedbackAgent agent = new FeedbackAgent(SettingsActivity.this);
                    agent.startFeedbackActivity();

                } else if (i == R.id.activity_settings_other_check) {
                    checkUmengUpdate();

                } else if (i == R.id.activity_settings_other_about) {
                    intent.setClass(SettingsActivity.this, AboutUsActivity.class);

                } else if (i == R.id.activity_settings_msgAlertSwitchLayout) {
//                    EasemodManager.getInstance().getSettings().setSettingMsgNotification(!mMsgAlertSwitch.isSelected());
//                    EasemodManager.getInstance().getSettings().commitSettings();
                    refreshView();

                } else if (i == R.id.activity_settings_soundSwitchLayout) {
//                    EasemodManager.getInstance().getSettings().setSettingMsgSound(!mSoundSwitch.isSelected());
//                    EasemodManager.getInstance().getSettings().commitSettings();
                    refreshView();

                } else if (i == R.id.activity_settings_vibrateSwitchLayout) {
//                    EasemodManager.getInstance().getSettings().setSettingMsgVibrate(!mVibrateSwitch.isSelected());
//                    EasemodManager.getInstance().getSettings().commitSettings();
                    refreshView();

                }
				
				if (intent.getComponent() != null) {
					startActivity(intent);
				}
			}
		};

        mQuit.setOnClickListener(listener);
        mFeedBack.setOnClickListener(listener);
        mCkeck.setOnClickListener(listener);
        mAbout.setOnClickListener(listener);
        
        mMsgAlertSwitchLayout.setOnClickListener(listener);
        mSoundSwitchLayout.setOnClickListener(listener);
        mVibrateSwitchLayout.setOnClickListener(listener);
	}
	
	private void logout(final Intent intent) {
		Dialog logoutDialog = new AlertDialog.Builder(this)
				.setMessage(R.string.alertDialog_logout_message)
				.setPositiveButton(R.string.alertDialog_positive, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						callAPILogout(intent);
					}
				})
				.setNegativeButton(R.string.alertDialog_negative, null)
				.create();
		Window window = logoutDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.8f;
		window.setAttributes(lp);
		logoutDialog.show();
	}
	
	private void callAPILogout(final Intent intent) {
		showLoading();
		Hashtable<String, String> parameters = new Hashtable<>();
		APIManager.getInstance(this).userLogout(parameters, 
				new Response.ErrorListener() {
			
					@Override
					public void onErrorResponse(VolleyError error) {
						hideLoading();
                        SettingsManager.init(getApplicationContext()).setLogDestroyFlag(true);
						intent.putExtra(Constant.EXTRA_KEY_LOGOUT, true);
						setResult(RESULT_OK, intent);
						finish();
					}
				}, new Response.Listener<RequestResult<?>>() {
					
					@Override
					public void onResponse(RequestResult<?> response) {
						hideLoading();
						if (response != null) {
							ToastUtil.show(SettingsActivity.this, response.message);
							intent.putExtra(Constant.EXTRA_KEY_LOGOUT, true);
							setResult(RESULT_OK, intent);
							finish();
						}
					}
				});
	}

	private void checkUmengUpdate() {
		UmengUpdateAgent.setUpdateListener(getUpdateListener(true));
		UmengUpdateAgent.update(this);
	}
	
	private void refreshView() {
		if(isDestroy()) {
			return ;
		}
		
//		PreferenceUtils settings = EasemodManager.getInstance().getSettings();
//        mMsgAlertSwitch.setSelected(settings.getSettingMsgNotification());
//        mSoundSwitch.setSelected(settings.getSettingMsgSound());
//        mVibrateSwitch.setSelected(settings.getSettingMsgVibrate());
        
//        if(settings.getSettingMsgNotification()) {
//        	mSoundSwitchLayout.setVisibility(View.VISIBLE);
//        	mSoundDivLine.setVisibility(View.VISIBLE);
//        	mVibrateSwitchLayout.setVisibility(View.VISIBLE);
//        	mVibrateDivLine.setVisibility(View.VISIBLE);
//        }
//        else {
//        	mSoundSwitchLayout.setVisibility(View.GONE);
//        	mSoundDivLine.setVisibility(View.GONE);
//        	mVibrateSwitchLayout.setVisibility(View.GONE);
//        	mVibrateDivLine.setVisibility(View.GONE);
//        }
	}
	
	@Override
	protected void onDestroy() {
		//防止内存泄露
		UmengUpdateAgent.setUpdateListener(null);
		super.onDestroy();
	}


    protected void initData() {
        mStartNotifacation.setOnClickListener(this);
        mEndNotifacation.setOnClickListener(this);
        mNotificationCheckBox.setOnClickListener(this);
        if (DemoContext.getInstance().getSharedPreferences() != null) {
            mIsSetting = DemoContext.getInstance().getSharedPreferences().getBoolean("IS_SETTING", false);
            if (mIsSetting) {

                Message msg = Message.obtain();
                msg.what = 3;
                mHandler.sendMessage(msg);
            } else {
                if (RongIM.getInstance() != null)
                    mThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            RongIM.getInstance().getConversationNotificationQuietHours(new RongIMClient.GetConversationNotificationQuietHoursCallback() {
                                @Override
                                public void onSuccess(String startTime, int spanMins) {
                                    Log.e(TAG, "----yb----获取会话通知周期-onSuccess起始时间startTime:" + startTime + ",间隔分钟数spanMins:" + spanMins);
                                    if (spanMins > 0) {
                                        Message msg = Message.obtain();
                                        msg.what = 1;
                                        msg.obj = startTime;
                                        msg.arg1 = spanMins;
                                        mHandler.sendMessage(msg);
                                    } else {
                                        Message mssg = Message.obtain();
                                        mssg.what = 2;
                                        mHandler.sendMessage(mssg);
                                    }
                                }

                                @Override
                                public void onError(ErrorCode errorCode) {
                                    Log.e(TAG, "----yb----获取会话通知周期-oonError:" + errorCode);
                                    mNotificationCheckBox.setSelected(false);
                                    mCloseNotifacation.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
            }
        }


    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start_notification) {
            if (DemoContext.getInstance().getSharedPreferences() != null) {
                String starttime = DemoContext.getInstance().getSharedPreferences().getString("START_TIME", null);
                if (starttime != null && !"".equals(starttime)) {
                    hourOfDays = Integer.parseInt(starttime.substring(0, 2));
                    minutes = Integer.parseInt(starttime.substring(3, 5));
                }
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    mStartTime = getDaysTime(hourOfDay, minute);

                    mStartTimeNofication.setText(mStartTime);
                    SharedPreferences.Editor editor = DemoContext.getInstance().getSharedPreferences().edit();
                    editor.putString("START_TIME", mStartTime);
                    editor.commit();

                    if (DemoContext.getInstance().getSharedPreferences() != null) {
                        String endtime = DemoContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                        if (endtime != null && !"".equals(endtime)) {
                            Date datastart = DateUtils.stringToDate(mStartTime, mTimeFormat);
                            Date dataend = DateUtils.stringToDate(endtime, mTimeFormat);
                            long spansTime = DateUtils.compareMin(datastart, dataend);
                            setConversationTime(mStartTime, (int) Math.abs(spansTime));
                        }
                    }
                }
            }, hourOfDays, minutes, true);
            timePickerDialog.show();


        } else if (i == R.id.end_notification) {
            TimePickerDialog timePickerDialog;
            if (DemoContext.getInstance().getSharedPreferences() != null) {
                String endtime = DemoContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                if (endtime != null && !"".equals(endtime)) {
                    hourOfDays = Integer.parseInt(endtime.substring(0, 2));
                    minutes = Integer.parseInt(endtime.substring(3, 5));
                }
            }

            timePickerDialog = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    mEndTime = getDaysTime(hourOfDay, minute);
                    mEndTimeNofication.setText(mEndTime);
                    SharedPreferences.Editor editor = DemoContext.getInstance().getSharedPreferences().edit();
                    editor.putString("END_TIME", mEndTime);
                    editor.commit();

                    if (DemoContext.getInstance().getSharedPreferences() != null) {
                        String starttime = DemoContext.getInstance().getSharedPreferences().getString("START_TIME", null);
                        if (starttime != null && !"".equals(starttime)) {
                            Date datastart = DateUtils.stringToDate(starttime, mTimeFormat);
                            Date dataend = DateUtils.stringToDate(mEndTime, mTimeFormat);
                            long spansTime = DateUtils.compareMin(datastart, dataend);
                            Log.e("", "------结束时间----" + mEndTime);
                            Log.e("", "------开始时间----" + starttime);
                            Log.e("", "------时间间隔----" + spansTime);

                            setConversationTime(mStartTime, (int) Math.abs(spansTime));
                        }
                    }
                }
            }, hourOfDays, minutes, true);
            timePickerDialog.show();


        } else if (i == R.id.notification_checkbox) {
            if (!mNotificationCheckBox.isSelected()) {
                Message msg = Message.obtain();
                msg.what = 3;
                mHandler.sendMessage(msg);
            } else {
                if (RongIM.getInstance() != null) {

                    showProgressDialog();
                    mThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            RongIM.getInstance().removeConversationNotificationQuietHours(new RongIMClient.RemoveConversationNotificationQuietHoursCallback() {

                                @Override
                                public void onSuccess() {
                                    Log.e(TAG, "----yb----移除会话通知周期-onSuccess");
                                    Message msg = Message.obtain();
                                    msg.what = 2;
                                    mHandler.sendMessage(msg);

                                }

                                @Override
                                public void onError(ErrorCode errorCode) {
                                    Log.e(TAG, "----yb-----移除会话通知周期-oonError:" + errorCode.getValue());
//                                    hideLoading();
                                    hideProgressDialog();
                                }
                            });
                        }
                    });

                }
            }

        }
    }

    /**
     * 得到"HH:mm:ss"类型时间
     *
     * @param hourOfDay 小时
     * @param minite    分钟
     * @return "HH:mm:ss"类型时间
     */
    private String getDaysTime(final int hourOfDay, final int minite) {
        String daysTime;
        String hourOfDayString = "0" + hourOfDay;
        String minuteString = "0" + minite;
        if (hourOfDay < 10 && minite >= 10) {
            daysTime = hourOfDayString + ":" + minite + ":00";
        } else if (minite < 10 && hourOfDay >= 10) {
            daysTime = hourOfDay + ":" + minuteString + ":00";
        } else if (hourOfDay < 10 && minite < 10) {
            daysTime = hourOfDayString + ":" + minuteString + ":00";
        } else {
            daysTime = hourOfDay + ":" + minite + ":00";
        }
        return daysTime;
    }

    /**
     * 设置勿扰时间
     *
     * @param startTime 设置勿扰开始时间 格式为：HH:mm:ss
     * @param spanMins  0 < 间隔时间 < 1440
     */
    private void setConversationTime(final String startTime, final int spanMins) {

        if (RongIM.getInstance() != null && startTime != null && !"".equals(startTime)) {
            mThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (spanMins > 0 && spanMins < 1440) {
                        Log.e("","----设置勿扰时间startTime；"+startTime+"---spanMins:"+spanMins);

                        RongIM.getInstance().setConversationNotificationQuietHours(startTime, spanMins, new RongIMClient.SetConversationNotificationQuietHoursCallback() {
                            @Override
                            public void onSuccess() {
                                Log.e(TAG, "----yb----设置会话通知周期-onSuccess");
                                SharedPreferences.Editor editor = DemoContext.getInstance().getSharedPreferences().edit();
                                editor.putBoolean("IS_SETTING", true);
                                editor.commit();
                            }

                            @Override
                            public void onError(ErrorCode errorCode) {
                                Log.e(TAG, "----yb----设置会话通知周期-oonError:" + errorCode.getValue());
                            }
                        });
                    } else {
                        WinToast.toast(getMyApplication(), "间隔时间必须大于0");
                    }
                }
            });
        }
    }
}
