package com.qmdeve.blurview.demo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.widget.BlurFloatingButtonView;

public class BlurFloatingButtonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blur_floatingbutton);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        BlurFloatingButtonView floatingButtonView = findViewById(R.id.blurFloatingButton);

        floatingButtonView.setOnClickListener(view -> {
            Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show();
        });

        floatingButtonView.setOnLongPressListener(view -> {
            Toast.makeText(this, "Long Press", Toast.LENGTH_SHORT).show();
        });
    }
}