package com.socialbusiness.dev.orangebusiness.model;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.socialbusiness.dev.orangebusiness.api.RequestAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单
 * Created by liangyaotian on 10/18/14.
 */
public class Order extends BaseModel {

    public static enum OrderType {
        /**
         * 待处理
         */
        OrderListTypeHandle(1),
        /**
         * 待付款
         */
        OrderListTypePay(2),
        /**
         * 待发货
         */
        OrderListTypeDeliver(3),
        /**
         * 已发货
         */
        OrderListTypeDelivered(4),
        /**
         * 已收货
         */
        OrderListTypeReceived(5);

        private int mIntValue;

        private OrderType(int intValue) {
            mIntValue = intValue;
        }

        public int getIntValue() {
            return mIntValue;
        }
    }

    public static enum OrderTradeType {
        /**
         * 我的采购
         */
        Buy,
        /**
         * 我的销售
         */
        Sell
    }

    public String createTime;
    public String status;
    public int statusCode;
    public String code;
    public User user;
    public String remark;
    public String payRemark;
    /**
     * 卖家备注
     */
    public String sellerremark;
    /**
     * 货运方式
     */
    public String express;
    /**
     * 货运单号
     */
    public String expresscode;
    public String expressStart;
    public String expressEnd;
    /**
     * 运费
     */
    public float freight;
    /**
     * 付款凭证的图片
     */
    public String payimage;
    public float paymoney;
    /**
     * 是否已转采购(1:已转，0：未转)
     */
    public int ismerge;
    public Address address;
    public boolean isSelected;
    /// 产品数组 [KWMProduct]
    public ArrayList<Product> products = new ArrayList<>();
    /// 产品id数组 [String]
    public ArrayList<String> productIds = new ArrayList<>();
    /// 产品数量数组 [Int]
    public ArrayList<Integer> productAmount = new ArrayList<>();
    /**
     * 多少个装一箱
     */
    public ArrayList<Float> boxCount = new ArrayList<>();

    /// 付款凭证图片数组 [String]
    public ArrayList<String> payimages = new ArrayList<>();

    /**
     * 购物车下单时提交的对象
     */
    public static class OrderAdd {
        public String addressId;
        public String remark;
        public String express;
        public float payMoney;//付款金额
        public String payRemark;//付款备注
        public String freight;
        public List<String> payImages = new ArrayList<>();
        /// 产品id数组 [String]
        public ArrayList<String> productIds = new ArrayList<>();
        /// 产品数量数组 [Int]
        public ArrayList<Integer> productAmount = new ArrayList<>();

        /// 产品价格 [float]
        public ArrayList<Float> productPrice = new ArrayList<>();
    }

    /**
     * 确认订单时提交的对象
     * id		订单ID	form	string
     * productIds		产品id列表	form	List[Int]
     * productAmount		产品数量列表	form	List[Int]
     * productPrice		产品价格列表	form	List[Float]
     * express		货运方式	form	string
     * remark
     * freight 运费
     */
    public static class OrderConfirm {
        public String id;
        public ArrayList<String> productIds = new ArrayList<>();
        public ArrayList<Integer> productAmount = new ArrayList<>();
        public ArrayList<Float> productPrice = new ArrayList<>();
        public String express;
        /**
         * 卖家备注
         */
        public String sellerremark;
        /**
         * 运费
         */
        public float freight;

        public String toJson() {
            return RequestAction.GSON.toJson(this);
        }
    }

    /**
     * 确认发货时提交的对象
     * <p/>
     * {
     * "remark":"卖家备注",
     * "expressEnd":"x000000020",
     * "id":"47f05a09-d7b5-43ee-ba60-4ce2a5c53246",
     * "freight":199,
     * "expressStart":"x000000001",
     * "express":"德邦物流",
     * "products":
     * [
     * {
     * "amount":2,
     * "productId":"897e1609-b644-4252-9e32-0c3c274ed895",
     * "price":99,
     * "qrcode":
     * [
     * "1111",
     * "2222"
     * ]
     * },
     * {
     * "amount":1,
     * "productId":"060f7f01-c190-43fa-8de9-4a546d895441",
     * "price":44,
     * "qrcode":
     * [
     * "1111"
     * ]
     * },
     * {
     * "amount":3,
     * "productId":"63c0cc78-1f99-4fc2-9447-a715ebf6528a",
     * "price":11,
     * "qrcode":
     * [
     * "1111",
     * "2222",
     * "3333"
     * ]
     * }
     * ]
     * }
     */
    public static class OrderDeliver implements Serializable {
        public String id;
        public String express;
        public String expressStart;
        public String expressEnd;
        public String sellerremark;
        public float freight;
        public ArrayList<QrcodeItem> qrcodes = new ArrayList<>();

