package com.socialbusiness.dev.orangebusiness.util;

import android.content.Context;
import android.text.TextUtils;

import com.socialbusiness.dev.orangebusiness.activity.base.BaseActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by liangyaotian on 2014-07-15.
 */
public class StringUtil {

    public static final String COMMON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DOUBLE_ZERO = "0.00";
    public static String stringFromDate(Date date, String format){
        return new SimpleDateFormat(format).format(date);
    }

    public static Date dateFromString(String dateString, String format){
        Date result = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            result = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String randomChar(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String getStringFromFloatKeep2(double value) {
        if (value == 0) {
            return "0";
        }
        return subZeroAndDot( new DecimalFormat("0.00").format(value));
    }

    public static String get2DecimalFromString(String value) {
        if (TextUtils.isEmpty(value)) {
            return "0";
        }
        return subZeroAndDot(new DecimalFormat("0.00").format(new BigDecimal(value).doubleValue()));
    }


    /**
     * 保存 原始值 去掉多余的值
     * @param value
     * @return
     */
    public static String sumFromString(String value){
        return subZeroAndDot(value);
    }


    /**
     * 使用java正则表达式去掉多余的.与0
     * @param s
     * @return
     */
    public static  String subZeroAndDot(String s){
        if(s.indexOf(".") >= 0){
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }

    public static String getStringFromAssets(Context context, String fileName) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(((BaseActivity)context).getResources().getAssets().open(fileName)));
            String line = "";
            String result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String showMessage(String message) {
        return message == null ? "" : message;
    }

}
