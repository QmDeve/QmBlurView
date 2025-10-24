package com.qmdeve.blurview.demo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.widget.BlurView;

public class BlurViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blur_view);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        BlurView blurView = findViewById(R.id.blurView);
        blurView.setBlurRadius(45);
        blurView.setCornerRadius(65);
        blurView.setOverlayColor(0x80FFFFFF);
    }
}