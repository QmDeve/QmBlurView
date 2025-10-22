package com.qmdeve.blurview.demo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.widget.QmBlurView;

public class BlurViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blur_view);

        QmBlurView blurView = findViewById(R.id.qmblurView);
        blurView.setBlurRadius(25);
        blurView.setCornerRadius(30);
        blurView.setOverlayColor(0x80FFFFFF);
    }
}