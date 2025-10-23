package com.qmdeve.blurview.demo;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        findViewById(R.id.blurViewButton).setOnClickListener(v -> startActivity(new Intent(this, BlurViewActivity.class)));
        findViewById(R.id.blurButtonButton).setOnClickListener(v -> startActivity(new Intent(this, BlurButtonViewActivity.class)));
        findViewById(R.id.progerssiveBlurButton).setOnClickListener(v -> startActivity(new Intent(this, ProgerssiveBlurActivity.class)));
        findViewById(R.id.blurTitlebar).setOnClickListener(v -> startActivity(new Intent(this, BlurTitlebarActivity.class)));
    }
}