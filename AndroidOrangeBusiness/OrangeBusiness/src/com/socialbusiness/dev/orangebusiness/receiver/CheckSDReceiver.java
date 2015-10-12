package com.socialbusiness.dev.orangebusiness.receiver;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.socialbusiness.dev.orangebusiness.Constant;

public class CheckSDReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Context appContext = context.getApplicationContext();
        File filesDir = null;

        if (Intent.ACTION_MEDIA_EJECT.equals(action)) {//用机身的
            filesDir = appContext.getFilesDir();
        } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {//用sd卡的
            filesDir = appContext.getExternalFilesDir(null);
        }

        if (filesDir == null) {
            filesDir = appContext.getFilesDir();
        }
        Constant.initFilesDir(filesDir);
    }
}
