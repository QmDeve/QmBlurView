package com.qmdeve.blurview.demo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.util.BlurUtils;

public class QmBlurImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blur_image);

        ImageView image = findViewById(R.id.image);
        Bitmap imageBitmap = BlurUtils.blurResId(this, R.drawable.image, 40);
        image.setImageBitmap(imageBitmap);
    }
}