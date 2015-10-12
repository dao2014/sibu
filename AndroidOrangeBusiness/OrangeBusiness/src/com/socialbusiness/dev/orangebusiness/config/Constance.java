package com.socialbusiness.dev.orangebusiness.config;

import com.socialbusiness.dev.orangebusiness.model.ScanItem;
import com.socialbusiness.dev.orangebusiness.model.StanderItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SAP100 on 2015/3/24.
 */
public class Constance {

    public static final String WEB_DETAIL = "web_detail";
    public static final String WEB_ACTIVITY_TITLE = "web_title";
    public static final String ISSELLER = "is_seller";

    public static final String PayMoney = "payMoney";
    public static final String PayRemark = "payRemark";
    public static final String PayImages = "payImages";
    public static final String OrderFreight = "order_freight";

    public static final String TotalFee = "total_fee";
    public static final String TransFee = "trans_fee";
    public static final String OrderIds = "OrderIds";

    public static final String INTENT_ONCANCELORDER_SECCUS = "com.socialbusiness.dev.orangebusiness.oncancelseccus";
    public static final String INTENT_ONADDRESSREFRESH_SECCUS = "com.socialbusiness.dev.orangebusiness.onaddressrefresh";

    public static final String COMEFROM = "comefrom";
    public static ArrayList<ScanItem> scanItems;
    public static HashMap<String, ArrayList<StanderItem>> selectStanders;
    public static boolean isCodeEdit = false;

    public static void setNull() {
        if (scanItems != null) {
            scanItems.clear();
        }
        if (selectStanders != null) {
            selectStanders.clear();
        }
        scanItems = null;
        selectStanders = null;
        isCodeEdit = false;
    }
}
