package com.socialbusiness.dev.orangebusiness.activity.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;
import com.socialbusiness.dev.orangebusiness.activity.order.ShoppingCartListActivity;
import com.socialbusiness.dev.orangebusiness.adapter.MallRightItemAdapter;
import com.socialbusiness.dev.orangebusiness.adapter.ProductAdapter;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.db.ShopCartDbHelper;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.util.AndroidUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ProductListActivity extends BaseActivity {

	private PullToRefreshListView mListView;
	private ProductAdapter mAdapter;
    MallRightItemAdapter mallRightItemAdapter;
	private List<Product> mProductList;
	private int mCurPage = 1;
	private int mTotalPage;

	private String categoryId;
	private boolean isSearch;
	private String keyword;
    private int mProductTypeInt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_list);
		setUp();
		findView();
		registerListener();
		callDifferentApi();
	}
    ShopCartDbHelper helper;
	private void setUp() {
		setTitle(R.string.product_list);

        helper = new ShopCartDbHelper(this);
		mAdapter = new ProductAdapter(this);
        mallRightItemAdapter=new MallRightItemAdapter(this);
        mallRightItemAdapter.setHelper(this.helper);

		categoryId = getIntent().getStringExtra("categoryId");
		isSearch = getIntent().getBooleanExtra("isSearch", false);
		keyword = getIntent().getStringExtra("keyword");
        mProductTypeInt = getIntent().getIntExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, Constant.DEFAULT_ILLEGAL_PRODUCT_TYPE);
        mAdapter.setProductTypeInt(mProductTypeInt);
	}

	private void findView() {
		mListView = (PullToRefreshListView) findViewById(R.id.activity_product_list);
		mListView.setAdapter(mallRightItemAdapter);
//		mListView.getRefreshableView().setDividerHeight(0);
	}

    private void callDifferentApi() {
        if (isSearch && mProductTypeInt == Product.ProductType.KWMProductTypeStock.getIntValue()) {
            callAPIListStockProductSearch();

        } else if (isSearch) {
            callAPIListProductSearch();

        } else if (mProductTypeInt == Product.ProductType.KWMProductTypeStock.getIntValue()) {
            callAPIListStockProduct();

        } else {
            callAPIListProduct();
        }
    }

	private void registerListener() {
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
				Product item = (Product) parent.getAdapter().getItem(position);
				if (item == null) {
                    return;
				}
				intent.putExtra("id", item.id);
                intent.putExtra(Constant.EXTRA_KEY_PRODUCT_TYPE, mProductTypeInt);
				startActivity(intent);
			}
		});

		mListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
                mCurPage = 1;
                if (isSearch) {
                    callAPIListProductSearch();
                } else {
                    callAPIListProduct();
                }
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
                if (mCurPage < mTotalPage && mProductList != null) {
                    if (isSearch) {
                        callAPIListProductSearch();
                    } else {
                        callAPIListProduct();
                    }
                }
			}
		});

        mallRightItemAdapter.setAddDeleteClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mall_right_item_delete:
                        Product p = (Product) v.getTag();
                        ShopCartItem item = helper.getShopCartItemByProductId(p.id);
                        if (item != null) {
                            if (item.quantity > 1)
                                helper.minusShopCart(item);
                            else helper.deleteShopCardItemById(item.id);
                        }
                        mallRightItemAdapter.notifyDataSetChanged();
//                        getShopingCart();
                        break;
                    case R.id.mall_right_item_add:
                        helper.addShopCart((Product) v.getTag());
                        mallRightItemAdapter.notifyDataSetChanged();
