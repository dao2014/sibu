package com.socialbusiness.dev.orangebusiness.fragment.mall;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.ShoppingCartListActivity;
import com.socialbusiness.dev.orangebusiness.activity.product.ProductListActivity;
import com.socialbusiness.dev.orangebusiness.adapter.MallLeftItemAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.MallRightItemAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.fragment.base.BaseFragment;
import com.socialbusiness.dev.orangebusiness.model.Category;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;
import com.socialbusiness.dev.orangebusiness.util.Log;
import com.socialbusiness.dev.orangebusiness.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by zkboos on 2015/7/3.
 */
public class MallFrament extends BaseFragment {
    BaseActivity baseActivity;
    //动画时间
    private int AnimationDuration = 1000;
    //正在执行的动画数量
    private int number = 0;
    //是否完成清理
    private boolean isClean = false;
    private FrameLayout animation_viewGroup;
    private Handler myHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 0:
                    //用来清除动画后留下的垃圾
                    try{
                        animation_viewGroup.removeAllViews();
                    }catch(Exception e){

                    }

                    isClean = false;

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
        helper = new ShopCartDbHelper(this.getActivity());
//        getDataBaseProducts();
    }

    HashMap<String, ShopCartItem> shopCartItemHashMap = new HashMap<>();

    public void getDataBaseProducts() {

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                shopCartItemHashMap.clear();
                List<ShopCartItem> list = helper.getAllShopCartItems();

                for (int i = 0; i < list.size(); i++) {
                    ShopCartItem item = list.get(i);
                    shopCartItemHashMap.put(item.id, item);
                }
                Log.d("==", "" + shopCartItemHashMap);
                if(mallRightItemAdapter!=null)
                    mallRightItemAdapter.setShopCartItemHashMap(shopCartItemHashMap);
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (mallRightItemAdapter != null)
                    mallRightItemAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void setContentView(LayoutInflater inflater, ViewGroup mLayerContextView) {
        inflater.inflate(R.layout.mall_layout, mLayerContextView);
        findViews(mLayerContextView);
        initData();
        setListeners();

    }

    private int mCurPage = 1;
    private int mTotalPage;
    ShopCartDbHelper helper;

    private void initData() {

        mallLeftItemAdapter = new MallLeftItemAdapter(this.getActivity());
        mall_layout_left_list.setAdapter(mallLeftItemAdapter);
        callAPIListProductCategory();


        mallRightItemAdapter = new MallRightItemAdapter(this.getActivity());
        mallRightItemAdapter.setHelper(this.helper);
        mallRightItemAdapter.SetOnSetHolderClickListener(new MallRightItemAdapter.HolderClickListener() {

            @Override
            public void onHolderClick(Drawable drawable, int[] start_location, View v) {
                if(drawable!=null)
                 doAnim(drawable,start_location);
                onclickSum(v);
            }
        });
        mall_layout_right_list.setAdapter(mallRightItemAdapter);

        mall_layout_right_list.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mall_layout_left_list.setMode(PullToRefreshBase.Mode.DISABLED);
    }

    /****start 动画****/

    private void doAnim(Drawable drawable,int[] start_location){
        if(!isClean){
            setAnim(drawable,start_location);
        }else{
            try{
                animation_viewGroup.removeAllViews();
                isClean = false;
                setAnim(drawable,start_location);
            }catch(Exception e){
                e.printStackTrace();
            }
            finally{
                isClean = true;
            }
        }
    }

    /**
     * @Description: 创建动画层
     * @param
     * @return void
     * @throws
     */
    private FrameLayout createAnimLayout() {
        ViewGroup rootView = (ViewGroup) getActivity().getWindow().getDecorView();
        FrameLayout animLayout = new FrameLayout(getActivity());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    /**
     * @deprecated 将要执行动画的view 添加到动画层
     * @param vg
     *        动画运行的层 这里是frameLayout
     * @param view
     *        要运行动画的View
     * @param location
     *        动画的起始位置
     * @return
     */
    private View addViewToAnimLayout(ViewGroup vg,View view,int[] location){
        int x = location[0];
        int y = location[1];
        vg.addView(view);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                dip2px(getActivity(),70),dip2px(getActivity(),70));
        lp.leftMargin = x;
        lp.topMargin = y;
        //view.setPadding(5, 5, 5, 5);
        view.setLayoutParams(lp);

        return view;
    }
    /**
     * 动画效果设置
     * @param drawable
     *       将要加入购物车的商品
     * @param start_location
     *        起始位置
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setAnim(Drawable drawable,int[] start_location){


        Animation mScaleAnimation = new ScaleAnimation(1.5f,0.0f,1.5f,0.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
        mScaleAnimation.setDuration(AnimationDuration);
        mScaleAnimation.setFillAfter(true);


        final ImageView iview = new ImageView(getActivity());
        iview.setImageDrawable(drawable);
        final View view = addViewToAnimLayout(animation_viewGroup,iview,start_location);
        view.setAlpha(0.6f);

        int[] end_location = new int[2];
        fragment_mypurchase_my_shopping_cart_nums.getLocationInWindow(end_location);
        int endX = end_location[0];
        int endY = end_location[1]-start_location[1];

        Animation mTranslateAnimation = new TranslateAnimation(0,endX,0,endY-190);
        /**第二个参数 旋转效果**/
        Animation mRotateAnimation = new RotateAnimation(0, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setDuration(AnimationDuration);
        mTranslateAnimation.setDuration(AnimationDuration);
        AnimationSet mAnimationSet = new AnimationSet(true);

        mAnimationSet.setFillAfter(true);
        mAnimationSet.addAnimation(mRotateAnimation);
        mAnimationSet.addAnimation(mScaleAnimation);
        mAnimationSet.addAnimation(mTranslateAnimation);

        mAnimationSet.setAnimationListener(new Animation.AnimationListener(){

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                number++;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub

                number--;
                if(number==0){
                    isClean = true;
                    myHandler.sendEmptyMessage(0);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

        });
        view.startAnimation(mAnimationSet);

    }
    /**
     * dip，dp转化成px 用来处理不同分辨路的屏幕
     * @param context
     * @param dpValue
     * @return
     */
    private int dip2px(Context context,float dpValue){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale +0.5f);
    }

    /**
     * 内存过低时及时处理动画产生的未处理冗余
     */
    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        isClean = true;
        try{
            animation_viewGroup.removeAllViews();
        }catch(Exception e){
            e.printStackTrace();
        }
        isClean = false;
        super.onLowMemory();
    }

        /***end 动画****/

    ArrayList<Category> categories;
    HashMap<String, ArrayList<Product>> productHashMap = new HashMap<>();

    private void callAPIListProductCategory() {
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", mCurPage + "");
        APIManager.getInstance(getActivity()).listProductCategories(parameters, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                showException(error);
            }
        }, new Response.Listener<RequestListResult<Category>>() {
            @Override
            public void onResponse(RequestListResult<Category> response) {
                hideLoading();
                if (hasError(response)) {
                    return;
                }
                categories = response.data;
                mallLeftItemAdapter.appendToList(response.data);

                if (response.data != null && response.data.size() != 0) {
                    callAPIListProduct(response.data.get(0).id);
                }
            }
        });

    }

    String currentCategoryId = "";

    private void callAPIListProduct(final String categoryId) {
        Log.e("=====", "==" + categoryId);
        currentCategoryId = categoryId;
        if (productHashMap.get(categoryId) != null) {

            mallRightItemAdapter.onDataChange(productHashMap.get(categoryId));
            /*
                         增加上啦加载更多，
                        mCurPage=1;

                         */
            return;
        }
        showLoading();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("page", mCurPage + "");
        parameters.put("pageSize", "1000");
        parameters.put("categoryId", categoryId);
        APIManager.getInstance(this.getMyApplication()).listProduct(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        showException(error);
                    }
                }, new Response.Listener<RequestListResult<Product>>() {

                    @Override
                    public void onResponse(RequestListResult<Product> response) {
                        hideLoading();
                        mall_layout_right_list.onRefreshComplete();
                        if (hasError(response)) {
                            return;
                        }
                        /*
                         增加上啦加载更多，
                         mallRightItemAdapter.appendlist
                         productHashMap.put(categoryId, response.data);
                         */

                        mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
                        Log.e("totalPage==", "" + mTotalPage);
                        mallRightItemAdapter.onDataChange(response.data);
                        productHashMap.put(categoryId, response.data);
                    }
                });
    }

    View mall_header_root;
    PullToRefreshListView mall_layout_left_list, mall_layout_right_list;
    MallLeftItemAdapter mallLeftItemAdapter;
    MallRightItemAdapter mallRightItemAdapter;
    EditText fragment_product_input;
    TextView fragment_mypurchase_my_shopping_cart_nums; //购物车

    public void findViews(View view) {

        mall_header_root = view.findViewById(R.id.mall_header_root);
        this.mall_header_root.setVisibility(View.VISIBLE);
        mall_layout_left_list = (PullToRefreshListView) view.findViewById(R.id.mall_layout_left_list);
        mall_layout_right_list = (PullToRefreshListView) view.findViewById(R.id.mall_layout_right_list);
        this.mall_header_root.setVisibility(View.VISIBLE);
        fragment_product_input = (EditText) this.mall_header_root.findViewById(R.id.fragment_product_input);
        fragment_mypurchase_my_shopping_cart_nums = (TextView) this.mall_header_root.findViewById(R.id.fragment_mypurchase_my_shopping_cart_nums);
        this.mall_header_root.findViewById(R.id.mall_shoping_cart_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ShoppingCartListActivity.class);
                startActivity(intent);
            }
        });
        animation_viewGroup = createAnimLayout();
    }

    public final int GETSHOPINGCARTS = 1000;
    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GETSHOPINGCARTS:
                    if (msg.arg1 == 0)
                        fragment_mypurchase_my_shopping_cart_nums.setVisibility(View.GONE);
                    fragment_mypurchase_my_shopping_cart_nums.setText(msg.arg1 + "");
                    break;
            }
        }
    };

    public void getShopingCart() {
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//        List<ShopCartItem> list = helper.getAllShopCartItems();
//        if (list.size() == 0)
//            fragment_mypurchase_my_shopping_cart_nums.setVisibility(View.GONE);
//        else fragment_mypurchase_my_shopping_cart_nums.setVisibility(View.VISIBLE);
//        fragment_mypurchase_my_shopping_cart_nums.setText(list.size() + "");

        int count = helper.getAllShopCartItemsCount();
        if (count == 0)
            fragment_mypurchase_my_shopping_cart_nums.setVisibility(View.GONE);
        else fragment_mypurchase_my_shopping_cart_nums.setVisibility(View.VISIBLE);
        fragment_mypurchase_my_shopping_cart_nums.setText(count + "");

//                handler.sendMessage(handler.obtainMessage(GETSHOPINGCARTS, list.size(), 0));

//            }
//        }.start();
    }

    int count;

    public void getShopingCart(boolean needSql) {


        if (needSql) {
            count = helper.getAllShopCartItemsCount();
            getDataBaseProducts();
        }
        if (count == 0)
            fragment_mypurchase_my_shopping_cart_nums.setVisibility(View.GONE);
        else fragment_mypurchase_my_shopping_cart_nums.setVisibility(View.VISIBLE);
        fragment_mypurchase_my_shopping_cart_nums.setText(count + "");

    }

    @Override
    public void onResume() {
        super.onResume();
        getShopingCart(true);

    }

    @Override
    public void onPause() {
        super.onPause();

    }


    private void setListeners() {
        mall_layout_left_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Log.e("left", "==" + position);
                mallLeftItemAdapter.setSelecteIndex(position - 1);
                mallLeftItemAdapter.notifyDataSetChanged();

                /*
                mCurPage = 1;
                 */

                callAPIListProduct(categories.get(position - 1).id);
            }
        });
        mall_layout_right_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                ToastUtil.show(getMyApplication(), "==");

