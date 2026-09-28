package com.qmdeve.blurview.demo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.widget.TitlebarView;

public class TitlebarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_titlebar);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        TitlebarView titlebarView1 = findViewById(R.id.blurTitlebar1);
        TitlebarView titlebarView2 = findViewById(R.id.blurTitlebar2);
        TitlebarView titlebarView3 = findViewById(R.id.blurTitlebar3);

        titlebarView1.setOnBackClickListener(this::finish);
        titlebarView2.setOnBackClickListener(this::finish);

        findViewById(R.id.button1).setOnClickListener(v -> {
            titlebarView1.setCenterTitle(true);
            titlebarView2.setCenterTitle(true);
            titlebarView3.setCenterTitle(true);
        });

        findViewById(R.id.button2).setOnClickListener(v -> {
            titlebarView1.setCenterTitle(false);
            titlebarView2.setCenterTitle(false);
            titlebarView3.setCenterTitle(false);
        });
    }
}