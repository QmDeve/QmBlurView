package com.qmdeve.blurview.demo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.widget.BlurSwitchButtonView;

public class BlurSwitchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blur_switch);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        BlurSwitchButtonView blurSwitch1 = findViewById(R.id.blurSwitch1);
        BlurSwitchButtonView blurSwitch2 = findViewById(R.id.blurSwitch2);

        blurSwitch1.setOnCheckedChangeListener(is -> {
            Toast.makeText(this, String.valueOf(is), Toast.LENGTH_SHORT).show();
        });

        blurSwitch1.setBaseColor(0xFF0161F2);
        blurSwitch1.setChecked(false, false);

        blurSwitch2.setBaseColor(0xFF09d235);
        blurSwitch2.setChecked(false, true);
    }
}
