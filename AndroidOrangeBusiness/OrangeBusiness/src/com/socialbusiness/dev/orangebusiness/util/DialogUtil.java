package com.socialbusiness.dev.orangebusiness.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import com.socialbusiness.dev.orangebusiness.R;

import java.io.File;


/**
 * Created by liangyaotian on 11/7/13.
 */
public final class DialogUtil {

	private DialogUtil() {

	}

	// public static Dialog showLoading(Activity activity) {
	// Dialog dialog = new Dialog(activity, R.style.MyDialog);
	// View contentView =
	// activity.getLayoutInflater().inflate(R.layout.view_loading_dialog, null);
	// dialog.setContentView(contentView);
	// dialog.show();
	// return dialog;
	// }

	public static Dialog showConfirm(Activity activity, int title, int message,
			DialogInterface.OnClickListener yesOnclickListener, DialogInterface.OnClickListener noOnclickListener) {
		return showConfirm(activity, activity.getString(title), activity.getString(message), yesOnclickListener,
				noOnclickListener);
	}

	public static Dialog showConfirm(Activity activity, String title, String message,
			DialogInterface.OnClickListener yesOnclickListener, DialogInterface.OnClickListener noOnclickListener) {
		return showConfirm(activity, title, message, null, null, yesOnclickListener, noOnclickListener);
	}

	public static Dialog showConfirm(Activity activity, int title, int message, int yesText, int noText,
			DialogInterface.OnClickListener yesOnclickListener, DialogInterface.OnClickListener noOnclickListener) {
		return showConfirm(activity, activity.getString(title), activity.getString(message),
				activity.getString(yesText), activity.getString(noText), yesOnclickListener, noOnclickListener);
	}

	public static Dialog showConfirm(Activity activity, String title, String message, String yesText, String noText,
			final DialogInterface.OnClickListener yesOnclickListener,
			final DialogInterface.OnClickListener noOnclickListener) {

		/**
		 * final Dialog dialog = new Dialog(activity, R.style.MyDialog); View
		 * contentView =
		 * activity.getLayoutInflater().inflate(R.layout.view_confirm_dialog,
		 * null); assert contentView != null; TextView tvTitle = (TextView)
		 * contentView.findViewById(R.id.tvTitle); TextView tvMessage =
		 * (TextView) contentView.findViewById(R.id.tvMessage); Button btnOk =
		 * (Button) contentView.findViewById(R.id.btnOk); Button btnCancel =
		 * (Button) contentView.findViewById(R.id.btnCancel);
		 * 
		 * if (!TextUtils.isEmpty(title)) { tvTitle.setText(title); } if
		 * (!TextUtils.isEmpty(message)) { tvMessage.setText(message); } if
		 * (!TextUtils.isEmpty(yesText)) { btnOk.setText(yesText); } if
		 * (!TextUtils.isEmpty(noText)) { btnCancel.setText(noText); }
		 * 
		 * View.OnClickListener onClickListener = new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { dialog.dismiss(); switch
		 *           (v.getId()) { case R.id.btnOk: if (yesOnclickListener !=
		 *           null) { yesOnclickListener.onClick(dialog, 0); } break;
		 *           case R.id.btnCancel: if (noOnclickListener != null) {
		 *           noOnclickListener.onClick(dialog, 0); } break; } } };
		 *           btnOk.setOnClickListener(onClickListener);
		 *           btnCancel.setOnClickListener(onClickListener); int width =
		 *           (int) (Constant.SCREEN_WIDTH * 0.8); int height =
		 *           WindowManager.LayoutParams.WRAP_CONTENT;
		 *           ViewGroup.LayoutParams params = new
		 *           ViewGroup.LayoutParams(width, height);
		 *           dialog.setContentView(contentView, params); dialog.show();
		 */

		Dialog dialog = new AlertDialog.Builder(activity).setTitle(title).setMessage(message)
				.setPositiveButton(yesText, yesOnclickListener).setNegativeButton(noText, noOnclickListener).show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	/**
	 * 显示编辑微博的对话框
	 * 
	 * @param activity
	 * @return
	 */
	public static Dialog showEditWeiboDialog(Activity activity) {
		Dialog dialog = new Dialog(activity);
		// TODO:显示编辑微博的对话框
		dialog.show();
		return dialog;
	}

	/**
	 * 从相册或图库取图
	 * 
	 * @param activity
	 * @return
	 */
	public static Dialog showTakePhotoDialog(final Activity activity,final File photoFile,DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(activity.getString(R.string.comment_take_photo_title));
		builder.setItems(new String[] { activity.getString(R.string.comment_take_photo_from_camera),
                activity.getString(R.string.comment_take_photo_from_library) },onClickListener );
		return builder.show();
	}
}
