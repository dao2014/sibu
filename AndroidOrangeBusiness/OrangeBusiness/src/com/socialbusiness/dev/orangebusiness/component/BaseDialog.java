package com.socialbusiness.dev.orangebusiness.component;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.socialbusiness.dev.orangebusiness.R;


public class BaseDialog extends Dialog {

	public BaseDialog(Context context) {
		super(context, R.style.NormalDialog);
		setOwnerActivity((Activity) context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
	}

	public BaseDialog(Context context,int style) {
		super(context, style);
		setOwnerActivity((Activity) context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

	}

}
