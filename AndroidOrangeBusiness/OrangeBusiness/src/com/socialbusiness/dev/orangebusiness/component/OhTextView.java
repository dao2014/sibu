package com.socialbusiness.dev.orangebusiness.component;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//TODO 未完整测试各种效果：粗斜体，斜体，粗体。。。
public class OhTextView extends TextView {
	
	private static Typeface customTypefaceNormal=null;
	private static Typeface customTypefaceBold=null;

	private int mStringRes;
	
	public OhTextView(Context context) {
		super(context);
	}
	
	public OhTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public OhTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mStringRes = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "text", 0);
	}
	
	@Override
	public void setTypeface(Typeface tf, int style) {
		Typeface paramTf=tf;
		if(style==Typeface.BOLD_ITALIC){
			paramTf=customTypefaceBold;
		}
		else if(style==Typeface.BOLD){
			paramTf=customTypefaceBold;
		}
		else if(style==Typeface.ITALIC){
			paramTf=customTypefaceNormal;
		}
		else{
			paramTf=customTypefaceNormal;
		}
		if(paramTf==null){
			paramTf=tf;
		}
		super.setTypeface(paramTf, style);
	}

	public static void setupCustomTypeface(Context context, String assetPathNormal,String assetPathBold){
		if(assetPathNormal!=null){
			customTypefaceNormal=Typeface.createFromAsset (context.getAssets(),assetPathNormal);
		}
		if(assetPathBold!=null){
			customTypefaceBold=Typeface.createFromAsset (context.getAssets(),assetPathBold);
		}
	}
	
	public void setTextRes(int textRes) {
		super.setText(textRes);
		mStringRes = textRes;
	}
	
	public void updateText() {
		if(mStringRes > 0) {
			setText(mStringRes);
		}
	}
	
	public static void updateAllTextInViewGroup(ViewGroup parent) {
		if(parent != null) {
			final int childCount = parent.getChildCount();
			for(int i = 0; i < childCount; i++) {
				final View view = parent.getChildAt(i);
				if(view instanceof ViewGroup) {
					updateAllTextInViewGroup((ViewGroup) view);
				}
				else if(view instanceof OhTextView) {
					((OhTextView) view).updateText();
				}
			}
		}
	}
}
