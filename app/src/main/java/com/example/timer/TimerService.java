package com.example.timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class TimerService extends Service {
    // id of channel for notifications
    public static final String CHANNEL_ID = "Stopwatch_Notifications";

    // service actions
    public static final String START = "START";
    public static final String STOP = "STOP";

    // intent extras
    public static final String TIMER_ACTION = "TIMER_ACTION";
    public static final String TIMER_TIME = "TIMER_TIME";
    public static final String TIMER_ELAPSED = "TIMER_ELAPSED";

    // intent actions
    public static final String TIMER_TICK = "TIMER_TICK";
    public static final String TIMER_FINISH = "TIMER_FINISH";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        getNotificationManager();

        String action = intent.getStringExtra(TIMER_ACTION);
        long time = intent.getLongExtra(TIMER_TIME, 120000);

        if (START.equals(action)) {
            startForeground(1, buildNotification());
            startTimer(time);
        } else if (STOP.equals(action)) {
            stopTimer();
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                "Timer",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        notificationChannel.setSound(null, null);
        // show count of notification on application icon
        notificationChannel.setShowBadge(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private NotificationManager getNotificationManager() {
        return ContextCompat.getSystemService(
                this, NotificationManager.class);
    }

    private Notification buildNotification() {
        String title = "Timer is running";

        long minutes = timeElapsed / 60000;
        long seconds = timeElapsed % 60000 / 1000;

        Intent intent = new Intent(this, TimerActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setOngoing(true)
                .setContentText(minutes + ":" + seconds)
                .setColorized(true)
                .setColor(Color.parseColor("#BEAEE2"))
                .setSmallIcon(R.drawable.ic_timer)
                .setOnlyAlertOnce(true)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();
    }

    private void updateNotification() {
        getNotificationManager().notify(1, buildNotification());
    }

    private CountDownTimer timer;
    private long timeElapsed = 0;

    private void startTimer(long time) {
        timeElapsed = time;

        timer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long l) {
                Intent tickIntent = new Intent();
                tickIntent.setAction(TIMER_TICK);

                timeElapsed = l;

                tickIntent.putExtra(TIMER_ELAPSED, timeElapsed);
                sendBroadcast(tickIntent);
                updateNotification();
            }

            @Override
            public void onFinish() {
                Intent finishIntent = new Intent();
                finishIntent.setAction(TIMER_FINISH);
                sendBroadcast(finishIntent);

                stopForeground(true);
            }
        };

        timer.start();
    }

    private void stopTimer() {
        timer.cancel();

        stopForeground(true);
    }
}