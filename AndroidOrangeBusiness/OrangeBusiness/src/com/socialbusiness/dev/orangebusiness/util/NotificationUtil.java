package com.socialbusiness.dev.orangebusiness.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.socialbusiness.dev.orangebusiness.R;

/**
 * User: Yaotian Leung
 * Date: 2013-11-16
 * Time: 18:46
 */
public final class NotificationUtil {
    public static final String TAG = "NotificationUtil";
    private static NotificationUtil instance;
    private final NotificationManager notificationManager;
    private Context appContext;

    private NotificationUtil(Context context){
        appContext = context.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static final NotificationUtil getInstance(Context context){
        if(instance == null){
            synchronized (NotificationUtil.class) {
                if(instance == null){
                    instance = new NotificationUtil(context);
                }
            }
        }
        return instance;
    }

    public void show(int id, String title, String message,boolean autoCancel, Uri sound, PendingIntent pendingIntent){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            show3(id, title, message, autoCancel, sound, pendingIntent);
        }else{
            show1(id, title, message, autoCancel, sound, pendingIntent);
        }
    }

    public void show(int id, RemoteViews views, boolean autoCancel, boolean ongoing, Uri sound, PendingIntent pendingIntent){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            show3(id, views, autoCancel, ongoing, sound, pendingIntent);
        }else{
            show1(id, views, autoCancel, ongoing, sound, pendingIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void show3(int id, String title, String message,boolean autoCancel, Uri sound, PendingIntent pendingIntent){
        Notification.Builder builder = new Notification.Builder(appContext);
//        RemoteViews remoteViews = new RemoteViews(appContext.getPackageName(), R.layout.view_notification);
//        remoteViews.setTextViewText(R.id.view_notification_title, title);
//        remoteViews.setTextViewText(R.id.view_notification_content, message);
        builder.setContentIntent(pendingIntent);
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setAutoCancel(autoCancel);
        builder.setSmallIcon(R.drawable.app_icon);
//        builder.setContent(remoteViews);
        if(sound != null){
            builder.setSound(sound);
        }
        
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            notificationManager.notify(id, builder.build());
        }else{
            notificationManager.notify(id, builder.getNotification());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void show3(int id, RemoteViews views, boolean autoCancel, boolean ongoing, Uri sound, PendingIntent pendingIntent){
        Notification.Builder builder = new Notification.Builder(appContext);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(autoCancel);
        builder.setOngoing(ongoing);
        builder.setSmallIcon(R.drawable.app_icon);
        if(sound != null){
            builder.setSound(sound);
        }
        builder.setContent(views);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            notificationManager.notify(id, builder.build());
        }else{
            notificationManager.notify(id, builder.getNotification());
        }
    }

    /**
     * 显示状态栏通知(某些MIUI系统不显示)
     * @param title
     * @param message
     * @param autoCancel
     * @param sound
     * @param pendingIntent
     * @return notification id
     */
    private void show2(int id, String title, String message,boolean autoCancel, Uri sound, PendingIntent pendingIntent){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setAutoCancel(autoCancel);
        builder.setSmallIcon(R.drawable.app_icon);
        if(sound != null){
            builder.setSound(sound);
        }
        notificationManager.notify(id, builder.build());
    }

    /**
     * 显示带RemoteViews的状态栏通知
     * Android 2.3以下系统不能显示,NotificationCompat.Builder有bug
     * https://code.google.com/p/android/issues/detail?id=30495
     * @param id
     * @param views
     * @param autoCancel
     * @param ongoing
     * @param sound
     * @param pendingIntent
     */
    private void show2(int id, RemoteViews views, boolean autoCancel, boolean ongoing, Uri sound, PendingIntent pendingIntent){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(autoCancel);
        builder.setOngoing(ongoing);
        builder.setSmallIcon(R.drawable.app_icon);
        if(sound != null){
            builder.setSound(sound);
        }
        builder.setContent(views);
        notificationManager.notify(id, builder.build());
    }

    /**
     * 显示状态栏通知
     * @param id
     * @param title
     * @param message
     * @param autoCancel
     * @param sound
     * @param pendingIntent
     */
    private void show1(int id, String title, String message, boolean autoCancel, Uri sound, PendingIntent pendingIntent){
        Notification notification = new Notification(R.drawable.app_icon, message, System.currentTimeMillis());
        notification.when = System.currentTimeMillis();
        if(autoCancel){
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        }else{
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        }
        if(sound != null){
            notification.sound = sound;
        }
        
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

        notification.setLatestEventInfo(appContext, TextUtils.isEmpty(title) ? appContext.getResources().getString(R.string.app_name_orangebusiness) : title,
                message, pendingIntent);

        notificationManager.notify(id, notification);
    }

    /**
     * 显示带RemoteViews的状态栏通知
     * @param id
     * @param views
     * @param autoCancel
     * @param ongoing
     * @param sound
     * @param pendingIntent
     */
    private void show1(int id, RemoteViews views, boolean autoCancel, boolean ongoing, Uri sound, PendingIntent pendingIntent){
        Notification notification = new Notification(R.drawable.app_icon, "", System.currentTimeMillis());
        notification.when = System.currentTimeMillis();
        if(autoCancel){
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        }
        if(ongoing){
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        }
        if(sound != null){
            notification.sound = sound;
        }
        notification.contentView = views;
        notification.contentIntent = pendingIntent;

        notificationManager.notify(id, notification);
    }

    public void hide(int id){
        notificationManager.cancel(id);
    }

}

