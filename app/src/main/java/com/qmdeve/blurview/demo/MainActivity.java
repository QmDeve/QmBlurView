package com.qmdeve.blurview.demo;

import android.content.Intent;
import android.net.Uri;
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
        findViewById(R.id.blurButtonButton).setOnClickListener(v -> startActivity(new Intent(this, BlurButtonActivity.class)));
        findViewById(R.id.progerssiveBlurButton).setOnClickListener(v -> startActivity(new Intent(this, ProgerssiveBlurActivity.class)));
        findViewById(R.id.blurTitlebar).setOnClickListener(v -> startActivity(new Intent(this, BlurTitlebarActivity.class)));
        findViewById(R.id.blurSwitchButton).setOnClickListener(v -> startActivity(new Intent(this, BlurSwitchActivity.class)));
        findViewById(R.id.blurFloatingButton).setOnClickListener(v -> startActivity(new Intent(this, BlurFloatingButtonActivity.class)));
        findViewById(R.id.blurBottomNavigationButton).setOnClickListener(v -> startActivity(new Intent(this, BlurBottomNavigationActivity.class)));
        findViewById(R.id.textureViewButton).setOnClickListener(v -> startActivity(new Intent(this, TextureViewActivity.class)));
        findViewById(R.id.surfaceViewButton).setOnClickListener(v -> startActivity(new Intent(this, SurfaceViewActivity.class)));
        findViewById(R.id.simpleSurfaceViewButton).setOnClickListener(v -> startActivity(new Intent(this, SimpleSurfaceViewActivity.class)));
        findViewById(R.id.liveVideoButton).setOnClickListener(v -> startActivity(new Intent(this, LiveVideoActivity.class)));
        findViewById(R.id.overlappingBlurButton).setOnClickListener(v -> startActivity(new Intent(this, OverlappingBlurActivity.class)));
        findViewById(R.id.glideBlurButton).setOnClickListener(v -> startActivity(new Intent(this, GlideBlurActivity.class)));
        findViewById(R.id.picassoBlurButton).setOnClickListener(v -> startActivity(new Intent(this, PicassoBlurActivity.class)));
        findViewById(R.id.github).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/QmDeve/QmBlurView"));
            startActivity(intent);
        });
    }
}