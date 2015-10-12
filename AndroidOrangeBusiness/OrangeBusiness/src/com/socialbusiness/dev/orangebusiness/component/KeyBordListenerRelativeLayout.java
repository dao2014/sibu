package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * Created by SAP100 on 2015/3/26.
 */
public class KeyBordListenerRelativeLayout extends RelativeLayout {

    public KeyBordListenerRelativeLayout(Context context) {
        super(context);
    }


    public KeyBordListenerRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public static  interface OnKeyBordStateChange{

        public void onKeyBordHide();
        public void onKeyBordShow();
    }
    private OnKeyBordStateChange onKeyBordStateChange;

    public void setOnKeyBordStateChange(OnKeyBordStateChange onKeyBordStateChange) {
        this.onKeyBordStateChange = onKeyBordStateChange;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("onSizeChanged", "==" + h + "==" + oldh);
        if (oldh == 0) {
            return;
        }
        if (h > oldh) {
            if(onKeyBordStateChange!=null){
                onKeyBordStateChange.onKeyBordHide();
            }
        } else if (h < oldh) {
            if(onKeyBordStateChange!=null){
                onKeyBordStateChange.onKeyBordShow();
            }
        }
    }
}
