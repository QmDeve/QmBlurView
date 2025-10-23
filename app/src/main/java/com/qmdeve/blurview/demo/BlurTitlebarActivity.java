package com.qmdeve.blurview.demo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.widget.BlurTitlebarView;

public class BlurTitlebarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blur_titlebar);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        BlurTitlebarView blurTitlebarView1 = findViewById(R.id.blurTitlebar1);
        BlurTitlebarView blurTitlebarView2 = findViewById(R.id.blurTitlebar2);
        BlurTitlebarView blurTitlebarView3 = findViewById(R.id.blurTitlebar3);

        blurTitlebarView1.setOnBackClickListener(this::finish);
        blurTitlebarView2.setOnBackClickListener(this::finish);

        findViewById(R.id.button1).setOnClickListener(v -> {
            blurTitlebarView1.setCenterTitle(true);
            blurTitlebarView2.setCenterTitle(true);
            blurTitlebarView3.setCenterTitle(true);
        });

        findViewById(R.id.button2).setOnClickListener(v -> {
            blurTitlebarView1.setCenterTitle(false);
            blurTitlebarView2.setCenterTitle(false);
            blurTitlebarView3.setCenterTitle(false);
        });
    }
}
