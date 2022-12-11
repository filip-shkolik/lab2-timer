package com.example.timer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.TextView;

public class TimerActivity extends AppCompatActivity {
    private TextView timer;

    private BroadcastReceiver tickReceiver;
    private BroadcastReceiver finishReceiver;
    HandlerThread handlerThread;
    Handler handler;
    Looper looper;
    Runnable updateTimerThread;
    long startTime=0L, timeInMilliseconds=0L, timeSwapBuff=0L, updateTime=0L;

    private long timeLeftInMilliseconds = 600000; // 10 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timer = findViewById(R.id.timer);

        findViewById(R.id.start).setOnClickListener(v -> startTimer());
        findViewById(R.id.stop).setOnClickListener(v -> stopTimer());

        handlerThread = new HandlerThread("TimerHandlerThread");
        handlerThread.start();
        looper = handlerThread.getLooper();
        handler = new Handler(looper);
        updateTimerThread = new Runnable() {
            @Override
            public void run() {
                timeInMilliseconds = SystemClock.uptimeMillis()-startTime;
                updateTime = timeSwapBuff+timeInMilliseconds;
                int seconds=(int)(updateTime/1000);
                int minutes=seconds/60;
                seconds%=60;
                int miliseconds=(int)(updateTime%1000);
            }
        };
        handler.post(updateTimerThread);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter tickFilter = new IntentFilter();
        tickFilter.addAction(TimerService.TIMER_TICK);
        tickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                timeLeftInMilliseconds = intent.getLongExtra(TimerService.TIMER_ELAPSED, 60000);
                updateTimer();
            }
        };
        registerReceiver(tickReceiver, tickFilter);

        IntentFilter finishFilter = new IntentFilter();
        finishFilter.addAction(TimerService.TIMER_FINISH);
        finishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                openDialog();
            }
        };
        registerReceiver(finishReceiver, finishFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(tickReceiver);
        unregisterReceiver(finishReceiver);
    }

    private void startTimer() {
        Intent timerIntent = new Intent(this, TimerService.class);
        timerIntent.putExtra(TimerService.TIMER_ACTION, TimerService.START);
        timerIntent.putExtra(TimerService.TIMER_TIME, timeLeftInMilliseconds);
        startService(timerIntent);
    }

    private void stopTimer() {
        Intent timerIntent = new Intent(this, TimerService.class);
        timerIntent.putExtra(TimerService.TIMER_ACTION, TimerService.STOP);
        startService(timerIntent);
    }

    private void openDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Timer is finished")
                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void updateTimer(){
        long minutes = timeLeftInMilliseconds / 60000;
        long seconds = timeLeftInMilliseconds % 60000 / 1000;

        String timeLeftText = "" + minutes + ":";
        if(seconds < 10){
            timeLeftText += "0";
        }
        timeLeftText += seconds;
        timer.setText(timeLeftText);
    }
}