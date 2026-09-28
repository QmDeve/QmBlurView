package com.qmdeve.blurview.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.engine.BlurEngines;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        findViewById(R.id.blurViewButton).setOnClickListener(v -> startActivity(new Intent(this, BlurViewActivity.class)));
        findViewById(R.id.blurIntensityButton).setOnClickListener(v -> startActivity(new Intent(this, BlurIntensityActivity.class)));
        findViewById(R.id.blurButtonButton).setOnClickListener(v -> startActivity(new Intent(this, BlurButtonActivity.class)));
        findViewById(R.id.progerssiveBlurButton).setOnClickListener(v -> startActivity(new Intent(this, ProgerssiveBlurActivity.class)));
        findViewById(R.id.titleBarButton).setOnClickListener(v -> startActivity(new Intent(this, TitlebarActivity.class)));
        findViewById(R.id.switchButton).setOnClickListener(v -> startActivity(new Intent(this, SwitchActivity.class)));
        findViewById(R.id.floatingButton).setOnClickListener(v -> startActivity(new Intent(this, FloatingButtonActivity.class)));
        findViewById(R.id.bottomTabbarButton).setOnClickListener(v -> startActivity(new Intent(this, BottomTabBarActivity.class)));
        findViewById(R.id.cardViewButton).setOnClickListener(v -> startActivity(new Intent(this, CardViewActivity.class)));
        findViewById(R.id.searchBarButton).setOnClickListener(v -> startActivity(new Intent(this, SearchBarActivity.class)));
        findViewById(R.id.github).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/QmDeve/QmBlurView"));
            startActivity(intent);
        });
    }
}