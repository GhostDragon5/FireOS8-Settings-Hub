package com.fireos8.settingshub;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public final class OverlayService extends Service {

    private static final String CHANNEL_ID = "fireos8_settings_hub_overlay";
    private static final int NOTIFICATION_ID = 1;

    private DashboardOverlay overlay;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, buildNotification());

        overlay = new DashboardOverlay(this);
        overlay.setCloseListener(this::stopSelf);
        overlay.show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (overlay != null && !overlay.isShowing()) {
            overlay.show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (overlay != null) {
            overlay.hide();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "FireOS8 Settings Hub",
                    NotificationManager.IMPORTANCE_MIN
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return builder
                .setContentTitle("FireOS8 Settings Hub")
                .setContentText("Dashboard overlay aktiv")
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true)
                .build();
    }
}
