package com.socialbusiness.dev.orangebusiness.activity.product;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.kollway.android.imageviewer.activity.ImageViewerActivity;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.MyApplication;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.base.WebActivity;
import com.socialbusiness.dev.orangebusiness.adapter.MainViewPagerAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.component.indicator.IconPageIndicator;
import com.socialbusiness.dev.orangebusiness.config.Constance;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.CommonUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.StringUtil;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;
import com.socialbusiness.dev.orangebusiness.util.WeChatUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class ProductDetailActivity extends BaseActivity {
    public static final String TAG = "ProductDetailActivity";

    private View mAllLayout;
    private Gallery mGallery;
    private ImageAdapter mImageAdapter;
    private TextView mName;
    private TextView mNo;
    private TextView mPrice;
    private TextView mProperties;
    private NetworkImageView mVideoImage;
    private ImageView mVideoImageClick;
    private TextView mProductIntroduce;
    private TextView mAddtoShoppingCart;
    private LinearLayout mDynamicLayout;
    private WebView productDetailWebView;

    private String id;
    private Product mProduct;
    private User mLoginUser;
    private ShopCartDbHelper helper;
    private ImageLoader imageLoader;
    private Bitmap shareImage;
    private int mProductTypeInt;
    IconPageIndicator indicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        setUp();
        findView();
        registerListener();
        callAPIGetProduct();


    }

    private void setUp() {
        mLoginUser = SettingsManager.getLoginUser();
        if (mLoginUser == null) {
            Intent intent = new Intent(Constant.RECEIVER_NO_LOGIN);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            finish();
        }
        setTitle(R.string.product_detail);
        imageLoader = APIManager.getInstance(ProductDetailActivity.this).getImageLoader();
        id = getIntent().getStringExtra("id");
        mProductTypeInt = getIntent().getIntExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, Constant.DEFAULT_ILLEGAL_PRODUCT_TYPE);
        helper = new ShopCartDbHelper(ProductDetailActivity.this);
    }

    LinearLayout activity_product_detail_properties_layout;
    View product_info_price_layout;
    private void findView() {
        mAllLayout = findViewById(R.id.activity_product_detail_all_layout);
        mGallery = (Gallery) findViewById(R.id.activity_product_detail_goods_images);
        mName = (TextView) findViewById(R.id.activity_product_detail_name);
        mNo = (TextView) findViewById(R.id.activity_product_detail_no);
        mPrice = (TextView) findViewById(R.id.activity_product_detail_price);
        mProperties = (TextView) findViewById(R.id.activity_product_detail_properties);
        mVideoImage = (NetworkImageView) findViewById(R.id.activity_product_detail_video_image);
        mVideoImageClick = (ImageView) findViewById(R.id.activity_product_detail_video_image_click);
        mProductIntroduce = (TextView) findViewById(R.id.activity_product_detail_product_introduce_value);
        mAddtoShoppingCart = (TextView) findViewById(R.id.activity_product_detail_add_to_shopping_cart);
        mDynamicLayout = (LinearLayout) findViewById(R.id.activity_product_detail_dynamic);

        mAllLayout.setVisibility(View.INVISIBLE);
        mDynamicLayout.setVisibility(View.GONE);
        mNo.setVisibility(View.GONE);

        if (mProductTypeInt == Product.ProductType.KWMProductTypeStock.getIntValue()) {
            mAddtoShoppingCart.setVisibility(View.GONE);

        }
        productDetailWebView = (WebView) findViewById(R.id.web_view);
        productDetailWebView.loadUrl(String.format(APIManager.PRODUCT_DETAIL_URL, mLoginUser.companyId, id));

        top_viewpager=(ViewPager)findViewById(R.id.top_viewpager);
        indicator=(IconPageIndicator)findViewById(R.id.indicator);

        activity_product_detail_properties_layout=(LinearLayout)findViewById(R.id.activity_product_detail_properties_layout);
        product_info_price_layout=findViewById(R.id.product_info_price_layout);

       View top_viewpager_layout= findViewById(R.id.top_viewpager_layout);
       ViewGroup.LayoutParams lp= top_viewpager_layout.getLayoutParams();
//        Log.e("===",""+CommonUtil.getScreenWidth(this));
        if(lp==null){
            lp=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtil.getScreenWidth(this));

        }else{
            lp.height= CommonUtil.getScreenWidth(this);
        }
        top_viewpager_layout.setLayoutParams(lp);
    }

    ViewPager top_viewpager;

    private void registerListener() {
        mGallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mProduct != null && mProduct.images != null && !mProduct.images.isEmpty()) {
                    ArrayList<String> urls = new ArrayList<>(mProduct.images.size());
                    for (String image : mProduct.images) {
                        urls.add(APIManager.toAbsoluteUrl(image));
                    }
                    Intent intent = new Intent(ProductDetailActivity.this, ImageViewerActivity.class);
                    intent.putStringArrayListExtra(ImageViewerActivity.KEY_URLS, urls);
                    intent.putExtra(ImageViewerActivity.KEY_INDEX, position);
                    startActivity(intent);
                }
            }
        });

        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.activity_product_detail_video_image_click) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(mProduct.video);
                        intent.setData(uri);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (v.getId() == R.id.activity_product_detail_add_to_shopping_cart) {
                    helper.addShopCart(mProduct);
                    ToastUtil.show(ProductDetailActivity.this, "加入购物车成功");
                }else if(v.getId() == R.id.product_info_price_layout){

                    Intent intent=new Intent(ProductDetailActivity.this, WebActivity.class);
                    intent.putExtra(Constance.WEB_ACTIVITY_TITLE,"图文详情");
                    intent.putExtra(Constance.WEB_DETAIL,String.format(APIManager.PRODUCT_DETAIL_URL, mLoginUser.companyId, id));
                    startActivity(intent);
                }
            }
        };

        mVideoImageClick.setOnClickListener(listener);
        mAddtoShoppingCart.setOnClickListener(listener);
        product_info_price_layout.setOnClickListener(listener);
    }

    private void callAPIGetProduct() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("id", id);
        APIManager.getInstance(this).getProduct(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);
                    }
                }, new Response.Listener<RequestResult<Product>>() {

                    @Override
                    public void onResponse(RequestResult<Product> response) {
                        hideLoading();
                        hideNoData();
                        if (hasError(response)) {
                            return;
                        }
                        if (response.data != null) {
                            mProduct = response.data;
//							mImageAdapter = new ImageAdapter();
//							mGallery.setAdapter(mImageAdapter);

                            viewPagerAdapter=new MainViewPagerAdapter(ProductDetailActivity.this);
                            viewPagerAdapter.onDataSetChange(mProduct.images);
                            top_viewpager.setAdapter(viewPagerAdapter);
                            indicator.setViewPager(top_viewpager);
                            setProduct(response.data);
                        }
                    }
                });
    }

    MainViewPagerAdapter viewPagerAdapter;

    private void setProduct(Product product) {
        if (product != null) {
            mName.setText(product.name);
            mPrice.setText(StringUtil.getStringFromFloatKeep2(product.price));
//            mProperties.setText(getStringFromProperties(product.properties));
            getStringFromProperties(product.properties);
            mVideoImage.setImageUrl(APIManager.toAbsoluteUrl(product.video), APIManager.getInstance(getApplicationContext()).getImageLoader());

            if (!TextUtils.isEmpty(product.code)) {
                mNo.setText(String.format("商品编号：%s", product.code));
                mNo.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(product.desc)) {
                mProductIntroduce.setText("此产品没有\"产品介绍\"");
            } else {
                mProductIntroduce.setText(product.desc + "");
            }
            setItem(product.items);
            if (product.canBuy == 0) {
                mAddtoShoppingCart.setVisibility(View.GONE);
                mPrice.setVisibility(View.GONE);
            }
            if (TextUtils.isEmpty(mProduct.video)) {
                findViewById(R.id.video_layout).setVisibility(View.GONE);
                findViewById(R.id.video_line).setVisibility(View.GONE);
            }
        }
        mAllLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 动态更新item
     *
     * @param item
     */
    private void setItem(ArrayList<Hashtable<String, String>> item) {
        String key;
        String value;
        boolean isAddViewDone = false;
        for (int i = 0; i < item.size(); i++) {
            for (Iterator<String> itr = item.get(i).keySet().iterator(); itr.hasNext(); ) {
                key = itr.next();
                value = item.get(i).get(key);
                if (!TextUtils.isEmpty(key)) {
                    addViews(key, value);
                    isAddViewDone = true;
                }
            }
        }

        if (isAddViewDone) {
            mDynamicLayout.setVisibility(View.VISIBLE);
        }
    }

    private void addViews(String key, String value) {
        mDynamicLayout.setOrientation(LinearLayout.VERTICAL);
        TextView keyTV = new TextView(this);
        TextView valueTV = new TextView(this);
        View view = new View(this);

        int px_10 = AndroidUtil.dip2px(getApplicationContext(), 10);
        int px_5 = AndroidUtil.dip2px(getApplicationContext(), 5);

        keyTV.setText(key);
        keyTV.setTextSize(14);
        keyTV.setPadding(px_10, px_5, px_10, 0);
        valueTV.setText(value);
        valueTV.setPadding(px_10, px_5, px_10, px_5);
        valueTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1));
        view.setBackgroundColor(getResources().getColor(R.color.cell_divider));

        mDynamicLayout.addView(keyTV);
        mDynamicLayout.addView(valueTV);
        mDynamicLayout.addView(view);
    }

    /**
     * 利用循环遍历出Hashtable的key和value并进行拼接
     *
     * @param properties ArrayList<Hashtable<String, String>>
     * @return String
     */
    private String getStringFromProperties(ArrayList<Hashtable<String, String>> properties) {
        String str = "";
        String key;
        for (int i = 0; i < properties.size(); i++) {
            for (Iterator<String> itr = properties.get(i).keySet().iterator(); itr.hasNext(); ) {
                key = itr.next();
//                str +=  properties.get(i).get(key).trim() + "\n";
                //TextView tv=(TextView)getLayoutInflater().inflate(R.layout.properties_text,null);
              //  tv.setText();
                if(i==0){
                    mName.setText(properties.get(i).get(key).trim());
                }else{
                    mProperties.setText(properties.get(i).get(key).trim());
                }
                //activity_product_detail_properties_layout.addView(tv);
            }
        }
        return str;
    }

    @Override
    public void finish() {
        super.finish();
        Log.e("detail", "===finish");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("detail", "===onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();

        setRightIcon(R.drawable.bg_selector_share, new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mProduct == null) {
                    return;
                }

                boolean isEmptyImages = mProduct.images == null || mProduct.images.isEmpty();
                boolean hasShareImage = shareImage != null && !shareImage.isRecycled();
                if (isEmptyImages || hasShareImage) {
                    shareToWeChat();
                } else {
                    int width = AndroidUtil.dip2px(ProductDetailActivity.this, 160);
                    int height = AndroidUtil.dip2px(ProductDetailActivity.this, 100);
                    String requestUrl = APIManager.toAbsoluteUrl(mProduct.images.get(0));

                    showLoading();
                    imageLoader.get(requestUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            ProductDetailActivity activity = (ProductDetailActivity) weakThis.get();
                            activity.hideLoading();

                            Bitmap result = response.getBitmap();
                            if (result != null) {
                                int w = result.getWidth();
                                float scale = 100.0f / w;
                                activity.shareImage = AndroidUtil.scale(result, scale);
                            }
                            activity.shareToWeChat();
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            BaseActivity activity = (BaseActivity) weakThis.get();
                            activity.hideLoading();
                        }
                    }, width, height);
                }
            }
        });
    }

    private void shareToWeChat() {
        if (mProduct == null || !mProduct.isValidate()) {
            return;
        }

        final MyApplication application = (MyApplication) weakThis.get().getApplication();
        CharSequence[] items = {"微信朋友圈", "微信好友"};
        final String title = mProduct.name;
        final String message = mProduct.shareText;

        String productId = mProduct.id;
        User loginUser = SettingsManager.getLoginUser();
        if (loginUser == null) {
            return;
        }
        String companyId = loginUser.companyId;
        String relativeUrlString = "/Product/Detail/Info?id=" + productId + "&companyId=" + companyId;
        final String urlString = APIManager.toAbsoluteUrl(relativeUrlString);

        new AlertDialog.Builder(this).setTitle("分享到").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        WeChatUtil.shareToCircle(title, message, shareImage, urlString, application);
                        break;
                    }
                    case 1: {
                        WeChatUtil.shareToFriend(title, message, shareImage, urlString, application);
                        break;
                    }
                    default:
                        break;
                }
            }
        }).show();
    }

    private class ImageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mProduct != null && mProduct.images != null ? mProduct.images.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mProduct != null && mProduct.images != null ? mProduct.images.get(position) : "";
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LinearLayout.inflate(ProductDetailActivity.this, R.layout.adapter_goods_gallery_item, null);
                holder = new ViewHolder();
                holder.imgView = (NetworkImageView) convertView.findViewById(R.id.adapter_goods_gallery_item_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.imgView.setErrorImageResId(R.drawable.ic_default);
            holder.imgView.setDefaultImageResId(R.drawable.ic_default);

            holder.imgView.setImageUrl(APIManager.toAbsoluteUrl((String) getItem(position)),
                    imageLoader);
            return convertView;
        }

        private class ViewHolder {
            NetworkImageView imgView;
        }
    }
}
