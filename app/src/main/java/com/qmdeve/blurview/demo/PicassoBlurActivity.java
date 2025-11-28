package com.qmdeve.blurview.demo;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.transform.picasso.BlurTransformation;
import com.squareup.picasso.Picasso;

public class PicassoBlurActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_f_blur);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        ImageView images = findViewById(R.id.images);

        Picasso.get()
                .load(R.drawable.image)
                .fit()
                .centerCrop()
                .transform(

                        /**
                         * Use the BlurTransformation class from the QmBlurView library
                         * Import Class: com.qmdeve.blurview.transform.picasso.BlurTransformation
                         *
                         * new BlurTransformation()
                         * new BlurTransformation(float blurRadius)
                         * new BlurTransformation(float blurRadius, float roundedCorners)
                         */
                        new BlurTransformation(25f, 50)
                )
                .into(images);
    }
}
