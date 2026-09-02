package com.fireos8.settingshub;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public final class OverlayService extends Service {

    private static final String CHANNEL_ID = "fireos8_settings_hub_overlay";
    private static final int NOTIFICATION_ID = 1;
    private static final long FALLBACK_SHOW_DELAY_MS = 1500;

    private DashboardOverlay overlay;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Application.ActivityLifecycleCallbacks lifecycleCallbacks;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, buildNotification());

        overlay = new DashboardOverlay(this);
        overlay.setCloseListener(this::stopSelf);

        // Show the overlay only once the transparent starter activity is really gone.
        // If the overlay takes focus while that activity is still alive, the system
        // reports lifecycle timeouts and the launcher shows a short black screen.
        lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activity instanceof MainActivity) {
                    showOverlayOnce();
                }
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

            @Override
            public void onActivityStarted(Activity activity) { }

            @Override
            public void onActivityResumed(Activity activity) { }

            @Override
            public void onActivityPaused(Activity activity) { }

            @Override
            public void onActivityStopped(Activity activity) { }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
        };
        getApplication().registerActivityLifecycleCallbacks(lifecycleCallbacks);

        // Fallback in case the destroy callback is missed, for example when the
        // process is recycled before the activity reports its destruction.
        mainHandler.postDelayed(this::showOverlayOnce, FALLBACK_SHOW_DELAY_MS);
    }

    private void showOverlayOnce() {
        if (overlay != null && !overlay.isShowing()) {
            overlay.show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            showOverlayOnce();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (overlay != null) {
            overlay.hide();
        }
        if (lifecycleCallbacks != null) {
            getApplication().unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
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
