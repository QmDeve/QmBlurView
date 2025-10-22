package com.qmdeve.blurview.demo;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.widget.QmBlurButtonView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        QmBlurButtonView blurViewButton = findViewById(R.id.blurViewButton);
        blurViewButton.setOnClickListener(v -> {
            startActivity(new Intent(this, BlurViewActivity.class));
        });

        QmBlurButtonView blurButtonButton = findViewById(R.id.blurButtonButton);
        blurButtonButton.setOnClickListener(v -> {
            startActivity(new Intent(this, QmBlurButtonViewActivity.class));
        });

        QmBlurButtonView blurImageButton = findViewById(R.id.blurImageButton);
        blurImageButton.setOnClickListener(v -> {
            startActivity(new Intent(this, QmBlurImageActivity.class));
        });
    }
}