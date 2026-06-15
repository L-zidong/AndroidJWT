package com.example.weatherforecast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 后台 Service：模拟定期拉取天气并发送通知。
 */
public class WeatherFetchService extends Service {
    private static final String CHANNEL_ID = "weather_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long INTERVAL_MS = 60_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService loadExecutor = Executors.newSingleThreadExecutor();
    private final Runnable fetchTask = this::fetchAndNotify;

    public static void scheduleIfNeeded(Context context) {
        Intent intent = new Intent(context, WeatherFetchService.class);
        if (AppPreferences.notificationsEnabled(context)) {
            context.startService(intent);
        } else {
            context.stopService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(fetchTask);
        if (AppPreferences.notificationsEnabled(this)) {
            fetchAndNotify();
            handler.postDelayed(fetchTask, INTERVAL_MS);
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    private void fetchAndNotify() {
        if (!AppPreferences.notificationsEnabled(this)) {
            stopSelf();
            return;
        }
        String city = AppPreferences.getCity(this);
        loadExecutor.execute(() -> {
            WeatherRepository repo = new WeatherRepository(this);
            List<WeatherDay> days = repo.loadForecast(city, true);
            if (days.isEmpty()) {
                return;
            }
            WeatherDay today = days.get(0);
            boolean f = AppPreferences.useFahrenheit(this);
            String date = today.getDate();
            String summary = today.getSummary();

            handler.post(() -> {
                String content = getString(
                        R.string.notification_content,
                        date,
                        summary,
                        today.formatTempRange(WeatherFetchService.this, f));
                postNotification(city, content);
            });
        });
    }

    private void postNotification(String city, String content) {
        if (!AppPreferences.notificationsEnabled(this)) {
            stopSelf();
            return;
        }
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(
                this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_title, city))
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, builder.build());
        }

        if (AppPreferences.notificationsEnabled(this)) {
            handler.postDelayed(fetchTask, INTERVAL_MS);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(fetchTask);
        loadExecutor.shutdownNow();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