//                        getShopingCart();
//                        Log.e("==", "加入购物车");
//                        ToastUtil.show(getMyApplication(),"=="+v.getTag());
                        break;
                }
            }
        });
	}

	private void callAPIListProduct() {
		showLoading();
		hideNoData();
		Hashtable<String, String> parameters = new Hashtable<>();
		parameters.put("page", mCurPage + "");
		parameters.put("categoryId", categoryId);
		APIManager.getInstance(this).listProduct(parameters,
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						errorCallBack(error);
					}
				}, new Response.Listener<RequestListResult<Product>>() {

					@Override
					public void onResponse(RequestListResult<Product> response) {
                        successCallBack(response);
					}
				});
	}

    private void callAPIListStockProduct() {
        showLoading();
        hideNoData();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("page", mCurPage + "");
        parameters.put("categoryId", categoryId);
        APIManager.getInstance(this).listStockProduct(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorCallBack(error);
                    }
                }, new Response.Listener<RequestListResult<Product.StockProduct>>() {

                    @Override
                    public void onResponse(RequestListResult<Product.StockProduct> response) {
                        successCallBackStockProduct(response);
                    }
                });
    }

	private void callAPIListProductSearch() {
		showLoading();
		hideNoData();
		Hashtable<String, String> parameters = new Hashtable<>();
		parameters.put("page", mCurPage + "");
		parameters.put("keyword", keyword);
		APIManager.getInstance(this).listProductSearch(parameters,
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						errorCallBack(error);
					}
				}, new Response.Listener<RequestListResult<Product>>() {

					@Override
					public void onResponse(RequestListResult<Product> response) {
						successCallBack(response);
					}
				});
	}

    private void callAPIListStockProductSearch() {
        showLoading();
        hideNoData();
        Hashtable<String, String> parameters = new Hashtable<>();
        parameters.put("page", mCurPage + "");
        parameters.put("keyword", keyword);
        APIManager.getInstance(this).listStockProductSearch(parameters,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorCallBack(error);
                    }
                }, new Response.Listener<RequestListResult<Product.StockProduct>>() {

                    @Override
                    public void onResponse(RequestListResult<Product.StockProduct> response) {
                        successCallBackStockProduct(response);
                    }
                });
    }

    private void errorCallBack(VolleyError error) {
        hideLoading();
        showException(error);
        mListView.onRefreshComplete();
    }

    private void successCallBack(RequestListResult<Product> response) {
        hideLoading();
        if (hasError(response)) {
            return;
        }
        if (response.data != null && response.data.size() > 0) {
            if (mCurPage == 1) {
                mProductList = response.data;
//                mAdapter.setProductList(mProductList);
                mallRightItemAdapter.onDataChange(mProductList);
            } else if (mCurPage > 1 && mProductList != null) {
//                mProductList.addAll(response.data);
                mallRightItemAdapter.appendToList(response.data);
            }

            mTotalPage = AndroidUtil.getTotalPage(response.totalCount, response.pageSize);
//            mAdapter.notifyDataSetChanged();
        }
        setListViewMode();
    }

    private void successCallBackStockProduct(RequestListResult<Product.StockProduct> response) {
        RequestListResult<Product> productResponse = new RequestListResult<>();
        productResponse.data = stockProductToProduct(response.data);
        productResponse.code = response.code;
        productResponse.message = response.message;
        productResponse.pageSize = response.pageSize;
        productResponse.page = response.page;
        productResponse.totalCount = response.totalCount;
        successCallBack(productResponse);
    }

    private void setListViewMode() {
        if (mallRightItemAdapter.getCount() == 0) {
            showNoDataTips();
        } else if (mCurPage >= mTotalPage) {
            mListView.setMode(Mode.PULL_FROM_START);
        } else {
            mListView.setMode(Mode.BOTH);
        }
        mListView.onRefreshComplete();
    }

    private ArrayList<Product> stockProductToProduct(ArrayList<Product.StockProduct> stockProductList) {
        ArrayList<Product> productList = new ArrayList<>();
        for (Product.StockProduct stockProduct : stockProductList) {
            Product product = new Product();
            product.id = stockProduct.id;
            product.name = stockProduct.name;
            product.video = stockProduct.video;
            product.desc = stockProduct.desc;
            product.images = stockProduct.images;
            product.properties = stockProduct.properties;
            product.items = stockProduct.items;
            product.code = stockProduct.code;
            product.price = stockProduct.price;
            product.seller = stockProduct.seller;
            product.canBuy = stockProduct.canBuy;
            product.amount = stockProduct.stockCount;
            productList.add(product);
        }
        return productList;
    }

	@Override
	protected void onResume() {
		super.onResume();
		setRightIcon(R.drawable.ic_shopcart_selector, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProductListActivity.this, ShoppingCartListActivity.class);
				startActivity(intent);
			}
		});
	}
	
	@Override
	public void showLoading() {
		if (mAdapter.getCount() == 0) {
			super.showLoading();
		}
	}

}