        public ArrayList<ProductItem> products = new ArrayList<>();

        protected Gson buildGson() {
            return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return "qrcodes".equals(f.getName());
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            }).create();
        }

        public String toJson() {
            Gson g = buildGson();

            if (qrcodes != null) {
                products.clear();

                for (QrcodeItem qrcodeItem : qrcodes) {

                    String code = qrcodeItem.code == null ? "" : qrcodeItem.code;
                    Product product = qrcodeItem.product;

                    if (product == null) {
                        continue;
                    }

                    ProductItem productItem = new ProductItem(product);
                    int existsIndex = products.indexOf(productItem);
                    if (existsIndex < 0) {
                        products.add(productItem);
                    } else {
                        productItem = products.get(existsIndex);
                        productItem.amount += 1;
                    }
                    productItem.qrcodeList.add(code);
                }
            }

            return g.toJson(this);
        }

        public String newToJson() {
            Gson g = buildGson();


            return g.toJson(this);
        }

        public static class ProductItem implements Serializable {
            public int amount;
            public String productId;
            public float price;
            @SerializedName("qrcode")
            public ArrayList<String> qrcodeList = new ArrayList<>();
            @SerializedName("xqrcode")
            public ArrayList<String> qrcodeList2 = new ArrayList<>();

            public ProductItem() {

            }

            public ProductItem(Product product) {
                this.productId = product.id;
                this.price = product.price;
                this.amount = 1;
            }


            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                ProductItem that = (ProductItem) o;

                if (productId != null ? !productId.equals(that.productId) : that.productId != null)
                    return false;

                return true;
            }

            @Override
            public int hashCode() {
                return productId != null ? productId.hashCode() : 0;
            }
        }

        public static class QrcodeItem implements Serializable {
            /**
             * 二维码
             */
            public String code;
            /**
             * 产品
             */
            public Product product;

            public QrcodeItem(String code, Product product) {
                this.code = code;
                this.product = product;
            }

            public QrcodeItem() {

            }
        }
    }

    /**
     * {
     * "username": "",
     * "phone": "",
     * "postCode": "",
     * "address": "",
     * "express": "",
     * "expressCode": "",
     * "freight": "float",
     * "sellerremark": "",
     * "productIds": "List[Int]",
     * "productAmount": "List[Int]",
     * "productQty": "List[Int]",
     * "prices": "List[float]"
     * }
     * 添加我的销售订单提交的对象
     */
    public static class OrderTradeAdd extends OrderDeliver implements Serializable {

        /**
         * 客户名称
         */
        public String name;
        /**
         * 收件人
         */
        public String username;
        public String phone;
        public String postCode;
        public String address;
        /**
         * 订单日期: 2014-12-18 15:00
         */
        public String orderDate;
        public float payMoney;

        @Override
        protected Gson buildGson() {

            return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return "qrcodes".equals(f.getName()) || "id".equals(f.getName());
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            }).create();
        }
    }

    /**
     * 交易订单(eg.我的销售订单，我的采购订单)
     */
    public static class OrderTrade extends BaseModel implements Serializable {

        /**
         * 客户名称
         */
        public String name;
        /**
         * 收件人
         */
        public String username;
        public String phone;
        public String postCode;
        public String address;
        /**
         * 订单日期: 2014-12-18 15:00
         */
        public String orderDate;
        public String express;
        public String expressStart;
        public String expressEnd;
        public float freight;
        public float payMoney;
        public String code;

        public ArrayList<ProductItemFromServer> products;

        public static class ProductItemFromServer extends OrderDeliver.ProductItem implements Serializable {

            public Product product;

            public ProductItemFromServer() {

            }

            public ProductItemFromServer(Product product) {
                super(product);
                this.product = product;
            }
        }
    }
}
