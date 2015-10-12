package com.socialbusiness.dev.orangebusiness.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * User: Yaotian Leung
 * Date: 2013-11-13
 * Time: 17:39
 */
public class ToastUtil {

    private static Toast toast;

    public static void show(Context context, int res){
        if(context == null){
            return;
        }
        show(context, context.getString(res));
    }
    public static void show(Context context, String message){
        if(context == null){
            return;
        }
        if(TextUtils.isEmpty(message)){
            return;
        }
        if(toast == null){
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        }

        toast.setText(message);
        toast.show();
    }

//    public static void show(Context context, String message){
//        if(context == null){
//            return;
//        }
//        if(TextUtils.isEmpty(message)){
//            return;
//        }
//        if(toast == null){
//            toast = new Toast(context.getApplicationContext());
//            toast.setDuration(Toast.LENGTH_SHORT);
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View content = inflater.inflate(R.layout.view_toast, null);
//            toast.setView(content);
//        }else{
//            toast.cancel();
//        }
//
//        TextView tvText = (TextView) toast.getView().findViewById(R.id.tvText);
//        tvText.setText(message);
//        toast.show();
//    }
}
