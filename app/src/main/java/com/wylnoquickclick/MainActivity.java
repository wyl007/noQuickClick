package com.wylnoquickclick;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private int count = 0;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);
        tv.setOnClickListener(this);
        Intent intent = new Intent();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv:
                count++;
                count += 2;
                tv.setText("值：" + count);
                break;
        }

    }
}
