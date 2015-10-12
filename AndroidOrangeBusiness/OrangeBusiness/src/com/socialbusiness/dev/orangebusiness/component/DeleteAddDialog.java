package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import com.socialbusiness.dev.orangebusiness.R;
import com.socialbusiness.dev.orangebusiness.util.CommonUtil;

/**
 * Created by zkboos on 2015/7/13.
 */
public class DeleteAddDialog extends  BaseDialog{
    public DeleteAddDialog(Context context) {
        super(context);
        setContentView(R.layout.delete_address_dialog);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = CommonUtil.getScreenWidth(this.getContext()) / 5 * 4;
        // lp.height = Util.getViewHeightInPix(context) / 5 * 2;
        getWindow().setAttributes(lp);
        initView();
    }

    View delete_add_ensure;
    private void initView() {
        findViewById(R.id.delete_add_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        delete_add_ensure=findViewById(R.id.delete_add_ensure);
    }

    public void setOnEnsure(View.OnClickListener listener){
        delete_add_ensure.setOnClickListener(listener);
    }
}
