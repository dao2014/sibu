package com.socialbusiness.dev.orangebusiness.activity.order;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.util.Log;

import java.lang.ref.WeakReference;

public class MyCaptureActivity extends CaptureActivity {
    public static final String TAG = "MyCaptureActivity";
    public static final String EXTRA_KEY_CONTENT = "EXTRA_KEY_CONTENT";
    public static final String EXTRA_KEY_DATA = "EXTRA_KEY_DATA";
    private static final int RESTART_DELAY = 2000;
    public static final String COMEFROME = "comefrom";

    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private ProgressDialog mProgressDialog;
    private ScanProductListActivity.ProductCodeData productCodeData;
    private WeakReference<Activity> weakThis = new WeakReference<Activity>(this);
    private Runnable autoRestart = new Runnable() {
        @Override
        public void run() {
            MyCaptureActivity activity = (MyCaptureActivity) weakThis.get();
            if (activity == null) {
                return;
            }

            activity.restartPreviewAfterDelay(RESTART_DELAY);
            activity.uiHandler.postDelayed(activity.autoRestart, 5000);
            Log.d(TAG, "autoRestart..........");
        }
    };
    private boolean isPauseScanning;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.productCodeData = (ScanProductListActivity.ProductCodeData) getIntent().getSerializableExtra(EXTRA_KEY_DATA);
        if (ScanProductListActivity.TAG.equals(getIntent().getStringExtra(COMEFROME))) {
            findViews();
            refreshView();
        }

    }


    View go_back;
    TextView capture_scan_status;
    TextView capture_not_num;
    TextView capture_alway_num;
    TextView capture_product_name;

    View product_info_layout;

    private void findViews() {

        product_info_layout = findViewById(R.id.product_info_layout);
        product_info_layout.setVisibility(View.VISIBLE);
        go_back = findViewById(R.id.go_back);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitScanning("");
            }
        });

        go_back.setVisibility(View.VISIBLE);
        capture_scan_status = (TextView) findViewById(R.id.capture_scan_status);
        capture_not_num = (TextView) findViewById(R.id.capture_not_num);
        capture_alway_num = (TextView) findViewById(R.id.capture_alway_num);
        capture_product_name = (TextView) findViewById(R.id.capture_product_name);
    }

    private void refreshView() {
        capture_scan_status.setText(productCodeData.getScanProductSatus());
        capture_not_num.setText(productCodeData.getNotScanSize());
        capture_alway_num.setText(productCodeData.getAlwayScanSize());
        capture_product_name.setText(productCodeData.getProductName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(autoRestart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHandler.postDelayed(autoRestart, 5000);
    }

    private boolean isContinueScan() {
        return productCodeData != null;
    }

    private boolean isScanFinish() {
        if (!isContinueScan()) {
            return true;
        }

        return !TextUtils.isEmpty(this.productCodeData.getCurrentCode()) && !this.productCodeData.isNextRowCodeEmpty();
    }

    private void checkExists(final String code, final Runnable callback) {

        if (productCodeData.existsCode(code)) {
            isPauseScanning = true;
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("产品列表中已经含有此二维码的商品，可能会导致重复，是否仍要录入？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isPauseScanning = false;


                            productCodeData.setCurrentCode(code);
                            uiHandler.post(callback);
//                            refreshView();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isPauseScanning = false;
                            uiHandler.post(callback);
                        }
                    })
                    .create()
                    .show();

            return;
        }
        productCodeData.setCurrentCode(code);
        uiHandler.post(callback);

    }

    @Override
    protected int overrideBeepRes() {
        return R.raw.found_target;
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        if (isPauseScanning) {
            return;
        }
        if (rawResult == null || TextUtils.isEmpty(rawResult.getText())) {
            restartPreviewAfterDelay(RESTART_DELAY);
            return;
        }
        if (beepManager != null) {
            beepManager.playBeepSoundAndVibrate();
        }

        uiHandler.removeCallbacks(autoRestart);
        uiHandler.postDelayed(autoRestart, 5000);

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        final String codeContent = rawResult.getText();
        uiHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                final MyCaptureActivity activity = (MyCaptureActivity) weakThis.get();
                if (activity == null) {
                    return;
                }

                if (activity.mProgressDialog != null) {
                    activity.mProgressDialog.dismiss();
                    activity.mProgressDialog = null;
                }

                if (activity.isContinueScan()) {
                    activity.checkExists(codeContent, new Runnable() {
                        @Override
                        public void run() {
                            boolean isEnd = true;
                            if (!activity.isScanFinish()) {
                                if (TextUtils.isEmpty(activity.productCodeData.getCurrentCode())
                                        || activity.productCodeData.increaseRowIndex()) {

                                    isEnd = false;
                                    refreshView();
                                    activity.restartPreviewAfterDelay(RESTART_DELAY);
                                }
                            }

                            if (isEnd) {
                                quitScanning(codeContent);
                            }
                        }
                    });

                    return;
                }

                quitScanning(codeContent);
            }
        }, 500);
    }

    public void quitScanning(String codeContent) {

        Intent data = new Intent();
        if (productCodeData != null) {
            data.putExtra(EXTRA_KEY_DATA, productCodeData);
        }
        data.putExtra(EXTRA_KEY_CONTENT, codeContent);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        goBack();
        return super.onKeyDown(keyCode, event);
    }

    private void goBack() {
        if (productCodeData != null) {
            Intent data = new Intent();
            data.putExtra(EXTRA_KEY_DATA, productCodeData);
            setResult(RESULT_OK, data);
        }
    }
}
