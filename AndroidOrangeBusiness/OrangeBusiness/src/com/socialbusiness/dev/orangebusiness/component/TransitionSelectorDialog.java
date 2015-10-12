package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.socialbusiness.dev.orangebusiness.R;

/**
 * Created by zkboos on 2015/6/17.
 */
public class TransitionSelectorDialog extends  BaseDialog{

    public TransitionSelectorDialog(Context context) {
        super(context);
        setContentView(R.layout.transition_selector_dialog);
        getWindow().setGravity(Gravity.BOTTOM);
        initView();

    }



    View multi_transition,merge_transition;
    private void initView() {
        multi_transition=findViewById(R.id.multi_transition);
        merge_transition=findViewById(R.id.merge_transition);
    }

    public void setOnClick(View.OnClickListener listener){
        multi_transition.setOnClickListener(listener);
        merge_transition.setOnClickListener(listener);
    }
}
