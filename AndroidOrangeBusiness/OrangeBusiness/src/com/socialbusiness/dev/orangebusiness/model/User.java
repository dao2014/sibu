package com.socialbusiness.dev.orangebusiness.model;

import com.socialbusiness.dev.orangebusiness.api.RequestAction;

import java.util.ArrayList;

public class User extends BaseModel {

    public String upUserId;
    public String masterGroupHxId;
    public String upMasterGroupHxId;
    public String nickName;
    public String remarkname;
    public String trueName;	/*-- 20141204新加字段，用户真实姓名 --*/
    public int male;
    public String account;
    public UserType type;
    public String head;
    public String idCard;
    public String phone;
    public String wechat;
    public String qq;
    public String email;
    public int monthlyOrderCount;
    public float monthlyOrderMoney;
    public String homeAddress;
    public String credit;
    public int isStaff;
    public int isMember;
    public float totalOrderMoney;//总金额
    public String companyId;
    public String rongcloudToken;
    
    /*-- 20141211新增字，橙单相关的数字信息 --*/
    /**
     * 买家待处理订单数
     */
    public int buyerWaitingOrderCount;

    /**
     * 买家待付款订单数
     */
    public int buyerWaitingPayOrderCount;

    /**
     * 买家待发货订单数
     */
    public int buyerShopOrderCount;

    /**
     * 买家已发货订单数
     */
    public int buyerShipOrderCount;

    /**
     * 卖家待处理订单数
     */
    public int sellerWaitingOrderCount;

    /**
     * 卖家待付款订单数
     */
    public int sellerWaitingPayOrderCount;

    /**
     * 卖家待发货订单数
     */
    public int sellerShopOrderCount;

    /**
     * 卖家已发货订单数
     */
    public int sellerShipOrderCount;

    public int buyerClosedOrderCount;//我的采购》已收货订单
    public int sellerClosedOrderCount;//我的销售》已收货订单
    //public int monthlyOrderMoney;//当月订单金额
    public float monthlyShipMoney;//当月出货金额
//    public int totalOrderMoney;//总金额
    public float mothlyProfit;

    public User() {

    }

    public static User cloneOne(User user) {
        String JSON = RequestAction.GSON.toJson(user, User.class);
        return RequestAction.GSON.fromJson(JSON, User.class);
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        }
        if (o instanceof User) {
            User target = (User) o;
            return target != null && target.id.equals(id);
        }
        return false;
    }

    /**
     * 已经分层的用户数据
     */
    public static class LeveledUsers extends BaseModel {
        /**
         * 上级
         */
        public ArrayList<User> up;
        /**
         * 平级
         */
        public ArrayList<User> center;
        /**
         * 下级
         */
        public ArrayList<User> down;

        public ArrayList<Group> group;
    }
    
//    public static List<UserEntity> convertToUserEntitys(List<User> users) {
//    	if(users == null || users.isEmpty()) {
//    		return null;
//    	}
//    	List<UserEntity> userEntitys = new ArrayList<UserEntity>(users.size());
//    	for(User user : users) {
//    		UserEntity userEntity = new UserEntity();
//    		userEntity.id = user.id;
//    		userEntity.nickname = user.nickName;
//    		userEntity.avatar = user.head;
//    		userEntity.emUsername = MD5.encode(user.id);
//    		userEntitys.add(userEntity);
//    	}
//    	return userEntitys;
//    }


    @Override
    public String toString() {
        return "User{" +
                "upUserId='" + upUserId + '\'' +
                ", masterGroupHxId='" + masterGroupHxId + '\'' +
                ", upMasterGroupHxId='" + upMasterGroupHxId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", remarkname='" + remarkname + '\'' +
                ", trueName='" + trueName + '\'' +
                ", male=" + male +
                ", account='" + account + '\'' +
                ", type=" + type +
                ", head='" + head + '\'' +
                ", idCard='" + idCard + '\'' +
                ", phone='" + phone + '\'' +
                ", wechat='" + wechat + '\'' +
                ", qq='" + qq + '\'' +
                ", email='" + email + '\'' +
                ", monthlyOrderCount=" + monthlyOrderCount +
                ", monthlyOrderMoney=" + monthlyOrderMoney +
                ", homeAddress='" + homeAddress + '\'' +
                ", credit='" + credit + '\'' +
                ", isStaff=" + isStaff +
                ", isMember=" + isMember +
                ", totalOrderMoney=" + totalOrderMoney +
                ", companyId='" + companyId + '\'' +
                ", rongcloudToken='" + rongcloudToken + '\'' +
                ", buyerWaitingOrderCount=" + buyerWaitingOrderCount +
                ", buyerWaitingPayOrderCount=" + buyerWaitingPayOrderCount +
                ", buyerShopOrderCount=" + buyerShopOrderCount +
                ", buyerShipOrderCount=" + buyerShipOrderCount +
                ", sellerWaitingOrderCount=" + sellerWaitingOrderCount +
                ", sellerWaitingPayOrderCount=" + sellerWaitingPayOrderCount +
                ", sellerShopOrderCount=" + sellerShopOrderCount +
                ", sellerShipOrderCount=" + sellerShipOrderCount +
                ", buyerClosedOrderCount=" + buyerClosedOrderCount +
                ", sellerClosedOrderCount=" + sellerClosedOrderCount +
                ", monthlyShipMoney=" + monthlyShipMoney +
                ", mothlyProfit=" + mothlyProfit +
                '}';
    }
}
