package com.example.timer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {

    Button btnTimer;
    TextClock textdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textdate = findViewById(R.id.textdateid);
        btnTimer = (Button) findViewById(R.id.btnTimer);
        btnTimer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnTimer:
                Intent intent = new Intent(this, TimerActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}