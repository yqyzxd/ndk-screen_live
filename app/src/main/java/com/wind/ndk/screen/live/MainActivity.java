package com.wind.ndk.screen.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ScreenLive mScreenLive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScreenLive=new ScreenLive();
        findViewById(R.id.btn_record_screen)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url="rtmp://192.168.31.110:1935/myapp";
                        mScreenLive.startLive(url,MainActivity.this);
                    }
                });

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mScreenLive.onActivityResult(requestCode,resultCode,data);
    }
}
