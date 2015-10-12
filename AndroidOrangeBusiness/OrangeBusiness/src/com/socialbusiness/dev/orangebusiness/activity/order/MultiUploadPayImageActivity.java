package com.socialbusiness.dev.orangebusiness.activity.order;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.MainActivity;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.MultiUploadPayImageLayout;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.model.MultiOrderUpload;
import com.socialbusiness.dev.orangebusiness.util.DialogUtil;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zkboos on 2015/6/17.
 */
public class MultiUploadPayImageActivity extends BaseActivity implements ImageChooserListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_upload_pay_image);

        initView();
        initData();
    }

    private int mChooserType;
    private ImageChooserManager mImageChooserManager;
    private String mImageFilePath;
    MultiUploadPayImageLayout multiUploadPayImageLayout;
    View order_detail_footerview_upload_pays_voucher;
    TextView bottom_total_fee;

    private void initView() {
        setTitle("上传付款凭证");

        multiUploadPayImageLayout = (MultiUploadPayImageLayout) findViewById(R.id.multi_upload_pay_image);
        order_detail_footerview_upload_pays_voucher = findViewById(R.id.order_detail_footerview_upload_pays_voucher);
        order_detail_footerview_upload_pays_voucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order_detail_footerview_upload_pays_voucher.setEnabled(false);
                uploadImages();
            }
        });

        multiUploadPayImageLayout.setListener(new MultiUploadPayImageLayout.UploadPVIClickListener() {
            @Override
            public void chosePayImage() {
                addImage();
            }

            @Override
            public void updatePayImage() {

            }
        });
        bottom_total_fee = (TextView) findViewById(R.id.bottom_total_fee);
        //        multiUploadPayImageLayout.setFeightTotalLayoutVisible();
    }

    private void addImage() {
        DialogUtil.showTakePhotoDialog(this, null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //take picture
                    mChooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
                    mImageChooserManager = new ImageChooserManager(MultiUploadPayImageActivity.this,
                            ChooserType.REQUEST_CAPTURE_PICTURE,
                            Constant.PATH_IMAGE_TEMP_FOLDER,
                            true);
                    mImageChooserManager.setImageChooserListener(MultiUploadPayImageActivity.this);
                    try {
                        mImageFilePath = mImageChooserManager.choose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //choose photo
                    mChooserType = ChooserType.REQUEST_PICK_PICTURE;
                    mImageChooserManager = new ImageChooserManager(MultiUploadPayImageActivity.this,
                            ChooserType.REQUEST_PICK_PICTURE,
                            Constant.PATH_IMAGE_TEMP_FOLDER,
                            true);
                    mImageChooserManager.setImageChooserListener(MultiUploadPayImageActivity.this);
                    try {
                        mImageFilePath = mImageChooserManager.choose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (RESULT_OK == resultCode) {


            if (requestCode == ChooserType.REQUEST_PICK_PICTURE
                    || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE) {
                //选择图片或拍照的result
                if (mImageChooserManager == null) {
                    return;
                }
                showProgressDialog();
                mImageChooserManager.submit(requestCode, intent);
            }
        }
    }

    MultiOrderUpload multiOrderUpload;

    private void initData() {
        multiOrderUpload = new MultiOrderUpload();

//        multiUploadPayImageLayout.setTotalFee(getIntent().getStringExtra(Constance.TotalFee));
//        multiUploadPayImageLayout.setTranstionFee(getIntent().getStringExtra(Constance.TransFee));
        bottom_total_fee.setText(getIntent().getStringExtra(Constance.TotalFee));
    }

    private void uploadImages() {
        if (!bottom_total_fee.getText().toString().equals(StringUtil.getStringFromFloatKeep2(new BigDecimal(multiUploadPayImageLayout.getPayMoney()).doubleValue()))) {

            DialogUtil.showConfirm(MultiUploadPayImageActivity.this, "", "您的付款金额跟总计金额不一致，是否继续？", "确定", "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startEnsure();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    order_detail_footerview_upload_pays_voucher.setEnabled(true);
                }
            });
            return;

        }
        startEnsure();
    }

    private void startEnsure() {
        multiOrderUpload.orderIds = (List<String>) getIntent().getSerializableExtra(Constance.OrderIds);
        multiOrderUpload.payMoney = Float.valueOf(this.multiUploadPayImageLayout.getPayMoney());
        multiOrderUpload.payRemark = this.multiUploadPayImageLayout.getPayRemark();
        multiOrderUpload.payImages = this.multiUploadPayImageLayout.getImageUrls();

        APIManager.getInstance(this).payOrder(multiOrderUpload, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                order_detail_footerview_upload_pays_voucher.setEnabled(true);
                hideLoading();
                hideNoData();
                showException(error);
            }
        }, new Response.Listener<RequestResult<?>>() {
            @Override
            public void onResponse(RequestResult<?> response) {
                hideLoading();
                hideNoData();

                if (hasError(response)) {
                    order_detail_footerview_upload_pays_voucher.setEnabled(true);
                    return;
                }
                if (!TextUtils.isEmpty(response.message)) {
                    ToastUtil.show(MultiUploadPayImageActivity.this, response.message);
                }
                Intent intent = new Intent();
                intent.setClass(MultiUploadPayImageActivity.this, MainActivity.class);
                intent.putExtra(Constant.EXTRA_KEY_TABPOSITION, MainActivity.TabPosition.Order.name());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        if (image != null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    hideProgressDialog();


                    mImageFilePath = image.getFilePathOriginal();

                    if (!TextUtils.isEmpty(mImageFilePath)) {
                        Uri uri = Uri.fromFile(new File(image.getFileThumbnailSmall()));
                        multiUploadPayImageLayout.loadVoucherImage(uri, mImageFilePath);
//
                    }

                }
            });
        }
    }

    @Override
    public void onError(String reason) {

    }
}
