package com.socialbusiness.dev.orangebusiness.model;

import java.util.List;

/**
 * Created by zkboos on 2015/6/18.
 */
public class MultiOrderUpload {
    public List<String> orderIds;//订单ID数组
    public float payMoney;//付款金额
    public String payRemark;//付款备注
    public List<String> payImages;//付款凭证数组(调用第6个接口)

}