//                Product item = (Product) parent.getAdapter().getItem(position);
//                if (item == null) {
//                    return;
//                }
//                Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
//                intent.putExtra("id", item.id);
//                intent.putExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, Constant.DEFAULT_ILLEGAL_PRODUCT_TYPE);
//                startActivity(intent);
            }
        });

        mall_layout_right_list.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mCurPage = 1;
                productHashMap.put(currentCategoryId, null);//值空map重新获取数据
                callAPIListProduct(currentCategoryId);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

                /*
                增加上啦加载更多
                if (mCurPage < mTotalPage && mCategoryList != null) {
					mCurPage++;
					callAPIListProduct();
				}
                 */
            }
        });

        mallRightItemAdapter.setAddDeleteClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fragment_product_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId || EditorInfo.IME_ACTION_SEARCH == actionId) {
                    String keyWord = v.getText().toString();
                    if (TextUtils.isEmpty(keyWord)) {
                        ToastUtil.show(getActivity(), "搜索内容不能为空");
                    } else {
                        Intent intent = new Intent(getActivity(),
                                ProductListActivity.class);
                        intent.putExtra("categoryId", "0");
                        intent.putExtra("isSearch", true);
                        intent.putExtra("keyword", keyWord);
                        // 将搜索框内容清空
                        startActivity(intent);
                    }
                }
                return false;
            }
        });
    }

    /**
     * 点击按钮时间
     * @param v
     */
    public void onclickSum(View v){
        switch (v.getId()) {
            case R.id.mall_right_item_delete:
                Product p = (Product) v.getTag();
                ShopCartItem item = helper.getShopCartItemByProductId(p.id);
                if (item != null) {
                    if (item.quantity > 1) {

                        helper.minusShopCart(item);
                        item.quantity--;//数量减一
                        shopCartItemHashMap.put(item.id, item);
                    } else {
                        helper.deleteShopCardItemById(item.id);
                        shopCartItemHashMap.remove(item.id);
                    }
                }
                mallRightItemAdapter.notifyDataSetChanged();
                count--;
                getShopingCart(false);
                break;
            case R.id.mall_right_item_add:
//
//                int[] start_location = new int[2];
//                Activity ac = (Activity) v.getContext();
//                NetworkImageView mImageView = (NetworkImageView) ac.findViewById(R.id.mall_right_item_image);
//                mImageView.getLocationInWindow(start_location);//获取点击商品图片的位置
//                Drawable drawable = mImageView.getDrawable();//复制一个新的商品图标
//                doAnim(drawable,start_location);

                p = (Product) v.getTag();
                item = helper.addShopCart(p);
                if (item == null) {//数据库没有数据新建一个
                    item = new ShopCartItem();
                    item.id = p.id;
                    item.quantity = 1;
                } else {//数据库有数量加一
                    item.quantity++;
                }
                shopCartItemHashMap.put(item.id, item);
                mallRightItemAdapter.notifyDataSetChanged();
                count++;
                getShopingCart(false);
//                        Log.e("==", "加入购物车");
//                        ToastUtil.show(getMyApplication(),"=="+v.getTag());
                break;
        }
    }

    @Override
    public void showFragment() {
        super.showFragment();


    }



    @Override
    public void hideFragment() {
        super.hideFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.helper.closeOpened();
    }
}
